package com.campus.lostfound.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    // 存储的是加密后的密码摘要，不是明文
    private String password;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer role;  // 0普通用户 1管理员
    private Integer status; // 0禁用 1正常
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
