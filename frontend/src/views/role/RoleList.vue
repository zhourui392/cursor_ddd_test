<template>
  <div class="role-container">
    <div class="table-operations">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 新增角色
      </el-button>
    </div>
    
    <el-table
      v-loading="loading"
      :data="roleList"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="角色名称" width="150" />
      <el-table-column prop="code" label="角色编码" width="150" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="permissions" label="权限" width="280">
        <template #default="scope">
          <el-tooltip
            v-if="scope.row.permissions && scope.row.permissions.length > 0"
            effect="dark"
            placement="top"
          >
            <template #content>
              <div v-for="perm in scope.row.permissions" :key="perm.id">
                {{ perm.name }}
              </div>
            </template>
            <el-tag>{{ scope.row.permissions.length }} 个权限</el-tag>
          </el-tooltip>
          <el-button v-else link type="primary" size="small" @click="openPermissionDialog(scope.row)">
            分配权限
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
          <el-button type="primary" link size="small" @click="openPermissionDialog(scope.row)">
            分配权限
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
    
    <!-- 角色编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增角色' : '编辑角色'" width="500px">
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="roleForm.code" placeholder="请输入角色编码" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="roleForm.description" type="textarea" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="roleForm.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 分配权限弹窗 -->
    <el-dialog v-model="permissionDialogVisible" title="分配权限" width="600px">
      <div v-if="selectedRole">
        <p>角色：{{ selectedRole.name }}</p>
        <el-divider />
        
        <el-tree
          ref="permissionTreeRef"
          node-key="id"
          :data="permissionTreeData"
          :props="{ label: 'name', children: 'children' }"
          show-checkbox
          default-expand-all
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveRolePermissions" :loading="permissionSubmitLoading">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRoleList, getRoleById, createRole, updateRole, deleteRole, addPermissionToRole, removePermissionFromRole } from '@/api/role'
import { getPermissionList } from '@/api/permission'
import { ElMessage, ElMessageBox } from 'element-plus'

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)
const permissionSubmitLoading = ref(false)

// 角色列表
const roleList = ref([])

// 权限树数据
const permissionTreeData = ref([])
const permissionTreeRef = ref(null)

// 弹窗控制
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const permissionDialogVisible = ref(false)

// 选中的角色
const selectedRole = ref(null)

// 角色表单
const roleFormRef = ref(null)
const roleForm = reactive({
  id: null,
  name: '',
  code: '',
  description: '',
  status: true
})

// 表单验证规则
const roleRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色编码只能包含大写字母和下划线', trigger: 'blur' }
  ]
}

// 获取角色列表
const fetchRoles = async () => {
  loading.value = true
  try {
    const res = await getRoleList()
    roleList.value = res.data
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

// 获取权限列表并构建树结构
const fetchPermissions = async () => {
  try {
    const res = await getPermissionList()
    const permissions = res.data
    
    // 这里简单处理，实际中可能需要根据数据结构构建层级树
    permissionTreeData.value = permissions.map(perm => ({
      id: perm.id,
      name: perm.name,
      code: perm.code
    }))
  } catch (error) {
    console.error('获取权限列表失败:', error)
    ElMessage.error('获取权限列表失败')
  }
}

// 新增角色
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  
  // 重置表单
  Object.keys(roleForm).forEach(key => {
    roleForm[key] = key === 'status' ? true : ''
  })
  
  // 等待DOM更新后重置表单验证
  setTimeout(() => {
    roleFormRef.value?.resetFields()
  }, 0)
}

// 编辑角色
const handleEdit = (row) => {
  dialogType.value = 'edit'
  dialogVisible.value = true
  
  // 填充表单
  Object.keys(roleForm).forEach(key => {
    roleForm[key] = row[key]
  })
  
  // 等待DOM更新后重置表单验证
  setTimeout(() => {
    roleFormRef.value?.clearValidate()
  }, 0)
}

// 删除角色
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除角色 ${row.name} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteRole(row.id)
      ElMessage.success('删除成功')
      fetchRoles()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 修改角色状态
const handleStatusChange = (row) => {
  const statusText = row.status ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${statusText}角色 ${row.name} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updateRole(row.id, { status: !row.status })
      ElMessage.success(`${statusText}成功`)
      fetchRoles()
    } catch (error) {
      ElMessage.error(`${statusText}失败`)
    }
  }).catch(() => {})
}

// 提交表单
const submitForm = () => {
  roleFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'add') {
          await createRole(roleForm)
        } else {
          await updateRole(roleForm.id, roleForm)
        }
        
        ElMessage.success(dialogType.value === 'add' ? '创建成功' : '更新成功')
        dialogVisible.value = false
        fetchRoles()
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

// 打开权限分配弹窗
const openPermissionDialog = async (row) => {
  selectedRole.value = row
  permissionDialogVisible.value = true
  
  // 获取最新的角色信息，以确保有最新的权限数据
  try {
    const res = await getRoleById(row.id)
    const roleWithPermissions = res.data
    
    // 设置已选择的权限
    setTimeout(() => {
      if (permissionTreeRef.value) {
        permissionTreeRef.value.setCheckedKeys(
          roleWithPermissions.permissions?.map(p => p.id) || []
        )
      }
    }, 100)
  } catch (error) {
    console.error('获取角色权限失败:', error)
  }
}

// 保存角色权限
const saveRolePermissions = async () => {
  if (!selectedRole.value || !permissionTreeRef.value) return
  
  permissionSubmitLoading.value = true
  try {
    const roleCode = selectedRole.value.code
    const currentPermissionIds = selectedRole.value.permissions?.map(p => p.id) || []
    const selectedPermissionIds = permissionTreeRef.value.getCheckedKeys()
    
    // 找出需要添加的权限
    const permissionsToAdd = selectedPermissionIds.filter(id => !currentPermissionIds.includes(id))
    
    // 找出需要移除的权限
    const permissionsToRemove = currentPermissionIds.filter(id => !selectedPermissionIds.includes(id))
    
    // 找到权限编码
    const permissionMap = permissionTreeData.value.reduce((map, perm) => {
      map[perm.id] = perm.code
      return map
    }, {})
    
    // 添加权限
    for (const permId of permissionsToAdd) {
      const permCode = permissionMap[permId]
      if (permCode) {
        await addPermissionToRole(roleCode, permCode)
      }
    }
    
    // 移除权限
    for (const permId of permissionsToRemove) {
      const permCode = permissionMap[permId]
      if (permCode) {
        await removePermissionFromRole(roleCode, permCode)
      }
    }
    
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
    fetchRoles()
  } catch (error) {
    console.error('权限分配失败:', error)
    ElMessage.error('权限分配失败')
  } finally {
    permissionSubmitLoading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchRoles()
  fetchPermissions()
})
</script>

<style scoped>
.role-container {
  padding: 10px;
}

.table-operations {
  margin-bottom: 18px;
}
</style> 