package com.bookstore.books.service;


import com.bookstore.books.model.BookStoreDetail;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class BookStoreService {

  public void validateRequest(BookStoreDetail bookStoreDetail){
    if (StringUtils.isEmpty(bookStoreDetail.getStoreName())) {
    }
  }

  public BookStoreDetail addBookStore(BookStoreDetail bookStoreDetail) {

    bookStoreDetail.setId(null);
    return null;
  }
}
