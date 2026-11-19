package com.zcst.hms.user.service;

import com.zcst.hms.common.dto.UserDTO;
import com.zcst.hms.common.util.JwtUtil;
import com.zcst.hms.user.entity.User;
import com.zcst.hms.user.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户 / 认证业务逻辑
 */
@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 用户注册，默认角色为患者
     */
    public UserDTO register(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (userMapper.selectByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("PATIENT");
        }
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes(StandardCharsets.UTF_8)));
        user.setStatus(1);
        userMapper.insert(user);
        return convert(user);
    }

    /**
     * 登录：校验账号密码，签发 JWT
     */
    public Map<String, Object> login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        String md5 = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!md5.equals(user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalArgumentException("账号已被停用，请联系管理员");
        }
        String token = JwtUtil.createToken(user.getId(), user.getUsername(), user.getRole(), user.getRealName());

        Map<String, Object> data = new HashMap<>(8);
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());
        data.put("deptCode", user.getDeptCode());
        data.put("title", user.getTitle());
        return data;
    }

    public UserDTO getById(Long id) {
        User user = userMapper.selectById(id);
        return user == null ? null : convert(user);
    }

    public UserDTO getByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        return user == null ? null : convert(user);
    }

    public UserDTO getByDoctorCode(String doctorCode) {
        return getByUsername(doctorCode);
    }

    public List<UserDTO> listDoctors(String deptCode, String keyword) {
        List<User> list = userMapper.selectDoctors(deptCode, keyword);
        List<UserDTO> result = new ArrayList<>();
        for (User u : list) {
            result.add(convert(u));
        }
        return result;
    }

    public UserDTO update(User user) {
        userMapper.updateById(user);
        return getById(user.getId());
    }

    private UserDTO convert(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}
