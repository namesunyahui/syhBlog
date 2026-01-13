package com.syh.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.syh.blog.entity.VisitLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 访问日志Mapper
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Mapper
public interface VisitLogMapper extends BaseMapper<VisitLog> {
}
