package com.bookstore.books.repository;

import com.bookstore.books.model.BookDetails;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookDetailRepository extends JpaRepository<BookDetails, Long> {

  BookDetails findByIsbn(String isbn);

  List<BookDetails> findByBooknameIgnoreCaseContainingAndAuthorIgnoreCaseContaining(String bookname,
      String author);

  BookDetails findByBooknameAndAuthorAndEdition(String bookname, String author, int edition);

}
