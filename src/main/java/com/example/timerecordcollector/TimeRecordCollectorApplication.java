package com.example.timerecordcollector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
public class TimeRecordCollectorApplication {


    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplication.run(TimeRecordCollectorApplication.class, args);
        //launch process
        startCapturePackage();
    }

    public final String FIXED_PCAP_FILE_PATH = "C:\\Users\\deepcode\\AppData\\Roaming\\Nordic Semiconductor\\Sniffer\\logs\\capture.pcap";

    //some important dictionary:
    //C:\Users\deepcode\AppData\Roaming\Nordic Semiconductor\Sniffer\logs
    //B:\Wireshark\extcap
    //B:\迅雷下载\ble4.1.0
    //B:\Wireshark
    public static void startCapturePackage() throws IOException, InterruptedException {
        //step 1, start wireshark without auto-stop time
        //B:\Wireshark\Wireshark.exe -i COM3-3.6 -k -a duration:30 -Y "btle.advertising_address==f6:41:14:f3:8c:aa"
        Process wiresharkProcess = new ProcessBuilder().command("B:\\Wireshark\\Wireshark.exe", "-i", "COM3-3.6", "-k").redirectErrorStream(true).start();
        Thread.sleep(7000);//let Wireshark wake up
        //step 2, start androidTest by command line to do ble interaction
        launchApplicationInteraction();
//        Thread.sleep(1000);
//        launchApplicationInteraction();
        Thread.sleep(2000);//let Wireshark capture the package
        //step 3, after ble interaction finish, kill Wireshark...
        Process killAllWiresharkTask = new ProcessBuilder().command("taskkill", "/F","/IM","Wireshark.exe").redirectErrorStream(true).start();
        int exitCode = killAllWiresharkTask.waitFor();
        if (exitCode == 0) {
            System.out.println("Kill success !");
        }
        //step 4, wait Wireshark to be killed...
        waitWiresharkToBeKilledWithBlock(wiresharkProcess);
        //step 5, convert pcap package to json format, so we can use parser to extract data we need
        extractJsonDataFromPcap();
        //step 6, custom json parser to parse data

    }



    //notice : the stupid powershell 5.x always print utf-8 with BOM(bytes order mark)
    //see https://stackoverflow.com/a/65192064 , so we need remove the bom in parser-side
    public static void extractJsonDataFromPcap() throws IOException, InterruptedException {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        //step 5, convert pcap package to json format, so we can use parser to extract data we need
        //for tshark extract data command, see : https://stackoverflow.com/questions/37953854/automating-exporting-packet-dissections-using-tshark
        //ProcessBuilder not a shell, see :https://stackoverflow.com/questions/37770383/how-to-use-processbuilder-when-using-redirection-in-linux
        Process wiresharkProcess = new ProcessBuilder().command("powershell.exe","-Command","B:\\Wireshark\\tshark.exe -r .\\temp\\capture.pcap -V -T json | out-file .\\temp\\ble_data.json -encoding utf8")
                .redirectErrorStream(true)
                .start();
        int i = wiresharkProcess.waitFor();
        System.out.println("Extract data from pcap success, state code " + i);
    }

    public static void launchApplicationInteraction() throws IOException {
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
    }

    public static void waitWiresharkToBeKilledWithBlock(Process wiresharkProcess) throws IOException {
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
