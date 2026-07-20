package com.example.springapp.repository;

import com.example.springapp.domain.Designer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DesignerRepository extends JpaRepository<Designer, Long> {
}
