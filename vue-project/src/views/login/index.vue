<template>
    <div class="login_container">
        <el-row>
            <el-col :span="12" :xs="0"></el-col>
            <el-col :span="12" :xs="24">
                <el-form class="login_form" :model="loginForm" :rules="rules" ref="loginForms">
                    <h1>hello</h1>
                    <h2>welcome to my world</h2>
                    <el-form-item prop="username">
                        <el-input :prefix-icon="User" v-model="loginForm.username"></el-input>
                    </el-form-item>
                    <el-form-item prop="password">
                        <el-input :prefix-icon="Lock" type="password" v-model="loginForm.password"
                            show-password></el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-button :loading="loading" class="login_btn" type="success" size="default" color="#fbf5f2"
                            @click="login">login</el-button>

                    </el-form-item>
                </el-form>

            </el-col>
        </el-row>
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
let loginForm = reactive({ username: '', password: '' })
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
        await useStore.userInfo();
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
</script>

<style scoped>
.login_container {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100vh;
    /* 覆盖整个视口 */
    background: url('../../assets/images/background.jpg') no-repeat center center;
    background-size: cover;
    /* 确保背景图适配屏幕 */
}

.login_form {
    position: relative;
    width: 60%;
    top: 30vh;
    /* background-color: #fbf5f2; */
}

.login_btn {
    width: 100%;
    color: black
}

h1 {
    color: black;
    font-size: 40px;
}

h2 {
    font-size: 20px;
    margin: 20px 0px;
}
</style>