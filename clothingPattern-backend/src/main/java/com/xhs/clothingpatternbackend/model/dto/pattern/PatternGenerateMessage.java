package com.xhs.clothingpatternbackend.model.dto.pattern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatternGenerateMessage implements Serializable {

    private String taskId;

    private Long userId;

    private PatternGenerateRequest request;

    private static final long serialVersionUID = 1L;
}
