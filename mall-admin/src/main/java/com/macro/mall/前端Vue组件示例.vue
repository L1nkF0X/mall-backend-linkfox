<template>
  <div class="log-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>日志管理</h2>
      <p>系统操作日志查询与管理</p>
    </div>

    <!-- 筛选搜索区域 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" ref="searchForm" :inline="true" class="search-form">
        <el-form-item label="用户名:" prop="username">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 200px;"
          />
        </el-form-item>
        
        <el-form-item label="请求方法:" prop="method">
          <el-select
            v-model="searchForm.method"
            placeholder="请选择请求方法"
            clearable
            style="width: 150px;"
          >
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch" icon="el-icon-search">
            查询搜索
          </el-button>
          <el-button @click="handleReset" icon="el-icon-refresh">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格区域 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="logList"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column prop="id" label="编号" width="80" align="center" />
        
        <el-table-column prop="username" label="用户名" width="120" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.username === 'admin'" type="danger">
              {{ scope.row.username }}
            </el-tag>
            <span v-else>{{ scope.row.username }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="ip" label="IP地址" width="140" align="center" />
        
        <el-table-column prop="method" label="请求方法" width="100" align="center">
          <template slot-scope="scope">
            <el-tag
              :type="getMethodTagType(scope.row.method)"
              size="small"
            >
              {{ scope.row.method }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="description" label="操作描述" width="150" align="center" />
        
        <el-table-column prop="params" label="请求参数" min-width="200">
          <template slot-scope="scope">
            <el-tooltip
              v-if="scope.row.params && scope.row.params.length > 50"
              :content="scope.row.params"
              placement="top"
            >
              <span>{{ scope.row.params.substring(0, 50) }}...</span>
            </el-tooltip>
            <span v-else>{{ scope.row.params }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="spendTime" label="耗时(ms)" width="100" align="center">
          <template slot-scope="scope">
            <span
              :class="{
                'slow-request': scope.row.spendTime > 2000,
                'normal-request': scope.row.spendTime <= 2000
              }"
            >
              {{ scope.row.spendTime }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template slot-scope="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button
              type="text"
              size="small"
              @click="handleViewDetail(scope.row)"
              icon="el-icon-view"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.pageNum"
          :page-sizes="[5, 10, 15, 20]"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>

    <!-- 日志详情弹窗 -->
    <el-dialog
      title="日志详情"
      :visible.sync="detailDialogVisible"
      width="60%"
      :before-close="handleCloseDetail"
    >
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="日志ID">
          {{ currentLog.id }}
        </el-descriptions-item>
        <el-descriptions-item label="操作用户">
          {{ currentLog.username }}
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">
          {{ currentLog.ip }}
        </el-descriptions-item>
        <el-descriptions-item label="请求方法">
          <el-tag :type="getMethodTagType(currentLog.method)">
            {{ currentLog.method }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">
          {{ currentLog.description }}
        </el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="json-content">{{ formatJson(currentLog.params) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="执行耗时">
          <span :class="{ 'slow-request': currentLog.spendTime > 2000 }">
            {{ currentLog.spendTime }}ms
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(currentLog.createTime) }}
        </el-descriptions-item>
      </el-descriptions>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { fetchLogList, getLogById } from '@/api/webLog'

export default {
  name: 'LogManagement',
  data() {
    return {
      // 搜索表单
      searchForm: {
        username: '',
        method: ''
      },
      
      // 分页信息
      pagination: {
        pageNum: 1,
        pageSize: 5,
        total: 0,
        totalPage: 0
      },
      
      // 数据列表
      logList: [],
      
      // 加载状态
      loading: false,
      
      // 详情弹窗
      detailDialogVisible: false,
      currentLog: null
    }
  },
  
  created() {
    this.fetchData()
  },
  
  methods: {
    // 获取数据
    async fetchData() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pagination.pageNum,
          pageSize: this.pagination.pageSize,
          username: this.searchForm.username || undefined,
          method: this.searchForm.method || undefined
        }
        
        const response = await fetchLogList(params)
        
        if (response.code === 200) {
          const { pageNum, pageSize, totalPage, total, list } = response.data
          
          this.logList = list
          this.pagination = {
            pageNum,
            pageSize,
            totalPage,
            total
          }
        } else {
          this.$message.error(response.message || '查询失败')
        }
      } catch (error) {
        console.error('查询日志失败:', error)
        this.$message.error('查询失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },
    
    // 搜索
    handleSearch() {
      this.pagination.pageNum = 1
      this.fetchData()
    },
    
    // 重置
    handleReset() {
      this.$refs.searchForm.resetFields()
      this.pagination.pageNum = 1
      this.fetchData()
    },
    
    // 每页大小改变
    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.pagination.pageNum = 1
      this.fetchData()
    },
    
    // 当前页改变
    handleCurrentChange(val) {
      this.pagination.pageNum = val
      this.fetchData()
    },
    
    // 查看详情
    async handleViewDetail(row) {
      try {
        const response = await getLogById(row.id)
        if (response.code === 200) {
          this.currentLog = response.data
          this.detailDialogVisible = true
        } else {
          this.$message.error(response.message || '获取详情失败')
        }
      } catch (error) {
        console.error('获取日志详情失败:', error)
        this.$message.error('获取详情失败，请稍后重试')
      }
    },
    
    // 关闭详情弹窗
    handleCloseDetail() {
      this.detailDialogVisible = false
      this.currentLog = null
    },
    
    // 获取请求方法标签类型
    getMethodTagType(method) {
      const typeMap = {
        'GET': 'success',
        'POST': 'primary',
        'PUT': 'warning',
        'DELETE': 'danger'
      }
      return typeMap[method] || 'info'
    },
    
    // 格式化日期时间
    formatDateTime(dateTime) {
      if (!dateTime) return '-'
      return new Date(dateTime).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },
    
    // 格式化JSON
    formatJson(jsonStr) {
      if (!jsonStr) return ''
      try {
        return JSON.stringify(JSON.parse(jsonStr), null, 2)
      } catch (e) {
        return jsonStr
      }
    }
  }
}
</script>

<style scoped>
.log-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 0;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.slow-request {
  color: #F56C6C;
  font-weight: bold;
}

.normal-request {
  color: #67C23A;
}

.json-content {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.dialog-footer {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-form .el-form-item {
    margin-bottom: 10px;
  }
  
  .pagination-container {
    text-align: center;
  }
}
</style>
