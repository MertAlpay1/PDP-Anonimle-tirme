package com.yazlab2proje1.repository;

import com.yazlab2proje1.entity.FPDF;
import com.yazlab2proje1.entity.PDF;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FPDFRepository extends JpaRepository <FPDF,Integer> {
        Optional<FPDF> findByPdf(PDF pdf);

}
