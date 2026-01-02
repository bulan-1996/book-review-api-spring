package com.example.bookmanagement.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.bookmanagement.controller.response.BookResponse;
import com.example.bookmanagement.controller.response.BookWithReviewsResponse;
import com.example.bookmanagement.domain.model.Book;
import com.example.bookmanagement.domain.model.Review;
import com.example.bookmanagement.domain.model.Status;
import com.example.bookmanagement.domain.repository.BookRepository;
import com.example.bookmanagement.domain.repository.ReviewRespository;

@ExtendWith(MockitoExtension.class)
class BookDomainServiceTest {
	@Mock
    private BookRepository bookRepository;
	@Mock
	private ReviewRespository reviewRespository;

    @InjectMocks
    private BookDomainService bookDomainService;
    
    // 1. findAll（全件取得）
    @Test
    @DisplayName("全件取得：本が複数登録されている場合、リストが正しく返ってくること")
    void findAll_success_multipleBooks() {
    	// 1. GIVEN: 事前に複数のBookオブジェクト（Entity）を作成する
        Book book1 = Book.builder()
                .id(1L).title("Java入門").author("著者A").isbn("111-111").status(Status.AVAILABLE)
                .build();
        Book book2 = Book.builder()
                .id(2L).title("Spring Boot解説").author("著者B").isbn("222-222").status(Status.BORROWED)
                .build();
        
        // 作成した本をリストにまとめる
        List<Book> bookList = List.of(book1,book2);
        
        // リポジトリがこのリストを返すように設定
        when(bookRepository.findAll()).thenReturn(bookList);
        
        List<BookResponse> returnList = bookDomainService.findAll();
        
     	// WHEN(実行)
        assertThat(returnList).hasSize(2);
        assertThat(returnList.get(0).title()).isEqualTo("Java入門");
        assertThat(returnList.get(1).title()).isEqualTo("Spring Boot解説");
    	
    	verify(bookRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("全件取得：本が1件も登録されていない場合、空のリストが返ってくること")
    void findAll_success_emptyList() {
    	// GIVENリポジトリが空のリストを返す
    	when(bookRepository.findAll()).thenReturn(List.of());
    	
    	// WHEN(実行)
    	List<BookResponse> returnList = bookDomainService.findAll();
    	
    	// THEN(検証)
    	assertThat(returnList).isNotNull().isEmpty();
    	verify(bookRepository, times(1)).findAll();
    }

    // 2. registerBook（新規登録）
    @Test
    @DisplayName("新規登録：新しいISBNで登録した際、ステータスがAVAILABLEで保存されること")
    void registerBook_success() {
    	// GIVEN
    	String title = "新規登録Java";
    	String author = "著者C";
    	String isbn = "1234567891011";
    	
    	when(bookRepository.existsByIsbn(isbn)).thenReturn(false);
    	when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArguments()[0]);
    	
    	// WHEN(実行)
    	BookResponse result = bookDomainService.registerBook(title,author,isbn);
    	
    	// THEN(検証)
    	assertThat(result.title()).isEqualTo(title);
    	assertThat(result.status()).isEqualTo("AVAILABLE");
    	verify(bookRepository,times(1)).save(any(Book.class));
    	
    }

    @Test
    @DisplayName("新規登録：既に存在するISBNを指定した場合、IllegalArgumentExceptionが発生すること")
    void registerBook_fail_duplicateIsbn() {
    	// GIVEN
    	String title = "新規登録Java2";
    	String author = "著者D";
    	String isbn = "1234567891012";
    	
    	when(bookRepository.existsByIsbn(isbn)).thenReturn(true);
    	
    	// THEN
    	assertThatThrownBy(() -> bookDomainService.registerBook(title, author, isbn))
    		.isInstanceOf(IllegalArgumentException.class)
    		.hasMessage("既に登録済みのISBNです: " + isbn);
    	
    	verify(bookRepository, times(1)).existsByIsbn(isbn);
    	verify(bookRepository, never()).save(any(Book.class));
    	
    }

    // 3. borrowBook（貸出処理）
    @Test
    @DisplayName("貸出処理：本が貸出可能ならステータスがBORROWEDになること")
    void borrowBook_success() {
    	// GIVEN(全体条件)
    	Long bookId = 1L;
    	Book targetBook = Book.builder()
    			.id(bookId)
    			.status(Status.AVAILABLE)
    			.build();
    	
    	when(bookRepository.findById(bookId)).thenReturn(Optional.of(targetBook));
    	when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArguments()[0]);
    	
    	// WHEN(実行)
    	BookResponse result = bookDomainService.borrowBook(bookId);
    	
    	// THEN(検証)
    	assertThat(result.status()).isEqualTo("BORROWED");
    	verify(bookRepository,times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("貸出処理:本が見つからない場合に例外が発生すること")
    void borrowBook_notFound() {
    	// GIVEN
    	Long bookId = 99L;
    	// リポジトリが「空」を返すように設定
    	when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
    	
    	// WHEN(実行) & THEN(検証)
        assertThatThrownBy(() -> bookDomainService.borrowBook(bookId))
            .isInstanceOf(IllegalArgumentException.class) // 期待する例外クラス（プロジェクトに合わせて変更）
            .hasMessage("指定されたIDの本が見つかりません: " + bookId); // 例外メッセージに含まれるべき文言

        // 異常終了するので、saveメソッドは一度も呼ばれないことを検証
        verify(bookRepository, never()).save(any(Book.class));		
    }

    @Test
    @DisplayName("貸出処理：すでに貸出中の本を借りようとした場合、エラーになること")
    void borrowBook_fail_alreadyBorrowed() {
    	// GIVEN
    	Long bookId = 1L;
    	Book book = Book.builder()
    			.id(bookId)
    			.status(Status.BORROWED)
    			.build();
    	
    	when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
    	
    	// WHEN(実行)
    	assertThatThrownBy(() -> bookDomainService.borrowBook(bookId))
        .isInstanceOf(IllegalStateException.class) // 期待する例外クラス（プロジェクトに合わせて変更）
        .hasMessage("この本は既に貸出中です。"); // 例外メッセージに含まれるべき文言
    	
    	verify(bookRepository,never()).save(any(Book.class));
    }

    // 4. returnBook（返却処理）
    @Test
    @DisplayName("返却処理：存在するIDかつ貸出中の本の場合、ステータスがAVAILABLEに戻ること")
    void returnBook_success() {
    	// GIVEN
    	Long bookId = 1L;
    	Book book = Book.builder()
    			.id(bookId)
    			.status(Status.BORROWED)
    			.build();
    	
    	when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
    	when(bookRepository.save(any(Book.class))).thenAnswer(i -> i.getArguments()[0]);
    	
    	// WHEN
    	BookResponse result = bookDomainService.returnBook(bookId);
    	
    	// THEN
    	assertThat(result.status()).isEqualTo("AVAILABLE");
    	verify(bookRepository,times(1)).save(any(Book.class));

    }

    @Test
    @DisplayName("返却処理：存在しないIDを指定した場合、IllegalArgumentExceptionが発生すること")
    void returnBook_fail_notFound() {
    	// GIVEN
    	Long bookId = 99L;
    	// リポジトリが「空」を返すように設定
    	when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
    	
    	assertThatThrownBy(() -> bookDomainService.returnBook(bookId))
    	.isInstanceOf(IllegalArgumentException.class)
    	.hasMessage("指定されたIDの本が見つかりません: " + bookId);
    	
    	// 以上終了するので、saveメソッドは一度も呼ばれないことを検証
    	verify(bookRepository,never()).save(any(Book.class));
    }

    // 5. addReview / getBookWithReviews（レビュー関連）
    @Test
    @DisplayName("レビュー追加：レビューを追加した際、リポジトリの保存メソッドが呼ばれること")
    void addReview_success() {
    	// GIVEN
    	Long bookId = 1L;
    	String content = "最高の一冊でした！";
        int rating = 5;
    	
    	Book book = Book.builder()
                .id(bookId).title("Java入門").author("著者A").isbn("111-111").status(Status.AVAILABLE)
                .build();
    	
    	when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

    	// WHEN
    	bookDomainService.addReview(bookId,content,rating);
    	
    	// THEN
    	verify(reviewRespository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("レビュー追加：本が存在しない場合、IllegalArgumentExceptionが発生すること")
    void addReview_fail_notFound() {
    	// GIVEN
    	Long bookId = 99L;
    	String content = "最高の一冊でした！";
    	int rating = 5;
    	
    	when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
    	// WHEN
    	assertThatThrownBy(() -> bookDomainService.addReview(bookId,content,rating))
    	.isInstanceOf(IllegalArgumentException.class)
    	.hasMessage("指定されたIDの本が見つかりません: " + bookId);
    	
    	// THEN
    	verify(reviewRespository, never()).save(any(Review.class));
    	
    }

    @Test
    @DisplayName("詳細取得：レビュー付きで本を取得した際、レスポンスにレビュー一覧が含まれていること")
    void getBookWithReviews_success() {
    	// GIVEN
    	// 1. GIVEN: レビューを1件持っている本を作成
        Long bookId = 1L;
        
        // 先にレビュー単体を作成
        Review review = Review.builder()
                .id(10L)
                .content("素晴らしい本です")
                .rating(5)
                .build();

        // そのレビューをリストに入れてBookに持たせる
        Book book = Book.builder()
                .id(bookId)
                .title("Java入門")
                .status(Status.AVAILABLE)
                .review(List.of(review))
                .build();
        
        // リポジトリがこのリストを返すように設定
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        
        // WHEN
        BookWithReviewsResponse result = bookDomainService.getBookWithReviews(bookId);
        
        // THEN
        assertThat(result.id()).isEqualTo(bookId);
        assertThat(result.reviews()).hasSize(1);
        assertThat(result.reviews().get(0).content()).isEqualTo("素晴らしい本です");
    }
}
