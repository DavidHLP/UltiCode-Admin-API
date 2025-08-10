export type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

export const getDifficultyTagType = (difficulty: string): TagType => {
  switch (difficulty) {
    case 'EASY':
      return 'success'
    case 'MEDIUM':
      return 'warning'
    case 'HARD':
      return 'danger'
    default:
      return 'info'
  }
}

export const getDifficultyChinese = (difficulty: string): string => {
  switch (difficulty) {
    case 'EASY':
      return '简单'
    case 'MEDIUM':
      return '中等'
    case 'HARD':
      return '困难'
    default:
      return '未知'
  }
}
