package org.finsible.backend.authentication.repository;

import org.finsible.backend.authentication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
