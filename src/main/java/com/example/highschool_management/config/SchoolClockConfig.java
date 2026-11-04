package com.example.highschool_management.config;

import java.time.*;
import org.springframework.context.annotation.*;

@Configuration
public class SchoolClockConfig {
  @Bean
  public Clock schoolClock() {
    return Clock.system(ZoneId.of("Asia/Ho_Chi_Minh"));
  }
}
