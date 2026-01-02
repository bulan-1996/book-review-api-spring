package com.example.bookmanagement.controller.response;

import java.util.List;

public record BookWithReviewsResponse(
		Long id,
		String title,
		String author,
		String isbn,
		String status,
		// レビュー情報
		List<ReviewResponse> reviews // レビュー一覧を含む
		) {}
