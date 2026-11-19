package com.demo.service;

import com.demo.common.BizException;
import com.demo.common.PageResult;
import com.demo.dto.EmployeeDTO;
import com.demo.entity.SysEmployee;
import com.demo.entity.SysRole;
import com.demo.mapper.SysDeptMapper;
import com.demo.mapper.SysEmployeeMapper;
import com.demo.mapper.SysRoleMapper;
import com.demo.vo.EmployeeRoleVO;
import com.demo.vo.EmployeeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final SysEmployeeMapper employeeMapper;
    private final SysRoleMapper roleMapper;
    private final SysDeptMapper deptMapper;
    private final PasswordEncoder passwordEncoder;

    public PageResult<EmployeeVO> page(String keyword, Long deptId, Integer status, Integer page, Integer pageSize) {
        int current = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 500);
        String kw = keyword == null || keyword.isBlank() ? null : keyword.trim();

        long total = employeeMapper.countPage(kw, deptId, status);
        List<EmployeeVO> list = employeeMapper.selectPage(kw, deptId, status, (current - 1) * size, size);
        fillRoles(list);
        list.forEach(this::fillGenderLabel);
        return new PageResult<>(list, total);
    }

    @Transactional
    public void save(Long id, EmployeeDTO dto, Long operatorId) {
        String empNo = dto.getEmpNo() == null ? "" : dto.getEmpNo().trim().toUpperCase();
        if (!empNo.matches("^[A-Z]\\d{3,9}$")) {
            throw new BizException("员工编号格式不正确，应形如 E1001");
        }
        if (dto.getDeptId() != null && deptMapper.selectById(dto.getDeptId()) == null) {
            throw new BizException("所属部门不存在");
        }

        SysEmployee sameNo = employeeMapper.selectByEmpNo(empNo);
        if (id == null) {
            if (sameNo != null) throw new BizException("员工编号已存在");
            insert(dto, empNo, operatorId);
            return;
        }

        SysEmployee old = employeeMapper.selectById(id);
        if (old == null) throw new BizException("员工不存在");
        if (sameNo != null && !sameNo.getId().equals(id)) throw new BizException("员工编号已存在");
        if (id.equals(operatorId) && dto.getStatus() != null && dto.getStatus() == 0) {
            throw new BizException("不能停用当前登录的账号");
        }

        try {
            SysEmployee employee = new SysEmployee();
            employee.setId(id);
            employee.setEmpNo(empNo);
            employee.setName(dto.getName());
            employee.setGender(dto.getGender());
            employee.setPhone(blankToNull(dto.getPhone()));
            employee.setEmail(blankToNull(dto.getEmail()));
            employee.setAvatar(blankToNull(dto.getAvatar()));
            employee.setDeptId(dto.getDeptId());
            employee.setJobTitle(dto.getJobTitle());
            employee.setHireDate(dto.getHireDate());
            employee.setStatus(dto.getStatus());
            employee.setRemark(dto.getRemark());
            employee.setUpdateBy(operatorId);
            employeeMapper.update(employee);
        } catch (DuplicateKeyException e) {
            throw new BizException("手机号已被其他员工使用");
        }

        // 多角色：先清空再写入
        employeeMapper.deleteEmployeeRoles(id);
        employeeMapper.insertEmployeeRoles(id, dto.getRoleIds());
    }

    private void insert(EmployeeDTO dto, String empNo, Long operatorId) {
        SysEmployee employee = new SysEmployee();
        employee.setEmpNo(empNo);
        String rawPwd = dto.getPassword() == null || dto.getPassword().isBlank()
                ? DEFAULT_PASSWORD : dto.getPassword();
        employee.setPassword(passwordEncoder.encode(rawPwd));
        employee.setName(dto.getName());
        employee.setGender(dto.getGender() == null ? 0 : dto.getGender());
        employee.setPhone(blankToNull(dto.getPhone()));
        employee.setEmail(blankToNull(dto.getEmail()));
        employee.setAvatar(blankToNull(dto.getAvatar()));
        employee.setDeptId(dto.getDeptId());
        employee.setJobTitle(dto.getJobTitle());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        employee.setRemark(dto.getRemark());
        employee.setCreateBy(operatorId);
        employee.setUpdateBy(operatorId);

        try {
            employeeMapper.insert(employee);
        } catch (DuplicateKeyException e) {
            throw new BizException("员工编号或手机号已存在");
        }
        employeeMapper.insertEmployeeRoles(employee.getId(), dto.getRoleIds());
    }

    @Transactional
    public void delete(Long id, Long operatorId) {
        if (id.equals(operatorId)) throw new BizException("不能删除当前登录的账号");
        SysEmployee employee = employeeMapper.selectById(id);
        if (employee == null) throw new BizException("员工不存在");
        checkLastAdmin(employee);

        employeeMapper.logicDelete(id, operatorId);
        employeeMapper.deleteEmployeeRoles(id);
    }

    public void updateStatus(Long id, Integer status, Long operatorId) {
        if (status == null || (status != 0 && status != 1)) throw new BizException("状态值不合法");
        if (id.equals(operatorId) && status == 0) throw new BizException("不能停用当前登录的账号");

        SysEmployee employee = employeeMapper.selectById(id);
        if (employee == null) throw new BizException("员工不存在");
        if (status == 0) checkLastAdmin(employee);

        employeeMapper.updateStatus(id, status, operatorId);
    }

    /** 重置密码，不传则重置为 123456 */
    public void resetPassword(Long id, String newPassword, Long operatorId) {
        SysEmployee employee = employeeMapper.selectById(id);
        if (employee == null) throw new BizException("员工不存在");

        String raw = newPassword == null || newPassword.isBlank() ? DEFAULT_PASSWORD : newPassword;
        if (raw.length() < 6) throw new BizException("密码长度不能少于 6 位");

        employeeMapper.updatePassword(id, passwordEncoder.encode(raw), java.time.LocalDateTime.now());
    }

    /** 系统里至少要留一个可用的超级管理员 */
    private void checkLastAdmin(SysEmployee employee) {
        List<SysRole> roles = roleMapper.selectByEmployeeId(employee.getId());
        boolean isAdmin = roles.stream().anyMatch(r -> "ADMIN".equals(r.getRoleCode()));
        if (!isAdmin) return;

        roles.stream()
                .filter(r -> "ADMIN".equals(r.getRoleCode()))
                .findFirst()
                .ifPresent(adminRole -> {
                    if (roleMapper.countEmployees(adminRole.getId()) <= 1) {
                        throw new BizException("系统至少需要保留一个超级管理员");
                    }
                });
    }

    /** 分页结果批量补角色，避免逐行查询 */
    private void fillRoles(List<EmployeeVO> list) {
        if (list.isEmpty()) return;

        List<Long> ids = new ArrayList<>();
        Map<Long, EmployeeVO> index = new LinkedHashMap<>();
        for (EmployeeVO vo : list) {
            ids.add(vo.getId());
            index.put(vo.getId(), vo);
            vo.setRoleIds(new ArrayList<>());
        }

        List<EmployeeRoleVO> rows = employeeMapper.selectRolesByEmpIds(ids);
        Map<Long, List<String>> names = new LinkedHashMap<>();
        for (EmployeeRoleVO row : rows) {
            EmployeeVO vo = index.get(row.getEmployeeId());
            if (vo == null) continue;
            vo.getRoleIds().add(row.getRoleId());
            names.computeIfAbsent(row.getEmployeeId(), k -> new ArrayList<>()).add(row.getRoleName());
        }
        index.forEach((empId, vo) -> vo.setRoleNames(String.join("、", names.getOrDefault(empId, List.of()))));
    }

    private void fillGenderLabel(EmployeeVO vo) {
        int gender = vo.getGender() == null ? 0 : vo.getGender();
        vo.setGenderLabel(switch (gender) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "未知";
        });
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
