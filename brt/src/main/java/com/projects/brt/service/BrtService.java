package com.projects.brt.service;

import com.projects.brt.dto.Cdr;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrtService {
    public String saveCdrs(List<Cdr> cdrs) {
        return "ok";
    }
}
