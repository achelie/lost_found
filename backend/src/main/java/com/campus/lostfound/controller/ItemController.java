package com.campus.lostfound.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.lostfound.dto.Result;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.service.ItemService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/list")
    public Result<IPage<Item>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return Result.success(itemService.listItems(page, size, type, category, keyword));
    }

    @GetMapping("/detail/{id}")
    public Result<Item> detail(@PathVariable Long id) {
        return Result.success(itemService.getDetail(id));
    }

    @PostMapping("/publish")
    public Result<Void> publish(Authentication auth, @RequestBody Item item) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("用户未认证");
        }
        Long userId = (Long) auth.getPrincipal();
        itemService.publish(userId, item);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<IPage<Item>> myItems(
            Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) auth.getPrincipal();
        return Result.success(itemService.getUserItems(userId, page, size));
    }

    @GetMapping("/stats")
    public Result<?> getStats() {
        return Result.success(itemService.getStatistics());
    }
}
