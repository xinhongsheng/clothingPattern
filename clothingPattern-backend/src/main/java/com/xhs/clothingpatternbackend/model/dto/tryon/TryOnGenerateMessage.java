package com.xhs.clothingpatternbackend.model.dto.tryon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TryOnGenerateMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String taskId;

    private Long userId;
}
