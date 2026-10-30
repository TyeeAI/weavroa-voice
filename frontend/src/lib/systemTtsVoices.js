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

/**
 * GET /api/agent/v1/tts/voices — 系统预设音色列表（与 voice 模块一致走 `/api/agent/` 前缀）
 * @returns {Promise<Array<{ voice: string, name: string, description?: string, gender?: string, supportedModels?: string[] }>>}
 */
export async function listSystemTtsVoices() {
  const response = await fetch(`${API_BASE_URL}/api/agent/v1/tts/voices`, {
    method: 'GET',
    headers: { ...optionalAuthHeaders() },
  })

  if (!response.ok) {
    throw new Error(`系统音色列表请求失败：${response.status}`)
  }

  const result = await response.json()

  if (!isApiSuccess(result?.code)) {
    throw new Error(result?.message || '接口返回异常')
  }

  const voices = result?.data?.voices
  return Array.isArray(voices) ? voices : []
}
