package com.yazlab2proje1.repository;

import com.yazlab2proje1.entity.Interest;
import com.yazlab2proje1.entity.PDF;
import com.yazlab2proje1.entity.Reviewer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ReviewerRepository extends JpaRepository <Reviewer,Integer> {
    
    List<Reviewer> findByInterestsContaining(Interest interest);
    
}
