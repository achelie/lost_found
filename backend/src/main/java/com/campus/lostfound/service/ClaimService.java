package com.campus.lostfound.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.lostfound.entity.Claim;

public interface ClaimService extends IService<Claim> {
    void submitClaim(Long userId, Claim claim);
    IPage<Claim> getClaimsForItem(Long itemId, int page, int size);
    void auditClaim(Long ownerId, Long claimId, Integer status, String remark);
}
