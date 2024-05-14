package com.anran.partner.service.impl;

import com.anran.partner.model.domain.Tag;
import com.anran.partner.mapper.TagMapper;
import com.anran.partner.service.TagService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;


/**
* @author DELL
* @description 针对表【tag(标签表)】的数据库操作Service实现
* @createDate 2024-05-09 09:38:13
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

    @Resource
    private TagMapper tagMapper;

}




