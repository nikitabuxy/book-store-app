package com.bookstore.books.service;

import com.bookstore.books.model.BookDetails;
import com.bookstore.books.repository.BookDetailRepository;
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

import javax.transaction.Transactional;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class BookDetailService {


    @Autowired
    private BookStoreService bookStoreService;

   /* @Autowired
    private AmazonS3 amazonS3;*/

    @Autowired
    private BookDetailRepository bookDetailRepository;

    /*@Value("${aws.s3.survey.bucket}")
    private String bucketName;
*/

    public List<String> validateCsvFile(MultipartFile[] multipartFile) {
        List<String> invalidFiles = new ArrayList<>();

        for (MultipartFile singleFile : multipartFile
        ) {
            if (!singleFile.getOriginalFilename().endsWith(".csv")) {
                log.error("Not a valid csv input: " + singleFile.getOriginalFilename());
                invalidFiles.add(singleFile.getOriginalFilename());
            }
        }
        return invalidFiles;
    }

    private File convertToFile(MultipartFile multipartFile, List<String> invalidFiles) throws IOException {
        File convertedFile = null;
        if (invalidFiles.stream().noneMatch(s -> s.equals(multipartFile.getOriginalFilename()))) {
            convertedFile = getFile(multipartFile);
        }
        log.info("Convert file method ends! ");
        return convertedFile;
    }

    private File getFile(MultipartFile singleFile) throws IOException {
        log.info("Get file method begins! ");
        File file = new File(singleFile.getOriginalFilename());
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(singleFile.getBytes());
        fos.close();
        log.info("Get file method ends! ");
        return file;
    }

    private List<BookDetails> getBookDetail(List<File> fileList) throws IOException {

        List<BookDetails> bookDetailsList = new ArrayList<>();
        for (File file :
                fileList) {
            parseSingleFile(file, bookDetailsList);
        }
        return bookDetailsList;
    }


    public List<String> createStockOnSequential(MultipartFile[] multipartFiles, List<String> invalidFiles) throws IOException {
        // check if file input is a valid csv or not
        // return the list of invalid files
        List<File> inputFileList = new ArrayList<>();
        for (MultipartFile multipartFile :
                multipartFiles) {
            File file = convertToFile(multipartFile, invalidFiles);
            inputFileList.add(file);
        }
        List<BookDetails> bookDetailsList = getBookDetail(inputFileList);

        try {
            bookDetailRepository.saveAll(bookDetailsList);
            //uploadFileToS3(inputFile);
            if (!invalidFiles.isEmpty()) {
                return invalidFiles;
            }
            return new ArrayList<String>();
        } catch (Exception e) {
            log.error("Error saving book details to the database", e);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to save book details to the repository! ");
        }
    }

/*    public void uploadFileToS3(File file) throws Exception {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.length());
            String fileKey = file.getName()
                    .concat("_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            amazonS3.putObject(bucketName, fileKey, new FileInputStream(file), metadata);
        } catch (IOException | AmazonClientException e) {
            throw new Exception("Failed to upload file to S3! ");
        }
    }*/

    public List<String> createStockOnParallel(MultipartFile[] multipartFiles, List<String> invalidFiles) throws IOException{
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        for (MultipartFile multipartFile :
                multipartFiles) {
            File file = convertToFile(multipartFile, invalidFiles);
            Runnable createStock = () -> {
                try {
                    addSingleFile(file, invalidFiles);
                } catch (IOException e) {
                    log.error(" Failed to create stock from input file");
                }
            };
            executorService.execute(createStock);

        }
        if (!invalidFiles.isEmpty()) {
            return invalidFiles;
        }
        executorService.shutdown();
        return new ArrayList<String>();
    }

    private List<BookDetails> parseSingleFile(File file, List<BookDetails> bookDetailsList) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        CSVParser csvParser = new CSVParser(bufferedReader,
                CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
        Iterable<CSVRecord> csvRecords = csvParser.getRecords();
        for (CSVRecord csvRecord : csvRecords
        ) {
            BookDetails bookDetails = new BookDetails(csvRecord.get("bookname"),
                    csvRecord.get("isbn"),
                    csvRecord.get("author"),
                    csvRecord.get("publisher"),
                    csvRecord.get("edition"),
                    csvRecord.get("genre"),
                    csvRecord.get("segment"),
                    Double.parseDouble(csvRecord.get("price")),
                    Integer.parseInt(csvRecord.get("quantity"))
                    );
            //bookStoreService.getBookStoreByName(file.getName().substring(0, file.getName().lastIndexOf('.')))
            bookDetails.setLastCreatedAt(new Date());
            bookDetailsList.add(bookDetails);
        }
        return bookDetailsList;
    }
/*
    private synchronized boolean  checkDuplicateBookEntry(BookDetails bookDetails){
        BookDetails existingBookDetails = bookDetailRepository.findByBookNameAndEditionAndIsbn(bookDetails.getBookname(), bookDetails.getEdition(), bookDetails.getIsbn());

        if(existingBookDetails == null){
            return false;
        }
*//*        if (!bookDetails.getEdition().equals(existingBookDetails.getEdition()) && !bookDetails.getBookname().equals(existingBookDetails.getBookname())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Name (or) Edition must not be updated for an ISBN number! ");
        }*//*
        bookDetails.setQuantity(existingBookDetails.getQuantity() + bookDetails.getQuantity());
        bookDetails.setId(existingBookDetails.getId());
        try{
            bookDetailRepository.save(bookDetails);
            return true;
        }catch (Exception e){
            log.error("Addition of quantity of existing book failed");
                    throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,"Failed to update book inventory");
        }
    }*/


    private void addSingleFile(File inputFile, List<String> invalidFiles) throws IOException {
        //File inputFile = convertToFile(multipartFile, invalidFiles);
        List<BookDetails> bookDetails = parseSingleFile(inputFile, new ArrayList<BookDetails>());
        try {
            bookDetailRepository.saveAll(bookDetails);
            //uploadFileToS3(inputFile);
        } catch (Exception e) {
            log.error("Error saving book details to the database", e);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to save book details to the repository! ");
        }
    }

    public BookDetails getBookDetails(String isbn) {
        try {
            return bookDetailRepository.findByIsbn(isbn);
        } catch (Exception e) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not fetch book details! ");
        }
    }

    public List<BookDetails> searchBook(String bookName, String author) {
        try {
            return bookDetailRepository.findByBooknameIgnoreCaseContainingAndAuthorIgnoreCaseContaining(bookName, author);
        } catch (Exception e) {
            log.error("Failed to fetch book required! ", e);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not fetch book details! ");
        }
    }

    public void removeBookInventory(String isbn) {
        try {
            bookDetailRepository.removeByIsbn(isbn);
        } catch (Exception e) {
            log.error("Delete book operation failed", e);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to remove book details from the inventory! ");
        }
    }

    public void updateBookDetails(BookDetails bookDetails) {
        BookDetails savedBookDetails;
        try {
            savedBookDetails = bookDetailRepository.findByIsbn(bookDetails.getIsbn());
        } catch (Exception e) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not fetch book details! ");
        }

        if (!bookDetails.getEdition().equals(savedBookDetails.getEdition()) && !bookDetails.getBookname().equals(savedBookDetails.getBookname())) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Name (or) Edition must not be updated for an ISBN number! ");
        }
        bookDetails.setId(savedBookDetails.getId());
        bookDetails.setLastUpdatedAt(new Date());
        try {
            bookDetailRepository.save(bookDetails);
        } catch (Exception e) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update book details in the inventory! ");
        }
    }

    public void purchaseBook(String isbn) {
        BookDetails existingBookDetails = bookDetailRepository.findByIsbn(isbn);

        if (existingBookDetails == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "No book exists with the given ISBN! ");
        }
        if (existingBookDetails.getQuantity() == 0) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Book Out of stock! ");
        }
        existingBookDetails.setQuantity(existingBookDetails.getQuantity() - 1);
        try {
            bookDetailRepository.save(existingBookDetails);
        } catch (Exception e) {
            log.error("Error updating quantity of books after purchase! ", e);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating quantity of books after purchase!");
        }
    }


}
