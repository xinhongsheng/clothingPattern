package com.xhs.clothingpatternbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 图案生成类型枚举
 */
@Getter
public enum GenerationTypeEnum {
    TEXT_GENERATED("文字生成", "TEXT_GENERATED"),
    IMAGE_REFERENCED("图片参考生成", "IMAGE_REFERENCED"),
    MJ_GENERATED("Midjourney生成", "MJ_GENERATED"),
    MANUAL_UPLOAD("手动上传", "手动上传");

    private final String text;
    private final String value;

    GenerationTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     *
     * @param value
     * @return
     */
    public static GenerationTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (GenerationTypeEnum typeEnum : GenerationTypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        return null;
    }
}
