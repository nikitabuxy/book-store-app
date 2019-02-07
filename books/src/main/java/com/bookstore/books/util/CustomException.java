package com.bookstore.books.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Getter
@Slf4j
public class CustomException extends Throwable {

  private HttpStatus httpStatus;
  private String message;

  public CustomException(HttpStatus httpStatus, String message){
    this.httpStatus = httpStatus;
    this.message = message;
  }

  public static CustomException get(HttpStatus status, String message, Throwable th) {
    log.error("Class: {}, Message: {}, Stacktrace: {}", th.getClass(), th.getMessage(), th.getStackTrace());
    return new CustomException(status, message);
  }


  public CustomException(HttpStatus httpStatus){
    this.httpStatus = httpStatus;
  }

  public CustomException(String message){
    this.message = message;
  }
}
