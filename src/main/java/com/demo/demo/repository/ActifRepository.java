package com.demo.demo.repository;

import com.demo.demo.entities.Actif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActifRepository extends JpaRepository<Actif, Integer> {
}
