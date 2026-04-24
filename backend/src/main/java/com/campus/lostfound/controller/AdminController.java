package com.campus.lostfound.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.dto.AdminChangePasswordDTO;
import com.campus.lostfound.dto.Result;
import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.NotificationService;
import com.campus.lostfound.service.UserService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ItemService itemService;
    private final UserService userService;
    private final ClaimService claimService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    public AdminController(ItemService itemService,
                           UserService userService,
                           ClaimService claimService,
                           NotificationService notificationService,
                           PasswordEncoder passwordEncoder) {
        this.itemService = itemService;
        this.userService = userService;
        this.claimService = claimService;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
    }

    // 管理员帖子列表：支持按状态、类型、分类和关键词组合筛选
    @GetMapping("/items")
    public Result<IPage<Item>> allItems(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        Page<Item> p = new Page<>(page, size);
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Item::getStatus, status);
        }
        if (type != null) {
            wrapper.eq(Item::getType, type);
        }
        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq(Item::getCategory, category.trim());
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Item::getTitle, kw)
                    .or().like(Item::getDescription, kw)
                    .or().like(Item::getLocation, kw)
                    .or().like(Item::getContact, kw));
        }
        wrapper.orderByDesc(Item::getCreatedAt).orderByDesc(Item::getId);

        return Result.success(itemService.page(p, wrapper));
    }

    // 审核帖子：通过后正常展示，拒绝时可记录拒绝理由并通知发布者
    @PostMapping("/items/{id}/audit")
    public Result<Void> auditItem(@PathVariable Long id, HttpServletRequest request) {
        try {
            Item item = itemService.getById(id);
            if (item == null) {
                return Result.error("物品不存在");
            }
            
            // 兼容前端两种传参格式: status=1 或者 status[status]=1
            String statusStr = request.getParameter("status");
            if (statusStr == null) {
                statusStr = request.getParameter("status[status]");
            }
            
            if (statusStr == null) {
                return Result.error("缺少status参数");
            }
            
            Integer status = Integer.parseInt(statusStr);
            item.setStatus(status);
            
            // 如果是拒绝状态，保存拒绝理由
            if (status == 2) {
                String rejectReason = request.getParameter("rejectReason");
                item.setRejectReason(rejectReason);
            } else {
                item.setRejectReason(null);
            }
            
            itemService.updateById(item);
            
            // 发送通知给发布用户
            String title, content;
            if (status == 1) {
                title = "帖子审核通过";
                content = "您发布的\"" + item.getTitle() + "\"已通过审核，现在已正常展示";
            } else if (status == 2) {
                title = "帖子审核未通过";
                content = "您发布的\"" + item.getTitle() + "\"审核被拒绝";
                if (item.getRejectReason() != null && !item.getRejectReason().isEmpty()) {
                    content = "您发布的\"" + item.getTitle() + "\"审核被拒绝，拒绝理由为：" + item.getRejectReason();
                }
            } else {
                return Result.success();
            }
            
            notificationService.send(item.getUserId(), title, content, 2, item.getId());
            
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("审核失败: " + e.getMessage());
        }
    }

    // 删除帖子时先清理它关联的认领记录，避免留下孤儿数据
    @DeleteMapping("/items/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        try {
            // 先删除该物品关联的所有认领记录，避免外键约束错误
            claimService.lambdaQuery().eq(Claim::getItemId, id).list().forEach(claim -> {
                claimService.removeById(claim.getId());
            });
            // 再删除物品
            itemService.removeById(id);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    // 管理员用户列表：支持关键词、状态和角色筛选
    @GetMapping("/users")
    public Result<IPage<User>> allUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer role) {
        Page<User> p = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(User::getUsername, kw)
                    .or().like(User::getNickname, kw)
                    .or().like(User::getEmail, kw)
                    .or().like(User::getPhone, kw));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreatedAt).orderByDesc(User::getId);

        IPage<User> result = userService.page(p, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result != null ? Result.success(result) : Result.error("查询失败");
    }

    // 启用/禁用用户时，不能误操作当前登录管理员自己
    @PatchMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("状态参数错误");
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Long currentUserId = getCurrentUserId();
        if (id.equals(currentUserId) && status == 0) {
            return Result.error("不能禁用当前登录账号");
        }

        user.setStatus(status);
        userService.updateById(user);
        return Result.success();
    }

    // 切换用户角色时，同样禁止把当前管理员降成普通用户
    @PatchMapping("/users/{id}/role")
    public Result<Void> updateUserRole(@PathVariable Long id, @RequestParam Integer role) {
        if (role == null || (role != 0 && role != 1)) {
            return Result.error("角色参数错误");
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        Long currentUserId = getCurrentUserId();
        if (id.equals(currentUserId) && role == 0) {
            return Result.error("不能取消当前登录账号的管理员权限");
        }

        user.setRole(role);
        userService.updateById(user);
        return Result.success();
    }

    // 管理员重置用户密码，直接覆盖为新密码的加密值
    @PutMapping("/users/{id}/password")
    public Result<Void> changeUserPassword(@PathVariable Long id, @Valid @RequestBody AdminChangePasswordDTO dto) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userService.updateById(user);
        return Result.success();
    }

    // 删除用户前先清理其认领记录、发布物品和物品下的认领记录
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        try {
            Long currentUserId = getCurrentUserId();
            
            // 禁止删除自己
            if (id.equals(currentUserId)) {
                return Result.error("不能删除当前登录的自己账号");
            }
            
            User user = userService.getById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 先删除该用户作为申请人的所有认领记录
            claimService.lambdaQuery().eq(Claim::getUserId, id).list().forEach(claim -> {
                claimService.removeById(claim.getId());
            });
            
            // 再删除该用户发布的所有物品（先删物品关联的所有认领，再删物品）
            itemService.lambdaQuery().eq(Item::getUserId, id).list().forEach(item -> {
                claimService.lambdaQuery().eq(Claim::getItemId, item.getId()).list().forEach(c -> claimService.removeById(c.getId()));
                itemService.removeById(item.getId());
            });
            
            // 最后删除用户
            userService.removeById(id);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    // 从 Spring Security 上下文取当前管理员 ID，用于防止误删自己
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        return Long.valueOf(auth.getName());
    }
}
