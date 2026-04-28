import axios from 'axios'

const API_BASE = '/api'

const api = axios.create({
  baseURL: API_BASE,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.response.use(
  response => {
    if (response.data && response.data.code === 200) {
      return response.data
    }
    return Promise.reject(new Error(response.data?.message || '请求失败'))
  },
  error => {
    return Promise.reject(error)
  }
)

export const productApi = {
  getAll: () => api.get('/products'),
  getById: (id) => api.get(`/products/${id}`),
  create: (data) => api.post('/products', data),
  update: (id, data) => api.put(`/products/${id}`, data),
  delete: (id) => api.delete(`/products/${id}`)
}

export const userApi = {
  getAll: () => api.get('/users'),
  getById: (id) => api.get(`/users/${id}`),
  create: (data) => api.post('/users', data),
  update: (id, data) => api.put(`/users/${id}`, data),
  verify: (id) => api.post(`/users/${id}/verify`),
  getQuota: (id) => api.get(`/users/${id}/quota`)
}

export const orderApi = {
  getAll: () => api.get('/orders'),
  getById: (id) => api.get(`/orders/${id}`),
  getByUser: (userId) => api.get(`/orders/user/${userId}`),
  create: (data) => api.post('/orders', data),
  pay: (id) => api.post(`/orders/${id}/pay`),
  getByStatus: (status) => api.get(`/orders/status/${status}`)
}

export const customsApi = {
  getAll: () => api.get('/customs'),
  getById: (id) => api.get(`/customs/${id}`),
  getByOrder: (orderId) => api.get(`/customs/order/${orderId}`),
  create: (orderId) => api.post(`/customs/create/${orderId}`),
  submit: (declarationId) => api.post(`/customs/submit/${declarationId}`),
  process: (declarationId) => api.post(`/customs/process/${declarationId}`),
  retry: (declarationId) => api.post(`/customs/retry/${declarationId}`),
  getFailed: () => api.get('/customs/failed')
}

export const logisticsApi = {
  getAll: () => api.get('/logistics'),
  getById: (id) => api.get(`/logistics/${id}`),
  getByOrder: (orderId) => api.get(`/logistics/order/${orderId}`),
  create: (data) => api.post('/logistics/create', data),
  ship: (logisticsId) => api.post(`/logistics/ship/${logisticsId}`),
  updateStatus: (data) => api.post('/logistics/update-status', data),
  getTracking: (logisticsId) => api.get(`/logistics/tracking/${logisticsId}`)
}

export const refundApi = {
  getAll: () => api.get('/refunds'),
  getById: (id) => api.get(`/refunds/${id}`),
  getByOrder: (orderId) => api.get(`/refunds/order/${orderId}`),
  apply: (data) => api.post('/refunds/apply', data),
  approve: (refundId) => api.post(`/refunds/approve/${refundId}`),
  reject: (refundId, reason) => api.post(`/refunds/reject/${refundId}`, { reason })
}

export default api
