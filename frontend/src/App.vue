<template>
  <el-config-provider :locale="zhCn">
    <div class="app-wrapper">
      <!-- Background decorations -->
      <div class="bg-decoration">
        <div class="bg-blob bg-blob-1"></div>
        <div class="bg-blob bg-blob-2"></div>
        <div class="bg-blob bg-blob-3"></div>
      </div>
      <AppHeader />
      <main class="main-content animate-fade-in">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
      <footer class="app-footer">
        <p>© 2026 校园失物招领平台 · 让每一件失物都能找到主人</p>
      </footer>
    </div>
  </el-config-provider>
</template>

<script setup>
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import AppHeader from './components/AppHeader.vue'
</script>

<style scoped>
.app-wrapper {
  min-height: 100vh;
  position: relative;
  display: flex;
  flex-direction: column;
}
.bg-decoration {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}
.bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.4;
}
.bg-blob-1 {
  width: 600px; height: 600px;
  background: rgba(99, 102, 241, 0.12);
  top: -200px; right: -100px;
}
.bg-blob-2 {
  width: 500px; height: 500px;
  background: rgba(139, 92, 246, 0.08);
  bottom: -150px; left: -100px;
}
.bg-blob-3 {
  width: 400px; height: 400px;
  background: rgba(16, 185, 129, 0.06);
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
}
.main-content {
  max-width: 1240px;
  width: 100%;
  margin: 0 auto;
  padding: 28px 24px 60px;
  position: relative;
  z-index: 1;
  flex: 1;
}
.app-footer {
  text-align: center;
  padding: 24px;
  color: var(--text-muted);
  font-size: 13px;
  position: relative;
  z-index: 1;
  border-top: 1px solid var(--border);
  background: rgba(255,255,255,0.5);
  backdrop-filter: blur(10px);
}
/* Page transition */
.page-enter-active { animation: fadeInUp 0.35s ease-out; }
.page-leave-active { animation: fadeIn 0.15s ease-in reverse; }
</style>
