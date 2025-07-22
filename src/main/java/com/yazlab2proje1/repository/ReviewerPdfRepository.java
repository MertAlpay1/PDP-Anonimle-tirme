package com.yazlab2proje1.repository;

import com.yazlab2proje1.entity.APDF;
import com.yazlab2proje1.entity.Reviewer;
import com.yazlab2proje1.entity.ReviewerPdf;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewerPdfRepository extends JpaRepository <ReviewerPdf,Integer> {
    List<ReviewerPdf> findByReviewer(Reviewer reviewer);
    Optional<ReviewerPdf> findByApdf(APDF apdf);
    
}
