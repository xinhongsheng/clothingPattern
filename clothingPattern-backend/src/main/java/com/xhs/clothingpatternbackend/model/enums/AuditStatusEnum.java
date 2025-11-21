package com.xhs.clothingpatternbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 图案审核状态枚举
 */
@Getter
public enum AuditStatusEnum {
    PENDING("待审核", "PENDING"),
    APPROVED("已通过", "APPROVED"),
    REJECTED("已拒绝", "REJECTED");

    private final String text;
    private final String value;

    AuditStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     *
     * @param value
     * @return
     */
    public static AuditStatusEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (AuditStatusEnum statusEnum : AuditStatusEnum.values()) {
            if (statusEnum.value.equals(value)) {
                return statusEnum;
            }
        }
        return null;
    }
}
