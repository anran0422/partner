package com.anran.partner.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的分页请求参数
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = -5860707094194210842L;

    /**
     * 每页显示条数
     */
    protected int pageSize = 10;

    /**
     * 当前第几页 1~n
     */
    protected int pageNum = 1;
}
