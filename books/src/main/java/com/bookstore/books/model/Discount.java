package com.bookstore.books.model;


import lombok.Data;

import java.util.Date;

@Data
public class Discount {

    private String discountPercentage;
    private Date startDate;
    private Date endDate;
}
