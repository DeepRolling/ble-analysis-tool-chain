package com.example.timerecordcollector;

import java.io.IOException;

public class TestWiresharkRecord {

    public static void main(String[] args) throws IOException, InterruptedException {
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
}
