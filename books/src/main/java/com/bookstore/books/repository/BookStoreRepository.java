package com.bookstore.books.repository;

import com.bookstore.books.model.BookStoreDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookStoreRepository extends JpaRepository<BookStoreDetail,Long> {
      BookStoreDetail findByStoreName(String storeName);
}
