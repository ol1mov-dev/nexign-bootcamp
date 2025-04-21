package com.projects.brt.service;

import com.projects.brt.configuration.RabbitMqConfiguration;
import com.projects.brt.dto.CdrDto;
import com.projects.brt.entities.Call;
import com.projects.brt.entities.User;
import com.projects.brt.repositories.CallRepository;
import com.projects.brt.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CallService {

    private final UserRepository userRepository;
    private final CallRepository callRepository;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final RabbitTemplate rabbitTemplate;

    public void saveCall(List<CdrDto> cdrs) {
        cdrs.forEach(cdr -> {
            boolean isMsisdn1OurClient = isOurClient(cdr.msisdn1());
            boolean isMsisdn2OurClient = isOurClient(cdr.msisdn2());

            if (isMsisdn1OurClient && isMsisdn2OurClient) {
                callRepository.save(buildCall(cdr, cdr.msisdn1(), cdr.msisdn2()));
                callRepository.save(buildCall(cdr, cdr.msisdn2(), cdr.msisdn1()));
            }
            else if (isMsisdn1OurClient) {
                callRepository.save(buildCall(cdr, cdr.msisdn1(), cdr.msisdn2()));
            }
            else if(isMsisdn2OurClient) {
                callRepository.save(buildCall(cdr, cdr.msisdn2(), cdr.msisdn1()));
            } else {
                log.info("Ни один из номеров не является клиентом нашей компании.");
            }
        });
    }

    public String calculateDuration(String start, String end) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                .toFormatter();

        LocalDateTime dateTime1 = LocalDateTime.parse(start, formatter);
        LocalDateTime dateTime2 = LocalDateTime.parse(end, formatter);

        return String.valueOf(
                ChronoUnit.MINUTES.between(dateTime1, dateTime2)
        );
    }

    public boolean isOurClient(String msisdn) {
        return userRepository.existsByMsisdn(msisdn);
    }

    public Call buildCall(CdrDto cdr, String userMsisdn, String strangerMsisdn) {
        User user = userRepository.findByMsisdn(userMsisdn);

        if(user != null && user.getTariff().getId() == 11) {

//            rabbitTemplate.convertAndSend(
//                    RabbitMqConfiguration.EXCHANGE_NAME,
//                    RabbitMqConfiguration.CALL_CREATED_QUEUE,
//                    RabbitMqConfiguration.CALL_CREATED_ROUTING_KEY
//            );
            return Call
                    .builder()
                    .user(user)
                    .strangerMsisdn(strangerMsisdn)
                    .startTime(cdr.startTime())
                    .endTime(cdr.endTime())
                    .duration(calculateDuration(cdr.startTime(), cdr.endTime()))
                    .callType(cdr.callType())
                    .build();
        }

        return Call
                .builder()
                .user(user)
                .strangerMsisdn(strangerMsisdn)
                .startTime(cdr.startTime())
                .endTime(cdr.endTime())
                .duration(calculateDuration(cdr.startTime(), cdr.endTime()))
                .callType(cdr.callType())
                .build();
    }
}