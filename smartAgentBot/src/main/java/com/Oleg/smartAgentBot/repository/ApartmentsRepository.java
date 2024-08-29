package com.Oleg.smartAgentBot.repository;

import com.Oleg.smartAgentBot.model.Apartments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApartmentsRepository extends JpaRepository<Apartments, Integer> {
    @Override
    List<Apartments> findAll();
    List<Apartments> findByUserId(long userId);
}
