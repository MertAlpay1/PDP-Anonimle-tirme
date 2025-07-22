package com.yazlab2proje1.repository;

import com.yazlab2proje1.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterestRepository extends JpaRepository<Interest,Integer> {
    
    List<Interest> findByinterestname(String keyword);
}
