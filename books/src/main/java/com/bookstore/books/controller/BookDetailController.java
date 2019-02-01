package com.bookstore.books.controller;


import com.bookstore.books.service.BookDetailService;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/book")
public class BookDetailController {

    @Autowired
    BookDetailService bookDetailService;


    @PostMapping(value = "/sequential", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addBookToStore(@RequestPart("file") MultipartFile[] multipartFiles) {
        try {
            List<String> invalidFiles = bookDetailService.validateCsvFile(multipartFiles);
            bookDetailService.createStockOnSequential(multipartFiles, invalidFiles);
            return ResponseEntity.ok("Book details added successfully! ");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to read file ");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Book details addition failed! ");
        }
    }

    @PostMapping(value = "/parallel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addBooksToStore(@RequestPart("file")MultipartFile[] multipartFiles){
        try {
            List<String> invalidFiles = bookDetailService.validateCsvFile(multipartFiles);
           return ResponseEntity.ok(bookDetailService.createStockOnParallel(multipartFiles,invalidFiles));
        }/*catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to read file ");
        }*/ catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Book details addition failed! ");
        }
    }
}
