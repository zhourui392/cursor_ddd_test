<template>
  <div class="menu-container">
    <div class="table-operations">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 新增菜单
      </el-button>
      <el-input
        v-model="searchQuery"
        placeholder="请输入菜单名称搜索"
        style="width: 250px; margin-left: 16px"
        clearable
        @clear="fetchMenus"
        @keyup.enter="fetchMenus"
      >
        <template #suffix>
          <el-icon @click="fetchMenus"><Search /></el-icon>
        </template>
      </el-input>
    </div>
    
    <el-table
      v-loading="loading"
      :data="tableData"
      row-key="id"
      border
      default-expand-all
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      style="width: 100%; margin-top: 15px;"
    >
      <el-table-column prop="name" label="菜单名称" min-width="180" />
      <el-table-column prop="path" label="路由路径" min-width="200" />
      <el-table-column prop="component" label="组件路径" min-width="200" />
      <el-table-column prop="permission" label="权限标识" min-width="180" />
      <el-table-column prop="icon" label="图标" width="80">
        <template #default="scope">
          <el-icon v-if="scope.row.icon">
            <component :is="scope.row.icon" />
          </el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="orderNum" label="排序" width="80" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.type === 0" type="success">目录</el-tag>
          <el-tag v-else-if="scope.row.type === 1" type="warning">菜单</el-tag>
          <el-tag v-else-if="scope.row.type === 2" type="info">按钮</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status ? 'success' : 'danger'">
            {{ scope.row.status ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="220">
        <template #default="scope">
          <el-button type="primary" link size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="primary" link size="small" @click="handleAdd(scope.row)">添加下级</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 菜单编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增菜单' : '编辑菜单'" width="650px">
      <el-form ref="menuFormRef" :model="menuForm" :rules="menuRules" label-width="100px">
        <el-form-item v-if="parentMenu" label="上级菜单">
          <el-input v-model="parentMenu.name" disabled />
        </el-form-item>
        
        <el-form-item label="菜单类型" prop="type">
          <el-radio-group v-model="menuForm.type">
            <el-radio :label="0">目录</el-radio>
            <el-radio :label="1">菜单</el-radio>
            <el-radio :label="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="menuForm.name" placeholder="请输入菜单名称" />
        </el-form-item>
        
        <el-form-item label="路由路径" prop="path" v-if="menuForm.type !== 2">
          <el-input v-model="menuForm.path" placeholder="请输入路由路径" />
        </el-form-item>
        
        <el-form-item label="组件路径" prop="component" v-if="menuForm.type === 1">
          <el-input v-model="menuForm.component" placeholder="请输入组件路径" />
        </el-form-item>
        
        <el-form-item label="权限标识" prop="permission" v-if="menuForm.type !== 0">
          <el-input v-model="menuForm.permission" placeholder="请输入权限标识" />
        </el-form-item>
        
        <el-form-item label="图标" prop="icon" v-if="menuForm.type !== 2">
          <el-input v-model="menuForm.icon" placeholder="请输入图标名称" />
        </el-form-item>
        
        <el-form-item label="排序" prop="orderNum">
          <el-input-number v-model="menuForm.orderNum" :min="0" />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-switch v-model="menuForm.status" active-text="启用" inactive-text="禁用" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '@/api/menu'

// 状态数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('add')
const tableData = ref([])
const searchQuery = ref('')
const parentMenu = ref(null)

// 菜单表单
const menuFormRef = ref(null)
const menuForm = reactive({
  id: null,
  name: '',
  path: '',
  component: '',
  icon: '',
  orderNum: 0,
  type: 1,
  permission: '',
  status: true,
  parentId: null
})

// 表单验证规则
const menuRules = {
  name: [
    { required: true, message: '请输入菜单名称', trigger: 'blur' }
  ],
  path: [
    { required: true, message: '请输入路由路径', trigger: 'blur', when: (form) => form.type !== 2 }
  ],
  component: [
    { required: true, message: '请输入组件路径', trigger: 'blur', when: (form) => form.type === 1 }
  ]
}

// 获取菜单列表
const fetchMenus = async () => {
  loading.value = true
  try {
    const res = await getMenuTree()
    if (res.code === '200') {
      if (searchQuery.value) {
        // 在实际应用中，可能需要递归搜索树形结构
        // 这里简化处理
        tableData.value = filterMenuTree(res.data, searchQuery.value)
      } else {
        tableData.value = res.data
      }
    } else {
      ElMessage.error(res.message || '获取菜单列表失败')
    }
  } catch (error) {
    console.error('获取菜单列表失败:', error)
    ElMessage.error('获取菜单列表失败')
    // 加载模拟数据，便于开发和演示
    tableData.value = getMockMenuData()
  } finally {
    loading.value = false
  }
}

// 递归搜索菜单树
const filterMenuTree = (menuTree, keyword) => {
  if (!keyword) return menuTree

  const result = []
  
  const traverse = (menus, keyword) => {
    if (!menus || !menus.length) return []
    
    return menus.filter(menu => {
      // 当前菜单匹配
      const isMatch = menu.name.includes(keyword) || 
                      (menu.path && menu.path.includes(keyword)) ||
                      (menu.permission && menu.permission.includes(keyword))
      
      // 处理子菜单
      if (menu.children && menu.children.length) {
        const matchedChildren = traverse(menu.children, keyword)
        // 如果有匹配的子菜单，保留父菜单
        if (matchedChildren.length) {
          menu.children = matchedChildren
          return true
        }
      }
      
      return isMatch
    })
  }
  
  return traverse(menuTree, keyword)
}

// 新增菜单
const handleAdd = (row) => {
  resetForm()
  dialogType.value = 'add'
  dialogVisible.value = true
  
  if (row && row.id) {
    parentMenu.value = row
    menuForm.parentId = row.id
    
    // 如果父级是目录，则子级默认是菜单
    if (row.type === 0) {
      menuForm.type = 1
    } 
    // 如果父级是菜单，则子级默认是按钮
    else if (row.type === 1) {
      menuForm.type = 2
    }
  } else {
    parentMenu.value = null
    menuForm.parentId = null
    // 默认为目录
    menuForm.type = 0
  }
}

// 编辑菜单
const handleEdit = (row) => {
  resetForm()
  dialogType.value = 'edit'
  
  Object.keys(menuForm).forEach(key => {
    if (key in row) {
      menuForm[key] = row[key]
    }
  })
  
  // 设置父菜单
  parentMenu.value = null
  menuForm.parentId = row.parentId || null
  
  dialogVisible.value = true
}

// 删除菜单
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确认删除菜单 "${row.name}" 吗？删除后不可恢复。`,
    '警告',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const res = await deleteMenu(row.id)
      if (res.code === '200') {
        ElMessage.success('删除成功')
        fetchMenus()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除菜单失败:', error)
      ElMessage.error('删除菜单失败')
    }
  }).catch(() => {})
}

// 提交表单
const submitForm = async () => {
  if (!menuFormRef.value) return
  
  await menuFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        let res
        if (dialogType.value === 'add') {
          res = await createMenu(menuForm)
        } else {
          res = await updateMenu(menuForm.id, menuForm)
        }
        
        if (res.code === '200') {
          ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功')
          dialogVisible.value = false
          fetchMenus()
        } else {
          ElMessage.error(res.message || (dialogType.value === 'add' ? '添加失败' : '更新失败'))
        }
      } catch (error) {
        console.error(dialogType.value === 'add' ? '添加菜单失败:' : '更新菜单失败:', error)
        ElMessage.error(dialogType.value === 'add' ? '添加菜单失败' : '更新菜单失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (menuFormRef.value) {
    menuFormRef.value.resetFields()
  }
  
  Object.assign(menuForm, {
    id: null,
    name: '',
    path: '',
    component: '',
    icon: '',
    orderNum: 0,
    type: 1,
    permission: '',
    status: true,
    parentId: null
  })
}

// 获取模拟菜单数据 (仅用于开发和演示)
const getMockMenuData = () => {
  return [
    {
      id: 1,
      name: '系统管理',
      path: '/system',
      component: '',
      icon: 'Setting',
      orderNum: 1,
      type: 0, // 目录
      permission: '',
      status: true,
      children: [
        {
          id: 2,
          name: '用户管理',
          path: 'user',
          component: 'views/user/UserList',
          icon: 'User',
          orderNum: 1,
          type: 1, // 菜单
          permission: 'USER_VIEW',
          status: true,
          parentId: 1,
          children: [
            {
              id: 6,
              name: '用户新增',
              path: '',
              component: '',
              icon: '',
              orderNum: 1,
              type: 2, // 按钮
              permission: 'USER_ADD',
              status: true,
              parentId: 2
            },
            {
              id: 7,
              name: '用户编辑',
              path: '',
              component: '',
              icon: '',
              orderNum: 2,
              type: 2,
              permission: 'USER_EDIT',
              status: true,
              parentId: 2
            },
            {
              id: 8,
              name: '用户删除',
              path: '',
              component: '',
              icon: '',
              orderNum: 3,
              type: 2,
              permission: 'USER_DELETE',
              status: true,
              parentId: 2
            }
          ]
        },
        {
          id: 3,
          name: '角色管理',
          path: 'role',
          component: 'views/role/RoleList',
          icon: 'UserFilled',
          orderNum: 2,
          type: 1,
          permission: 'ROLE_VIEW',
          status: true,
          parentId: 1
        },
        {
          id: 4,
          name: '权限管理',
          path: 'permission',
          component: 'views/permission/PermissionList',
          icon: 'Key',
          orderNum: 3,
          type: 1,
          permission: 'PERMISSION_VIEW',
          status: true,
          parentId: 1
        },
        {
          id: 5,
          name: '菜单管理',
          path: 'menu',
          component: 'views/menu/MenuList',
          icon: 'Menu',
          orderNum: 4,
          type: 1,
          permission: 'MENU_VIEW',
          status: true,
          parentId: 1
        }
      ]
    }
  ]
}

// 页面加载时获取菜单列表
onMounted(() => {
  fetchMenus()
})
</script>

<style scoped>
.menu-container {
  padding: 10px;
}

.table-operations {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}
</style> 