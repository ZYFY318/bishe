<template>
    <div class="login_container">
   
                <el-form class="login_form" :model="loginForm" :rules="rules" ref="loginForms">
                    <h1 class="animate">WELCOME</h1>
                    <el-form-item prop="username" class="animate">
                        <el-input :prefix-icon="User" v-model="loginForm.username" placeholder="Your account"></el-input>
                    </el-form-item>
                    <el-form-item prop="password" class="animate">
                        <el-input :prefix-icon="Lock" type="password" v-model="loginForm.password"
                            show-password placeholder="Your password"></el-input>
                    </el-form-item>
                    <el-form-item class="remember-forget animate">
                        <div class="remember-forget-container">
                            <p class="remember">
                                <input type="checkbox">
                                <span>Remember me</span>
                            </p>
                            <a class="forget">Forget password?</a>
                        </div>
                    </el-form-item>
                    <el-form-item class="animate">
                        <el-button :loading="loading" class="login_btn" type="success" size="default" color="#fbf5f2"
                            @click="login">login</el-button>
                    </el-form-item>
                    
                    <!-- 添加注册链接 -->
                    <div class="register-link animate">
                        <span>Don't have an account? </span>
                        <a @click="handleRegister">Register</a>
                    </div>
                </el-form>
 
    </div>
</template>

<script setup lang="ts">

import { User, Lock, Loading } from '@element-plus/icons-vue'
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
//用户仓库
import useUserStore from '@/stores/modules/user'
//获取el提示框vv
import { ElNotification } from 'element-plus';
//引入获取时间函数
import { getTime } from '@/utils/time'
//获取路由对象
import { useRoute } from 'vue-router';
const $route = useRoute();
let useStore = useUserStore();
//获取el-form组件
let loginForms = ref();
//获取路由器
let $router = useRouter();
//定义变量控制按钮加载效果
let loading = ref(false);
//收集账号和密码的数据
let loginForm = reactive({ username: '', password: '', remember: false })
//登录按钮回调
const login = async () => {
    //保证全部表单项校验通过再发请求
    await loginForms.value.validate();
    loading.value = true;

    try {
        //保证登陆成功
        await useStore.userLogin(loginForm);
        const redirect: any = $route.query.redirect;
        $router.push({ path: redirect || '/' })
        //登陆成功提示信息
        ElNotification({
            type: 'success',
            message: `欢迎回来${useStore.username}`,
            title: `HI,${getTime()}好`
        })
        loading.value = false;
    } catch (error) {
        loading.value = false;
        ElNotification({
            type: 'error',
            message: (error as Error).message
        })
    }


}
//自定义校验规则函数
const validatorUserName = (rule: any, value: string, callback: Function) => {
    //rule校验规则对象
    //value表单元素文本内容
    //函数:如果符合条件通过callback放行通过
    //不符合通过callback注入错误信息
    if (value.length >= 5) {
        callback();
    } else {
        callback(new Error('账号长度至少五位'))
    }
}

const validatorPassword = (rule: any, value: string, callback: Function) => {
    //rule校验规则对象
    //value表单元素文本内容
    //函数:如果符合条件通过callback放行通过
    //不符合通过callback注入错误信息
    if (value.length >= 6) {
        callback();
    } else {
        callback(new Error('密码长度至少六位'))
    }
}

//定义表单校验需要配置对象
const rules = {
    //required代表这个字段务必要校验
    username: [
        { trigger: 'change', validator: validatorUserName }
    ],
    password: [
        { trigger: 'change', validator: validatorPassword }
    ]
}

// 添加忘记密码处理函数
const handleForgetPassword = () => {
    // 处理忘记密码的逻辑
    console.log('Forget password clicked')
}

// 添加注册处理函数
const handleRegister = () => {
    $router.push('/register')  // 跳转到注册页面
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');
.login_container {
    font-family: 'Poppins', sans-serif;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
    top: 0;
    left: 0;
    width: 100%;
    height: 100vh;
    /* 覆盖整个视口 */
    background: url('../../assets/images/p0.png') no-repeat center center;
    background-size: cover;
    /* 确保背景图适配屏幕 */
}

.login_form {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
    padding: 50px 10px;
    width: 35%;
    top: 30vh;
    background-color: rgba(255, 255, 255, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 10px ;
    backdrop-filter: blur(10px);
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
    /* background-color: #fbf5f2; */
}

.login_btn {
    width: 100%;
    height: 55px;
    color: black;
    border-radius:50px;
    font-size: 1.2rem;
    font-weight: 600;
    letter-spacing: 1px;
}

.login_btn:hover {
    background-color: rgb(184, 181, 181);
}

h1 {
    color: white;
    font-size: 2.5rem;
    margin-bottom: 50px;
    font-weight: 700;
    letter-spacing: 3px ;
    animation: reloadA 1s ease-out forwards;
    opacity: 0;
    animation-delay: 0.2s;
}


/* 为所有输入框设置圆角 */
:deep(.el-input__wrapper) {
    border: 1px solid white;
  border-radius: 50px;  /* 可以调整数值来改变弧度大小 */
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
    font-size: 1.2rem;  /* 设置占位符文字大小 */
    /* font-style: italic;  设置斜体 */
}
/* 添加输入文字的样式 */
:deep(.el-input__inner) {
    color: white !important;  /* 输入文字颜色 */
    font-size: 1.2rem !important;  /* 输入文字大小 */
    background-color: transparent !important;  /* 确保背景透明 */
    font-family: 'Poppins', sans-serif;  /* 保持字体一致 */
}

/* 设置图标颜色为白色 */
:deep(.el-input__prefix-icon) {
    color: white !important;
}
/* 添加 Remember me 和 Forget password 的样式 */
.remember-forget-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    color: white;
    margin-bottom: 25px;
}

.remember {
    font-size: 1.2rem;
    display: flex;
    align-items: center;
    gap: 5px;  /* 复选框和文字之间的间距 */
    margin: 0;  /* 移除默认边距 */
}

.forget {
    font-size: 1.2rem;
    color: white;
    text-decoration: none;  /* 移除下划线 */
    cursor: pointer;  /* 添加手型光标 */
}

.register-link {
    margin-top: 15px;
    color: white;
    font-size: 1.2rem;
    text-align: center;
    margin-bottom: 15px;
}

.register-link span {
    color: white;
}

.register-link a {
    color: #fbf5f2;
    font-weight: 500;
    text-decoration: none;
    cursor: pointer;
    margin-left: 5px;
}

.register-link a:hover {
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
    animation-delay: 0.2s;  /* 标题最先出现 */
}

.el-form-item:nth-of-type(1).animate {
    animation-delay: 0.4s;  /* 用户名输入框 */
}

.el-form-item:nth-of-type(2).animate {
    animation-delay: 0.6s;  /* 密码输入框 */
}

.remember-forget.animate {
    animation-delay: 0.8s;  /* Remember me 和 Forget password */
}

.el-form-item:nth-of-type(4).animate {
    animation-delay: 1s;    /* 登录按钮 */
}

.register-link.animate {
    animation-delay: 1.2s;  /* 注册链接最后出现 */
}

/* 可以添加悬停效果 */
.login_btn:hover {
    transform: scale(1.02);
    transition: transform 0.3s ease;
}

/* 确保动画结束后保持状态 */
.animate {
    animation-fill-mode: forwards;
}
</style>

