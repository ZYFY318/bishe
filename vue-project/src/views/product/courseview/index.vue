<template>
  <div class="course-view-container">
    <div class="course-header">
      <el-button text @click="goBack" class="back-button">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="course-title">{{ courseTitle }}</h1>
      <!-- 添加编辑按钮，仅教师可见 -->
      <el-button
        v-if="userStore.userType === 'TEACHER'"
        type="primary"
        @click="openEditDialog"
      >
        编辑课程
      </el-button>
    </div>

    <div class="course-content">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="10" animated />
      </div>
      <div v-else-if="!courseData" class="placeholder-text">
        <el-empty description="未找到课程信息" :image-size="200">
          <template #image>
            <el-icon class="empty-icon"><DocumentCopy /></el-icon>
          </template>
        </el-empty>
      </div>
      <div v-else class="course-details">
        <!-- 第一板块：课程主要内容 -->
        <el-card class="course-section-card">
          <template #header>
            <div class="section-header">
              <h3>课程介绍</h3>
            </div>
          </template>
          <div class="course-description">
            <p>{{ courseData.description || "本课程暂无详细介绍" }}</p>
          </div>
        </el-card>

        <!-- 第二板块：视频窗口 -->
        <el-card class="course-section-card">
          <template #header>
            <div class="section-header">
              <h3>课程视频</h3>
            </div>
          </template>
          <div class="video-container">
            <div v-if="courseData.videoUrl" class="video-player">
              <!-- 这里可以根据实际情况使用视频播放组件 -->
              <iframe
                src="https://player.bilibili.com/player.html?bvid=BV1cMFZe9EKW"
                frameborder="0"
                allowfullscreen
                style="width: 100%; height: 500px"
              >
              </iframe>
            </div>
            <div v-else class="video-placeholder">
              <el-empty description="暂无视频资源" :image-size="100">
                <template #image>
                  <el-icon class="empty-icon"><VideoCamera /></el-icon>
                </template>
              </el-empty>
            </div>
          </div>
        </el-card>

        <!-- 第三板块：讨论区 -->
        <el-card class="course-section-card">
          <template #header>
            <div class="section-header">
              <h3>讨论区</h3>
            </div>
          </template>
          <div class="discussion-container">
            <!-- 评论输入框 -->
            <div class="comment-input">
              <el-input
                v-model="newComment"
                type="textarea"
                :rows="3"
                placeholder="分享你的想法..."
              />
              <el-button
                type="primary"
                @click="submitComment"
                :disabled="!newComment.trim()"
              >
                发表评论
              </el-button>
            </div>

            <!-- 评论列表 -->
            <div v-if="comments.length > 0" class="comments-list">
              <div
                v-for="(comment, index) in comments"
                :key="index"
                class="comment-item"
              >
                <div class="comment-avatar">
                  <el-avatar :size="40" :src="comment.avatar || ''">{{
                    comment.username?.charAt(0) || "U"
                  }}</el-avatar>
                </div>
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="comment-username">{{
                      comment.username || "匿名用户"
                    }}</span>
                    <span class="comment-time">{{
                      formatDate(comment.createdAt)
                    }}</span>
                  </div>
                  <div class="comment-text">{{ comment.content }}</div>
                </div>
              </div>
            </div>
            <div v-else class="no-comments">
              <p>暂无评论，快来发表第一条评论吧！</p>
            </div>
          </div>
        </el-card>
      </div>
    </div>
    <!-- 添加编辑对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑课程内容" width="50%">
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="课程描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入课程描述"
          />
        </el-form-item>
        <el-form-item label="视频链接">
          <el-input v-model="editForm.videoUrl" placeholder="请输入视频链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="saving">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, DocumentCopy, VideoCamera } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { fetchCourseDetail } from "@/api/product/course";
import type { CourseItem } from "@/api/product/course/type";
import useUserStore from "@/stores/modules/user";
import { updateCourseContent } from "@/api/product/course";
import type { CourseContentUpdateData } from "@/api/product/course/type";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

// 从路由参数中获取课程ID和标题
const courseId = ref(Number(route.query.courseId) || 0);
const courseTitle = ref((route.query.courseTitle as string) || "未命名课程");

// 课程内容
const loading = ref(false);
const courseData = ref<CourseItem | null>(null);
console.log(courseData.value);
// 评论相关
interface Comment {
  id?: number;
  userId?: number;
  username?: string;
  avatar?: string;
  content: string;
  createdAt: string;
}

const newComment = ref("");
const comments = ref<Comment[]>([]); // 这里应该从API获取，现在用模拟数据

// 加载课程数据
const loadCourseData = async () => {
  if (!courseId.value) {
    ElMessage.error("课程ID无效");
    return;
  }

  try {
    loading.value = true;
    const result = await fetchCourseDetail(courseId.value);
    if (result.code === 200) {
      courseData.value = result.data;
      // 模拟加载评论数据
      loadComments();
    } else {
      ElMessage.error(result.message || "获取课程信息失败");
    }
  } catch (error) {
    console.error("获取课程信息出错:", error);
    ElMessage.error("获取课程信息失败");
  } finally {
    loading.value = false;
  }
};

// 模拟加载评论数据
const loadComments = () => {
  // 这里应该是API调用，现在用模拟数据
  comments.value = [
    {
      id: 1,
      userId: 101,
      username: "张三",
      avatar: "",
      content: "这门课程讲解得非常清晰，很容易理解！",
      createdAt: "2023-05-15T10:30:00",
    },
    {
      id: 2,
      userId: 102,
      username: "李四",
      avatar: "",
      content: "视频质量很高，学到了很多知识。",
      createdAt: "2023-05-16T14:20:00",
    },
  ];
};

// 提交评论
const submitComment = () => {
  if (!newComment.value.trim()) return;

  // 这里应该是API调用，现在用模拟数据
  const comment: Comment = {
    id: comments.value.length + 1,
    userId: userStore.userId || 0,
    username: userStore.username || "当前用户",
    content: newComment.value,
    createdAt: new Date().toISOString(),
  };

  comments.value.unshift(comment);
  newComment.value = "";
  ElMessage.success("评论发表成功");
};

// 返回上一页，根据用户角色决定返回路径
const goBack = () => {
  // 根据用户角色决定返回的页面
  if (userStore.userType === "TEACHER") {
    // 教师返回课程管理页面
    router.push("/product/course");
  } else {
    // 学生返回课程列表页面（如果有的话）
    router.push("/home");
  }
};

// 获取完整的图片URL
const getImgUrl = (url: string) => {
  if (!url) return "";

  // 如果是完整URL，直接返回
  if (url.startsWith("http://") || url.startsWith("https://")) {
    return url;
  }

  // 如果是相对路径，添加API基础URL
  const baseURL = import.meta.env.VITE_APP_BASE_API || "http://localhost:9090";

  // 确保路径正确
  if (url.startsWith("/")) {
    return `${baseURL}${url}`;
  } else {
    return `${baseURL}/${url}`;
  }
};

// 获取视频URL
const getVideoUrl = (url: string) => {
  // 复用图片URL处理逻辑
  return getImgUrl(url);
};

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return `${date.getFullYear()}-${(date.getMonth() + 1)
    .toString()
    .padStart(2, "0")}-${date.getDate().toString().padStart(2, "0")}`;
};

// 编辑相关
const editDialogVisible = ref(false);
const editForm = ref<CourseContentUpdateData>({
  description: "",
  videoUrl: "",
});
const saving = ref(false);

// 打开编辑对话框
const openEditDialog = () => {
  editForm.value.description = courseData.value?.description || "";
  editForm.value.videoUrl = courseData.value?.videoUrl || "";
  editDialogVisible.value = true;
};

// 保存课程内容
const handleSave = async () => {
  if (!courseId.value) return;

  try {
    saving.value = true;
    const result = await updateCourseContent(courseId.value, editForm.value);
    if (result.code === 200) {
      ElMessage.success("保存成功");
      editDialogVisible.value = false;
      // 重新加载课程数据
      await loadCourseData();
    } else {
      ElMessage.error(result.message || "保存失败");
    }
  } catch (error) {
    console.error("保存课程内容出错:", error);
    ElMessage.error("保存失败");
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  loadCourseData();
});
</script>

<style scoped lang="scss">
.course-view-container {
  width: 90%;
  margin: 20px auto;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 140px);
  background-color: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  box-shadow: var(--shadow);
  overflow: hidden;
}

.course-header {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
  background-color: var(--card-bg);
}

.back-button {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 16px;
  color: var(--text-color);
}

.course-title {
  margin: 0 0 0 20px;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-color);
  flex: 1;
}

.course-content {
  flex: 1;
  padding: 40px;
}

.loading-container {
  margin: 20px;
}

.placeholder-text {
  text-align: center;
  color: var(--text-color);
}

.empty-icon {
  font-size: 100px;
  color: var(--text-color);
  opacity: 0.5;
}

.course-details {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.course-section-card {
  background-color: var(--card-bg);

  .section-header {
    h3 {
      margin: 0;
      color: var(--text-color);
      font-size: 18px;
      font-weight: 600;
    }
  }
}

.course-description {
  padding: 20px 0;
  color: var(--text-color);
  line-height: 1.6;
  font-size: 16px;

  p {
    margin: 0;
  }
}

.video-container {
  padding: 20px 0;

  .video-player {
    width: 100%;
    display: flex;
    justify-content: center;

    .video-element {
      width: 100%;
      max-width: 800px;
      height: auto;
      border-radius: 8px;
    }
  }

  .video-placeholder {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 300px;
    color: var(--text-color);
  }
}

.discussion-container {
  padding: 20px 0;

  .comment-input {
    margin-bottom: 30px;
    display: flex;
    flex-direction: column;
    gap: 10px;

    .el-button {
      align-self: flex-end;
    }
  }

  .comments-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .comment-item {
    display: flex;
    gap: 15px;

    .comment-avatar {
      flex-shrink: 0;
    }

    .comment-content {
      flex: 1;

      .comment-header {
        display: flex;
        justify-content: space-between; // 添加此行以调整按钮位置
        margin-bottom: 8px;

        .comment-username {
          font-weight: 600;
          color: var(--text-color);
        }

        .comment-time {
          color: var(--text-color-secondary);
          font-size: 12px;
        }
      }

      .comment-text {
        color: var(--text-color);
        line-height: 1.5;
      }
    }
  }

  .no-comments {
    text-align: center;
    color: var(--text-color-secondary);
    padding: 30px 0;
  }
}
</style>

<style scoped lang="scss">
.course-view-container {
  width: 90%;
  margin: 20px auto;
  display: flex;
  flex-direction: column;
  min-height: calc(100vh - 140px);
  background-color: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 10px;
  box-shadow: var(--shadow);
  overflow: hidden;
}

.course-header {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
  background-color: var(--card-bg);
}

.back-button {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 16px;
  color: var(--text-color);
}

.course-title {
  margin: 0 0 0 20px;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-color);
  flex: 1;
}

.course-content {
  flex: 1;
  padding: 40px;
}

.loading-container {
  margin: 20px;
}

.placeholder-text {
  text-align: center;
  color: var(--text-color);
}

.empty-icon {
  font-size: 100px;
  color: var(--text-color);
  opacity: 0.5;
}

.course-details {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.course-section-card {
  background-color: var(--card-bg);

  .section-header {
    h3 {
      margin: 0;
      color: var(--text-color);
      font-size: 18px;
      font-weight: 600;
    }
  }
}

.course-description {
  padding: 20px 0;
  color: var(--text-color);
  line-height: 1.6;
  font-size: 16px;

  p {
    margin: 0;
  }
}

.video-container {
  padding: 20px 0;

  .video-player {
    width: 100%;
    display: flex;
    justify-content: center;

    .video-element {
      width: 100%;
      max-width: 800px;
      height: auto;
      border-radius: 8px;
    }
  }

  .video-placeholder {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 300px;
    color: var(--text-color);
  }
}

.discussion-container {
  padding: 20px 0;

  .comment-input {
    margin-bottom: 30px;
    display: flex;
    flex-direction: column;
    gap: 10px;

    .el-button {
      align-self: flex-end;
    }
  }

  .comments-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .comment-item {
    display: flex;
    gap: 15px;

    .comment-avatar {
      flex-shrink: 0;
    }

    .comment-content {
      flex: 1;

      .comment-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;

        .comment-username {
          font-weight: 600;
          color: var(--text-color);
        }

        .comment-time {
          color: var(--text-color-secondary);
          font-size: 12px;
        }
      }

      .comment-text {
        color: var(--text-color);
        line-height: 1.5;
      }
    }
  }

  .no-comments {
    text-align: center;
    color: var(--text-color-secondary);
    padding: 30px 0;
  }
}
</style>
