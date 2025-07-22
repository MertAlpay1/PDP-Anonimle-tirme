package com.yazlab2proje1.repository;

import com.yazlab2proje1.entity.Keywords;
import com.yazlab2proje1.entity.PDF;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface KeywordsRepository extends JpaRepository <Keywords,Integer> {
    
        boolean existsByKeywordAndPdf(String keyword, PDF pdf);
        
        List<Keywords> findByPdfId(Integer pdfId);

    
}
