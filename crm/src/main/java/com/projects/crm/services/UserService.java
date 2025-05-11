package com.projects.crm.services;

import com.projects.crm.controllers.requests.CreateAbonentHrsRequest;
import com.projects.crm.controllers.requests.CreateUserRequest;
import com.projects.crm.controllers.responses.UserCreatedResponse;
import com.projects.crm.controllers.responses.UserInfoResponse;
import com.projects.crm.dto.AbonentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class UserService {

    @Value(value = "${brt-service.uri}")
    private String brtServiceUri;

    @Value(value = "${hrs-service.uri}")
    private String hrsServiceUri;

    public ResponseEntity<UserInfoResponse> getInfo(long id) {
        return ResponseEntity.ok(null);
    }

    public ResponseEntity<UserCreatedResponse> create(CreateUserRequest createUserRequest) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Long> abonentId  = restTemplate.postForEntity(
            brtServiceUri + "/abonent/create",
            AbonentDto
                    .builder()
                    .firstName(createUserRequest.firstName())
                    .name(createUserRequest.name())
                    .lastName(createUserRequest.lastName())
                    .msisdn(createUserRequest.msisdn())
                    .balance(createUserRequest.balance())
                    .build(),
                Long.class
        );

        restTemplate.postForEntity(
                hrsServiceUri + "/abonent/create",
                CreateAbonentHrsRequest
                        .builder()
                        .userId(abonentId.getBody())
                        .tariffId(createUserRequest.tariffId())
                        .build(),
                String.class
        );

        return ResponseEntity.ok(
                UserCreatedResponse
                        .builder()
                        .msisdn(createUserRequest.msisdn())
                        .message("Пользователь успешно создан!")
                        .build()
        );
    }
}
