package com.projects.cdr.repository;

import com.projects.cdr.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT * FROM USERS ORDER BY RAND() LIMIT 1", nativeQuery = true)
    User findRandomUser();
}
