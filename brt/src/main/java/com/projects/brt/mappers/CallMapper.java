package com.projects.brt.mappers;

import com.projects.brt.dto.CallDto;
import com.projects.brt.entities.Call;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CallMapper {
    Call toCallEntity(CallDto callDto);
}
