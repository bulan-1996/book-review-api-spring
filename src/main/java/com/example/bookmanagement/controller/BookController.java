package com.example.bookmanagement.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookmanagement.controller.request.BookCreateRequest;
import com.example.bookmanagement.controller.request.ReviewCreateRequest;
import com.example.bookmanagement.controller.response.BookResponse;
import com.example.bookmanagement.controller.response.BookWithReviewsResponse;
import com.example.bookmanagement.service.BookDomainService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
	private final BookDomainService bookDomainService;
	
	//1. 全書籍の一覧を取得
    @GetMapping
    public List<BookResponse> getAllBooks() {
        return bookDomainService.findAll();
    }
    
    // 2. 新しい書籍を登録
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookResponse createBook(@Valid @RequestBody BookCreateRequest request) {
        return bookDomainService.registerBook(request.title(), request.author(), request.isbn());
    }
    
    // 3. 貸し出しステータス更新ロジックの実装
    @PatchMapping("/{id}/borrow")
    public BookResponse borrowBook(@PathVariable Long id) {
    	return bookDomainService.borrowBook(id);
    }
    
    // 4. 返却処理
    @PatchMapping("/{id}/return")
    public BookResponse returnBook(@PathVariable Long id) {
    	return bookDomainService.returnBook(id);
    }
    
    // 5.レビューを投稿(登録)
    // 本の子要素のため、ここに記載
    @PostMapping("/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public void addReview(@PathVariable Long id,
    		@Valid @RequestBody ReviewCreateRequest request) {
    	bookDomainService.addReview(id,request.content(),request.rating());
    }
    
    
    // 6.レビューも含めて一括で取得
    @GetMapping("/{id}/bookWithReviews")
    public BookWithReviewsResponse getBookWithReviews(@PathVariable Long id){
    	return bookDomainService.getBookWithReviews(id);
    	
    }
}
