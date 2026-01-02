package com.example.bookmanagement.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookmanagement.domain.model.Review;

public interface ReviewRespository extends JpaRepository<Review, Long>{

}
