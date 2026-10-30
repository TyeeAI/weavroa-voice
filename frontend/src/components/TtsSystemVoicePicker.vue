<script setup>
import { onMounted, ref } from 'vue'
import { listSystemTtsVoices } from '../lib/systemTtsVoices.js'

const emit = defineEmits(['pick'])

const voices = ref([])
const loading = ref(false)
const error = ref('')

function pickDefaultModel(supportedModels) {
  const list = Array.isArray(supportedModels) ? supportedModels : []
  const flash = list.find((m) => typeof m === 'string' && m.includes('flash'))
  if (flash) {
    return flash
  }
  return list[0] || 'qwen3-tts-flash'
}

function genderLabel(g) {
  if (g === 'female') {
    return '女'
  }
  if (g === 'male') {
    return '男'
  }
  return g || '—'
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    voices.value = await listSystemTtsVoices()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
    voices.value = []
  } finally {
    loading.value = false
  }
}

function apply(row) {
  const model = pickDefaultModel(row.supportedModels)
  emit('pick', { voice: row.voice, model, label: row.name || row.voice })
}

onMounted(() => {
  load()
})
</script>

<template>
  <div class="sys-voices">
    <div class="sys-voices-head">
      <h3 class="sys-voices-title">内置音色</h3>
      <button type="button" class="btn-ghost" :disabled="loading" @click="load">更新列表</button>
    </div>
    <p class="sys-voices-desc">任选一种官方音色，点击下方「使用」后，在下方输入文字即可生成语音。</p>
    <p v-if="error" class="sys-voices-alert">{{ error }}</p>
    <p v-else-if="loading" class="sys-voices-muted">加载中…</p>
    <p v-else-if="!voices.length" class="sys-voices-muted">暂无数据。</p>
    <ul v-else class="sys-voice-list">
      <li v-for="v in voices" :key="v.voice" class="sys-voice-item">
        <div class="sys-voice-main">
          <p class="sys-voice-line">
            <span class="sys-voice-name">{{ v.name }}</span>
            <span class="sys-voice-gender">{{ genderLabel(v.gender) }}</span>
          </p>
          <p v-if="v.description" class="sys-voice-desc-text">{{ v.description }}</p>
        </div>
        <button type="button" class="btn-pick" @click="apply(v)">使用</button>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.sys-voices {
  margin-bottom: 18px;
  padding: 14px 14px 16px;
  border-radius: 14px;
  border: 1px solid rgba(197, 181, 152, 0.45);
  background: rgba(255, 252, 247, 0.65);
}

.sys-voices-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.sys-voices-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #221c16;
}

.sys-voices-desc {
  margin: 0 0 10px;
  font-size: 12px;
  line-height: 1.5;
  color: #6d655a;
}

.sys-voices-muted {
  margin: 0;
  font-size: 13px;
  color: #7a6f5f;
}

.sys-voices-alert {
  margin: 0 0 8px;
  font-size: 13px;
  color: #a14d4d;
}

.sys-voice-list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 280px;
  overflow: auto;
}

.sys-voice-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(197, 181, 152, 0.35);
}

.sys-voice-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.sys-voice-main {
  min-width: 0;
  flex: 1;
}

.sys-voice-line {
  margin: 0 0 4px;
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 10px;
}

.sys-voice-name {
  font-weight: 600;
  color: #2c261c;
  font-size: 14px;
}

.sys-voice-gender {
  font-size: 12px;
  color: #7a6f5f;
}

.sys-voice-desc-text {
  margin: 0;
  font-size: 12px;
  line-height: 1.45;
  color: #5c5348;
}

.btn-ghost {
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 12px;
  border: 1px solid rgba(197, 181, 152, 0.55);
  background: transparent;
  color: #5c5348;
  cursor: pointer;
}

.btn-ghost:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-pick {
  flex-shrink: 0;
  margin-top: 2px;
  border: none;
  background: transparent;
  color: #8d6a3a;
  font-size: 13px;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
  padding: 4px 0;
}
</style>
