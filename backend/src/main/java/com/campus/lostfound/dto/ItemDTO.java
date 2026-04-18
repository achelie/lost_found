package com.campus.lostfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemDTO {
    @NotNull(message = "类型不能为空")
    private Integer type;
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "分类不能为空")
    private String category;
    private String location;
    private String itemTime;
    private String description;
    private String contact;
    private String images;
}
