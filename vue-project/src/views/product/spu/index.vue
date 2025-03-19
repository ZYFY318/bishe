<template>
    <div class="login_container">
        <h1>spu</h1>

        <!-- <div>
                <el-radio-group v-model="selectedNum" size="medium" class="el-group" style="margin-top: 1rem;">
                    <el-radio-button label="3">3题</el-radio-button>
                    <el-radio-button label="5">5题</el-radio-button>
                    <el-radio-button label="10">10题</el-radio-button>
                </el-radio-group>
            </div> -->
        <div class="mb-2 ml-4">
            <el-radio-group v-model="radio1">
                <el-radio value="1" size="large">Option 1</el-radio>
                <el-radio value="2" size="large">Option 2</el-radio>
            </el-radio-group>
        </div>

    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
const radio1 = ref('1')
const selectedNum = ref(3); // 用户选择的题目数量
</script>

<style scoped lang="scss"></style>