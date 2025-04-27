package com.projects.hrs.services;

import com.projects.hrs.controller.requests.CreateClientRequest;
import com.projects.hrs.entities.Client;
import com.projects.hrs.entities.Tariff;
import com.projects.hrs.exceptions.RecordNotFoundException;
import com.projects.hrs.repositories.ClientRepository;
import com.projects.hrs.repositories.TariffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;
    private final TariffRepository tariffRepository;

    public Client create(CreateClientRequest request){
        Tariff tariff = tariffRepository.findById(request.tariffId())
                                        .orElseThrow(
                                                () -> new RecordNotFoundException("Такого тарифа не существует!")
                                        );
        return clientRepository.save(
                Client
                        .builder()
                        .userId(request.userId())
                        .tariff(tariff)
                        .build()
        );
    }
}
