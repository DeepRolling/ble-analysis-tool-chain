package com.example.timerecordcollector;

import com.example.timerecordcollector.dispatcher.MainDispatcher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TimeRecordCollectorApplication {

    public static MainDispatcher mainDispatcher = new MainDispatcher();


    public static void main(String[] args) {
        SpringApplication.run(TimeRecordCollectorApplication.class, args);
        mainDispatcher.loop();
    }


}
