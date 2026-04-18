package com.xhs.clothingpatternbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum GenerationTypeEnum {
    TEXT_GENERATED("文字生成", "TEXT_GENERATED"),
    IMAGE_GENERATED("图片生成", "IMAGE_GENERATED"),
    IMAGE_REFERENCED("图片参考生成", "IMAGE_REFERENCED"),
    MANUAL_UPLOAD("手动上传", "手动上传");

    private final String text;
    private final String value;

    GenerationTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

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
