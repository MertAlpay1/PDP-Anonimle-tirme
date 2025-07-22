package com.yazlab2proje1.repository;

import com.yazlab2proje1.entity.Message;
import com.yazlab2proje1.entity.PDF;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository <Message,Integer>{
    
    List<Message> findByPdf(PDF pdf);
}
