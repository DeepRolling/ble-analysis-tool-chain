package com.example.timerecordcollector.controller;

import com.example.timerecordcollector.bean.BleTimeRecord;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;

@RestController
public class TimeRecordHandler {

    LinkedList<BleTimeRecord> records = new LinkedList<>();

    @PostMapping("/applicationTime")
    String applicationTimeRecord(@RequestBody BleTimeRecord newEmployee) {
        System.out.println(newEmployee.getStartScan());
        records.add(newEmployee);
        return "ok";
    }

}
