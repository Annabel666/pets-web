<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { loadSession, register } from "../api";
import { flash } from "../flash";

const router = useRouter();
const username = ref("");
const password = ref("");
const confirm = ref("");
const error = ref("");
const pending = ref(false);

onMounted(() => {
  loadSession().catch(() => {});
});

async function submit() {
  error.value = "";
  pending.value = true;
  try {
    await register(username.value, password.value, confirm.value);
    flash.notice = "注册成功，登录后就可以留言。";
    router.push("/login");
  } catch (err) {
    error.value = err.message;
  } finally {
    pending.value = false;
  }
}
</script>

<template>
  <main class="auth">
    <router-link class="auth-back" to="/">回到礼物厅</router-link>
    <div class="auth-grid">
      <section class="auth-copy">
        <p class="kicker">PETS TSENG</p>
        <h1>留一个名字</h1>
        <p class="lede">用 2 到 20 位中文、字母或数字。登记之后回来登录，就可以留言。</p>
      </section>
      <form class="auth-form" @submit.prevent="submit">
        <p class="kicker">JOIN</p>
        <h2>登记</h2>
        <p v-if="error" class="form-error">{{ error }}</p>
        <label class="field">
          <span>名字</span>
          <input v-model="username" maxlength="20" autocomplete="username" placeholder="例如：厅里的人" required />
        </label>
        <label class="field">
          <span>密码</span>
          <input v-model="password" type="password" minlength="6" maxlength="64" autocomplete="new-password" placeholder="至少 6 位" required />
        </label>
        <label class="field">
          <span>再输入一次</span>
          <input v-model="confirm" type="password" minlength="6" maxlength="64" autocomplete="new-password" placeholder="再写一遍密码" required />
        </label>
        <button class="primary" type="submit" :disabled="pending">登记名字</button>
        <p class="quiet">已经有名字了？<router-link to="/login">去登录</router-link></p>
      </form>
    </div>
  </main>
</template>
