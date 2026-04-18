package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("item")
public class Item {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer type;  // 0失物 1招领
    private String title;
    private String category;
    private String location;
    private LocalDateTime itemTime;
    private String description;
    private String contact;
    private String images;
    private Integer status; // 0待审核 1已通过 2已拒绝 3已认领 4已关闭
    private String rejectReason; // 管理员拒绝理由
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String nickName;
}
