package com.example.springapp.repository;

import com.example.springapp.domain.BeautyService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeautyServiceRepository extends JpaRepository<BeautyService, Long> {
}
