package com.projects.brt.services;

import com.projects.brt.controllers.requests.CreateAbonentRequest;
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

    public ResponseEntity<Long> create(CreateAbonentRequest createAbonentRequest) {
        return ResponseEntity.ok(
                abonentRepository.save(
                        Abonent
                                .builder()
                                .firstName(createAbonentRequest.firstName())
                                .name(createAbonentRequest.name())
                                .middleName(createAbonentRequest.lastName())
                                .msisdn(createAbonentRequest.msisdn())
                                .balance(createAbonentRequest.balance())
                                .build()
                ).getId()
        );
    }
}
