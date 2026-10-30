const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

const voiceCloneDebug =
  import.meta.env.DEV || String(import.meta.env.VITE_VOICE_CLONE_DEBUG || '') === 'true'

/** @param {...unknown} args */
function vcLog(...args) {
  if (voiceCloneDebug) {
    console.log('[voiceclone]', ...args)
  }
}

/**
 * File/Blob → data URL 后取出纯 Base64（无 `data:*;base64,` 前缀），供 `POST .../clone/base64`。
 * @param {File|Blob} file
 */
function readFileAsBase64Payload(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = reader.result
      if (typeof result !== 'string') {
        reject(new Error('无法读取文件'))
        return
      }
      const i = result.indexOf(',')
      resolve(i >= 0 ? result.slice(i + 1) : result)
    }
    reader.onerror = () => reject(reader.error || new Error('读取文件失败'))
    reader.readAsDataURL(file)
  })
}

function isApiSuccess(code) {
  return code === 200 || code === '200'
}

function optionalAuthHeaders() {
  const key = import.meta.env.VITE_VOICE_API_KEY
  if (!key) {
    return {}
  }
  return { 'X-API-Key': key }
}

async function request(path, options = {}) {
  const headers = { ...optionalAuthHeaders(), ...(options.headers || {}) }
  const rawBody = options.body
  const isFormData = rawBody instanceof FormData
  const isPlainObject =
    rawBody &&
    typeof rawBody === 'object' &&
    !isFormData &&
    !(rawBody instanceof Blob) &&
    !(rawBody instanceof ArrayBuffer)

  let body = rawBody
  if (isPlainObject) {
    body = JSON.stringify(rawBody)
    if (!headers['Content-Type']) {
      headers['Content-Type'] = 'application/json'
    }
  }

  // multipart 必须由浏览器自动带 boundary；若误带 Content-Type（尤其 application/json）会导致 Spring 报 400
  if (isFormData) {
    delete headers['Content-Type']
    delete headers['content-type']
  }

  const method = options.method || 'GET'

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body,
  })

  if (!response.ok) {
    const errText = await response.text().catch(() => '')
    let detail = errText?.trim() || ''
    if (detail.startsWith('{')) {
      try {
        const j = JSON.parse(detail)
        detail = j.message || j.error || j.detail || detail
      } catch {
        /* keep raw */
      }
    }
    throw new Error(detail || `接口请求失败：${response.status}`)
  }

  const result = await response.json()

  if (!isApiSuccess(result?.code)) {
    throw new Error(result?.message || '接口返回异常')
  }

  return result
}

/**
 * 创建复刻音色：`POST /api/agent/v1/voices/clone/base64`（经 Nginx `location /api/agent/` → 后端 `/api/agent/...`）。
 * @param {File|Blob} file
 */
export async function createCloneVoice({ file, targetModel, preferredName, text, language }) {
  if (!(file instanceof Blob) || file.size <= 0) {
    throw new Error('请选择有效的音频文件')
  }

  const audioBase64 = await readFileAsBase64Payload(file)
  const fileName = file instanceof File && file.name ? file.name : 'audio.wav'

  vcLog('clone/base64', 'fileBytes=', file.size, 'base64Len=', audioBase64.length)

  return request('/api/agent/v1/voices/clone/base64', {
    method: 'POST',
    body: {
      audioBase64,
      fileName,
      targetModel: String(targetModel ?? ''),
      preferredName: String(preferredName ?? ''),
      text: text != null ? String(text) : '',
      language: language != null && String(language) !== '' ? String(language) : 'zh',
    },
  })
}

export function listCloneVoices(pageIndex = 0, pageSize = 10) {
  const params = new URLSearchParams({
    pageIndex: String(pageIndex),
    pageSize: String(pageSize),
  })
  return request(`/api/agent/v1/voices/clone?${params.toString()}`, {
    method: 'GET',
  })
}

export function deleteCloneVoice(voiceId) {
  const encoded = encodeURIComponent(voiceId)
  return request(`/api/agent/v1/voices/clone/${encoded}`, {
    method: 'DELETE',
  })
}

export function synthesizeTts({ model, voice, text }) {
  return request('/api/agent/v1/tts/synthesize', {
    method: 'POST',
    body: { model, voice, text },
  })
}

export { listSystemTtsVoices } from '../../../lib/systemTtsVoices.js'
