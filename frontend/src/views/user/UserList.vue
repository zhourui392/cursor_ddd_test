<template>
  <div class="user-container">
    <div class="table-operations">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 新增用户
      </el-button>
      <el-input
        v-model="searchQuery"
        placeholder="请输入用户名或昵称搜索"
        style="width: 250px; margin-left: 16px"
        clearable
        @clear="fetchUsers"
        @keyup.enter="fetchUsers"
      >
        <template #suffix>
          <el-icon @click="fetchUsers"><Search /></el-icon>
        </template>
      </el-input>
    </div>
    
    <el-table
      v-loading="loading"
      :data="userList"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="nickname" label="昵称" width="150" />
      <el-table-column prop="email" label="邮箱" width="200" />
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column prop="roles" label="角色" width="250">
        <template #default="scope">
          <el-tag v-for="role in scope.row.roles" :key="role.code" style="margin-right: 5px">
            {{ role.name }}
          </el-tag>
          <el-button v-if="!scope.row.roles || scope.row.roles.length === 0" 
            link type="primary" size="small" @click="openRoleDialog(scope.row)">
            分配角色
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status ? 'success' : 'danger'">
            {{ scope.row.status ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" fixed="right" width="250">
        <template #default="scope">
          <el-button type="primary" link size="small" @click="handleEdit(scope.row)">
            编辑
          </el-button>
          <el-button type="primary" link size="small" @click="openRoleDialog(scope.row)">
            分配角色
          </el-button>
          <el-button 
            type="primary" 
            link 
            size="small" 
            @click="handleStatusChange(scope.row)"
          >
            {{ scope.row.status ? '禁用' : '启用' }}
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(scope.row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination-container">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 用户编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增用户' : '编辑用户'" width="600px">
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="userForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" v-if="dialogType === 'add'">
          <el-input v-model="userForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="userForm.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 角色分配弹窗 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px">
      <div v-if="selectedUser">
        <p>用户：{{ selectedUser.nickname || selectedUser.username }}</p>
        <el-divider />
        <el-checkbox-group v-model="selectedRoles">
          <el-checkbox v-for="role in roleList" :key="role.code" :label="role.code">
            {{ role.name }}
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUserRoles" :loading="roleSubmitLoading">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserList, getUserById, updateUser, deleteUser, addRoleToUser, removeRoleFromUser } from '@/api/user'
import { getRoleList } from '@/api/role'
import { ElMessage, ElMessageBox } from 'element-plus'

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)
const roleSubmitLoading = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索参数
const searchQuery = ref('')

// 用户列表
const userList = ref([])

// 角色列表
const roleList = ref([])

// 弹窗控制
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const roleDialogVisible = ref(false)

// 选中的用户和角色
const selectedUser = ref(null)
const selectedRoles = ref([])

// 用户表单
const userFormRef = ref(null)
const userForm = reactive({
  id: null,
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  status: true
})

// 表单验证规则
const validatePass2 = (rule, value, callback) => {
  if (value !== userForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur', when: () => dialogType.value === 'add' },
    { min: 6, max: 30, message: '长度在 6 到 30 个字符', trigger: 'blur', when: () => dialogType.value === 'add' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur', when: () => dialogType.value === 'add' },
    { validator: validatePass2, trigger: 'blur', when: () => dialogType.value === 'add' }
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

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      query: searchQuery.value
    }
    
    const res = await getUserList(params)
    userList.value = res.data
    total.value = res.total
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 获取角色列表
const fetchRoles = async () => {
  try {
    const res = await getRoleList()
    roleList.value = res.data
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  }
}

// 新增用户
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  
  // 重置表单
  Object.keys(userForm).forEach(key => {
    userForm[key] = key === 'status' ? true : ''
  })
  
  // 等待DOM更新后重置表单验证
  setTimeout(() => {
    userFormRef.value?.resetFields()
  }, 0)
}

// 编辑用户
const handleEdit = (row) => {
  dialogType.value = 'edit'
  dialogVisible.value = true
  
  // 填充表单
  Object.keys(userForm).forEach(key => {
    if (key !== 'password' && key !== 'confirmPassword') {
      userForm[key] = row[key]
    }
  })
  
  // 等待DOM更新后重置表单验证
  setTimeout(() => {
    userFormRef.value?.clearValidate()
  }, 0)
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户 ${row.nickname || row.username} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      fetchUsers()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 修改用户状态
const handleStatusChange = (row) => {
  const statusText = row.status ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${statusText}用户 ${row.nickname || row.username} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateUser(row.id, { status: !row.status })
      ElMessage.success(`${statusText}成功`)
      fetchUsers()
    } catch (error) {
      ElMessage.error(`${statusText}失败`)
    }
  }).catch(() => {})
}

// 提交表单
const submitForm = () => {
  userFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 如果是编辑模式，去掉密码相关字段
        const submitData = { ...userForm }
        if (dialogType.value === 'edit') {
          delete submitData.password
          delete submitData.confirmPassword
        }
        
        if (dialogType.value === 'add') {
          // TODO: 替换为创建用户的API调用
          await updateUser(0, submitData)
        } else {
          await updateUser(userForm.id, submitData)
        }
        
        ElMessage.success(dialogType.value === 'add' ? '创建成功' : '更新成功')
        dialogVisible.value = false
        fetchUsers()
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error(dialogType.value === 'add' ? '创建失败' : '更新失败')
      } finally {
        submitLoading.value = false
      }
    } else {
      return false
    }
  })
}

// 打开角色分配弹窗
const openRoleDialog = (row) => {
  selectedUser.value = row
  roleDialogVisible.value = true
  
  // 设置已选择的角色
  selectedRoles.value = row.roles ? row.roles.map(role => role.code) : []
}

// 保存用户角色
const saveUserRoles = async () => {
  if (!selectedUser.value) return
  
  roleSubmitLoading.value = true
  try {
    const username = selectedUser.value.username
    const currentRoles = selectedUser.value.roles ? selectedUser.value.roles.map(role => role.code) : []
    
    // 找出需要添加的角色
    const rolesToAdd = selectedRoles.value.filter(role => !currentRoles.includes(role))
    
    // 找出需要移除的角色
    const rolesToRemove = currentRoles.filter(role => !selectedRoles.value.includes(role))
    
    // 添加角色
    for (const roleCode of rolesToAdd) {
      await addRoleToUser(username, roleCode)
    }
    
    // 移除角色
    for (const roleCode of rolesToRemove) {
      await removeRoleFromUser(username, roleCode)
    }
    
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
    fetchUsers()
  } catch (error) {
    console.error('角色分配失败:', error)
    ElMessage.error('角色分配失败')
  } finally {
    roleSubmitLoading.value = false
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchUsers()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchUsers()
}

// 页面加载时获取数据
onMounted(() => {
  fetchUsers()
  fetchRoles()
})
</script>

<style scoped>
.user-container {
  padding: 10px;
}

.table-operations {
  margin-bottom: 18px;
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style> 