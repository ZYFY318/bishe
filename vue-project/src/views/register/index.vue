<template>
    <div class="register_container">
        <el-form class="register_form" :model="registerForm" :rules="rules" ref="registerForms">
            <h1 class="animate">REGISTER</h1>
            <el-form-item prop="username" class="animate">
                <el-input :prefix-icon="User" v-model="registerForm.username" placeholder="Username"></el-input>
            </el-form-item>
            <el-form-item prop="password" class="animate">
                <el-input :prefix-icon="Lock" type="password" v-model="registerForm.password"
                    show-password placeholder="Password"></el-input>
            </el-form-item>
            <el-form-item prop="userType" class="animate">
                <div class="user-type-buttons">
                    <el-button 
                        :class="['type-btn', registerForm.userType === 'STUDENT' ? 'active' : '']"
                        @click="registerForm.userType = 'STUDENT'"
                    >
                        Student
                    </el-button>
                    <el-button 
                        :class="['type-btn', registerForm.userType === 'TEACHER' ? 'active' : '']"
                        @click="registerForm.userType = 'TEACHER'"
                    >
                        Teacher
                    </el-button>
                </div>
            </el-form-item>
            <el-form-item prop="email" class="animate">
                <el-input :prefix-icon="Message" v-model="registerForm.email" placeholder="Email (optional)"></el-input>
            </el-form-item>
            <el-form-item class="animate">
                <el-button :loading="loading" class="register_btn" type="success" size="default" color="#fbf5f2"
                    @click="register">Register</el-button>
            </el-form-item>
            
            <!-- 添加登录链接 -->
            <div class="login-link animate">
                <span>Already have an account? </span>
                <a @click="handleLogin">Login</a>
            </div>
        </el-form>
    </div>
</template>

<script setup lang="ts">
import { User, Lock, Message } from '@element-plus/icons-vue'
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { reqRegister } from '@/api/user';
import type { registerForm } from '@/api/user/type';
import { ElNotification } from 'element-plus';

// 获取el-form组件
let registerForms = ref();
// 获取路由器
let $router = useRouter();
// 定义变量控制按钮加载效果
let loading = ref(false);

// 收集注册数据
let registerForm = reactive<registerForm>({
    username: '',
    password: '',
    userType: '',
    email: ''
});

// 注册按钮回调
const register = async () => {
    // 保证全部表单项校验通过再发请求
    await registerForms.value.validate();
    loading.value = true;

    try {
        // 发送注册请求
        const result = await reqRegister(registerForm);
        
        if (result.code === 200) {
            // 注册成功提示信息
            ElNotification({
                type: 'success',
                message: result.message || '注册成功，请登录',
                title: '注册成功'
            });
            
            // 跳转到登录页
            $router.push('/login');
        } else {
            throw new Error(result.message || '注册失败');
        }
    } catch (error) {
        ElNotification({
            type: 'error',
            message: (error as Error).message || '注册失败，请重试',
            title: '注册失败'
        });
    } finally {
        loading.value = false;
    }
};

// 自定义校验规则函数
const validatorUserName = (rule: any, value: string, callback: Function) => {
    if (value.length >= 5) {
        callback();
    } else {
        callback(new Error('用户名长度至少五位'));
    }
};

const validatorPassword = (rule: any, value: string, callback: Function) => {
    if (value.length >= 6) {
        callback();
    } else {
        callback(new Error('密码长度至少六位'));
    }
};

const validatorUserType = (rule: any, value: string, callback: Function) => {
    if (value) {
        callback();
    } else {
        callback(new Error('请选择用户类型'));
    }
};

// 定义表单校验需要配置对象
const rules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { trigger: 'change', validator: validatorUserName }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { trigger: 'change', validator: validatorPassword }
    ],
    userType: [
        { required: true, message: '请选择用户类型', trigger: 'change' },
        { validator: validatorUserType, trigger: 'change' }
    ]
};

// 跳转到登录页
const handleLogin = () => {
    $router.push('/login');
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');
.register_container {
    font-family: 'Poppins', sans-serif;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
    top: 0;
    left: 0;
    width: 100%;
    height: 100vh;
    background: url('../../assets/images/p0.png') no-repeat center center;
    background-size: cover;
}

.register_form {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
    padding: 50px 10px;
    width: 35%;
    top: 30vh;
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 10px;
    backdrop-filter: blur(10px);
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
}

.register_btn {
    width: 100%;
    height: 55px;
    color: black;
    border-radius: 50px;
    font-size: 1.2rem;
    font-weight: 600;
    letter-spacing: 1px;
}

.register_btn:hover {
    background-color: rgb(184, 181, 181);
}

h1 {
    color: white;
    font-size: 2.5rem;
    margin-bottom: 50px;
    font-weight: 700;
    letter-spacing: 3px;
    animation: reloadA 1s ease-out forwards;
    opacity: 0;
    animation-delay: 0.2s;
}

/* 为所有输入框设置圆角 */
:deep(.el-input__wrapper) {
    border: 1px solid white;
    border-radius: 50px;
    height: 45px;
    margin-bottom: 25px;
    border: 1px solid white;
    box-shadow: 0 0 5px rgba(255, 255, 255, 0.799);
    background-color: transparent;
}

:deep(.el-form-item) {
    width: 90%;
}

/* 自定义占位符样式 */
:deep(.el-input__inner::placeholder) {
    background-color: transparent;
    color: white;
    font-size: 1.2rem;
}

/* 添加输入文字的样式 */
:deep(.el-input__inner) {
    color: white !important;
    font-size: 1.2rem !important;
    background-color: transparent !important;
    font-family: 'Poppins', sans-serif;
}

/* 设置图标颜色为白色 */
:deep(.el-input__prefix-icon) {
    color: white !important;
}

/* 用户类型按钮样式 */
.user-type-buttons {
    display: flex;
    justify-content: space-between;
    width: 100%;
    margin-bottom: 25px;
}

.type-btn {
    width: 48%;
    height: 45px;
    font-size: 1.2rem;
    border-radius: 50px;
    border: 1px solid white;
    background-color: transparent;
    color: white;
    transition: all 0.3s ease;
}

.type-btn:hover {
    background-color: rgba(255, 255, 255, 0.1);
}

.type-btn.active {
    background-color: rgba(255, 255, 255, 0.3);
    border: 1px solid white;
    box-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
    font-weight: 600;
}

.login-link {
    margin-top: 15px;
    color: white;
    font-size: 1.2rem;
    text-align: center;
    margin-bottom: 15px;
}

.login-link span {
    color: white;
}

.login-link a {
    color: #fbf5f2;
    font-weight: 500;
    text-decoration: none;
    cursor: pointer;
    margin-left: 5px;
}

.login-link a:hover {
    text-decoration: underline;
}

@keyframes reloadA {
    from {
        transform: translateY(250px);
    }
    to {
        transform: translateY(0);
        opacity: 1;
    }
}

/* 添加动画关键帧 */
@keyframes slideDown {
    from {
        transform: translateY(-30px);
        opacity: 0;
    }
    to {
        transform: translateY(0);
        opacity: 1;
    }
}

/* 为不同元素设置不同的动画延迟 */
.animate {
    opacity: 0;
    animation: slideDown 0.6s ease forwards;
}

h1.animate {
    animation-delay: 0.2s;
}

.el-form-item:nth-of-type(1).animate {
    animation-delay: 0.4s;
}

.el-form-item:nth-of-type(2).animate {
    animation-delay: 0.6s;
}

.el-form-item:nth-of-type(3).animate {
    animation-delay: 0.8s;
}

.el-form-item:nth-of-type(4).animate {
    animation-delay: 1s;
}

.el-form-item:nth-of-type(5).animate {
    animation-delay: 1.2s;
}

.login-link.animate {
    animation-delay: 1.4s;
}

/* 可以添加悬停效果 */
.register_btn:hover {
    transform: scale(1.02);
    transition: transform 0.3s ease;
}

/* 确保动画结束后保持状态 */
.animate {
    animation-fill-mode: forwards;
}
</style> 