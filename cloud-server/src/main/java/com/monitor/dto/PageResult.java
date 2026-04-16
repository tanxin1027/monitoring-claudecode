package com.monitor.dto;

import lombok.Data;

import java.util.List;

/**
 * 分页结果 DTO
 */
@Data
public class PageResult<T> {
    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;
}