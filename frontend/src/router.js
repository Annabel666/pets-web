import { createRouter, createWebHistory } from "vue-router";
import HallView from "./views/HallView.vue";
import LoginView from "./views/LoginView.vue";
import RegisterView from "./views/RegisterView.vue";

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", component: HallView },
    { path: "/login", component: LoginView },
    { path: "/register", component: RegisterView },
  ],
  scrollBehavior(to) {
    if (to.hash) {
      return { el: to.hash, behavior: "smooth" };
    }
    return { top: 0 };
  },
});
