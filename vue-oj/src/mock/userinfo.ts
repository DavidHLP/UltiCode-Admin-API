import type { DeveloperProfile } from '@/types/userinfo'

/**
 * 生成贡献热力图数据
 */
function generateContributionHeatmap() {
  const contributions = []
  const now = new Date()
  const oneYearAgo = new Date(now.getFullYear() - 1, now.getMonth(), now.getDate())

  for (let d = new Date(oneYearAgo); d <= now; d.setDate(d.getDate() + 1)) {
    const random = Math.random()
    let level: 0 | 1 | 2 | 3 | 4 = 0

    if (random > 0.85) level = 4
    else if (random > 0.7) level = 3
    else if (random > 0.5) level = 2
    else if (random > 0.3) level = 1

    contributions.push({
      date: d.toISOString().split('T')[0],
      count: level * Math.floor(Math.random() * 5),
      level
    })
  }

  return contributions
}

/**
 * 生成贡献趋势图数据
 */
function generateContributionChart() {
  const points = []
  for (let i = 0; i < 12; i++) {
    const date = new Date(2023, i, 1)
    points.push({
      date: date.toISOString().split('T')[0],
      value: Math.floor(Math.random() * 200) + 50
    })
  }
  return points
}

/**
 * 开发者档案 Mock 数据
 */
export const mockDeveloperProfile: DeveloperProfile = {
  userProfile: {
    username: 'David',
    handle: '@david.yang@xm',
    avatar: { type: 'char', value: 'D' },
    globalRank: 100000,
    bio: '无',
    location: '重庆',
    gender: '男',
    school: '洛阳师范学院',
    primaryLanguages: ['Java', 'Python'],
    isFollowing: false
  },

  achievements: [
    {
      name: '贡献奖章',
      icon: '🏆',
      iconColor: '#fd7e14',
      count: 1,
      type: 'contribution'
    },
    {
      name: '点赞奖章',
      icon: '💙',
      iconColor: '#58a6ff',
      count: 177,
      type: 'like'
    },
    {
      name: '评论奖章',
      icon: '💬',
      iconColor: '#39d353',
      count: 1,
      type: 'comment'
    },
    {
      name: '获得收藏',
      icon: '⭐',
      iconColor: '#fd7e14',
      count: 0,
      type: 'favorite'
    }
  ],

  languageSkills: [
    {
      name: 'Java',
      solvedCount: 278,
      color: '#58a6ff'
    },
    {
      name: 'Python3',
      solvedCount: 11,
      color: '#39d353'
    },
    {
      name: 'C++',
      solvedCount: 5,
      color: '#fd7e14'
    }
  ],

  skillTags: [
    {
      name: '前端开发',
      level: 'intermediate',
      backgroundColor: '#238636'
    },
    {
      name: '设计',
      level: 'beginner',
      backgroundColor: '#238636'
    }
  ],

  foundationSkill: {
    categories: ['数组', '二分查找', '计算', '哈希表', '贪心', '堆优先队列', '滑动窗口', '栈', '递归', '模拟', '体系'],
    skillPoints: [
      { name: '数组', value: 85, color: '#fd7e14' },
      { name: '二分查找', value: 70, color: '#238636' },
      { name: '计算', value: 90, color: '#58a6ff' },
      { name: '哈希表', value: 75, color: '#fd7e14' },
      { name: '贪心', value: 60, color: '#238636' },
      { name: '堆优先队列', value: 65, color: '#58a6ff' }
    ]
  },

  statistics: {
    contributionScore: 1557,
    globalRanking: {
      current: 51127,
      total: 219796,
      percentage: 34.67
    },
    topPercentage: 34.67
  },

  contributionChart: generateContributionChart(),

  contributionHeatmap: {
    year: 2024,
    dailyContributions: generateContributionHeatmap(),
    totalContributions: 282,
    yearContributions: 104
  },

  activities: [
    {
      id: '1',
      type: 'solution',
      icon: '📝',
      text: '10050! 夜宴哪位的新小菜谱',
      time: '2024-02-21T10:30:00Z',
      relativeTime: '9个月前'
    },
    {
      id: '2',
      type: 'solution',
      icon: '📝',
      text: '3366. 最小数组和',
      time: '2024-02-20T15:45:00Z',
      relativeTime: '9个月前'
    },
    {
      id: '3',
      type: 'solution',
      icon: '📝',
      text: '3365. 重新排列后的最大子数组大小',
      time: '2024-02-19T09:20:00Z',
      relativeTime: '9个月前'
    },
    {
      id: '4',
      type: 'solution',
      icon: '📝',
      text: '3341. 找出符合条件路径的最小代价',
      time: '2024-01-15T14:10:00Z',
      relativeTime: '10个月前'
    },
    {
      id: '5',
      type: 'solution',
      icon: '📝',
      text: '3340. 检查是否平行令符',
      time: '2024-01-14T11:25:00Z',
      relativeTime: '10个月前'
    },
    {
      id: '6',
      type: 'solution',
      icon: '📝',
      text: '3335. 字符串的K长度子串 I',
      time: '2024-01-13T16:40:00Z',
      relativeTime: '10个月前'
    },
    {
      id: '7',
      type: 'solution',
      icon: '📝',
      text: '3326. 敵戦哨斥候的最小操作秒数据',
      time: '2024-01-12T13:15:00Z',
      relativeTime: '10个月前'
    },
    {
      id: '8',
      type: 'solution',
      icon: '📝',
      text: '3325. 字符串至少需要 K 次字符的操作 I',
      time: '2024-01-11T12:30:00Z',
      relativeTime: '10个月前'
    },
    {
      id: '9',
      type: 'solution',
      icon: '📝',
      text: '1456. 定长子串中元音的最大数目',
      time: '2024-01-10T08:45:00Z',
      relativeTime: '10个月前'
    },
    {
      id: '10',
      type: 'solution',
      icon: '📝',
      text: '4296. 移山所需的最少秒数',
      time: '2023-08-21T10:20:00Z',
      relativeTime: '1年前'
    },
    {
      id: '11',
      type: 'solution',
      icon: '📝',
      text: '3281. 范围内整数的最大得分',
      time: '2023-08-20T17:35:00Z',
      relativeTime: '1年前'
    }
  ],

  metricCards: [
    {
      name: '数据统计',
      value: 2,
      description: '数据统计',
      icon: '📊',
      iconBgColor: '#58a6ff'
    },
    {
      name: '天成就徽章',
      value: 100,
      description: '天成就徽章',
      icon: '⚡',
      iconBgColor: '#fd7e14'
    }
  ],

  activityTabs: [
    {
      key: 'overview',
      name: '概况通知',
      icon: '📋',
      active: false
    },
    {
      key: 'solutions',
      name: '题解',
      icon: '🔔',
      active: false
    },
    {
      key: 'qa',
      name: '问答',
      icon: '💬',
      active: false
    },
    {
      key: 'dynamic',
      name: '动态条件',
      icon: '🔍',
      active: true
    }
  ]
}

/**
 * 模拟 API 调用延迟
 */
export function mockApiDelay(ms = 1000): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 获取开发者档案数据 (模拟 API)
 */
export async function getMockDeveloperProfile(): Promise<DeveloperProfile> {
  await mockApiDelay()
  return { ...mockDeveloperProfile }
}
