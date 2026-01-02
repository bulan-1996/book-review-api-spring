package com.example.bookmanagement.controller.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(
	
	@NotBlank(message = "レビュー内容は必須です")
	String content,
	
	@NotNull(message = "評価は必須です")
    @Min(value = 1, message = "評価は1以上で入力してください")
    @Max(value = 5, message = "評価は5以下で入力してください")
	int rating
){}
