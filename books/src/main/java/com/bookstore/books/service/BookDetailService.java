package com.bookstore.books.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.bookstore.books.model.BookDetails;
import com.bookstore.books.repository.BookDetailRepository;

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
import java.util.concurrent.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private AmazonS3 amazonS3;

    @Autowired
    BookDetailRepository bookDetailRepository;

    @Value("${aws.s3.survey.bucket}")
    String bucketName;


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

    public List<File> convertToFile(MultipartFile[] multipartFile, List<String> invalidFiles) throws IOException {
        List<File> validFiles = new ArrayList<>();
        for (MultipartFile singleFile :
                multipartFile) {
            if (invalidFiles.stream().noneMatch(s -> s.equals(singleFile.getOriginalFilename()))) {
                validFiles.add(getFile(singleFile));
            }
        }
        return validFiles;
    }

    public File getFile(MultipartFile singleFile) throws IOException{
        File file = new File(singleFile.getOriginalFilename());
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(singleFile.getBytes());
        fos.close();
        return file;
    }

    public List<BookDetails> getBookDetail(List<File> fileList) throws IOException {

        List<BookDetails> bookDetailsList = new ArrayList<>();
        for (File file :
                fileList) {
            parseSingleFile(file, bookDetailsList);
        }
        return bookDetailsList;
    }

    public List<BookDetails> parseSingleFile(File file, List<BookDetails> bookDetailsList) throws IOException{

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
                    Integer.parseInt(csvRecord.get("quantity")),
                    bookStoreService
                            .getBookStoreByName(file.getName().substring(0, file.getName().lastIndexOf('.'))));

            bookDetailsList.add(bookDetails);
        }
        return bookDetailsList;
    }

    public void addSingleFile(MultipartFile file) throws IOException{
        List<BookDetails> bookDetails = parseSingleFile(file,new ArrayList<BookDetails>());
        try {
            bookDetailRepository.saveAll(bookDetails);
            //uploadFileToS3(inputFile);
        } catch (Exception e) {
            log.error("Error saving book details to the database", e);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to save book details to the repository! ");
        }
    }
    public boolean createStockOnSequential(MultipartFile[] multipartFiles, List<String> invalidFiles) throws IOException {
        // check if file input is a valid csv or not
        // return the list of invalid files
        List<File> inputFile = convertToFile(multipartFiles, invalidFiles);
        List<BookDetails> bookDetailsList = getBookDetail(inputFile);

        try {
            bookDetailRepository.saveAll(bookDetailsList);
            //uploadFileToS3(inputFile);
            return true;
        } catch (Exception e) {
            log.error("Error saving book details to the database", e);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to save book details to the repository! ");
        }
    }

    public void uploadFileToS3(File file) throws Exception {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.length());
            String fileKey = file.getName()
                    .concat("_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            amazonS3.putObject(bucketName, fileKey, new FileInputStream(file), metadata);
        } catch (IOException | AmazonClientException e) {
            throw new Exception("Failed to upload file to S3! ");
        }
    }

    public List<String> createStockOnParallel(MultipartFile[] multipartFiles, List<String> invalidFiles) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        Runnable createStock = () -> {
            try {
                addSingleFile(multipartFiles, invalidFiles);
            } catch (IOException e) {
                log.error(" Failed to create stock from input file");
            }

        };
        executorService.execute(createStock);
        if (!invalidFiles.isEmpty()) {
            return invalidFiles;
        }
        return new ArrayList<String>();
    }

}
