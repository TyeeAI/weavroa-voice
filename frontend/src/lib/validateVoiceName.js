/**
 * 清洗/校验复刻音色名称：仅保留字母数字下划线与中文，最长 32 字。
 * @param {string} name
 * @returns {{ valid: boolean, message?: string, cleaned?: string, suggestion?: string }}
 */
export function validateVoiceName(name) {
  const raw = typeof name === 'string' ? name : ''
  const cleaned = raw.replace(/[^a-zA-Z0-9_\u4e00-\u9fa5]/g, '')

  if (cleaned.length === 0) {
    return { valid: false, message: '音色名称不能全为特殊字符' }
  }

  if (cleaned.length > 32) {
    return {
      valid: false,
      message: `音色名称过长（${cleaned.length} 字符），请使用已截断至 32 字内的名称后再次提交`,
      suggestion: cleaned.substring(0, 32),
    }
  }

  if (cleaned !== raw) {
    return {
      valid: true,
      message: '已自动移除非法字符',
      cleaned,
    }
  }

  return { valid: true, cleaned: raw }
}
