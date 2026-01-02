package com.example.bookmanagement.service.command;

public record BookCreateCommand(
	    String title,
	    String author,
	    String isbn
	) {}
