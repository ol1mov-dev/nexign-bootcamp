package com.projects.cdr.mapper;

import com.projects.cdr.dto.CdrDto;
import com.projects.cdr.entities.Cdr;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CdrMapper {
    CdrDto toCdrDto(Cdr cdr);

    Cdr toCdr(CdrDto cdrDto);
}
