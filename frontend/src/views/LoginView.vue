<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import { useRouter } from "vue-router";
import { loadSession, login } from "../api";
import { flash } from "../flash";

const router = useRouter();
const username = ref("");
const password = ref("");
const error = ref("");
const pending = ref(false);
const notice = ref(flash.notice);

onMounted(() => {
  loadSession().catch(() => {});
});

onUnmounted(() => {
  flash.notice = "";
});

async function submit() {
  error.value = "";
  pending.value = true;
  try {
    await login(username.value, password.value);
    router.push({ path: "/", hash: "#guestbook" });
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
        <h1>回来听一首</h1>
        <p class="lede">登录之后，名字会留在礼物厅一侧，也可以在留言册里写一句。</p>
      </section>
      <form class="auth-form" @submit.prevent="submit">
        <p class="kicker">SIGN IN</p>
        <h2>登录</h2>
        <p v-if="notice" class="notice">{{ notice }}</p>
        <p v-if="error" class="form-error">{{ error }}</p>
        <label class="field">
          <span>名字</span>
          <input v-model="username" maxlength="20" autocomplete="username" placeholder="你在厅里的名字" required />
        </label>
        <label class="field">
          <span>密码</span>
          <input v-model="password" type="password" maxlength="64" autocomplete="current-password" placeholder="至少 6 位" required />
        </label>
        <button class="primary" type="submit" :disabled="pending">进入</button>
        <p class="quiet">还没有名字？<router-link to="/register">去登记</router-link></p>
      </form>
    </div>
  </main>
</template>
