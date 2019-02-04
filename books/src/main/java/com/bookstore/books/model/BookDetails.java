package com.bookstore.books.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
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
  private int edition;

  @NotNull
  private String genre;

  @NotNull
  private String segment;

  @NotNull
  private double price;

  @NotNull
  private int quantity;


  @ManyToOne
  private BookStoreDetail bookStoreDetail;

  public BookDetails(@NotNull String bookname,
      @NotNull String isbn, @NotNull String author,
      @NotNull String publisher, @NotNull int edition,
      @NotNull String genre, @NotNull String segment,
      @NotNull double price, @NotNull int quantity) {
    //BookStoreDetail bookStoreDetail) {
    this.bookname = bookname;
    this.isbn = isbn;
    this.author = author;
    this.publisher = publisher;
    this.edition = edition;
    this.genre = genre;
    this.segment = segment;
    this.price = price;
    this.quantity = quantity;
    //this.bookStoreDetail = bookStoreDetail;
  }
}
