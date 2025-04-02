<template>
  <div class="permission-container">
    <div class="table-operations">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 新增权限
      </el-button>
    </div>
    
    <el-table
      v-loading="loading"
      :data="permissionList"
      border
      style="width: 100%"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="权限名称" width="180" />
      <el-table-column prop="code" label="权限编码" width="180" />
      <el-table-column prop="module" label="所属模块" width="120" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status ? 'success' : 'danger'">
            {{ scope.row.status ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" fixed="right" width="200">
        <template #default="scope">
          <el-button type="primary" link size="small" @click="handleEdit(scope.row)">
            编辑
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
    
    <!-- 权限编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增权限' : '编辑权限'" width="500px">
      <el-form ref="permissionFormRef" :model="permissionForm" :rules="permissionRules" label-width="100px">
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="permissionForm.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="code">
          <el-input v-model="permissionForm.code" placeholder="请输入权限编码" :disabled="dialogType === 'edit'" />
        </el-form-item>
        <el-form-item label="模块名称" prop="module">
          <el-input v-model="permissionForm.module" placeholder="请输入模块名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="permissionForm.description" type="textarea" placeholder="请输入权限描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="permissionForm.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getPermissionList, getPermissionById, createPermission, updatePermission, deletePermission } from '@/api/permission'
import { ElMessage, ElMessageBox } from 'element-plus'

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 权限列表
const permissionList = ref([])

// 弹窗控制
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'

// 权限表单
const permissionFormRef = ref(null)
const permissionForm = reactive({
  id: null,
  name: '',
  code: '',
  module: '',
  description: '',
  status: true
})

// 表单验证规则
const permissionRules = {
  name: [
    { required: true, message: '请输入权限名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '权限编码只能包含大写字母和下划线', trigger: 'blur' }
  ],
  module: [
    { required: true, message: '请输入模块名称', trigger: 'blur' }
  ]
}

// 获取权限列表
const fetchPermissions = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    const res = await getPermissionList(params)
    permissionList.value = res.data
    total.value = res.total || res.data.length
  } catch (error) {
    console.error('获取权限列表失败:', error)
    ElMessage.error('获取权限列表失败')
  } finally {
    loading.value = false
  }
}

// 新增权限
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  
  // 重置表单
  Object.keys(permissionForm).forEach(key => {
    permissionForm[key] = key === 'status' ? true : ''
  })
  
  // 等待DOM更新后重置表单验证
  setTimeout(() => {
    permissionFormRef.value?.resetFields()
  }, 0)
}

// 编辑权限
const handleEdit = (row) => {
  dialogType.value = 'edit'
  dialogVisible.value = true
  
  // 填充表单
  Object.keys(permissionForm).forEach(key => {
    permissionForm[key] = row[key]
  })
  
  // 等待DOM更新后重置表单验证
  setTimeout(() => {
    permissionFormRef.value?.clearValidate()
  }, 0)
}

// 删除权限
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除权限 ${row.name} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePermission(row.id)
      ElMessage.success('删除成功')
      fetchPermissions()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 修改权限状态
const handleStatusChange = (row) => {
  const statusText = row.status ? '禁用' : '启用'
  ElMessageBox.confirm(`确定要${statusText}权限 ${row.name} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await updatePermission(row.id, { status: !row.status })
      ElMessage.success(`${statusText}成功`)
      fetchPermissions()
    } catch (error) {
      ElMessage.error(`${statusText}失败`)
    }
  }).catch(() => {})
}

// 提交表单
const submitForm = () => {
  permissionFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'add') {
          await createPermission(permissionForm)
        } else {
          await updatePermission(permissionForm.id, permissionForm)
        }
        
        ElMessage.success(dialogType.value === 'add' ? '创建成功' : '更新成功')
        dialogVisible.value = false
        fetchPermissions()
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

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchPermissions()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchPermissions()
}

// 组件挂载时获取权限列表
onMounted(() => {
  fetchPermissions()
})
</script>

<style scoped>
.permission-container {
  width: 100%;
}

.table-operations {
  margin-bottom: 16px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style>
 