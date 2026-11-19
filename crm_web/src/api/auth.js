import request from './request'

export const loginApi = (data) => request.post('/auth/login', data)

export const logoutApi = () => request.post('/auth/logout')

export const getProfileApi = () => request.get('/auth/profile')

/** 手机号登录（注册用户） */
export const phoneLoginApi = (data) => request.post('/auth/login/phone', data)

/** 发送短信验证码 */
export const sendSmsCodeApi = (phone) =>
  request.post('/auth/send-code', null, { params: { phone } })

/** 手机号注册 */
export const registerApi = (data) => request.post('/auth/register', data)
