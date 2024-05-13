package com.viladev.fundshare.model.dto;

import java.util.Calendar;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonSerializable.Base;
import com.viladev.fundshare.model.BaseEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class BaseEntityDto {

    UUID id;
    Calendar createdAt;
    UserDto createdBy;

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        BaseEntityDto other = (BaseEntityDto) obj;
        return this.id.equals(other.id);
    }

    public BaseEntityDto(BaseEntity baseEntity) {
        this.id = baseEntity.getId();
        this.createdAt = baseEntity.getCreatedAt();
        this.createdBy = baseEntity.getCreatedBy() != null ? new UserDto(baseEntity.getCreatedBy()) : null;
    }

}
