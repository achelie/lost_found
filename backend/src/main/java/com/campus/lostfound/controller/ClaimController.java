package com.campus.lostfound.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.lostfound.dto.Result;
import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.service.ClaimService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping("/submit")
    public Result<Void> submit(Authentication auth, @RequestBody Claim claim) {
        Long userId = (Long) auth.getPrincipal();
        claimService.submitClaim(userId, claim);
        return Result.success();
    }

    @GetMapping("/item/{itemId}")
    public Result<IPage<Claim>> itemClaims(
            @PathVariable Long itemId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(claimService.getClaimsForItem(itemId, page, size));
    }

    @PostMapping("/audit/{claimId}")
    public Result<Void> audit(
            Authentication auth,
            @PathVariable Long claimId,
            @RequestParam Integer status,
            @RequestParam(required = false) String remark) {
        Long userId = (Long) auth.getPrincipal();
        claimService.auditClaim(userId, claimId, status, remark);
        return Result.success();
    }
}
