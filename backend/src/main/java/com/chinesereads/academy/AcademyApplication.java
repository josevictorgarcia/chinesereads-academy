package com.chinesereads.academy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.modulith.Modulithic;

@Modulithic(sharedModules = "shared")
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
public class AcademyApplication {

  public static void main(String[] args) {
    SpringApplication.run(AcademyApplication.class, args);
  }
}
