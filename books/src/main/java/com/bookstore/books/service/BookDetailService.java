package com.bookstore.books.service;

import com.bookstore.books.model.BookDetails;
import com.bookstore.books.repository.BookDetailRepository;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class BookDetailService {


  @Autowired
  BookStoreService bookStoreService;

  @Autowired
  BookDetailRepository bookDetailRepository;

  public void validateCsvFile(MultipartFile multipartFile) {
    if (!multipartFile.getName().endsWith(".csv")) {
      throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Provide CSV file as input! ");
    }
  }

  public File convertToFile(MultipartFile multipartFile) throws IOException {
    File file = new File(multipartFile.getOriginalFilename());
    multipartFile.transferTo(file);
    return file;
  }

  public List<BookDetails> getBookDetail(MultipartFile file) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new FileReader(convertToFile(file)));
    CSVParser csvParser = new CSVParser(bufferedReader,
        CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());

    List<BookDetails> bookDetailsList = new ArrayList<>();

    Iterable<CSVRecord> csvRecords = csvParser.getRecords();
    for (CSVRecord csvRecord : csvRecords
    ) {
      BookDetails bookDetails = new BookDetails( csvRecord.get("bookname"),
          csvRecord.get("isbn"),
          csvRecord.get("author"),
          csvRecord.get("publisher"),
          csvRecord.get("edition"),
          csvRecord.get("genre"),
          csvRecord.get("segment"),
          csvRecord.get("price"),
          Integer.parseInt(csvRecord.get("quantity")),
          bookStoreService.getBookStoreByName(file.getName()));

      bookDetailsList.add(bookDetails);
    }
    return bookDetailsList;
  }

  public boolean updateStock(MultipartFile file) throws IOException {
    List<BookDetails> bookDetailsList = getBookDetail(file);

    try {
      bookDetailRepository.saveAll(bookDetailsList);
      return true;
    } catch (Exception e) {
      log.error("Error saving book details to the database", e);
      throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,"Failed to save book details to the repository! ");
    }
  }
}
