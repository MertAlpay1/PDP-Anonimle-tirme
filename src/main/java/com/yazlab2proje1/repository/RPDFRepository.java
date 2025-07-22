
package com.yazlab2proje1.repository;

import com.yazlab2proje1.entity.PDF;
import com.yazlab2proje1.entity.RPDF;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RPDFRepository extends JpaRepository<RPDF,Integer> {
    Optional<RPDF> findByPdf(PDF pdf);
    
}
