package com.tuya.iot.suit.starter;

import com.tuya.iot.framework.spring.annotations.ConnectorScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.tuya.iot.suit")
@ConnectorScan(basePackages = {"com.tuya.iot.suit.ability.*.connector"})
public class IotSuitApplication {

    public static void main(String[] args) {
        SpringApplication.run(IotSuitApplication.class, args);
    }

}
