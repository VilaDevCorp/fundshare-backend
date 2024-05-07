package com.template.backtemplate.model;

import com.template.backtemplate.utils.ValidationCodeTypeEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.*;

@Entity
@Table(name = "validation_codes")
@Getter
@Setter
@NoArgsConstructor
public class ValidationCode extends BaseEntity {

    public static final int EXPIRATION_MINUTES = 15;
    
    Random random = new Random();

    private String fillWithZeros(String code) {
        if (code.length() < 6) {
            code = "0" + code;
            return fillWithZeros(code);
        } else {
            return code;
        }
    }

    private String code = fillWithZeros(Integer.toString(random.nextInt(999999)));
    private String type;
    private boolean used = false;

    public ValidationCode(ValidationCodeTypeEnum type) {
        this.type = type.getType();
    }

}
