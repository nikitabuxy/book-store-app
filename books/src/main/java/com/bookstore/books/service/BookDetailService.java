package com.bookstore.books.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.bookstore.books.model.BookDetails;
import com.bookstore.books.repository.BookDetailRepository;
import com.bookstore.books.util.BookPurchaseRequest;
import com.bookstore.books.util.CustomException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookDetailService {

  private final AmazonS3 amazonS3;

  private final BookDetailRepository bookDetailRepository;

  @Value("${aws.s3.survey.bucket}")
  private String bucketName;


  public List<String> validateCsvFile(MultipartFile[] multipartFile) {
    List<String> invalidFiles = new ArrayList<>();

    for (MultipartFile singleFile : multipartFile
    ) {
      if (singleFile.getOriginalFilename() != null && !singleFile.getOriginalFilename()
          .endsWith(".csv")) {
        log.error("Not a valid csv input: " + singleFile.getOriginalFilename());
        invalidFiles.add(singleFile.getOriginalFilename());
      }
    }
    return invalidFiles;
  }

  public List<File> convertToFile(MultipartFile[] multipartFileList, List<String> invalidFiles)
      throws CustomException {
    List<File> generatedFiles = new ArrayList<>();
    for (MultipartFile multipartFile :
        multipartFileList) {
      File convertedFile = null;
      if (invalidFiles.stream().noneMatch(s -> s.equals(multipartFile.getOriginalFilename()))) {
        convertedFile = getFile(multipartFile);
      }
      generatedFiles.add(convertedFile);
    }
    log.info("Convert file method ends! ");
    return generatedFiles;
  }

  private File getFile(MultipartFile singleFile) throws CustomException {
    log.info("Get file method begins! ");
    File file = new File(singleFile.getOriginalFilename());
    try {
      file.createNewFile();
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(singleFile.getBytes());
      fos.close();
    } catch (IOException e) {
      log.error("Cannot convert multipart to file", e);
      throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Failed to convert Multipart File to File");
    }

    log.info("Get file method ends! ");
    return file;
  }

  private List<BookDetails> getBookDetail(List<File> fileList) throws CustomException {

    List<BookDetails> bookDetailsList = new ArrayList<>();
    for (File file :
        fileList) {
      parseSingleFile(file, bookDetailsList);
    }
    return bookDetailsList;
  }


  public void createStockOnSequential(List<File> inputFileList) throws CustomException {
    // check if file input is a valid csv or not
    // return the list of invalid files
    List<BookDetails> bookDetailsList = getBookDetail(inputFileList);

    try {
      bookDetailRepository.saveAll(bookDetailsList);
      log.info("Book details saved ! ");
    } catch (Exception e) {
      log.error("Error saving book details to the database", e);
      throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save book details! ");
    }
    for (File inputFile : inputFileList
    ) {
      uploadFileToS3(inputFile);
    }
  }

  private void uploadFileToS3(File file) {
    try {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(file.length());
      String fileKey = file.getName()
          .concat("_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
      amazonS3.putObject(bucketName, fileKey, new FileInputStream(file), metadata);
    } catch (IOException | AmazonClientException e) {
      log.error("Error uploading file to S3! ", e);
    }
  }

  public void createStockOnParallel(List<File> inputFileList) throws CustomException {
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    for (File file :
        inputFileList) {
      try {
        Runnable createStock = () -> {
          //log.info("### Saving file {}", file.getName());
          try {
            addSingleFile(file);
          } catch (CustomException ex) {
            throw new RuntimeException(ex);
          }
        };
        executorService.execute(createStock);
      } catch (RuntimeException e) {
        log.error(" Failed to create stock from input file");
        throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
      }

    }
  }

  private List<BookDetails> parseSingleFile(File file, List<BookDetails> bookDetailsList)
      throws CustomException {

    BufferedReader bufferedReader;
    CSVParser csvParser;
    Iterable<CSVRecord> csvRecords;
    try {
      bufferedReader = new BufferedReader(new FileReader(file));
      csvParser = new CSVParser(bufferedReader,
          CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
      csvRecords = csvParser.getRecords();
    } catch (IOException e) {
      throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse CSV file");
    }

    for (CSVRecord csvRecord : csvRecords
    ) {
      BookDetails bookDetails = new BookDetails(csvRecord.get("bookname"),
          csvRecord.get("isbn"),
          csvRecord.get("author"),
          csvRecord.get("publisher"),
          Integer.parseInt(csvRecord.get("edition")),
          csvRecord.get("genre"),
          csvRecord.get("segment"),
          Double.parseDouble(csvRecord.get("price")),
          Integer.parseInt(csvRecord.get("quantity"))
      );
      //bookStoreService.getBookStoreByName(file.getName().substring(0, file.getName().lastIndexOf('.')))
      bookDetailsList.add(bookDetails);
    }
    return bookDetailsList;
  }

  public void addSingleFile(File inputFile) throws CustomException {
    List<BookDetails> bookDetails = parseSingleFile(inputFile, new ArrayList<>());
    try {
      bookDetailRepository.saveAll(bookDetails);
      uploadFileToS3(inputFile);
    } catch (Exception e) {
      log.error("Error saving book details to the database", e);
      throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Failed to save book details to the repository! ");
    }
  }

  public BookDetails getBookDetails(String isbn) throws CustomException {
    try {
      return bookDetailRepository.findByIsbn(isbn);
    } catch (Exception e) {
      throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Could not fetch book details! ");
    }
  }

  public List<BookDetails> searchBook(String bookName, String author) throws CustomException {
    try {
      return bookDetailRepository
          .findByBooknameIgnoreCaseContainingAndAuthorIgnoreCaseContaining(bookName, author);
    } catch (Exception e) {
      log.error("Failed to fetch book required! ", e);
      throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Could not fetch book details! ");
    }
  }

  public void removeBookInventory(String isbn) throws CustomException {
    BookDetails bookDetails;
    try {
      bookDetails = bookDetailRepository.findByIsbn(isbn);
      bookDetailRepository.deleteById(bookDetails.getId());
    } catch (Exception e) {
      log.error("Delete book operation failed", e);
      throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Failed to remove book details from the inventory! ");
    }
  }

  public void updateBookDetails(BookDetails bookDetails) throws CustomException {
    BookDetails savedBookDetails;
    try {
      savedBookDetails = bookDetailRepository.findByIsbn(bookDetails.getIsbn());
    } catch (Exception e) {
      log.error("Book with given ISBN doesnot exist! ");
      throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Could not fetch book details. Not a valid ISBN!  ");
    }

    if (bookDetails.getEdition() != savedBookDetails.getEdition() && !bookDetails
        .getBookname().equals(savedBookDetails.getBookname())) {
      throw new CustomException(HttpStatus.BAD_REQUEST,
          "Name (or) Edition must not be updated for an ISBN number! ");
    }
    bookDetails.setId(savedBookDetails.getId());
    try {
      bookDetailRepository.save(bookDetails);
    } catch (Exception e) {
      log.error("Error updating book details! ", e);
      throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
          "Failed to update book details in the inventory! ");
    }
  }

  public void purchaseBook(BookPurchaseRequest bookPurchaseRequest) throws CustomException {
    BookDetails existingBookDetails = bookDetailRepository
        .findByBooknameAndAuthorAndEdition(bookPurchaseRequest.getBookname(),
            bookPurchaseRequest.getAuthor(), bookPurchaseRequest.getEdition());

    if (existingBookDetails == null) {
      throw new CustomException(HttpStatus.BAD_REQUEST,
          "No book exists with the given details!  ");
    }
    if (existingBookDetails.getQuantity() == 0) {
      throw new CustomException(HttpStatus.BAD_REQUEST, "Book Out of stock! ");
    } else {
      existingBookDetails.setQuantity(existingBookDetails.getQuantity() - 1);
      try {
        bookDetailRepository.save(existingBookDetails);
      } catch (Exception e) {
        log.error("Error updating quantity of books after purchase! ", e);
        throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
            "Error updating quantity of books after purchase!");
      }
    }
  }

}
