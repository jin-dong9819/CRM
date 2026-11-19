package com.demo.service;

import com.demo.common.BizException;
import com.demo.dto.LoginDTO;
import com.demo.dto.PhoneLoginDTO;
import com.demo.entity.SysDept;
import com.demo.entity.SysEmployee;
import com.demo.entity.SysRole;
import com.demo.entity.User;
import com.demo.mapper.SysDeptMapper;
import com.demo.mapper.SysEmployeeMapper;
import com.demo.mapper.SysRoleMapper;
import com.demo.mapper.UserMapper;
import com.demo.util.TokenStore;
import com.demo.vo.LoginVO;
import com.demo.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysEmployeeMapper employeeMapper;
    private final SysRoleMapper roleMapper;
    private final SysDeptMapper deptMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenStore tokenStore;

    public LoginVO login(LoginDTO dto, String ip) {
        String empNo = dto.getEmpNo() == null ? "" : dto.getEmpNo().trim().toUpperCase();
        SysEmployee employee = employeeMapper.selectByEmpNo(empNo);

        // 账号不存在与密码错误统一提示，避免暴露工号是否存在
        if (employee == null || !passwordEncoder.matches(dto.getPassword(), employee.getPassword())) {
            throw new BizException(401, "员工编号或密码错误");
        }
        if (employee.getStatus() != null && employee.getStatus() == 0) {
            throw new BizException(403, "该员工已被停用，请联系管理员");
        }

        String token = tokenStore.create(employee.getId());
        employeeMapper.updateLoginInfo(employee.getId(), LocalDateTime.now(), ip);
        return new LoginVO(token, buildUserInfo(employee));
    }

    public UserInfoVO profile(Long empId) {
        SysEmployee employee = employeeMapper.selectById(empId);
        if (employee == null) throw new BizException(401, "登录状态已失效，请重新登录");
        return buildUserInfo(employee);
    }

    /** 手机号登录：注册用户走 sys_user，与员工登录互不影响 */
    public LoginVO loginByPhone(PhoneLoginDTO dto) {
        String phone = dto.getPhone() == null ? "" : dto.getPhone().trim();
        User user = userMapper.selectByPhone(phone);

        // 与员工登录一致：账号不存在和密码错误统一提示
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BizException(401, "手机号或密码错误");
        }

        String token = tokenStore.createUser(user.getId());
        return new LoginVO(token, buildUserInfo(user));
    }

    public UserInfoVO profileOfUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BizException(401, "登录状态已失效，请重新登录");
        return buildUserInfo(user);
    }

    public void logout(String token) {
        tokenStore.remove(token);
    }

    /** 组装当前用户信息：部门名 + 多角色 + 权限并集 */
    private UserInfoVO buildUserInfo(SysEmployee employee) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(employee.getId());
        vo.setEmpNo(employee.getEmpNo());
        vo.setName(employee.getName());
        vo.setAvatar(employee.getAvatar());
        vo.setDeptId(employee.getDeptId());

        SysDept dept = employee.getDeptId() == null ? null : deptMapper.selectById(employee.getDeptId());
        vo.setDeptName(dept == null ? null : dept.getDeptName());

        List<SysRole> roles = roleMapper.selectByEmployeeId(employee.getId());
        vo.setRoleNames(String.join("、", roles.stream().map(SysRole::getRoleName).toList()));
        vo.setRoleCodes(roles.stream().map(SysRole::getRoleCode).toList());
        vo.setPermissions(employeeMapper.selectPermCodesByEmpId(employee.getId()));
        return vo;
    }

    /** 注册用户信息：无部门无角色，仅展示打码手机号 */
    private UserInfoVO buildUserInfo(User user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setName(maskPhone(user.getPhone()));
        vo.setRoleNames("注册用户");
        vo.setRoleCodes(List.of(TokenStore.TYPE_USER));
        vo.setPermissions(List.of());
        return vo;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
