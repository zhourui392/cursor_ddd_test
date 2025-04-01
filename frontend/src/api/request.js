import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api', // API 基础路径
  timeout: 15000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    console.log('准备发送请求:', config.url)
    // 请求头添加 token
    const token = localStorage.getItem('token')
    if (token) {
      // 检查token是否已包含Bearer前缀
      if (token.startsWith('Bearer ')) {
        console.log('使用完整token格式')
        config.headers['Authorization'] = token
      } else {
        console.log('添加Bearer前缀到token')
        config.headers['Authorization'] = `Bearer ${token}`
      }
      
      console.log('已设置Authorization头')
    } else {
      console.log('未找到token')
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    console.log('收到响应:', response.config.url, response.data)
    const res = response.data
    
    // 正常返回 - 兼容字符串类型的code
    if (res.code === 200 || res.code === "200") {
      return res
    }
    
    // 处理特殊错误码
    if (res.code === 401 || res.code === "401") {
      // 未授权，清除 token 并重定向到登录页
      console.warn('响应状态码401，未授权')
      localStorage.removeItem('token')
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else {
      // 其他错误
      console.error('响应错误码:', res.code, res.message)
      ElMessage.error(res.message || '服务器错误')
    }
    
    return Promise.reject(new Error(res.message || '服务器错误'))
  },
  error => {
    console.error('请求错误:', error.config?.url, error)
    const { response } = error
    
    if (response) {
      console.error('错误响应状态码:', response.status, response.data)
      // 根据响应状态码显示不同的错误信息
      switch (response.status) {
        case 401:
          localStorage.removeItem('token')
          router.push('/login')
          ElMessage.error('登录已过期，请重新登录')
          break
        case 403:
          ElMessage.error('没有权限访问该资源')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.message || '未知错误')
      }
    } else {
      // 请求超时或网络错误
      if (error.message.includes('timeout')) {
        console.error('请求超时')
        ElMessage.error('请求超时，请稍后重试')
      } else {
        console.error('网络错误')
        ElMessage.error('网络错误，请检查网络连接')
      }
    }
    
    return Promise.reject(error)
  }
)

export default service 