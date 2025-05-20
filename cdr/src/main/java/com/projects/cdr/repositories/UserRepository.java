package com.projects.cdr.repositories;

import com.projects.cdr.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT * FROM USERS ORDER BY RAND() LIMIT 1", nativeQuery = true)
    User findRandomUser();
}
