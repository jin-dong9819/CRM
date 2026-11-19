import request from './request'

// 线索
export const getLeadPage = (params) => request.get('/leads', { params })
export const createLead = (data) => request.post('/leads', data)
export const updateLead = (id, data) => request.put(`/leads/${id}`, data)
export const deleteLead = (id) => request.delete(`/leads/${id}`)

// 客户
export const getCustomerPage = (params) => request.get('/customers', { params })
export const createCustomer = (data) => request.post('/customers', data)
export const updateCustomer = (id, data) => request.put(`/customers/${id}`, data)
export const deleteCustomer = (id) => request.delete(`/customers/${id}`)

// 商机
export const getOpportunityPage = (params) => request.get('/opportunities', { params })
export const createOpportunity = (data) => request.post('/opportunities', data)
export const updateOpportunity = (id, data) => request.put(`/opportunities/${id}`, data)
export const deleteOpportunity = (id) => request.delete(`/opportunities/${id}`)

// 订单
export const getOrderPage = (params) => request.get('/orders', { params })
export const createOrder = (data) => request.post('/orders', data)
export const updateOrder = (id, data) => request.put(`/orders/${id}`, data)
export const deleteOrder = (id) => request.delete(`/orders/${id}`)

// 产品（易损件 / 养护用品）
export const getProductPage = (params) => request.get('/products', { params })
export const getProduct = (id) => request.get(`/products/${id}`)
export const createProduct = (data) => request.post('/products', data)
export const updateProduct = (id, data) => request.put(`/products/${id}`, data)
export const deleteProduct = (id) => request.delete(`/products/${id}`)

// 产品分类
export const getProductCategories = (params) => request.get('/product-categories', { params })
export const createProductCategory = (data) => request.post('/product-categories', data)
export const updateProductCategory = (id, data) => request.put(`/product-categories/${id}`, data)
export const deleteProductCategory = (id) => request.delete(`/product-categories/${id}`)
