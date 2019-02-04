package com.bookstore.books.model;


import javax.persistence.Entity;
import lombok.Data;

import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public class Discount {

    private String discountPercentage;

    @DateTimeFormat(iso = ISO.DATE)
    private Date startDate;

    @DateTimeFormat(iso = ISO.DATE)
    private Date endDate;
}
