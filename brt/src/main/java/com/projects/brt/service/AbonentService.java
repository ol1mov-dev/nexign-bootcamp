package com.projects.brt.service;

import com.projects.brt.dto.AbonentDto;
import com.projects.brt.entities.Abonent;
import com.projects.brt.repositories.AbonentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AbonentService {
    private final AbonentRepository abonentRepository;

    public AbonentService(AbonentRepository abonentRepository) {
        this.abonentRepository = abonentRepository;
    }

    public ResponseEntity<Long> create(AbonentDto abonentDto) {
        return ResponseEntity.ok(
                abonentRepository.save(
                        Abonent
                                .builder()
                                .firstName(abonentDto.firstName())
                                .name(abonentDto.name())
                                .lastName(abonentDto.lastName())
                                .msisdn(abonentDto.msisdn())
                                .balance(abonentDto.balance())
                                .build()
                ).getId()
        );
    }
}
