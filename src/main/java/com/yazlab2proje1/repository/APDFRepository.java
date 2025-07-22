package com.yazlab2proje1.repository;

import com.yazlab2proje1.entity.APDF;
import com.yazlab2proje1.entity.PDF;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;



public interface APDFRepository extends JpaRepository <APDF,Integer> {
        Optional<APDF> findByPdf(PDF pdf);

}
