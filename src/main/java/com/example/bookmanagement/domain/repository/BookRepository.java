package com.example.bookmanagement.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.bookmanagement.domain.model.Book;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    // JpaRepositoryを継承するだけで、save(), findById(), findAll() などが使えるようになります。
	
	/**
     * ISBNが既にDBに存在するかどうかを確認する
     * * @param isbn チェックしたいISBNコード
     * @return 存在すればtrue, 存在しなければfalse
     */
    boolean existsByIsbn(String isbn);
}
