<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>用户总数</span>
              <el-icon><User /></el-icon>
            </div>
          </template>
          <div class="stat-value">56</div>
          <div class="stat-footer">
            <el-tag type="success">
              <el-icon><ArrowUp /></el-icon> 12%
            </el-tag>
            <span class="compare-text">较上月</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>角色数量</span>
              <el-icon><UserFilled /></el-icon>
            </div>
          </template>
          <div class="stat-value">8</div>
          <div class="stat-footer">
            <el-tag type="info">
              <el-icon><Right /></el-icon> 0%
            </el-tag>
            <span class="compare-text">较上月</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>权限数量</span>
              <el-icon><Key /></el-icon>
            </div>
          </template>
          <div class="stat-value">24</div>
          <div class="stat-footer">
            <el-tag type="success">
              <el-icon><ArrowUp /></el-icon> 4%
            </el-tag>
            <span class="compare-text">较上月</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>今日活跃用户</span>
              <el-icon><View /></el-icon>
            </div>
          </template>
          <div class="stat-value">18</div>
          <div class="stat-footer">
            <el-tag type="danger">
              <el-icon><ArrowDown /></el-icon> 8%
            </el-tag>
            <span class="compare-text">较昨日</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <el-empty description="图表数据加载中..." />
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>用户角色分布</span>
            </div>
          </template>
          <div class="chart-placeholder">
            <el-empty description="图表数据加载中..." />
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="activity-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
              <el-button type="primary" link>查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentActivities" style="width: 100%">
            <el-table-column prop="user" label="用户" width="180" />
            <el-table-column prop="action" label="操作" width="180" />
            <el-table-column prop="target" label="目标" width="180" />
            <el-table-column prop="time" label="时间" />
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <el-tag :type="scope.row.status === '成功' ? 'success' : 'danger'">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 模拟最近活动数据
const recentActivities = ref([
  {
    user: '管理员',
    action: '创建',
    target: '新用户',
    time: '2023-08-15 10:30:22',
    status: '成功'
  },
  {
    user: '管理员',
    action: '修改',
    target: '用户角色',
    time: '2023-08-15 09:15:18',
    status: '成功'
  },
  {
    user: '测试用户',
    action: '登录',
    target: '系统',
    time: '2023-08-15 08:45:01',
    status: '成功'
  },
  {
    user: '管理员',
    action: '删除',
    target: '角色权限',
    time: '2023-08-14 16:22:45',
    status: '失败'
  },
  {
    user: '管理员',
    action: '创建',
    target: '新角色',
    time: '2023-08-14 15:30:12',
    status: '成功'
  }
])
</script>

<style scoped>
.dashboard-container {
  padding: 10px;
}

.stat-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  margin: 10px 0;
  color: #409EFF;
}

.stat-footer {
  display: flex;
  align-items: center;
  margin-top: 10px;
}

.compare-text {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}

.chart-row, .activity-row {
  margin-top: 20px;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style> 