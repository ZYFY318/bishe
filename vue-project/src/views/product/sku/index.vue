<template>
    <div class="login_container">
        <h1>sku</h1>
        <el-button type="primary" size="default" @click="add">add question</el-button>

    </div>
</template>

<script setup lang="ts">
const add = () => {
    for (let i = 1; i <= 10; i++) {
        fetch("http://localhost:9090/question", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                title: `测试题目 ${i}`,
                options: [`A. 选项1`, `B. 选项2`, `C. 选项3`, `D. 选项4`],
                answer: "A. 选项1"
            })
        });
    }

}
</script>

<style scoped lang="scss"></style>