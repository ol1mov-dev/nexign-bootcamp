package com.projects.brt.mappers;

import com.projects.brt.dto.CallDto;
import com.projects.brt.entities.Call;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CallMapper {
    CallDto toCallDto(Call call);

    @Mapping(target = "user", source = "user")
    Call toCallEntity(CallDto callDto);
}
