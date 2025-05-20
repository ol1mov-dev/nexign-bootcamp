package com.projects.brt.services;

import com.projects.brt.controllers.requests.TopUpBalanceRequest;
import com.projects.brt.controllers.responses.TopUpBalanceResponse;
import com.projects.brt.dto.BillDto;
import com.projects.brt.entities.Abonent;
import com.projects.brt.repositories.AbonentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Slf4j
@Service
public class BalanceService {
    private final AbonentRepository abonentRepository;

    public BalanceService(AbonentRepository abonentRepository) {
        this.abonentRepository = abonentRepository;
    }

    public ResponseEntity<TopUpBalanceResponse> topUp(
            @Valid TopUpBalanceRequest topUpBalanceRequest
    ){
        Abonent abonent = abonentRepository.findById(topUpBalanceRequest.abonentId())
                .orElseThrow(EntityNotFoundException::new);

        BigDecimal newBalance = abonent.getBalance().add(topUpBalanceRequest.amount());
        abonent.setBalance(newBalance);
        abonentRepository.save(abonent);

        return ResponseEntity.status(200).body(
                TopUpBalanceResponse
                        .builder()
                        .abonentId(topUpBalanceRequest.abonentId())
                        .totalBalance(newBalance)
                        .build()
        );
    }
    /**
     * Оплата счета, который приходит из HRS
     * @param billDto информация о счете которая приходит из HRS
     */
    @RabbitListener(queues = "${rabbitmq.bill-created-queue}")
    public void payBill(BillDto billDto){
        Abonent abonent = abonentRepository
                .findById(billDto.abonentId())
                .orElseThrow(() -> new EntityNotFoundException("Такой абонент не найден"));

        if(abonent != null){
            BigDecimal currentBalance = abonent
                                        .getBalance()
                                        .subtract(billDto.totalPrice());

            abonent.setBalance(currentBalance);
            abonentRepository.save(abonent);
            log.info("ID: {} currentBalance: {}", billDto.abonentId(), currentBalance);
        }
    }
}
