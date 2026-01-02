package com.example.bookmanagement.controller.response;

import java.util.Map;

/**
 * クライアントに返却する共通エラーレスポンス形式
 * @param status HTTPステータスコード
 * @param message エラーの概要メッセージ
 * @param errors フィールドごとの詳細なエラー（バリデーション用、なければnull）
 */
public record ErrorResponse(
	    int status,
	    String message,
	    Map<String, String> errors
	) {}
