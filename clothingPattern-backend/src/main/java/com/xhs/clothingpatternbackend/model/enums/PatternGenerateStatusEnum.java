package com.xhs.clothingpatternbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum PatternGenerateStatusEnum {
    PENDING("Pending", "PENDING"),
    PROCESSING("Processing", "PROCESSING"),
    SUCCEEDED("Succeeded", "SUCCEEDED"),
    FAILED("Failed", "FAILED");

    private final String text;
    private final String value;

    PatternGenerateStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static PatternGenerateStatusEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PatternGenerateStatusEnum statusEnum : PatternGenerateStatusEnum.values()) {
            if (statusEnum.value.equals(value)) {
                return statusEnum;
            }
        }
        return null;
    }
}
