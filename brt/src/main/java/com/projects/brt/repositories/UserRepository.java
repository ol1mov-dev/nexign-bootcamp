package com.projects.brt.repositories;

import com.projects.brt.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByMsisdn(String msisdn);
    User findByMsisdn(String msisdn);
}
