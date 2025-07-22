
package com.yazlab2proje1.repository;

import com.yazlab2proje1.entity.Log;
import com.yazlab2proje1.entity.PDF;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface LogRepository extends JpaRepository <Log,Integer> {
    List<Log> findByPdf(PDF pdf);
}
