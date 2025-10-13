package com.demo.demo.repository;

import com.demo.demo.entities.CoursModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoursModuleRepository extends JpaRepository<CoursModule, Integer> {
}
