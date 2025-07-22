package com.yazlab2proje1.repository;

import com.yazlab2proje1.entity.PDF;
import com.yazlab2proje1.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ReviewRepository extends JpaRepository <Review,Integer> {
    
    List<Review> findByPdf(PDF pdf);
}
