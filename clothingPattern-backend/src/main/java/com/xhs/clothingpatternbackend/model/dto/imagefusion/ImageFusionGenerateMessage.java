package com.xhs.clothingpatternbackend.model.dto.imagefusion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageFusionGenerateMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String taskId;

    private Long userId;
}
