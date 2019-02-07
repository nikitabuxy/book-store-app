package com.bookstore.books.controller;

import com.bookstore.books.model.BookStoreDetail;
import com.bookstore.books.service.BookStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/bookstore")
@RequiredArgsConstructor
public class BookStoreController {

  private BookStoreService bookStoreService;

  @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity createBookStore(@RequestBody BookStoreDetail bookStoreDetail) {

    bookStoreService.validateRequest(bookStoreDetail);
    bookStoreService.addBookStore(bookStoreDetail);
    return ResponseEntity.ok("Book store added successfully! ");
  }
}
