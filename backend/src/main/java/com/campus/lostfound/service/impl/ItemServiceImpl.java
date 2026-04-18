package com.campus.lostfound.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.lostfound.entity.Item;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.mapper.ItemMapper;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.UserService;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {

    private final UserService userService;

    public ItemServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public IPage<Item> listItems(int page, int size, Integer type, String category, String keyword) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getStatus, 1); // 只显示审核通过的
        if (type != null) wrapper.eq(Item::getType, type);
        if (category != null && !category.isEmpty()) wrapper.eq(Item::getCategory, category);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Item::getTitle, keyword)
                    .or().like(Item::getLocation, keyword)
                    .or().like(Item::getDescription, keyword));
        }
        wrapper.orderByDesc(Item::getCreatedAt);
        IPage<Item> page_result = page(new Page<>(page, size), wrapper);
        
        // 处理图片路径和用户昵称
        for (Item item : page_result.getRecords()) {
            // 设置用户昵称，优先使用昵称，如果没有则使用用户名
            if (item.getUserId() != null) {
                User user = userService.getById(item.getUserId());
                if (user != null) {
                    String displayName = user.getNickname();
                    if (displayName == null || displayName.isEmpty()) {
                        displayName = user.getUsername();
                    }
                    item.setNickName(displayName);
                }
            }
            
            if (item.getImages() != null && !item.getImages().isEmpty()) {
                String[] images = item.getImages().split(",");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < images.length; i++) {
                    String img = images[i].trim();
                    if (!img.startsWith("/uploads/")) {
                        img = "/uploads/" + img;
                    }
                    if (i > 0) sb.append(",");
                    sb.append(img);
                }
                item.setImages(sb.toString());
            }
        }
        return page_result;
    }

    @Override
    public Item getDetail(Long id) {
        Item item = getById(id);
        if (item != null && item.getUserId() != null) {
            User user = userService.getById(item.getUserId());
            if (user != null) {
                // 优先使用昵称，如果没有昵称则使用用户名
                String displayName = user.getNickname();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = user.getUsername();
                }
                item.setNickName(displayName);
            }
        }
        // 处理图片路径，确保可以正确加载
        if (item != null && item.getImages() != null && !item.getImages().isEmpty()) {
            String[] images = item.getImages().split(",");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < images.length; i++) {
                String img = images[i].trim();
                // 确保图片URL是相对URL
                if (!img.startsWith("/uploads/")) {
                    img = "/uploads/" + img;
                }
                if (i > 0) sb.append(",");
                sb.append(img);
            }
            item.setImages(sb.toString());
        }
        return item;
    }

    @Override
    public void publish(Long userId, Item item) {
        item.setUserId(userId);
        item.setStatus(0); // 待审核
        save(item);
    }

    @Override
    public IPage<Item> getUserItems(Long userId, int page, int size) {
        LambdaQueryWrapper<Item> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Item::getUserId, userId).orderByDesc(Item::getCreatedAt);
        IPage<Item> page_result = page(new Page<>(page, size), wrapper);
        
        // 处理图片路径和用户昵称
        User user = userService.getById(userId);
        for (Item item : page_result.getRecords()) {
            // 设置用户昵称
            if (user != null) {
                String displayName = user.getNickname();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = user.getUsername();
                }
                item.setNickName(displayName);
            }
            
            if (item.getImages() != null && !item.getImages().isEmpty()) {
                String[] images = item.getImages().split(",");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < images.length; i++) {
                    String img = images[i].trim();
                    if (!img.startsWith("/uploads/")) {
                        img = "/uploads/" + img;
                    }
                    if (i > 0) sb.append(",");
                    sb.append(img);
                }
                item.setImages(sb.toString());
            }
        }
        return page_result;
    }

    @Override
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        
        LambdaQueryWrapper<Item> normalWrapper = new LambdaQueryWrapper<>();
        normalWrapper.eq(Item::getStatus, 1); // 正常发布状态
        
        stats.put("lost", count(normalWrapper.clone().eq(Item::getType, 0)));
        stats.put("found", count(normalWrapper.clone().eq(Item::getType, 1)));
        
        LambdaQueryWrapper<Item> claimedWrapper = new LambdaQueryWrapper<>();
        claimedWrapper.eq(Item::getStatus, 3); // 已认领状态
        stats.put("claimed", count(claimedWrapper));
        
        return stats;
    }
}
