package com.projects.cdr.mapper;

import com.projects.cdr.dto.CdrDto;
import com.projects.cdr.entities.Cdr;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CdrMapper {
    CdrDto toCdrDto(Cdr cdr);

    @Mapping(target = "id", ignore = true)
    Cdr toCdrEntity(CdrDto cdrDto);
}
