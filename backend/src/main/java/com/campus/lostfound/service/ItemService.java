package com.campus.lostfound.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.lostfound.entity.Item;
import java.util.Map;

public interface ItemService extends IService<Item> {
    IPage<Item> listItems(int page, int size, Integer type, String category, String keyword);
    Item getDetail(Long id);
    void publish(Long userId, Item item);
    IPage<Item> getUserItems(Long userId, int page, int size);
    Map<String, Long> getStatistics();
}
