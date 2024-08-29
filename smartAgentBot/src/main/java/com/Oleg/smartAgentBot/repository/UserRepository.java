package com.Oleg.smartAgentBot.repository;

import com.Oleg.smartAgentBot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}