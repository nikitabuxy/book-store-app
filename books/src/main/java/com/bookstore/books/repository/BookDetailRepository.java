package com.bookstore.books.repository;

import com.bookstore.books.model.BookDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookDetailRepository extends JpaRepository<BookDetails,Long> {
    BookDetails findByIsbn(String isbn);

    List<BookDetails> findByBooknameIgnoreCaseLikeAndAuthorIgnoreCaseLike(String bookname, String author);

    BookDetails deleteByIsbn(String isbn);

//    BookDetails findByBookNameAndEditionAndIsbn(String bookName, String edition, String isbn);

}
