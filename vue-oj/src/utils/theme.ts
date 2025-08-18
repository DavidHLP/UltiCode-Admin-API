export type ThemeColors = {
  primary: string
  success: string
  warning: string
  danger: string
  info: string
  text: string
  subText: string
  border: string
  neutral: string
}

const fallback: ThemeColors = {
  primary: '#409EFF',
  success: '#67C23A',
  warning: '#E6A23C',
  danger: '#F56C6C',
  info: '#909399',
  text: '#111827',
  subText: '#6B7280',
  border: '#E5E7EB',
  neutral: '#E5E7EB',
}

export function getThemeColors(doc: Document = document): ThemeColors {
  try {
    const styles = getComputedStyle(doc.documentElement)
    const get = (name: string, fb: string) => styles.getPropertyValue(name)?.trim() || fb
    // Element Plus CSS variables
    return {
      primary: get('--el-color-primary', fallback.primary),
      success: get('--el-color-success', fallback.success),
      warning: get('--el-color-warning', fallback.warning),
      danger: get('--el-color-error', fallback.danger),
      info: get('--el-color-info', fallback.info),
      text: get('--el-text-color-primary', fallback.text),
      subText: get('--el-text-color-secondary', fallback.subText),
      border: get('--el-border-color', fallback.border),
      neutral: get('--el-fill-color-light', fallback.neutral),
    }
  } catch {
    return fallback
  }
}

export function makeLinearGradient(color: string): any {
  // ECharts Linear Gradient helper (top -> bottom)
  return {
    type: 'linear',
    x: 0,
    y: 0,
    x2: 0,
    y2: 1,
    colorStops: [
      { offset: 0, color },
      { offset: 1, color: `${color}cc` },
    ],
  }
}
