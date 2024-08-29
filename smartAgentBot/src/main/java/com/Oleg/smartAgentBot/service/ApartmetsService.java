package com.Oleg.smartAgentBot.service;

import com.Oleg.smartAgentBot.model.Apartments;
import com.Oleg.smartAgentBot.repository.ApartmentsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class ApartmetsService {
    private final ApartmentsRepository apartmetsRepository;

    public void save(Apartments apartments) {
        apartmetsRepository.save(apartments);
    }

    public List<Apartments> findAllApartments() {
        return apartmetsRepository.findAll();
    }



    public List<Apartments> getFilteredApartments(String selectedDistrict, Integer minPrice, Integer maxPrice) {
        return apartmetsRepository.findAll().stream()
                .filter(apartment -> (selectedDistrict == null || apartment.getDistrict().equals(selectedDistrict)) &&
                        (minPrice == null || apartment.getPrice() >= minPrice) &&
                        (maxPrice == null || apartment.getPrice() <= maxPrice))
                .collect(Collectors.toList());
    }


    public List<Apartments> findApartmentsByUserId(long chatId) {
        return apartmetsRepository.findByUserId(chatId);
    }

    public void deleteApartment(Long id) {
        apartmetsRepository.deleteById(Math.toIntExact(id));
    }
}
