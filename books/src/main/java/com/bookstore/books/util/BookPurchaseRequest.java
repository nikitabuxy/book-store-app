package com.bookstore.books.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookPurchaseRequest {

  private String bookname;
  private String author;
  private int edition;

}
