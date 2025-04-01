<template>
  <div class="layout-container">
    <!-- 顶部导航 -->
    <el-header height="60px" class="layout-header">
      <div class="header-left">
        <div class="logo">管理系统</div>
        <el-icon class="toggle-icon" @click="toggleSidebar">
          <component :is="isCollapse ? 'Expand' : 'Fold'" />
        </el-icon>
      </div>
      
      <div class="header-right">
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="el-dropdown-link">
            <el-avatar :size="32" icon="UserFilled" />
            <span class="username">{{ userStore.nickname || userStore.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <div class="layout-content">
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-sidebar">
        <el-scrollbar>
          <el-menu
            :default-active="activeMenu"
            :collapse="isCollapse"
            background-color="#001529"
            text-color="#fff"
            active-text-color="#409EFF"
            :unique-opened="true"
            :collapse-transition="false"
            router
          >
            <template v-for="(route, index) in routes" :key="index">
              <el-menu-item v-if="!route.children && checkPermission(route)" :index="route.path">
                <el-icon v-if="route.meta && route.meta.icon">
                  <component :is="route.meta.icon" />
                </el-icon>
                <template #title>{{ route.meta && route.meta.title }}</template>
              </el-menu-item>
              
              <el-sub-menu v-else-if="checkPermission(route)" :index="route.path">
                <template #title>
                  <el-icon v-if="route.meta && route.meta.icon">
                    <component :is="route.meta.icon" />
                  </el-icon>
                  <span>{{ route.meta && route.meta.title }}</span>
                </template>
                
                <el-menu-item 
                  v-for="child in route.children" 
                  :key="child.path"
                  :index="route.path + '/' + child.path"
                  v-if="checkPermission(child)"
                >
                  <el-icon v-if="child.meta && child.meta.icon">
                    <component :is="child.meta.icon" />
                  </el-icon>
                  <template #title>{{ child.meta && child.meta.title }}</template>
                </el-menu-item>
              </el-sub-menu>
            </template>
          </el-menu>
        </el-scrollbar>
      </el-aside>
      
      <!-- 主要内容区 -->
      <el-main class="layout-main">
        <el-breadcrumb separator="/" class="breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="$route.meta && $route.meta.title">
            {{ $route.meta.title }}
          </el-breadcrumb-item>
        </el-breadcrumb>
        
        <div class="main-content">
          <router-view />
        </div>
      </el-main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../store/user'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 当前激活的菜单项
const activeMenu = computed(() => {
  return route.path
})

// 侧边栏路由
const routes = computed(() => {
  // 过滤掉不需要显示在菜单中的路由
  return router.options.routes.find(r => r.path === '/').children || []
})

// 权限检查
const checkPermission = (route) => {
  if (!route.meta || !route.meta.permission) {
    return true
  }
  
  // 检查是否有管理员角色
  const hasAdminRole = userStore.userInfo?.roles?.some(role => role.code === 'ROLE_ADMIN') || false
  if (hasAdminRole) {
    // 管理员角色拥有所有权限
    return true
  }
  
  const hasPermission = userStore.hasPermission(route.meta.permission)
  console.log(`菜单权限检查: ${route.path} - ${route.meta.permission}, 结果: ${hasPermission}`)
  return hasPermission
}

// 下拉菜单操作
const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

// 组件挂载时输出用户信息和权限
onMounted(() => {
  console.log('Layout组件已挂载')
  console.log('当前用户信息:', userStore.userInfo)
  console.log('当前用户角色:', userStore.roles)
  console.log('当前用户权限:', userStore.permissions)
  
  // 检查用户是否有角色，若无则尝试获取用户信息
  if (!userStore.userInfo || !userStore.userInfo.roles || userStore.userInfo.roles.length === 0) {
    console.log('用户信息不完整，尝试重新获取')
    userStore.getUserInfo().then(() => {
      console.log('用户信息重新获取成功')
    }).catch(err => {
      console.error('用户信息重新获取失败:', err)
    })
  }
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.layout-header {
  background-color: #fff;
  color: #333;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  position: relative;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.logo {
  font-size: 18px;
  font-weight: bold;
  margin-right: 20px;
}

.toggle-icon {
  font-size: 20px;
  cursor: pointer;
  padding: 0 10px;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin: 0 5px;
}

.layout-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.layout-sidebar {
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;
}

.layout-main {
  flex: 1;
  background-color: #f5f7f9;
  overflow: auto;
  padding: 20px;
}

.breadcrumb {
  margin-bottom: 15px;
}

.main-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  min-height: calc(100% - 40px);
}
</style> 