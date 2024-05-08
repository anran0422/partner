package com.anran.partner.mapper;

import com.anran.partner.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author DELL
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2024-05-08 14:38:24
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




