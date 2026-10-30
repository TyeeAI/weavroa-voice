const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

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

  const method = options.method || 'GET'

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body,
  })

  if (!response.ok) {
    throw new Error(`接口请求失败：${response.status}`)
  }

  const result = await response.json()

  if (!isApiSuccess(result?.code)) {
    throw new Error(result?.message || '接口返回异常')
  }

  return result
}

export function createDesignVoice(payload) {
  return request('/api/agent/v1/voices/design', {
    method: 'POST',
    body: payload,
  })
}

export function listDesignVoices(pageIndex = 0, pageSize = 10) {
  const params = new URLSearchParams({
    pageIndex: String(pageIndex),
    pageSize: String(pageSize),
  })
  return request(`/api/agent/v1/voices/design?${params.toString()}`, {
    method: 'GET',
  })
}

export function getDesignVoice(voiceId) {
  const encoded = encodeURIComponent(voiceId)
  return request(`/api/agent/v1/voices/design/${encoded}`, {
    method: 'GET',
  })
}

export function deleteDesignVoice(voiceId) {
  const encoded = encodeURIComponent(voiceId)
  return request(`/api/agent/v1/voices/design/${encoded}`, {
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
