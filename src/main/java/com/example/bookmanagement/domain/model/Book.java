package com.example.bookmanagement.domain.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Getter // Setterをあえて作らず、メソッド経由で更新させるのがDDD流
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA用の空コンストラクタ
@AllArgsConstructor
@Builder
public class Book {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;
    
    @Column(unique = true, length = 13)
    private String isbn;

    @Enumerated(EnumType.STRING) // 文字列でDBに保存（AVAILABLE / BORROWED）
    @Column(nullable = false, length = 20)
    private Status status;
    
    // 1対多のリレーションシップ（本1：レビュー多）
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> review = new ArrayList<>();
    
 // --- 業務ロジック（Domain Method） ---

    /**
     * 本を貸出状態にする
     */
    public void borrow() {
        if (this.status == Status.BORROWED) {
            throw new IllegalStateException("この本は既に貸出中です。");
        }
        this.status = Status.BORROWED;
    }

    /**
     * 書籍を返却可能に変更 (PATCH /api/books/{id}/return 用)
     */
    public void returnBook() {
        if (this.status == Status.AVAILABLE) {
            throw new IllegalStateException("この書籍は既に返却されています。");
        }
        this.status = Status.AVAILABLE;
    }
}
