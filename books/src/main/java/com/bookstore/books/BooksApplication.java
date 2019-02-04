package com.bookstore.books;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BooksApplication {

  @Value("${s3.region}")
  private String s3Region;
/*
  @Value("${aws.access.key.id}")
  private String accessKey;

  @Value("${aws.secret.access.key}")
  private String secretKey;*/


  @Bean("amazonS3")
  public AmazonS3 amazonS3Client() {
    // removing instance credentials provider, not required, since
    // s3 resolves the credentials by priority
//    BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
    return AmazonS3ClientBuilder.standard()
        .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
        .withRegion(s3Region)
        .build();
  }

  public static void main(String[] args) {
    SpringApplication.run(BooksApplication.class, args);

  }

}


