<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
              <el-button type="primary" link @click="handleEdit">
                修改
              </el-button>
            </div>
          </template>
          
          <div class="avatar-box">
            <el-avatar :size="100" icon="UserFilled" />
            <h3 class="nickname">{{ userStore.nickname || userStore.username }}</h3>
          </div>
          
          <div class="info-list">
            <div class="info-item">
              <span class="label">用户名：</span>
              <span class="value">{{ userStore.userInfo?.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱：</span>
              <span class="value">{{ userStore.userInfo?.email }}</span>
            </div>
            <div class="info-item">
              <span class="label">手机：</span>
              <span class="value">{{ userStore.userInfo?.phone }}</span>
            </div>
            <div class="info-item">
              <span class="label">角色：</span>
              <span class="value">
                <el-tag v-for="role in userStore.roles" :key="role.code" style="margin-right: 5px">
                  {{ role.name }}
                </el-tag>
              </span>
            </div>
            <div class="info-item">
              <span class="label">状态：</span>
              <span class="value">
                <el-tag :type="userStore.userInfo?.status ? 'success' : 'danger'">
                  {{ userStore.userInfo?.status ? '正常' : '禁用' }}
                </el-tag>
              </span>
            </div>
            <div class="info-item">
              <span class="label">创建时间：</span>
              <span class="value">{{ userStore.userInfo?.createTime }}</span>
            </div>
            <div class="info-item">
              <span class="label">最后登录：</span>
              <span class="value">{{ userStore.userInfo?.lastLoginTime }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>修改密码</span>
            </div>
          </template>
          
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="120px"
            class="password-form"
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入原密码"
              />
            </el-form-item>
            
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码"
              />
            </el-form-item>
            
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入新密码"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <el-card shadow="hover" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>我的权限</span>
            </div>
          </template>
          
          <el-empty v-if="!userStore.permissions || userStore.permissions.length === 0" description="暂无权限" />
          
          <div v-else class="permission-list">
            <el-tag
              v-for="permission in userStore.permissions"
              :key="permission"
              style="margin-right: 5px; margin-bottom: 5px"
            >
              {{ permission }}
            </el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 编辑个人信息弹窗 -->
    <el-dialog v-model="dialogVisible" title="修改个人信息" width="500px">
      <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="100px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProfile" :loading="profileLoading">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

// 修改密码表单
const passwordFormRef = ref(null)
const passwordLoading = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 修改个人信息
const profileFormRef = ref(null)
const profileLoading = ref(false)
const dialogVisible = ref(false)
const profileForm = reactive({
  nickname: '',
  email: '',
  phone: ''
})

// 表单验证规则
const validatePass = (rule, value, callback) => {
  if (value === passwordForm.oldPassword) {
    callback(new Error('新密码不能与原密码相同'))
  } else {
    if (passwordForm.confirmPassword !== '') {
      passwordFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, max: 30, message: '长度在 6 到 30 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 30, message: '长度在 6 到 30 个字符', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ]
}

const profileRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

// 打开编辑个人信息弹窗
const handleEdit = () => {
  dialogVisible.value = true
  
  // 填充表单
  const { nickname, email, phone } = userStore.userInfo || {}
  profileForm.nickname = nickname || ''
  profileForm.email = email || ''
  profileForm.phone = phone || ''
  
  // 等待DOM更新后重置表单验证
  setTimeout(() => {
    profileFormRef.value?.clearValidate()
  }, 0)
}

// 提交修改个人信息
const submitProfile = () => {
  profileFormRef.value.validate(async (valid) => {
    if (valid) {
      profileLoading.value = true
      try {
        // TODO: 调用更新个人信息接口
        // await userStore.updateProfile(profileForm)
        
        // 模拟更新成功
        setTimeout(() => {
          ElMessage.success('个人信息更新成功')
          dialogVisible.value = false
          
          // 重新获取用户信息
          userStore.getUserInfo()
          
          profileLoading.value = false
        }, 1000)
      } catch (error) {
        console.error('更新个人信息失败:', error)
        ElMessage.error('更新个人信息失败')
        profileLoading.value = false
      }
    } else {
      return false
    }
  })
}

// 修改密码
const handleChangePassword = () => {
  passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        // TODO: 调用修改密码接口
        // await userStore.changePassword({
        //   oldPassword: passwordForm.oldPassword,
        //   newPassword: passwordForm.newPassword
        // })
        
        // 模拟修改成功
        setTimeout(() => {
          ElMessage.success('密码修改成功，请重新登录')
          
          // 清空表单
          passwordForm.oldPassword = ''
          passwordForm.newPassword = ''
          passwordForm.confirmPassword = ''
          
          // 等待DOM更新后重置表单验证
          setTimeout(() => {
            passwordFormRef.value?.resetFields()
          }, 0)
          
          // 退出登录并重定向到登录页
          userStore.logout()
          
          passwordLoading.value = false
        }, 1000)
      } catch (error) {
        console.error('修改密码失败:', error)
        ElMessage.error('修改密码失败')
        passwordLoading.value = false
      }
    } else {
      return false
    }
  })
}

// 页面加载时获取最新用户信息
onMounted(() => {
  if (!userStore.userInfo) {
    userStore.getUserInfo()
  }
})
</script>

<style scoped>
.profile-container {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.nickname {
  margin-top: 15px;
  font-size: 18px;
}

.info-list {
  margin-top: 20px;
}

.info-item {
  display: flex;
  margin-bottom: 15px;
  font-size: 14px;
}

.label {
  color: #909399;
  width: 80px;
}

.value {
  color: #303133;
  flex: 1;
}

.password-form {
  max-width: 500px;
}

.permission-list {
  margin-top: 10px;
}
</style> 