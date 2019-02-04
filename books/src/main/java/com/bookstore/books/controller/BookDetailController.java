package com.bookstore.books.controller;


import com.bookstore.books.model.BookDetails;
import com.bookstore.books.model.Discount;
import com.bookstore.books.service.BookDetailService;

import com.bookstore.books.util.BookPurchaseRequest;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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
            invalidFiles = bookDetailService.createStockOnSequential(multipartFiles, invalidFiles);
            if (invalidFiles.isEmpty()) {
                return ResponseEntity.ok("Files added successfully! ");
            } else {
                return ResponseEntity.ok("The following files are not valid CSV files: " +
                        String.join(" , ", invalidFiles));
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to read file ");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Book details addition failed! ");
        }
    }

    @PostMapping(value = "/parallel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addBooksToStore(@RequestPart("file") MultipartFile[] multipartFiles) {

        try {
            List<String> invalidFiles = bookDetailService.validateCsvFile(multipartFiles);
            invalidFiles = bookDetailService.createStockOnParallel(multipartFiles, invalidFiles);

            if (invalidFiles.isEmpty()) {
                return ResponseEntity.ok("Books added successfully! ");
            } else {
                return ResponseEntity.ok("The following files are not valid CSV files: " +
                        String.join(" , ", invalidFiles));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Book details addition failed! ");
        }
    }

    @GetMapping(value = "/details/{isbn}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getBookDetailsByISBN(@PathVariable("isbn") String isbn) {

        if (StringUtils.isEmpty(isbn)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Enter a valid ISBN number");
        }

        BookDetails bookDetails = bookDetailService.getBookDetails(isbn);

        if (StringUtils.isEmpty(bookDetails)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No book exists with the given ISBN");
        } else {
            return ResponseEntity.ok(bookDetails);
        }
    }

    // search book by name and author
    @GetMapping(value = "/searchby/bookname/{name}/author/{author}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity searchBookByNameAndAuthor(@PathVariable("name") String bookName,
                                                    @PathVariable("author") String author) {

        if (StringUtils.isEmpty(bookName) && StringUtils.isEmpty(author)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Provide at least either one of book name or author ");
        }

        List<BookDetails> bookDetails = bookDetailService.searchBook(bookName, author);

        if (bookDetails == null || bookDetails.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No book exists with the given search details! ");
        } else {
            return ResponseEntity.ok(bookDetails);
        }

    }

    @DeleteMapping(value = "/{isbn}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteBookInventoryByIsbn(@PathVariable("isbn") String isbn) {

        if (StringUtils.isEmpty(isbn)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Enter a valid ISBN number");
        }

        bookDetailService. removeBookInventory(isbn);
        return ResponseEntity.ok("Book details removed successfully! ");
    }

    @PutMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateBookDetails(@RequestBody BookDetails bookDetails){
        if (StringUtils.isEmpty(bookDetails)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Enter a valid book details! ");
        }

        bookDetailService.updateBookDetails(bookDetails);
        return ResponseEntity.ok("Book details updated successfully! ");
    }

     @PostMapping(value = "/purchase", consumes =  MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity purchaseBook(@RequestBody BookPurchaseRequest bookPurchaseRequest ){
        if (StringUtils.isEmpty(bookPurchaseRequest)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Enter valid book details !!!  ");
        }
        bookDetailService.purchaseBook(bookPurchaseRequest);
        return ResponseEntity.ok("Book Purchase successful!");
    }


  /*  @PostMapping(value = "/discount", produces =  MediaType.APPLICATION_JSON_VALUE )
    public ResponseEntity discountScheme(@RequestBody Discount discount){
        if (StringUtils.isEmpty(discount)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Enter valid discount detail input!  ");
        }

        bookDetailService.discountPeriod(discount);
        return null;
    }*/
}
