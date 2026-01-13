package com.syh.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syh.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
