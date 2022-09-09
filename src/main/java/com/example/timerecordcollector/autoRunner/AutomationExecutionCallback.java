package com.example.timerecordcollector.autoRunner;

public interface AutomationExecutionCallback {
    void onSuccess(ParserResult parserResult);
    void onFail(AutomationExecuteFailReason reason);
}
