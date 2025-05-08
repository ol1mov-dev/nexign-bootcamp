package com.projects.hrs.repositories;

import com.projects.hrs.entities.Abonent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AbonentRepository extends JpaRepository<Abonent, Long> { }
