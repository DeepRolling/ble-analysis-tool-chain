package com.example.timerecordcollector.autoRunner;

import com.google.common.io.Files;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class AutomationRunner {

    private static final String FIXED_PCAP_FILE_PATH = "C:\\Users\\deepcode\\AppData\\Roaming\\Nordic Semiconductor\\Sniffer\\logs\\capture.pcap";


    //you should call this function whether current automation task success or fail
    //The sake for this function is prepare clear environment for next execution
    private static void clearUpResource() {
        try {
            killWiresharkProcess();
            //backup malformed packet
            Files.move(new File(".\\temp\\capture.pcap"), new File(".\\errorlog\\pcap\\capture.pcap"));
            convertJsonFromPcap(".\\errorlog\\pcap\\");
            //clear dictionaries
            File tempDictionary = new File(".\\temp\\");
            for (File file : Objects.requireNonNull(tempDictionary.listFiles())) {
                boolean delete = file.delete();
                if (!delete) {
                    throw new Exception(file.getAbsolutePath() + " can't be deleted");
                }
            }
            File logDictionary = new File("C:\\Users\\deepcode\\AppData\\Roaming\\Nordic Semiconductor\\Sniffer\\logs\\");
            for (File file : Objects.requireNonNull(logDictionary.listFiles())) {
                boolean delete = file.delete();
                if (!delete) {
                    throw new Exception(file.getAbsolutePath() + "can't be deleted");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void startCapturePackage(AutomationExecutionCallback callback) {
        try {
            //step 1, start wireshark without auto-stop time
            Process wiresharkProcess = launchWireshark();
            Thread.sleep(5000);//let Wireshark wake up
            //step 2, start androidTest by command line to do ble interaction
            boolean applicationExecuteSuccess = launchApplicationInteraction();
            if (!applicationExecuteSuccess) {
                clearUpResource();
                callback.onFail(AutomationExecuteFailReason.ANDROID_TEST_EXECUTE_FAIL);
                return;
            }
            //todo determine this step is necessary
            //Thread.sleep(2000);//let Wireshark capture the package
            //step 3, after ble interaction finish, kill Wireshark...
            int killWiresharkStatus = killWiresharkProcess();
            if (killWiresharkStatus != 0) {
                clearUpResource();
                callback.onFail(AutomationExecuteFailReason.KILL_WIRESHARK_FAIL);
                return;
            }
            //step 4, wait Wireshark to be killed...
            waitWiresharkToBeKilledWithBlock(wiresharkProcess);
            //step 5, convert pcap package to json format, so we can use parser to extract data we need
            //shift wireshark captured file to project scope
            Files.copy(new File(FIXED_PCAP_FILE_PATH), new File(".\\temp\\capture.pcap"));
            int convertJsonStatus = convertJsonFromPcap(".\\temp\\");
            if (convertJsonStatus != 0) {
                clearUpResource();
                callback.onFail(AutomationExecuteFailReason.CONVERT_JSON_FAIL);
                return;
            }
            //step 6, custom json parser to parse data
            int parseFrameStatus = parseFrameFromBleData();
            if (parseFrameStatus != 0) {
                clearUpResource();
                callback.onFail(AutomationExecuteFailReason.PARSE_FRAME_FAIL);
                return;
            }
            //step 7, deal result file
            ParserResult parserResult = dealResultFile();
            //should call this before callback, because callback will launch another automation
            clearUpResource();
            callback.onSuccess(parserResult);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            //clear up resource(file or anther things)
            clearUpResource();
            callback.onFail(AutomationExecuteFailReason.UNKNOWN);
        }
    }

    private static ParserResult dealResultFile() throws IOException {
        Gson gson = new Gson();
        FileReader fileReader = new FileReader(".\\temp\\parser_result.json");
        ParserResult parserResult = gson.fromJson(fileReader, ParserResult.class);
        fileReader.close();
        return parserResult;
    }

    private static int parseFrameFromBleData() throws IOException, InterruptedException {
        Process customParserProcess = new ProcessBuilder().directory(new File(".\\custom-parser\\"))
                .command("node", "dist\\index.js")
                .redirectErrorStream(true)
                .start();
        int exitCode = customParserProcess.waitFor();
        System.out.println("parser exit with code :" + exitCode);
        if (exitCode == 0) {
            System.out.println("parser running success !");
            Thread.sleep(1000);
        }
        return exitCode;
    }


    //notice : the stupid powershell 5.x always print utf-8 with BOM(bytes order mark)
    //see https://stackoverflow.com/a/65192064 , so we need remove the bom in parser-side
    private static int convertJsonFromPcap(String pcapFileDir) throws IOException, InterruptedException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        //step 5, convert pcap package to json format, so we can use parser to extract data we need
        //for tshark extract data command, see : https://stackoverflow.com/questions/37953854/automating-exporting-packet-dissections-using-tshark
        //ProcessBuilder not a shell, see :https://stackoverflow.com/questions/37770383/how-to-use-processbuilder-when-using-redirection-in-linux
        Process tsharkCovertProcess = new ProcessBuilder().command("powershell.exe", "-Command", "B:\\Wireshark\\tshark.exe -r " + pcapFileDir + "capture.pcap -V -T json | out-file " + pcapFileDir + "ble_data.json -encoding utf8")
                .redirectErrorStream(true)
                .start();
        int tsharkExitCode = tsharkCovertProcess.waitFor();
        System.out.println("Extract data from pcap success, state code " + tsharkExitCode);
        return tsharkExitCode;
    }

    private static Process launchWireshark() throws IOException {
        //B:\Wireshark\Wireshark.exe -i COM3-3.6 -k -a duration:30 -Y "btle.advertising_address==f6:41:14:f3:8c:aa"
        return new ProcessBuilder().command("B:\\Wireshark\\Wireshark.exe", "-i", "COM3-3.6", "-k").redirectErrorStream(true).start();
    }

    private static boolean launchApplicationInteraction() throws IOException {
        //use this command to display all instrumentations : adb shell pm list instrumentation
        //adb shell am instrument -e class com.unionpayproject.BleInteractionTest -w com.unionpayproject.test/androidx.test.runner.AndroidJUnitRunner
        Process applicationInteraction = new ProcessBuilder().
                command("adb", "shell", "am", "instrument", "-e", "class",
                        "com.unionpayproject.BleInteractionTest", "-w", "com.unionpayproject.test/androidx.test.runner.AndroidJUnitRunner")
                .redirectErrorStream(true).start();
        //"Tests run: 1,  Failures: 1" indicate test error, "OK (1 test)" indicate test finish
        InputStream in = applicationInteraction.getInputStream();
        byte[] re = new byte[1024];
        boolean executeSuccess = false;
        StringBuilder result = new StringBuilder();
        while (in.read(re) != -1) {
            System.out.println(new String(re));
            result.append(new String(re));
            if (result.toString().contains("OK (1 test)")) {
                executeSuccess = true;
                break;
            }
            if (result.toString().contains("Tests run: 1,  Failures: 1")) {
                executeSuccess = false;
                break;
            }
        }
        in.close();
        applicationInteraction.destroy();
        System.out.println("androidTest running finish ! execute Success : " + executeSuccess);
        return executeSuccess;
    }

    private static int killWiresharkProcess() throws IOException, InterruptedException {
        Process killAllWiresharkTask = new ProcessBuilder().command("taskkill", "/F", "/IM", "Wireshark.exe").redirectErrorStream(true).start();
        int exitCode = killAllWiresharkTask.waitFor();
        if (exitCode == 0) {
            System.out.println("Kill success !");
        }
        return exitCode;
    }

    private static void waitWiresharkToBeKilledWithBlock(Process wiresharkProcess) throws IOException {
        InputStream in = wiresharkProcess.getInputStream();
        byte[] re = new byte[1024];
        StringBuilder result = new StringBuilder();
        while (in.read(re) != -1) {
            System.out.println(new String(re));
            result.append(new String(re));
            // if you kill the Wireshark process intently before capturing finish, you will receive following message
            // (wireshark:25832) 18:25:19.569649 [Capture MESSAGE] -- Capture Stop ...
            if (result.toString().contains("Capture Stop ...")) {
                break;
            }
        }
        in.close();
        wiresharkProcess.destroy();
        System.out.println("Capture package finish, start kill all Wireshark processes !");
    }

}
