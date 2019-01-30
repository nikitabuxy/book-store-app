package com.bookstore.books.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "book_store")
public class BookStoreDetail {

  @Id
  @GeneratedValue
  private Long id;

  @NotNull
  private String storeName;

  @NotNull
  private String address;

}
