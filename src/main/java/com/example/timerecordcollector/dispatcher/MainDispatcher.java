package com.example.timerecordcollector.dispatcher;

import com.example.timerecordcollector.autoRunner.AutomationExecuteFailReason;
import com.example.timerecordcollector.autoRunner.AutomationExecutionCallback;
import com.example.timerecordcollector.autoRunner.AutomationRunner;
import com.example.timerecordcollector.autoRunner.ParserResult;
import com.example.timerecordcollector.bean.ApplicationSideData;
import com.example.timerecordcollector.bean.BleCommunicationSummary;
import com.example.timerecordcollector.bean.BleTimeRecord;

import java.io.File;
import java.util.ArrayList;

public class MainDispatcher {

    private ArrayList<BleTimeRecord> timeRecords;

    public MainDispatcher() {
        timeRecords = new ArrayList<>();
    }

    private BleTimeRecord currentRunningRecord;
    //todo consider some backup strategy if necessary
    public void clearUpCurrentRunningRecord(AutomationExecuteFailReason reason){
        System.out.println("Error open during execute automation : "+reason.toString());
        System.exit(1);
        currentRunningRecord = null;
    }
    public void clearUpCurrentRunningRecord(Exception e){
        System.out.println("Error open during execute automation : "+e.toString());
        System.exit(1);
        currentRunningRecord = null;
    }
    public void clearUpCurrentRunningRecord(){
        currentRunningRecord = null;
    }
    public void gainDataFromApplicationSide(ApplicationSideData applicationSideData){
        currentRunningRecord = new BleTimeRecord();
        currentRunningRecord.gainApplicationSideData(applicationSideData);
    }
    public void gainDataFromWiresharkSide(ParserResult parserResult) throws Exception {
        if (currentRunningRecord == null) {
            throw new Exception("gain wireshark data before application data takein...");
        }
        currentRunningRecord.gainWiresharkSideData(parserResult);
        timeRecords.add(currentRunningRecord);
        clearUpCurrentRunningRecord();
    }

    public void loop(){
        AutomationRunner.startCapturePackage(new AutomationExecutionCallback() {
            @Override
            public void onSuccess(ParserResult parserResult) {
                try {
                    gainDataFromWiresharkSide(parserResult);
                    if (timeRecords.size() == 10) {
                        //finish loop, export Excel file
                        BleCommunicationXmlWriter bleCommunicationXmlWriter = new BleCommunicationXmlWriter(new File(".\\result\\bleCommunication.xlsx"));
                        for (BleTimeRecord timeRecord : timeRecords) {
                            BleCommunicationSummary bleCommunicationSummary = timeRecord.summaryData();
                            bleCommunicationXmlWriter.Write(bleCommunicationSummary);
                        }
                        bleCommunicationXmlWriter.Extract();
                    }else {
                        loop();
                    }
                } catch (Exception e) {
                    clearUpCurrentRunningRecord(e);
                }
            }

            @Override
            public void onFail(AutomationExecuteFailReason reason) {
                if (reason == AutomationExecuteFailReason.PARSE_FRAME_FAIL) {
                    //don't care about parser error, because sniffer sometimes may lose some frame
                    loop();
                }else {
                    clearUpCurrentRunningRecord(reason);
                }
            }
        });
    }
}
