package com.bookstore.books.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
public class BookStoreDetail {

  @Id
  @GeneratedValue
  private Long id;

  @NotNull
  private String storeName;

  @NotNull
  private String address;

}
