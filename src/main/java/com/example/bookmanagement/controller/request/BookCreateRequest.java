package com.example.bookmanagement.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookCreateRequest(
		@NotBlank(message = "タイトルは必須です")
		String title, 
		
		@NotBlank(message = "著者名は必須です")
		String author, 
		
		@NotBlank(message = "ISBNは必須です")
		@Size(min = 13, max = 13, message = "ISBNはハイフンなしの13文字で入力してください")
		String isbn
) {}