package com.example.bookmanagement.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookmanagement.controller.response.BookResponse;
import com.example.bookmanagement.controller.response.BookWithReviewsResponse;
import com.example.bookmanagement.controller.response.ReviewResponse;
import com.example.bookmanagement.domain.model.Book;
import com.example.bookmanagement.domain.model.Review;
import com.example.bookmanagement.domain.model.Status;
import com.example.bookmanagement.domain.repository.BookRepository;
import com.example.bookmanagement.domain.repository.ReviewRespository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional // 異常があった場合にDBの状態をロールバック（元に戻す）する
@RequiredArgsConstructor // Repositoryのコンストラクタ注入を自動化
public class BookDomainService {
	private final BookRepository bookRepository;
	private final ReviewRespository reviewRespository;
	
	/**
     * 一覧取得（参照系はreadOnly=trueにするとパフォーマンスが向上します）
     */
    @Transactional(readOnly = true)
	public List<BookResponse> findAll() {
//        return bookRepository.findAll();
    	return bookRepository.findAll().stream()
                .map(this::toBookResponse) // Service内に変換メソッドを持つ
                .toList();
    }
    
    /**
     * 本の新規登録（仕様書に合わせたビジネスロジック）
     */
    @Transactional
    public BookResponse registerBook(String title, String author, String isbn) {
        // 1. ドメインルール：ISBNの重複チェック
        if (isbn != null && bookRepository.existsByIsbn(isbn)) {
            throw new IllegalArgumentException("既に登録済みのISBNです: " + isbn);
        }

        // 2. Entityの組み立て（初期状態 AVAILABLE をここで強制する）
        Book book = Book.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .status(Status.AVAILABLE)
                .build();

        bookRepository.save(book);
        
        return toBookResponse(book);
    }
    
    /**
     * 本の貸し出し処理
     */
    @Transactional
    public BookResponse borrowBook(Long id) {
    	// 1. 取得
    	Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("指定されたIDの本が見つかりません: " + id));
    	
    	book.borrow();
    	bookRepository.save(book);
    	
    	return toBookResponse(book);
    }
    
    /**
     * 本の返却処理
     */
    @Transactional
    public BookResponse returnBook(Long id) {
    	// 1. 取得
    	Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("指定されたIDの本が見つかりません: " + id));
    	
    	book.returnBook();
    	bookRepository.save(book);
    	
    	return toBookResponse(book);
    }
    
    /**
     * レビューの追加
     */
    @Transactional
    public void addReview(Long id, String content, int rating) {
    	
    	// 1. 取得
    	Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("指定されたIDの本が見つかりません: " + id));
    	
    	
    	Review review = Review.builder()
    			.book(book)
    			.content(content)
    			.rating(rating)
    			.build();
    	
    	// 3. 保存処理の実行
    	reviewRespository.save(review);
    }
    
    /**
     * レビューも含めて一括で取得
     */
    @Transactional(readOnly = true)
    public BookWithReviewsResponse  getBookWithReviews(Long id){
    	// 1. 取得
    	Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("指定されたIDの本が見つかりません: " + id));
    	
    	// 2. DTOへ変換（ここに移設！）
    	List<ReviewResponse> reviews = book.getReview().stream()
    			.map(r -> new ReviewResponse(
    					r.getId(),
                        r.getContent(),
                        r.getRating(),
                        r.getCreatedAt()
    					)).toList();

        return new BookWithReviewsResponse(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getIsbn(),
            book.getStatus().name(),
            reviews
        );
    }
    
    private BookResponse toBookResponse(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(), book.getStatus().name());
    }
}
