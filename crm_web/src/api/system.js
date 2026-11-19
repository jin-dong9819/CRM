import request from './request'

// 员工
export const getEmployeePage = (params) => request.get('/system/employees', { params })
export const createEmployee = (data) => request.post('/system/employees', data)
export const updateEmployee = (id, data) => request.put(`/system/employees/${id}`, data)
export const deleteEmployee = (id) => request.delete(`/system/employees/${id}`)
export const changeEmployeeStatus = (id, status) =>
  request.patch(`/system/employees/${id}/status`, { status })
export const resetEmployeePassword = (id, password) =>
  request.put(`/system/employees/${id}/password`, password ? { password } : {})

// 角色
export const getRolePage = (params) => request.get('/system/roles', { params })
export const getAllRoles = () => request.get('/system/roles/all')
export const createRole = (data) => request.post('/system/roles', data)
export const updateRole = (id, data) => request.put(`/system/roles/${id}`, data)
export const deleteRole = (id) => request.delete(`/system/roles/${id}`)

// 权限
export const getPermissionTreeAll = () => request.get('/system/permissions/tree/all')
export const createPermission = (data) => request.post('/system/permissions', data)
export const updatePermission = (id, data) => request.put(`/system/permissions/${id}`, data)
export const deletePermission = (id) => request.delete(`/system/permissions/${id}`)

// 角色权限
export const getPermissionTree = () => request.get('/system/permissions/tree')
export const getRolePermissions = (id) => request.get(`/system/roles/${id}/permissions`)
export const assignRolePermissions = (id, permissionIds) =>
  request.put(`/system/roles/${id}/permissions`, { permissionIds })

// 部门
export const getDeptTree = (params) => request.get('/system/depts', { params })
export const getAllDepts = () => request.get('/system/depts/all')
export const createDept = (data) => request.post('/system/depts', data)
export const updateDept = (id, data) => request.put(`/system/depts/${id}`, data)
export const deleteDept = (id) => request.delete(`/system/depts/${id}`)
export const changeDeptStatus = (id, status) => request.patch(`/system/depts/${id}/status`, { status })
