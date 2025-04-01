package com.example.demo.infrastructure.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.StringUtils;

/**
 * MyBatis Plus SQL工具类
 */
public class SqlUtil {
    
    /**
     * 构建排序分页对象
     *
     * @param pageNum      页码
     * @param pageSize     每页大小
     * @param orderField   排序字段
     * @param isAsc        是否升序
     * @param <T>          实体类型
     * @return             分页对象
     */
    public static <T> Page<T> buildPage(int pageNum, int pageSize, String orderField, Boolean isAsc) {
        Page<T> page = new Page<>(pageNum, pageSize);
        
        if (StringUtils.hasText(orderField)) {
            OrderItem orderItem = new OrderItem();
            orderItem.setColumn(orderField);
            orderItem.setAsc(isAsc != null && isAsc);
            page.addOrder(orderItem);
        }
        
        return page;
    }
    
    /**
     * 构建模糊查询条件
     *
     * @param queryWrapper 查询包装器
     * @param keyword      关键字
     * @param columns      要查询的列
     * @param <T>          实体类型
     * @return             查询包装器
     */
    @SafeVarargs
    public static <T> LambdaQueryWrapper<T> buildLikeQuery(LambdaQueryWrapper<T> queryWrapper, String keyword, SFunction<T, ?>... columns) {
        if (queryWrapper == null) {
            return null;
        }
        
        if (StringUtils.hasText(keyword) && columns != null && columns.length > 0) {
            queryWrapper.and(wrapper -> {
                for (int i = 0; i < columns.length; i++) {
                    if (i == 0) {
                        wrapper.like(columns[i], keyword);
                    } else {
                        wrapper.or().like(columns[i], keyword);
                    }
                }
            });
        }
        
        return queryWrapper;
    }
} 