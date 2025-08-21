// 开发者档案相关类型定义

/** 用户基本信息 */
export interface UserProfile {
  /** 用户名 */
  username: string
  /** 用户句柄 */
  handle: string
  /** 头像（如果是字符则显示字符，否则为图片URL） */
  avatar: string | { type: 'char'; value: string } | { type: 'url'; value: string }
  /** 全球排名 */
  globalRank: number
  /** 个人简介 */
  bio?: string
  /** 位置信息 */
  location: string
  /** 性别 */
  gender: string
  /** 学校 */
  school: string
  /** 主要编程语言 */
  primaryLanguages: string[]
  /** 关注状态 */
  isFollowing: boolean
}

/** 成就奖章 */
export interface Achievement {
  /** 奖章名称 */
  name: string
  /** 图标 */
  icon: string
  /** 图标颜色 */
  iconColor: string
  /** 奖章数量 */
  count: number
  /** 奖章类型 */
  type: 'contribution' | 'like' | 'comment' | 'favorite'
}

/** 编程语言技能 */
export interface LanguageSkill {
  /** 语言名称 */
  name: string
  /** 解决题目数量 */
  solvedCount: number
  /** 语言颜色（用于显示） */
  color: string
}

/** 技能标签 */
export interface SkillTag {
  /** 技能名称 */
  name: string
  /** 技能级别 */
  level: 'beginner' | 'intermediate' | 'advanced'
  /** 背景色 */
  backgroundColor: string
}

/** 基础实力数据 */
export interface FoundationSkill {
  /** 技能分类 */
  categories: string[]
  /** 技能点（用于雷达图显示） */
  skillPoints: Array<{
    name: string
    value: number
    color: string
  }>
}

/** 统计数据 */
export interface StatisticsData {
  /** 贡献分数 */
  contributionScore: number
  /** 全球排名 */
  globalRanking: {
    current: number
    total: number
    percentage: number
  }
  /** Top 百分比 */
  topPercentage: number
}

/** 贡献图表数据点 */
export interface ContributionPoint {
  /** 日期 */
  date: string
  /** 贡献值 */
  value: number
}

/** 贡献热力图数据 */
export interface ContributionHeatmap {
  /** 年份 */
  year: number
  /** 每日贡献数据 */
  dailyContributions: Array<{
    date: string
    count: number
    level: 0 | 1 | 2 | 3 | 4 // 贡献强度等级
  }>
  /** 总贡献次数 */
  totalContributions: number
  /** 去年贡献次数 */
  yearContributions: number
}

/** 活动项 */
export interface ActivityItem {
  /** 活动ID */
  id: string
  /** 活动类型 */
  type: 'solution' | 'question' | 'answer' | 'notification'
  /** 活动图标 */
  icon: string
  /** 活动文本 */
  text: string
  /** 活动时间 */
  time: string
  /** 相对时间显示 */
  relativeTime: string
}

/** 指标卡片数据 */
export interface MetricCard {
  /** 指标名称 */
  name: string
  /** 指标值 */
  value: number | string
  /** 指标描述 */
  description: string
  /** 指标图标 */
  icon: string
  /** 图标背景色 */
  iconBgColor: string
}

/** 活动标签页 */
export interface ActivityTab {
  /** 标签键 */
  key: string
  /** 标签名称 */
  name: string
  /** 标签图标 */
  icon: string
  /** 是否激活 */
  active: boolean
}

/** 完整的开发者档案数据 */
export interface DeveloperProfile {
  /** 用户基本信息 */
  userProfile: UserProfile
  /** 成就奖章列表 */
  achievements: Achievement[]
  /** 编程语言技能 */
  languageSkills: LanguageSkill[]
  /** 技能标签 */
  skillTags: SkillTag[]
  /** 基础实力 */
  foundationSkill: FoundationSkill
  /** 统计数据 */
  statistics: StatisticsData
  /** 贡献图表数据 */
  contributionChart: ContributionPoint[]
  /** 贡献热力图 */
  contributionHeatmap: ContributionHeatmap
  /** 活动列表 */
  activities: ActivityItem[]
  /** 指标卡片 */
  metricCards: MetricCard[]
  /** 活动标签页 */
  activityTabs: ActivityTab[]
}

/** ECharts 配置类型 */
export interface ChartOption {
  [key: string]: unknown
}

/** 组件状态 */
export interface ComponentState {
  /** 当前活动标签 */
  activeTab: string
  /** 加载状态 */
  loading: boolean
  /** 错误信息 */
  error: string | null
}
