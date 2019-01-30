package com.bookstore.books.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "book_details")
public class BookDetails {


  @Id
  @GeneratedValue
  private Long id;

  @NotNull
  private String bookname;

  @NotNull
  private String isbn;

  @NotNull
  private String author;

  @NotNull
  private String publisher;

  @NotNull
  private String edition;

  @NotNull
  private String genre;

  @NotNull
  private String segment;

  @NotNull
  private String price;

  @NotNull
  private String quantity;


  @ManyToOne
  private BookStoreDetail bookStoreDetail;
}
