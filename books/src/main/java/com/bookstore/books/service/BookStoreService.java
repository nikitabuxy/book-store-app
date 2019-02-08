package com.bookstore.books.service;


import com.bookstore.books.model.BookStoreDetail;
import com.bookstore.books.repository.BookStoreRepository;
import com.bookstore.books.util.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Service
@Slf4j
public class BookStoreService {

  @Autowired
  BookStoreRepository bookStoreRepository;

  public void validateRequest(BookStoreDetail bookStoreDetail) throws CustomException{
    if (StringUtils.isEmpty(bookStoreDetail.getStoreName())) {
      throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Store name cannot be empty! ");
    }

    if (StringUtils.isEmpty(bookStoreDetail.getAddress())) {
      throw new CustomException(HttpStatus.BAD_REQUEST, "Store name cannot be empty! ");
    }
  }

  public BookStoreDetail addBookStore(BookStoreDetail bookStoreDetail) throws CustomException {

    bookStoreDetail.setId(null);
    try {
          return bookStoreRepository.save(bookStoreDetail);
    }catch (Exception e){
        log.error("Error adding book store!", e);
      throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,"Failed to add book store! ");
    }
  }

  public BookStoreDetail getBookStoreByName(String name ){
    return bookStoreRepository.findByStoreName(name);
  }
}
