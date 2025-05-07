package com.projects.brt.repositories;

import com.projects.brt.entities.Abonent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbonentRepository extends JpaRepository<Abonent, Long> {
    boolean existsByMsisdn(String msisdn);
    Abonent findByMsisdn(String msisdn);
}
