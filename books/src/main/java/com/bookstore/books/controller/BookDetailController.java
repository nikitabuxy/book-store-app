package com.bookstore.books.controller;


import com.bookstore.books.service.BookDetailService;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/book")
public class BookDetailController {

  @Autowired
  BookDetailService bookDetailService;


  @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity addBookToStore(
      @PathVariable("storeName") @RequestParam("file") MultipartFile multipartFile) {
    bookDetailService.validateCsvFile(multipartFile);
    try {
      bookDetailService.updateStock(multipartFile);
      return ResponseEntity.ok("Book details added successfully! ");
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to read file ");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Book details addition failed! ");
    }

  }
}
