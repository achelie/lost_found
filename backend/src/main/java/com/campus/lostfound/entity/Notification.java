package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    // 通知接收者
    private Long userId;
    private String title;
    private String content;
    private Integer type; // 0认领申请 1审核结果 2系统通知
    // 关联业务主键（如 itemId），用于点击跳转
    private Long relatedId;
    private Integer isRead; // 0未读 1已读
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
