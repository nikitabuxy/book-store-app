package com.bookstore.books.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
public class BookDetails extends TimeStamp{


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

  private int discount;

  public int getDiscount() {
    return discount;
  }

  public void setDiscount(int discount) {
    this.discount = discount;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getBookname() {
    return bookname;
  }

  public void setBookname(String bookname) {
    this.bookname = bookname;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String publisher) {
    this.publisher = publisher;
  }

  public String getEdition() {
    return edition;
  }

  public void setEdition(String edition) {
    this.edition = edition;
  }

  public String getGenre() {
    return genre;
  }

  public void setGenre(String genre) {
    this.genre = genre;
  }

  public String getSegment() {
    return segment;
  }

  public void setSegment(String segment) {
    this.segment = segment;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public BookStoreDetail getBookStoreDetail() {
    return bookStoreDetail;
  }

  public void setBookStoreDetail(BookStoreDetail bookStoreDetail) {
    this.bookStoreDetail = bookStoreDetail;
  }

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
      @NotNull String publisher, @NotNull String edition,
      @NotNull String genre, @NotNull String segment,
      @NotNull double price, @NotNull int quantity,
      BookStoreDetail bookStoreDetail) {
    this.bookname = bookname;
    this.isbn = isbn;
    this.author = author;
    this.publisher = publisher;
    this.edition = edition;
    this.genre = genre;
    this.segment = segment;
    this.price = price;
    this.quantity = quantity;
    this.bookStoreDetail = bookStoreDetail;
  }
}
