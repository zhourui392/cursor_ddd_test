<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-title">
        <h2>系统登录</h2>
      </div>
      
      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="loginRules" 
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            :loading="loading" 
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 调试信息 -->
      <div v-if="debugInfo" class="debug-info">
        <h3>API调试信息：</h3>
        <pre>{{ debugInfo }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'

// 路由
const router = useRouter()
const route = useRoute()

// 用户状态仓库
const userStore = useUserStore()

// 登录表单引用
const loginFormRef = ref(null)

// 表单数据
const loginForm = reactive({
  username: '',
  password: ''
})

// 调试信息
const debugInfo = ref(null)

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '长度在 6 到 30 个字符', trigger: 'blur' }
  ]
}

// 加载状态
const loading = ref(false)

// 登录方法
const handleLogin = () => {
  // 清除之前的调试信息
  debugInfo.value = null
  
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        console.log('开始登录操作')
        const loginResult = await userStore.loginAction(loginForm)
        console.log('登录请求成功:', loginResult)
        
        ElMessage.success('登录成功')
        
        // 确保本地存储的token已更新
        const currentToken = localStorage.getItem('token')
        console.log('本地token更新状态:', !!currentToken)
        
        // 获取用户信息
        console.log('获取用户信息')
        try {
          const userInfoResult = await userStore.getUserInfo()
          console.log('用户信息获取成功:', userInfoResult)
          
          // 跳转到重定向页面或默认页面
          const redirectPath = route.query.redirect || '/'
          console.log('准备跳转到:', redirectPath)
          
          // 使用 replace 代替 push，确保导航触发
          setTimeout(() => {
            console.log('执行路由跳转')
            // 使用window.location强制进行导航
            if (redirectPath === '/') {
              window.location.href = '/'
            } else {
              router.replace(redirectPath)
            }
          }, 10)
        } catch (userError) {
          // 获取用户信息失败，记录错误并显示调试信息
          console.error('获取用户信息失败:', userError)
          
          if (userError.response) {
            debugInfo.value = JSON.stringify(userError.response.data, null, 2)
            ElMessage.error(`获取用户信息失败: ${userError.response.data?.message || '请检查API路径是否正确'}`)
          } else {
            debugInfo.value = userError.message || '未知错误'
            ElMessage.error('获取用户信息失败，请联系管理员')
          }
        }
      } catch (error) {
        console.error('登录过程错误:', error)
        if (error.response) {
          console.error('登录响应错误:', error.response.data)
          debugInfo.value = JSON.stringify(error.response.data, null, 2)
          ElMessage.error(`登录失败: ${error.response.data?.message || '请检查网络连接'}`)
        } else if (error.request) {
          console.error('未收到响应:', error.request)
          debugInfo.value = '服务器未响应'
          ElMessage.error('服务器未响应，请检查网络连接')
        } else {
          console.error('登录失败:', error.message)
          debugInfo.value = error.message || '未知错误'
          ElMessage.error(error.message || '登录失败，请重试')
        }
      } finally {
        loading.value = false
      }
    } else {
      return false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7f9;
}

.login-box {
  width: 400px;
  padding: 30px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
}

.login-form {
  margin-top: 20px;
}

.login-button {
  width: 100%;
}

.debug-info {
  margin-top: 20px;
  padding: 10px;
  background-color: #f8f8f8;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
  overflow-x: auto;
}

.debug-info pre {
  margin: 0;
  white-space: pre-wrap;
}
</style> 