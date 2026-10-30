const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

function buildAuthHeaders() {
  const key = import.meta.env.VITE_VOICE_API_KEY
  if (!key) {
    return {}
  }
  return { 'X-API-Key': key }
}

/**
 * 整段 Base64 解码（勿对「被任意截断的」子串分别 atob 再拼 Uint8Array，会破坏 4 字符分组）。
 * 去掉空白、URL-safe、data: 前缀后，直接 atob；不在超长串上做整串正则（易误判或极慢）。
 */
function decodeBase64ToBytes(b64) {
  if (typeof b64 !== 'string' || !b64.length) {
    return null
  }
  let s = b64.trim()
  const dataUrlIdx = s.indexOf('base64,')
  if (s.startsWith('data:') && dataUrlIdx !== -1) {
    s = s.slice(dataUrlIdx + 7)
  }
  s = s.replace(/\s/g, '')
  s = s.replace(/-/g, '+').replace(/_/g, '/')
  const pad = (4 - (s.length % 4)) % 4
  if (pad) {
    s += '='.repeat(pad)
  }
  try {
    const binary = atob(s)
    const out = new Uint8Array(binary.length)
    for (let i = 0; i < binary.length; i += 1) {
      out[i] = binary.charCodeAt(i)
    }
    return out
  } catch {
    return null
  }
}

function concatUint8Arrays(parts) {
  let total = 0
  for (const p of parts) {
    total += p.length
  }
  const out = new Uint8Array(total)
  let offset = 0
  for (const p of parts) {
    out.set(p, offset)
    offset += p.length
  }
  return out
}

/** 对合并后的完整缓冲区判断 MIME */
function sniffAudioMimeFromBuffer(u8) {
  if (!u8?.length) {
    return 'audio/mpeg'
  }
  if (u8.length >= 4 && u8[0] === 0x52 && u8[1] === 0x49 && u8[2] === 0x46 && u8[3] === 0x46) {
    return 'audio/wav'
  }
  if (u8.length >= 4 && u8[0] === 0x4f && u8[1] === 0x67 && u8[2] === 0x67 && u8[3] === 0x53) {
    return 'audio/ogg'
  }
  if (u8.length >= 4 && u8[0] === 0x1a && u8[1] === 0x45 && u8[2] === 0xdf && u8[3] === 0xa3) {
    return 'audio/webm'
  }
  if (u8.length >= 3 && u8[0] === 0x49 && u8[1] === 0x44 && u8[2] === 0x33) {
    return 'audio/mpeg'
  }
  if (u8.length >= 2 && u8[0] === 0xff && (u8[1] & 0xe0) === 0xe0) {
    return 'audio/mpeg'
  }
  const scan = Math.min(u8.length, 65536)
  for (let i = 0; i < scan - 1; i += 1) {
    if (u8[i] === 0xff && (u8[i + 1] & 0xe0) === 0xe0) {
      return 'audio/mpeg'
    }
  }
  if (u8.length >= 8 && u8[4] === 0x66 && u8[5] === 0x74 && u8[6] === 0x79 && u8[7] === 0x70) {
    return 'audio/mp4'
  }
  return 'audio/mpeg'
}

function tryParseJsonString(str) {
  if (typeof str !== 'string') {
    return null
  }
  const t = str.trim()
  if (!t.startsWith('{')) {
    return null
  }
  try {
    return JSON.parse(t)
  } catch {
    return null
  }
}

/** 合并解码后的整段是否为可播放（拒绝空 WAV 头） */
function isDecodedAudioPlayable(u8) {
  if (!u8 || u8.length < 32) {
    return false
  }
  if (u8[0] === 0x52 && u8[1] === 0x49 && u8[2] === 0x46 && u8[3] === 0x46) {
    let off = 12
    while (off + 8 <= u8.length) {
      const id = String.fromCharCode(u8[off], u8[off + 1], u8[off + 2], u8[off + 3])
      const size = u8[off + 4] | (u8[off + 5] << 8) | (u8[off + 6] << 16) | (u8[off + 7] << 24)
      if (id === 'data') {
        return size > 0
      }
      off += 8 + size
    }
    return false
  }
  return true
}

/**
 * 从一条 SSE JSON 里取出「要拼进总 Base64 的」子串（不在这里 decode）。
 * 兼容：
 * - 扁平：{ type:'session_started'|'session_completed'|'audio_chunk', data?, text? }
 * - 外层：{ data: 字符串, isLast, timestamp }，其中 data 为内层 JSON 字符串或裸 Base64 分片
 * 注意：勿因 isLast:true 丢弃 data；不少后端在最后一帧同时带音频与 isLast。
 * 与「else push(data.data)」对齐：除 session 控制帧外，凡 data 为非 JSON 的字符串均视为音频片。
 */
function extractAudioBase64Fragment(payload) {
  if (!payload) {
    return null
  }

  const topType = typeof payload.type === 'string' ? payload.type : ''
  if (topType === 'session_started' || topType === 'session_completed') {
    return null
  }
  if (topType === 'audio_chunk' && typeof payload.data === 'string') {
    const d = payload.data.trim()
    if (d.length && !d.startsWith('{')) {
      return d
    }
  }

  if (typeof payload.data === 'string' && payload.data.length) {
    const inner = tryParseJsonString(payload.data)
    if (inner && typeof inner === 'object') {
      const it = typeof inner.type === 'string' ? inner.type : ''
      if (it === 'session_started' || it === 'session_completed') {
        return null
      }
      if (it === 'audio_chunk' && typeof inner.data === 'string') {
        return inner.data
      }
      if (typeof inner.data === 'string') {
        const id = inner.data.trim()
        if (id.length && !id.startsWith('{')) {
          return inner.data
        }
      }
      return null
    }
  }

  if (typeof payload.data === 'string') {
    const t = payload.data.trim()
    if (t.length && !t.startsWith('{')) {
      return t
    }
  }

  return null
}

/**
 * POST /api/agent/v1/tts/synthesize/stream（SSE），经网关 `/api/agent/` 转发。
 * 将各事件里的 Base64 **字符串按顺序拼接后一次性 decode**（勿对切段分别 atob 再拼字节）。
 * 若你的网关只支持 GET，可设 VITE_TTS_STREAM_METHOD=GET 改用查询参数。
 * @param {{ model: string, voice: string, text: string, signal?: AbortSignal, onProgress?: (info: { chunks: number }) => void }} params
 * @returns {Promise<{ blobUrl: string, mime: string, revoke: () => void }>}
 */
export async function synthesizeTtsStreamPost({ model, voice, text, signal, onProgress }) {
  const useGet = String(import.meta.env.VITE_TTS_STREAM_METHOD || '').toUpperCase() === 'GET'
  const response = useGet
    ? await fetch(
        `${API_BASE_URL}/api/agent/v1/tts/synthesize/stream?${new URLSearchParams({
          model: String(model || ''),
          voice: String(voice || ''),
          text: String(text || ''),
        }).toString()}`,
        {
          method: 'GET',
          headers: {
            Accept: 'text/event-stream',
            ...buildAuthHeaders(),
          },
          signal,
        },
      )
    : await fetch(`${API_BASE_URL}/api/agent/v1/tts/synthesize/stream`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Accept: 'text/event-stream',
          ...buildAuthHeaders(),
        },
        body: JSON.stringify({ model, voice, text }),
        signal,
      })

  if (!response.ok) {
    const errText = await response.text().catch(() => '')
    throw new Error(errText?.trim() || `流式接口请求失败：${response.status}`)
  }

  const reader = response.body?.getReader()
  if (!reader) {
    throw new Error('当前环境不支持 ReadableStream，无法读取流式响应')
  }

  const decoder = new TextDecoder()
  let buffer = ''
  const base64Fragments = []

  const consumePayload = (payload) => {
    if (payload?.error) {
      throw new Error(String(payload.error))
    }
    if (payload?.message != null && payload?.code != null) {
      const c = Number(payload.code)
      if (!Number.isNaN(c) && c !== 200 && c !== 0) {
        throw new Error(String(payload.message))
      }
    }
    if (payload?.success === false && payload?.message) {
      throw new Error(String(payload.message))
    }

    const frag = extractAudioBase64Fragment(payload)
    if (frag) {
      base64Fragments.push(frag)
      onProgress?.({ chunks: base64Fragments.length })
    }
  }

  const processLine = (line) => {
    const trimmed = line.trim()
    if (!trimmed) {
      return
    }
    let raw
    if (trimmed.startsWith('data:')) {
      raw = trimmed.startsWith('data: ') ? trimmed.slice(6) : trimmed.slice(5).trimStart()
    } else if (trimmed.startsWith('{')) {
      raw = trimmed
    } else {
      return
    }
    if (!raw || raw === '[DONE]') {
      return
    }
    let payload
    try {
      payload = JSON.parse(raw)
    } catch {
      return
    }
    consumePayload(payload)
  }

  while (true) {
    const { done, value } = await reader.read()
    if (done) {
      break
    }
    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split(/\r?\n/)
    buffer = lines.pop() ?? ''
    for (const line of lines) {
      processLine(line)
    }
  }

  buffer += decoder.decode(new Uint8Array())
  if (buffer.trim()) {
    for (const line of buffer.split(/\r?\n/)) {
      processLine(line)
    }
  }

  const mergedB64 = base64Fragments.map((s) => String(s).replace(/\s/g, '')).join('')
  if (!mergedB64.length) {
    throw new Error(
      '流式结果里没有可播放的音频（常见原因：服务端只下发了空占位片段）。请优先点击「生成语音」；若仍需要流式，请联系后端确认是否下发完整音频分片。',
    )
  }

  /** 先按「整段 Base64 被切开」拼接后解码；失败再按「每帧独立 Base64」逐段解码拼二进制 */
  let merged = decodeBase64ToBytes(mergedB64)
  if (!merged || !isDecodedAudioPlayable(merged)) {
    const parts = []
    for (const frag of base64Fragments) {
      const p = decodeBase64ToBytes(String(frag))
      if (p?.length) {
        parts.push(p)
      }
    }
    merged = parts.length ? concatUint8Arrays(parts) : null
  }
  if (!merged || !isDecodedAudioPlayable(merged)) {
    throw new Error(
      '流式音频 Base64 解码失败或内容无效。请确认接口返回格式；可尝试「生成语音」或检查 model/voice 参数。',
    )
  }

  const mime = sniffAudioMimeFromBuffer(merged)
  const blob = new Blob([merged], { type: mime })
  const blobUrl = URL.createObjectURL(blob)
  return {
    blobUrl,
    mime,
    revoke: () => URL.revokeObjectURL(blobUrl),
  }
}
