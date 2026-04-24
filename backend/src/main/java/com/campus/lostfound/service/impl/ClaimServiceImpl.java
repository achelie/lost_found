package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.mapper.ClaimMapper;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.NotificationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.service.UserService;

@Service
public class ClaimServiceImpl extends ServiceImpl<ClaimMapper, Claim> implements ClaimService {

    private final ItemService itemService;
    private final NotificationService notificationService;
    private final UserService userService;

    public ClaimServiceImpl(@Lazy ItemService itemService, NotificationService notificationService, UserService userService) {
        this.itemService = itemService;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @Override
    public void submitClaim(Long userId, Claim claim) {
        Long itemId = claim.getItemId();
        boolean exists = count(new LambdaQueryWrapper<Claim>()
                .eq(Claim::getItemId, itemId)
                .eq(Claim::getUserId, userId)) > 0;
        if (exists) {
            throw new RuntimeException("请不要重复申领");
        }

        claim.setUserId(userId);
        claim.setStatus(0);

        try {
            save(claim);
        } catch (DuplicateKeyException e) {
            throw new RuntimeException("请不要重复申领");
        }

        // 通知物品发布者
        Item item = itemService.getDetail(itemId);
        if (item != null) {
            notificationService.send(item.getUserId(), "收到认领申请",
                    "您发布的物品\"" + item.getTitle() + "\"收到了一条认领申请", 0, item.getId());
        }
    }

    @Override
    public IPage<Claim> getClaimsForItem(Long itemId, int page, int size) {
        IPage<Claim> claimPage = page(new Page<>(page, size), 
            new LambdaQueryWrapper<Claim>().eq(Claim::getItemId, itemId).orderByDesc(Claim::getCreatedAt));
        
        // 填充用户昵称
        claimPage.getRecords().forEach(claim -> {
            User user = userService.getById(claim.getUserId());
            if (user != null) {
                claim.setUserName(user.getNickname());
            }
        });
        
        return claimPage;
    }

    @Override
    public void auditClaim(Long ownerId, Long claimId, Integer status, String remark) {
        Claim claim = getById(claimId);
        if (claim == null) throw new RuntimeException("认领申请不存在");
        Item item = itemService.getDetail(claim.getItemId());
        if (item == null || !item.getUserId().equals(ownerId)) {
            throw new RuntimeException("无权操作");
        }
        claim.setStatus(status);
        claim.setRemark(remark);
        updateById(claim);
        // 如果通过，更新物品状态为已认领
        if (status == 1) {
            item.setStatus(3);
            itemService.updateById(item);
        }
        // 通知申请人
        String msg = status == 1 ? "您的认领申请已通过" : "您的认领申请被拒绝";
        notificationService.send(claim.getUserId(), "认领审核结果",
                msg + "（物品：" + item.getTitle() + "）", 1, item.getId());
    }
}
