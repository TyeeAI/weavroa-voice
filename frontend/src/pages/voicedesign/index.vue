<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import TtsSystemVoicePicker from '../../components/TtsSystemVoicePicker.vue'
import { createDesignVoice, deleteDesignVoice, listDesignVoices, synthesizeTts } from './api/index.js'

const DEFAULT_TARGET_MODEL = 'qwen3-tts-vd-2026-01-26'
const PAGE_SIZE = 10

const voicePrompt = ref('')
const previewText = ref('')
const preferredName = ref('')
const language = ref('zh')
const targetModel = ref(DEFAULT_TARGET_MODEL)

const isSubmitting = ref(false)
const errorMessage = ref('')
const lastVoiceId = ref('')
const previewSrc = ref('')
const previewAudioRef = ref(null)
const previewPlaying = ref(false)
const previewCurrentTime = ref(0)
const previewDuration = ref(0)
const previewVolume = ref(1)
const isPreviewDownloading = ref(false)
const previewAudioError = ref('')

const listItems = ref([])
const pageIndex = ref(0)
const hasMore = ref(true)
const isListLoading = ref(false)

const selectedVoiceId = ref('')
const ttsText = ref('')
const ttsModel = ref(DEFAULT_TARGET_MODEL)
const isTtsLoading = ref(false)
const ttsAudioUrl = ref('')
const ttsAudioMime = ref('')
const ttsAudioRef = ref(null)
const ttsPlaying = ref(false)
const ttsCurrentTime = ref(0)
const ttsDuration = ref(0)
const ttsVolume = ref(1)
const isTtsDownloading = ref(false)
const ttsError = ref('')
const ttsVoiceLabel = ref('')

const previewProgressPct = computed(() => {
  const d = previewDuration.value
  if (!d || !Number.isFinite(d)) {
    return 0
  }
  return Math.min(100, (previewCurrentTime.value / d) * 100)
})

const ttsProgressPct = computed(() => {
  const d = ttsDuration.value
  if (!d || !Number.isFinite(d)) {
    return 0
  }
  return Math.min(100, (ttsCurrentTime.value / d) * 100)
})

watch(previewSrc, async (url) => {
  previewAudioError.value = ''
  if (!url) {
    previewCurrentTime.value = 0
    previewDuration.value = 0
    previewPlaying.value = false
    return
  }
  previewCurrentTime.value = 0
  previewDuration.value = 0
  previewPlaying.value = false
  await nextTick()
  const el = previewAudioRef.value
  if (el) {
    el.volume = previewVolume.value
    el.load?.()
  }
})

watch(previewVolume, (v) => {
  const el = previewAudioRef.value
  if (el) {
    el.volume = Math.min(1, Math.max(0, v))
  }
})

watch(ttsAudioUrl, async (url) => {
  if (!url) {
    ttsAudioMime.value = ''
    ttsCurrentTime.value = 0
    ttsDuration.value = 0
    ttsPlaying.value = false
    return
  }
  ttsCurrentTime.value = 0
  ttsDuration.value = 0
  ttsPlaying.value = false
  await nextTick()
  const el = ttsAudioRef.value
  if (el) {
    el.volume = ttsVolume.value
    el.load?.()
  }
})

watch(ttsVolume, (v) => {
  const el = ttsAudioRef.value
  if (el) {
    el.volume = Math.min(1, Math.max(0, v))
  }
})

function formatAudioTime(sec) {
  if (!Number.isFinite(sec) || sec < 0) {
    return '0:00'
  }
  const m = Math.floor(sec / 60)
  const s = Math.floor(sec % 60)
  return `${m}:${String(s).padStart(2, '0')}`
}

function guessExtFromMime(mime) {
  const m = (mime || '').toLowerCase()
  if (m.includes('mpeg') || m.includes('mp3')) {
    return 'mp3'
  }
  if (m.includes('wav')) {
    return 'wav'
  }
  if (m.includes('webm')) {
    return 'webm'
  }
  if (m.includes('ogg')) {
    return 'ogg'
  }
  if (m.includes('mp4') || m.includes('m4a')) {
    return 'm4a'
  }
  return 'audio'
}

function onPreviewLoadedMetadata() {
  const el = previewAudioRef.value
  const d = el?.duration
  previewDuration.value = Number.isFinite(d) ? d : 0
}

function onPreviewTimeUpdate() {
  const el = previewAudioRef.value
  previewCurrentTime.value = el?.currentTime ?? 0
}

function togglePreviewPlay() {
  const el = previewAudioRef.value
  if (!el) {
    return
  }
  if (el.paused) {
    el.play().catch(() => {})
  } else {
    el.pause()
  }
}

function onPreviewPlayEvent() {
  previewPlaying.value = true
}

function onPreviewPauseEvent() {
  previewPlaying.value = false
}

function onPreviewEndedEvent() {
  previewPlaying.value = false
  previewCurrentTime.value = 0
}

function seekPreviewFromPointer(ev) {
  const el = previewAudioRef.value
  const track = ev.currentTarget
  if (!el || !track || !Number.isFinite(el.duration) || el.duration <= 0) {
    return
  }
  const rect = track.getBoundingClientRect()
  const x = ev.clientX - rect.left
  const ratio = Math.min(1, Math.max(0, x / rect.width))
  el.currentTime = ratio * el.duration
}

function onPreviewProgressKeydown(ev) {
  const el = previewAudioRef.value
  if (!el || !Number.isFinite(el.duration) || el.duration <= 0) {
    return
  }
  if (ev.key === 'ArrowLeft' || ev.key === 'ArrowDown') {
    ev.preventDefault()
    el.currentTime = Math.max(0, el.currentTime - 5)
  } else if (ev.key === 'ArrowRight' || ev.key === 'ArrowUp') {
    ev.preventDefault()
    el.currentTime = Math.min(el.duration, el.currentTime + 5)
  } else if (ev.key === 'Home') {
    ev.preventDefault()
    el.currentTime = 0
  } else if (ev.key === 'End') {
    ev.preventDefault()
    el.currentTime = el.duration
  }
}

function onPreviewVolumeInput(ev) {
  const raw = Number(ev?.target?.value)
  previewVolume.value = Number.isFinite(raw) ? raw : 1
}

function onPreviewAudioError(ev) {
  const code = ev?.target?.error?.code
  const map = {
    1: '音频加载已中止。',
    2: '网络异常，音频无法加载。',
    3: '解码失败（数据可能损坏或格式与后缀不一致）。',
    4: '当前浏览器不支持该音频格式。',
  }
  previewAudioError.value = map[code] || '预览无法播放，请重试创作或联系技术支持。'
  previewPlaying.value = false
}

async function downloadPreviewAudio() {
  const url = previewSrc.value
  if (!url || isPreviewDownloading.value) {
    return
  }
  isPreviewDownloading.value = true
  const stamp = new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-')
  try {
    const res = await fetch(url)
    if (!res.ok) {
      throw new Error(String(res.status))
    }
    const blob = await res.blob()
    const extFromBlob = guessExtFromMime(blob.type)
    const ext = extFromBlob !== 'audio' ? extFromBlob : 'wav'
    const filename = `设计试听_${stamp}.${ext}`
    const dlUrl = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = dlUrl
    a.download = filename
    a.rel = 'noopener'
    a.click()
    URL.revokeObjectURL(dlUrl)
  } catch {
    const a = document.createElement('a')
    a.href = url
    a.download = `设计试听_${stamp}.wav`
    a.target = '_blank'
    a.rel = 'noopener noreferrer'
    a.click()
  } finally {
    isPreviewDownloading.value = false
  }
}

function onTtsLoadedMetadata() {
  const el = ttsAudioRef.value
  const d = el?.duration
  ttsDuration.value = Number.isFinite(d) ? d : 0
}

function onTtsTimeUpdate() {
  const el = ttsAudioRef.value
  ttsCurrentTime.value = el?.currentTime ?? 0
}

function toggleTtsPlay() {
  const el = ttsAudioRef.value
  if (!el) {
    return
  }
  if (el.paused) {
    el.play().catch(() => {})
  } else {
    el.pause()
  }
}

function onTtsPlayEvent() {
  ttsPlaying.value = true
}

function onTtsPauseEvent() {
  ttsPlaying.value = false
}

function onTtsEndedEvent() {
  ttsPlaying.value = false
  ttsCurrentTime.value = 0
}

function seekTtsFromPointer(ev) {
  const el = ttsAudioRef.value
  const track = ev.currentTarget
  if (!el || !track || !Number.isFinite(el.duration) || el.duration <= 0) {
    return
  }
  const rect = track.getBoundingClientRect()
  const x = ev.clientX - rect.left
  const ratio = Math.min(1, Math.max(0, x / rect.width))
  el.currentTime = ratio * el.duration
}

function onTtsProgressKeydown(ev) {
  const el = ttsAudioRef.value
  if (!el || !Number.isFinite(el.duration) || el.duration <= 0) {
    return
  }
  if (ev.key === 'ArrowLeft' || ev.key === 'ArrowDown') {
    ev.preventDefault()
    el.currentTime = Math.max(0, el.currentTime - 5)
  } else if (ev.key === 'ArrowRight' || ev.key === 'ArrowUp') {
    ev.preventDefault()
    el.currentTime = Math.min(el.duration, el.currentTime + 5)
  } else if (ev.key === 'Home') {
    ev.preventDefault()
    el.currentTime = 0
  } else if (ev.key === 'End') {
    ev.preventDefault()
    el.currentTime = el.duration
  }
}

function onTtsVolumeInput(ev) {
  const raw = Number(ev?.target?.value)
  ttsVolume.value = Number.isFinite(raw) ? raw : 1
}

function onTtsAudioError(ev) {
  const code = ev?.target?.error?.code
  const map = {
    1: '音频加载已中止。',
    2: '网络异常，音频无法加载。',
    3: '解码失败（数据可能损坏或格式与后缀不一致）。',
    4: '当前浏览器不支持该音频格式。',
  }
  ttsError.value = map[code] || '音频无法播放，请尝试「生成语音」或稍后重试。'
  ttsPlaying.value = false
}

async function downloadTtsAudio() {
  const url = ttsAudioUrl.value
  if (!url || isTtsDownloading.value) {
    return
  }
  isTtsDownloading.value = true
  const stamp = new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-')
  try {
    const res = await fetch(url)
    if (!res.ok) {
      throw new Error(String(res.status))
    }
    const blob = await res.blob()
    const extFromBlob = guessExtFromMime(blob.type)
    const ext = extFromBlob !== 'audio' ? extFromBlob : guessExtFromMime(ttsAudioMime.value)
    const filename = `语音试听_${stamp}.${ext}`
    const dlUrl = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = dlUrl
    a.download = filename
    a.rel = 'noopener'
    a.click()
    URL.revokeObjectURL(dlUrl)
  } catch {
    const ext = guessExtFromMime(ttsAudioMime.value)
    const filename = `语音试听_${stamp}.${ext}`
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.target = '_blank'
    a.rel = 'noopener noreferrer'
    a.click()
  } finally {
    isTtsDownloading.value = false
  }
}

/**
 * 兼容多种列表包裹：直接数组、{ items }、Spring Page 的 { content }、{ list/records/voices } 等。
 * @param {unknown} data
 */
function normalizeListPayload(data) {
  if (Array.isArray(data)) {
    return data
  }
  if (!data || typeof data !== 'object') {
    return []
  }
  const o = /** @type {Record<string, unknown>} */ (data)
  const keys = ['items', 'content', 'list', 'records', 'rows', 'voices', 'data']
  for (const k of keys) {
    const v = o[k]
    if (Array.isArray(v)) {
      return v
    }
  }
  return []
}

/** 从创作接口的 ApiResponse.data 里取出音色 id */
function extractCreatedVoiceId(res) {
  const raw = res?.data
  if (raw == null) {
    return ''
  }
  if (typeof raw === 'string') {
    return raw.trim()
  }
  if (typeof raw === 'object') {
    const o = /** @type {Record<string, unknown>} */ (raw)
    const v = o.voice ?? o.voiceId ?? o.id ?? o.voice_id
    return typeof v === 'string' ? v.trim() : v != null ? String(v).trim() : ''
  }
  return String(raw).trim()
}

function displayDesignTitle(item) {
  return item.preferredName || item.name || '我的定制音色'
}

const canSubmit = computed(() => {
  return Boolean(voicePrompt.value.trim()) && Boolean(previewText.value.trim()) && !isSubmitting.value
})

function handleRequestError(error, targetRef) {
  const msg = error instanceof Error ? error.message : '请求失败，请稍后重试。'
  targetRef.value = msg
}

function buildPreviewSrc(base64) {
  if (!base64) {
    return ''
  }
  return `data:audio/wav;base64,${base64}`
}

async function loadList(reset) {
  if (reset) {
    pageIndex.value = 0
    listItems.value = []
    hasMore.value = true
  }
  if (!hasMore.value && !reset) {
    return
  }

  isListLoading.value = true
  errorMessage.value = ''
  try {
    const res = await listDesignVoices(pageIndex.value, PAGE_SIZE)
    const chunk = normalizeListPayload(res?.data)
    if (reset) {
      listItems.value = chunk
    } else {
      listItems.value = listItems.value.concat(chunk)
    }
    hasMore.value = chunk.length === PAGE_SIZE
    if (chunk.length && !selectedVoiceId.value) {
      selectVoiceForTts(chunk[0])
    }
  } catch (error) {
    handleRequestError(error, errorMessage)
  } finally {
    isListLoading.value = false
  }
}

async function loadMore() {
  if (!hasMore.value || isListLoading.value) {
    return
  }
  pageIndex.value += 1
  isListLoading.value = true
  errorMessage.value = ''
  try {
    const res = await listDesignVoices(pageIndex.value, PAGE_SIZE)
    const chunk = normalizeListPayload(res?.data)
    listItems.value = listItems.value.concat(chunk)
    hasMore.value = chunk.length === PAGE_SIZE
  } catch (error) {
    pageIndex.value -= 1
    handleRequestError(error, errorMessage)
  } finally {
    isListLoading.value = false
  }
}

async function submitDesign() {
  if (!canSubmit.value) {
    return
  }
  isSubmitting.value = true
  errorMessage.value = ''
  lastVoiceId.value = ''
  previewSrc.value = ''
  try {
    const payload = {
      voicePrompt: voicePrompt.value.trim(),
      targetModel: targetModel.value.trim(),
      previewText: previewText.value.trim(),
      preferredName: preferredName.value.trim() || undefined,
      language: language.value || undefined,
    }
    const res = await createDesignVoice(payload)
    const data =
      typeof res?.data === 'object' && res?.data != null ? /** @type {Record<string, unknown>} */ (res.data) : {}
    const voiceId = extractCreatedVoiceId(res)
    lastVoiceId.value = voiceId
    if (voiceId) {
      selectedVoiceId.value = voiceId
    }
    const tm = data.targetModel
    ttsModel.value = (typeof tm === 'string' && tm) || targetModel.value.trim()
    const b64 = data.previewAudioBase64 ?? data.previewAudio
    previewSrc.value = buildPreviewSrc(typeof b64 === 'string' ? b64 : '')
    ttsVoiceLabel.value = preferredName.value.trim() || '刚创作的音色'
    await loadList(true)
    if (voiceId && !listItems.value.some((it) => it?.voice === voiceId)) {
      const nm = preferredName.value.trim()
      listItems.value = [{ voice: voiceId, preferredName: nm, name: nm }, ...listItems.value]
    }
  } catch (error) {
    handleRequestError(error, errorMessage)
  } finally {
    isSubmitting.value = false
  }
}

async function onDelete(item) {
  if (!item?.voice) {
    return
  }
  if (!window.confirm(`确定删除「${displayDesignTitle(item)}」吗？删除后无法恢复。`)) {
    return
  }
  errorMessage.value = ''
  try {
    await deleteDesignVoice(item.voice)
    if (selectedVoiceId.value === item.voice) {
      selectedVoiceId.value = ''
      ttsVoiceLabel.value = ''
    }
    await loadList(true)
  } catch (error) {
    handleRequestError(error, errorMessage)
  }
}

function selectVoiceForTts(item) {
  if (!item?.voice) {
    return
  }
  selectedVoiceId.value = item.voice
  if (item.targetModel) {
    ttsModel.value = item.targetModel
  }
  ttsVoiceLabel.value = displayDesignTitle(item)
}

function onSystemVoicePick({ voice, model, label }) {
  selectedVoiceId.value = voice
  ttsModel.value = model
  ttsVoiceLabel.value = label || '内置音色'
}

function revokeTtsBlobIfNeeded() {
  if (ttsAudioUrl.value?.startsWith('blob:')) {
    URL.revokeObjectURL(ttsAudioUrl.value)
  }
}

async function runTts() {
  const voice = selectedVoiceId.value.trim()
  const model = ttsModel.value.trim()
  const text = ttsText.value.trim()
  if (!voice || !model || !text) {
    ttsError.value = '请先选好要用的声音，并输入要说的内容。'
    return
  }
  isTtsLoading.value = true
  ttsError.value = ''
  revokeTtsBlobIfNeeded()
  ttsAudioUrl.value = ''
  ttsAudioMime.value = ''
  try {
    const res = await synthesizeTts({ model, voice, text })
    const url = res?.data?.audioUrl
    if (!url) {
      throw new Error('未返回音频地址')
    }
    ttsAudioUrl.value = url
  } catch (error) {
    ttsError.value = error instanceof Error ? error.message : '合成失败'
  } finally {
    isTtsLoading.value = false
  }
}

onMounted(() => {
  loadList(true)
})

onBeforeUnmount(() => {
  revokeTtsBlobIfNeeded()
})
</script>

<template>
  <div class="voice-page">
    <div class="voice-page-inner">
    <header class="hero">
      <p class="eyebrow">AI 定制声音</p>
      <h1 class="title">专属音色与配音音效设计</h1>
      <p class="lede">
        支持个性化语音制作，适配短视频配音、品牌旁白、个人专属声纹定制。用文字描述即可生成可合成的设计音色。
      </p>
    </header>

    <section class="card main-card">
      <h2 class="card-title">描述您想要的声音</h2>
      <p class="hint">用日常语言形容即可，例如年龄、气质、使用场景。再写一句希望它读出来的试听话，生成后可直接试听。</p>

      <div class="field-grid">
        <label class="field field-span">
          <span class="label">希望是怎样的声音</span>
          <textarea
            v-model="voicePrompt"
            class="input textarea"
            rows="4"
            placeholder="例如：温柔亲切的女声，语速适中，适合接听客户电话"
          />
        </label>
        <label class="field field-span">
          <span class="label">试听时读的一句话</span>
          <textarea
            v-model="previewText"
            class="input textarea"
            rows="2"
            placeholder="例如：您好，很高兴为您服务。"
          />
        </label>
        <label class="field">
          <span class="label">给这个声音起个名字（选填）</span>
          <input v-model="preferredName" class="input" type="text" placeholder="方便您自己辨认" />
        </label>
        <label class="field">
          <span class="label">语言</span>
          <select v-model="language" class="input select">
            <option value="zh">中文</option>
            <option value="en">英文</option>
          </select>
        </label>
      </div>

      <p v-if="errorMessage" class="alert">{{ errorMessage }}</p>
      <div v-if="lastVoiceId" class="result-block">
        <p class="success">创作完成，已保存到「我的定制音色」。您可先听下方预览，或在「生成语音试听」里继续试读。</p>
        <p v-if="previewAudioError" class="alert tight">{{ previewAudioError }}</p>
        <div v-if="previewSrc" class="tts-player-wrap">
          <audio
            :key="previewSrc"
            ref="previewAudioRef"
            class="tts-audio-el"
            playsinline
            preload="auto"
            :src="previewSrc"
            @loadedmetadata="onPreviewLoadedMetadata"
            @durationchange="onPreviewLoadedMetadata"
            @timeupdate="onPreviewTimeUpdate"
            @play="onPreviewPlayEvent"
            @pause="onPreviewPauseEvent"
            @ended="onPreviewEndedEvent"
            @error="onPreviewAudioError"
          />
          <div class="tts-player" role="region" aria-label="设计预览播放器">
            <button
              type="button"
              class="tts-play-btn"
              :aria-label="previewPlaying ? '暂停' : '播放'"
              @click="togglePreviewPlay"
            >
              <svg v-if="!previewPlaying" class="tts-icon" viewBox="0 0 24 24" aria-hidden="true">
                <path fill="currentColor" d="M8 5v14l11-7z" />
              </svg>
              <svg v-else class="tts-icon" viewBox="0 0 24 24" aria-hidden="true">
                <path fill="currentColor" d="M6 19h4V5H6v14zm8-14v14h4V5h-4z" />
              </svg>
            </button>
            <div class="tts-player-main">
              <div
                class="tts-progress-track"
                role="slider"
                tabindex="0"
                :aria-valuenow="Math.round(previewProgressPct)"
                aria-valuemin="0"
                aria-valuemax="100"
                aria-label="预览播放进度"
                @click="seekPreviewFromPointer"
                @keydown="onPreviewProgressKeydown"
              >
                <div class="tts-progress-fill" :style="{ width: previewProgressPct + '%' }" />
              </div>
              <div class="tts-time-row">
                <span class="tts-time">{{ formatAudioTime(previewCurrentTime) }}</span>
                <span class="tts-time muted">{{ formatAudioTime(previewDuration) }}</span>
              </div>
            </div>
            <div class="tts-vol-wrap" title="音量">
              <svg class="tts-vol-icon" viewBox="0 0 24 24" aria-hidden="true">
                <path
                  fill="currentColor"
                  d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z"
                />
              </svg>
              <input
                class="tts-vol-range"
                type="range"
                min="0"
                max="1"
                step="0.05"
                :value="previewVolume"
                @input="onPreviewVolumeInput"
              />
            </div>
            <button
              type="button"
              class="btn secondary tts-download-btn"
              :disabled="isPreviewDownloading"
              @click="downloadPreviewAudio"
            >
              {{ isPreviewDownloading ? '准备中…' : '下载音频' }}
            </button>
          </div>
        </div>
      </div>

      <div class="actions">
        <button type="button" class="btn primary" :disabled="!canSubmit" @click="submitDesign">
          {{ isSubmitting ? '创作中…' : '开始创作' }}
        </button>
      </div>
    </section>

    <section class="card">
      <div class="card-head">
        <h2 class="card-title">我的定制音色</h2>
        <button type="button" class="btn ghost" :disabled="isListLoading" @click="loadList(true)">刷新</button>
      </div>

      <div v-if="!listItems.length && !isListLoading" class="empty">
        还没有保存的定制音色，完成上一步后会出现在这里。
      </div>

      <ul v-else class="list">
        <li v-for="item in listItems" :key="item.voice" class="list-item">
          <div class="list-main">
            <p class="voice-title">{{ displayDesignTitle(item) }}</p>
            <p class="meta">{{ item.gmtCreate || item.createdAt || '' }}</p>
          </div>
          <div class="list-actions">
            <button type="button" class="btn text" @click="selectVoiceForTts(item)">用这个声音</button>
            <button type="button" class="btn text danger" @click="onDelete(item)">删除</button>
          </div>
        </li>
      </ul>

      <div class="actions center">
        <button v-if="hasMore" type="button" class="btn ghost" :disabled="isListLoading" @click="loadMore">
          {{ isListLoading ? '加载中…' : '加载更多' }}
        </button>
      </div>
    </section>

    <section class="card">
      <h2 class="card-title">生成语音试听</h2>
      <p class="hint">先用「我的定制音色」或下面的「内置音色」选定一种声音，再输入想听的内容。</p>

      <p v-if="ttsVoiceLabel" class="picked-voice">当前使用：<strong>{{ ttsVoiceLabel }}</strong></p>
      <p v-else class="hint soft">尚未选择声音，请从「我的定制音色」或「内置音色」中任选其一。</p>

      <TtsSystemVoicePicker @pick="onSystemVoicePick" />

      <label class="field field-span simple-field">
        <span class="label">想听 Ta 说什么</span>
        <textarea v-model="ttsText" class="input textarea" rows="3" placeholder="例如：欢迎来到我们的频道。" />
      </label>

      <p v-if="ttsError" class="alert">{{ ttsError }}</p>

      <div class="actions">
        <button type="button" class="btn primary" :disabled="isTtsLoading" @click="runTts">
          {{ isTtsLoading ? '生成中…' : '生成语音' }}
        </button>
      </div>

      <div v-if="ttsAudioUrl" class="tts-player-wrap">
        <audio
          :key="ttsAudioUrl"
          ref="ttsAudioRef"
          class="tts-audio-el"
          playsinline
          preload="auto"
          :src="ttsAudioUrl"
          @loadedmetadata="onTtsLoadedMetadata"
          @durationchange="onTtsLoadedMetadata"
          @timeupdate="onTtsTimeUpdate"
          @play="onTtsPlayEvent"
          @pause="onTtsPauseEvent"
          @ended="onTtsEndedEvent"
          @error="onTtsAudioError"
        />
        <div class="tts-player" role="region" aria-label="语音试听播放器">
          <button
            type="button"
            class="tts-play-btn"
            :aria-label="ttsPlaying ? '暂停' : '播放'"
            @click="toggleTtsPlay"
          >
            <svg v-if="!ttsPlaying" class="tts-icon" viewBox="0 0 24 24" aria-hidden="true">
              <path fill="currentColor" d="M8 5v14l11-7z" />
            </svg>
            <svg v-else class="tts-icon" viewBox="0 0 24 24" aria-hidden="true">
              <path fill="currentColor" d="M6 19h4V5H6v14zm8-14v14h4V5h-4z" />
            </svg>
          </button>
          <div class="tts-player-main">
            <div
              class="tts-progress-track"
              role="slider"
              tabindex="0"
              :aria-valuenow="Math.round(ttsProgressPct)"
              aria-valuemin="0"
              aria-valuemax="100"
              aria-label="播放进度"
              @click="seekTtsFromPointer"
              @keydown="onTtsProgressKeydown"
            >
              <div class="tts-progress-fill" :style="{ width: ttsProgressPct + '%' }" />
            </div>
            <div class="tts-time-row">
              <span class="tts-time">{{ formatAudioTime(ttsCurrentTime) }}</span>
              <span class="tts-time muted">{{ formatAudioTime(ttsDuration) }}</span>
            </div>
          </div>
          <div class="tts-vol-wrap" title="音量">
            <svg class="tts-vol-icon" viewBox="0 0 24 24" aria-hidden="true">
              <path
                fill="currentColor"
                d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z"
              />
            </svg>
            <input
              class="tts-vol-range"
              type="range"
              min="0"
              max="1"
              step="0.05"
              :value="ttsVolume"
              @input="onTtsVolumeInput"
            />
          </div>
          <button
            type="button"
            class="btn secondary tts-download-btn"
            :disabled="isTtsDownloading"
            @click="downloadTtsAudio"
          >
            {{ isTtsDownloading ? '准备中…' : '下载音频' }}
          </button>
        </div>
      </div>
    </section>
    </div>
  </div>
</template>

<style scoped>
.voice-page {
  min-height: 100vh;
  width: 100%;
  margin: 0;
  padding: 48px 0 64px;
  box-sizing: border-box;
  color: #1f1f1f;
  background: linear-gradient(165deg, #fdfcfa 0%, #f3f0ea 48%, #ebe6de 100%);
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Segoe UI', sans-serif;
}

.voice-page-inner {
  max-width: 920px;
  margin: 0 auto;
  padding: 0 20px;
}

.hero {
  margin-bottom: 32px;
}

.eyebrow {
  margin: 0 0 8px;
  letter-spacing: 0.28em;
  font-size: 11px;
  text-transform: uppercase;
  color: #8a7a62;
}

.title {
  margin: 0 0 12px;
  font-size: clamp(26px, 4vw, 34px);
  font-weight: 600;
  letter-spacing: 0.02em;
  color: #14120f;
}

.lede {
  margin: 0;
  max-width: 640px;
  line-height: 1.7;
  color: #4a453d;
  font-size: 15px;
}

.card {
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(197, 181, 152, 0.35);
  border-radius: 18px;
  padding: 24px 22px 26px;
  margin-bottom: 20px;
  box-shadow: 0 18px 42px rgba(62, 52, 38, 0.08);
  backdrop-filter: blur(8px);
}

.main-card {
  border-color: rgba(184, 154, 108, 0.45);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.card-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: #221c16;
}

.card-head .card-title {
  margin: 0;
}

.hint {
  margin: 0 0 16px;
  font-size: 13px;
  color: #6d655a;
  line-height: 1.5;
}

.hint.soft {
  margin-top: -8px;
  margin-bottom: 12px;
}

.picked-voice {
  margin: 0 0 14px;
  font-size: 14px;
  color: #4a453d;
}

.picked-voice strong {
  color: #221c16;
}

.simple-field {
  margin-top: 16px;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-span {
  grid-column: 1 / -1;
}

.label {
  font-size: 13px;
  color: #5c5348;
}

.input {
  border-radius: 10px;
  border: 1px solid rgba(197, 181, 152, 0.55);
  padding: 10px 12px;
  font-size: 14px;
  color: #1f1f1f;
  background: #fffdf9;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.input:focus {
  border-color: rgba(150, 118, 72, 0.85);
  box-shadow: 0 0 0 3px rgba(184, 154, 108, 0.2);
}

.textarea {
  resize: vertical;
  min-height: 72px;
}

.select {
  appearance: none;
  background-image: linear-gradient(45deg, transparent 50%, #8a7a62 50%),
    linear-gradient(135deg, #8a7a62 50%, transparent 50%);
  background-position: calc(100% - 16px) calc(50% - 3px), calc(100% - 11px) calc(50% - 3px);
  background-size: 5px 5px, 5px 5px;
  background-repeat: no-repeat;
}

.result-block {
  margin-top: 12px;
}

.result-block .tts-player-wrap {
  margin-top: 12px;
}

.actions {
  margin-top: 18px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.actions.center {
  justify-content: center;
}

.btn {
  border-radius: 999px;
  padding: 10px 22px;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid transparent;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease, opacity 0.15s ease;
}

.btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.btn.primary {
  color: #fffdf7;
  background: linear-gradient(120deg, #b8925a, #8d6a3a);
  box-shadow: 0 10px 24px rgba(141, 106, 58, 0.28);
}

.btn.primary:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 30px rgba(141, 106, 58, 0.32);
}

.btn.secondary {
  color: #3d3428;
  border-color: rgba(184, 154, 108, 0.55);
  background: rgba(255, 255, 255, 0.85);
}

.btn.secondary:not(:disabled):hover {
  border-color: rgba(150, 118, 72, 0.9);
}

.btn.ghost {
  color: #5c5348;
  border-color: rgba(197, 181, 152, 0.55);
  background: transparent;
}

.btn.text {
  background: transparent;
  border: none;
  color: #8d6a3a;
  padding: 6px 10px;
}

.btn.text.danger {
  color: #a14d4d;
}

.alert {
  margin: 12px 0 0;
  color: #a14d4d;
  font-size: 14px;
}

.alert.tight {
  margin: 0 0 8px;
}

.success {
  margin: 0 0 8px;
  color: #4d6a4f;
  font-size: 14px;
}

.tts-player-wrap {
  margin-top: 18px;
}

.tts-audio-el {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.tts-player {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px 16px;
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(255, 253, 248, 0.98) 0%, rgba(245, 238, 226, 0.95) 100%);
  border: 1px solid rgba(197, 181, 152, 0.45);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.65), 0 10px 28px rgba(62, 52, 38, 0.06);
}

.tts-play-btn {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fffdf7;
  background: linear-gradient(145deg, #c9a066, #8d6a3a);
  box-shadow: 0 8px 20px rgba(141, 106, 58, 0.35);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.tts-play-btn:hover {
  transform: scale(1.04);
  box-shadow: 0 10px 26px rgba(141, 106, 58, 0.4);
}

.tts-play-btn:active {
  transform: scale(0.98);
}

.tts-icon {
  width: 26px;
  height: 26px;
  display: block;
}

.tts-player-main {
  flex: 1;
  min-width: 0;
}

.tts-progress-track {
  position: relative;
  height: 8px;
  border-radius: 999px;
  background: rgba(197, 181, 152, 0.35);
  cursor: pointer;
  outline: none;
}

.tts-progress-track:focus-visible {
  box-shadow: 0 0 0 3px rgba(184, 154, 108, 0.35);
}

.tts-progress-fill {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  border-radius: 999px;
  background: linear-gradient(90deg, #b8925a, #8d6a3a);
  pointer-events: none;
  transition: width 0.08s linear;
}

.tts-time-row {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  font-variant-numeric: tabular-nums;
  color: #5c5348;
}

.tts-time.muted {
  color: #9a8f80;
}

.tts-vol-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  color: #7a6f5f;
}

.tts-vol-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  opacity: 0.85;
}

.tts-vol-range {
  width: 88px;
  height: 4px;
  accent-color: #8d6a3a;
  cursor: pointer;
}

.tts-download-btn {
  flex-shrink: 0;
  margin-left: auto;
}

.empty {
  padding: 28px 12px;
  text-align: center;
  color: #7a6f5f;
  font-size: 14px;
}

.list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.list-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid rgba(197, 181, 152, 0.35);
}

.list-item:last-child {
  border-bottom: none;
}

.list-main {
  flex: 1;
  min-width: 0;
}

.voice-title {
  margin: 0 0 4px;
  font-size: 15px;
  font-weight: 600;
  color: #2c261c;
}

.meta {
  margin: 0;
  font-size: 12px;
  color: #7a6f5f;
}

.list-actions {
  display: flex;
  flex-shrink: 0;
  gap: 4px;
}

@media (max-width: 640px) {
  .field-grid {
    grid-template-columns: 1fr;
  }

  .tts-player {
    display: grid;
    grid-template-columns: auto 1fr;
    grid-template-areas:
      'play main'
      'vol vol'
      'dl dl';
    align-items: center;
    gap: 12px 14px;
  }

  .tts-play-btn {
    grid-area: play;
    align-self: start;
  }

  .tts-player-main {
    grid-area: main;
  }

  .tts-vol-wrap {
    grid-area: vol;
    width: 100%;
  }

  .tts-vol-range {
    flex: 1;
    width: auto;
    min-width: 0;
  }

  .tts-download-btn {
    grid-area: dl;
    margin-left: 0;
    width: 100%;
    justify-content: center;
  }
}
</style>
