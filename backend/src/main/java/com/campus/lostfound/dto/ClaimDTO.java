package com.campus.lostfound.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClaimDTO {
    @NotNull(message = "物品ID不能为空")
    private Long itemId;
    private String proof;
    private String proofImages;
}
