package com.example.timerecordcollector.controller;

import com.example.timerecordcollector.TimeRecordCollectorApplication;
import com.example.timerecordcollector.bean.ApplicationSideData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TimeRecordHandler {



    @PostMapping("/applicationTime")
    String applicationTimeRecord(@RequestBody ApplicationSideData applicationSideData) {
        System.out.println("gain application side data : "+applicationSideData.toString());
        TimeRecordCollectorApplication.mainDispatcher.gainDataFromApplicationSide(applicationSideData);
        return "ok";
    }

}
