package com.zcst.hms.user.mapper;

import com.zcst.hms.user.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问接口
 */
public interface UserMapper {

    User selectById(@Param("id") Long id);

    User selectByUsername(@Param("username") String username);

    List<User> selectByRole(@Param("role") String role);

    List<User> selectDoctors(@Param("deptCode") String deptCode, @Param("keyword") String keyword);

    int insert(User user);

    int updateById(User user);
}
