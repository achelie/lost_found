package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("claim")
public class Claim {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long itemId;
    private Long userId;
    // 文字证明（例如丢失细节、特征描述）
    private String proof;
    // 证明图片，多个同样用逗号分隔
    private String proofImages;
    private Integer status; // 0待审核 1通过 2拒绝
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 仅用于前端展示申请人姓名，不落库
    @TableField(exist = false)
    private String userName;
}
