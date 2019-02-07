package com.bookstore.books.configuration;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Amazonconfiguration {

  @Value("${s3.region}")
  private String s3Region;

  @Bean
  public AmazonS3 amazonS3Client() {

    return AmazonS3ClientBuilder.standard()
        .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
        .withRegion(s3Region)
        .build();
  }
}
