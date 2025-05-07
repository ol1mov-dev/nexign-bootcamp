package com.projects.cdr.repository;

import com.projects.cdr.entities.Cdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CdrRepository extends JpaRepository<Cdr, Long> { }
