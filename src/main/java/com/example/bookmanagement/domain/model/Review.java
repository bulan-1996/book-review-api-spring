package com.example.bookmanagement.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Reviews")
@Getter // Setterをあえて作らず、メソッド経由で更新させるのがDDD流
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA用の空コンストラクタ
@AllArgsConstructor
@Builder
public class Review {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
	private Book book;

	@Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
	@Column(nullable = false)
    private int rating;

	@Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
	
	// 保存前に投稿日時を自動設定
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
