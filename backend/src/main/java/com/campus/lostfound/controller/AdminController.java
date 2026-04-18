package com.campus.lostfound.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lostfound.dto.Result;
import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.NotificationService;
import com.campus.lostfound.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ItemService itemService;
    private final UserService userService;
    private final ClaimService claimService;
    private final NotificationService notificationService;

    public AdminController(ItemService itemService, UserService userService, ClaimService claimService, NotificationService notificationService) {
        this.itemService = itemService;
        this.userService = userService;
        this.claimService = claimService;
        this.notificationService = notificationService;
    }

    @GetMapping("/items")
    public Result<IPage<Item>> allItems(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Page<Item> p = new Page<>(page, size);
        if (status != null) {
            return Result.success(itemService.lambdaQuery().eq(Item::getStatus, status).orderByDesc(Item::getCreatedAt).page(p));
        }
        return Result.success(itemService.lambdaQuery().orderByDesc(Item::getCreatedAt).page(p));
    }

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

    @GetMapping("/users")
    public Result<IPage<User>> allUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> p = new Page<>(page, size);
        IPage<User> result = userService.page(p);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result != null ? Result.success(result) : Result.error("查询失败");
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        try {
            // 获取当前登录用户ID
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            Long currentUserId = Long.valueOf(auth.getName());
            
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
}
