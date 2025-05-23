package com.anran.partner.model.VueObject;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍用信息封装类(脱敏)
 * 包含创建人信息
 * 是否已经加入队伍
 */
@Data
public class TeamUserVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 创建人用户信息
     */
    UserVo createUser;

    /**
     * 是否已经加入队伍
     */
    private boolean hasJoin = false;

    /**
     * 加入的用户数
     */
    private Integer hasJoinNum;
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 0-公开 1-私有 2-加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTme;
}
