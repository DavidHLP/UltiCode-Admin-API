import type { UserInfoData } from '@/types/userinfo'

export const mockUserInfoData: UserInfoData = {
  profile: {
    username: 'David',
    handle: 'github.com/david',
    avatar: 'D',
    ranking: 100000,
    location: '重庆',
    education: '博士',
    gender: '男',
    school: '深圳信息职业学院',
    skills: ['Java', 'Python']
  },
  bio: [
    { icon: '📍', content: '重庆' },
    { icon: '🏢', content: '博士' },
    { icon: '👤', content: '男' },
    { icon: '🎓', content: '深圳信息职业学院' },
    { icon: '💼', content: 'Java, Python' }
  ],
  achievements: [
    {
      id: 'contribution',
      name: '贡献徽章 青木',
      icon: '🏆',
      value: '青木',
      color: '#58a6ff'
    },
    {
      id: 'activity',
      name: '活动徽章',
      icon: '📊',
      value: 176,
      color: '#39d353'
    },
    {
      id: 'streak',
      name: '连续徽章',
      icon: '🔥',
      value: 1,
      color: '#fd7e14'
    },
    {
      id: 'opensource',
      name: '开源徽章',
      icon: '⭐',
      value: 0,
      color: '#f85149'
    }
  ],
  languages: [
    { name: 'JavaScript', color: '#f1e05a', percentage: 35 },
    { name: 'Python', color: '#3572a5', percentage: 30 },
    { name: 'HTML', color: '#e34c26', percentage: 20 },
    { name: 'CSS', color: '#563d7c', percentage: 15 }
  ],
  skills: [
    { name: '算法', level: '一分靠运气', value: 60 },
    { name: '数学', level: '只剩脑', value: 75 },
    { name: '计算', level: '还能行', value: 80 },
    { name: '前端', level: '熟练', value: 85 },
    { name: '后端', level: '精通', value: 90 },
    { name: '数据库', level: '良好', value: 70 }
  ],
  stats: [
    {
      title: '累计热度',
      value: 1557,
      change: '218.7% ↑ 51,127 平均排名',
      percentage: '34.67%',
      type: 'heat'
    },
    {
      title: '已解题数',
      value: 262,
      change: '本月新增 12 题',
      type: 'solved'
    },
    {
      title: '全站排名',
      value: 15432,
      change: '↑ 1,234',
      type: 'ranking'
    },
    {
      title: '最长连击',
      value: 45,
      change: '当前连击 12 天',
      type: 'streak'
    }
  ],
  activity: generateActivityData(),
  projects: [
    {
      id: '1',
      title: '100501. 仅含置位的所有非零数',
      time: '9 个月前',
      difficulty: 'hard',
      tags: ['位运算', '数学']
    },
    {
      id: '2',
      title: '3386. 插入间隔',
      time: '9 个月前',
      difficulty: 'medium',
      tags: ['数组', '排序']
    },
    {
      id: '3',
      title: '3385. 重新安排数组里的数字并删除子字符串',
      time: '9 个月前',
      difficulty: 'hard',
      tags: ['字符串', '动态规划']
    },
    {
      id: '4',
      title: '3341. 判定是否一个词的问题没定义！',
      time: '9 个月前',
      difficulty: 'easy',
      tags: ['字符串']
    },
    {
      id: '5',
      title: '3340. 检查平衡字符串',
      time: '9 个月前',
      difficulty: 'medium',
      tags: ['字符串', '哈希表']
    },
    {
      id: '6',
      title: '3335. 字符串的原生长度！',
      time: '10 个月前',
      difficulty: 'easy',
      tags: ['字符串']
    },
    {
      id: '7',
      title: '3336. 字符串的原生长度！',
      time: '10 个月前',
      difficulty: 'medium',
      tags: ['字符串', '前缀和']
    },
    {
      id: '8',
      title: '3326. 改数组进来的数字就是和子数组个数',
      time: '10 个月前',
      difficulty: 'hard',
      tags: ['数组', '前缀和']
    }
  ],
  tabs: [
    { key: 'problems', label: '最近温题', icon: '📊', active: true },
    { key: 'solutions', label: '题解', icon: '📝', active: false },
    { key: 'collections', label: '题单', icon: '📋', active: false },
    { key: 'discussions', label: '讨论发表', icon: '🔗', active: false }
  ]
}

function generateActivityData() {
  const data = []
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 365)
  
  for (let i = 0; i < 365; i++) {
    const date = new Date(startDate)
    date.setDate(date.getDate() + i)
    
    const activity = Math.random()
    let level: 0 | 1 | 2 | 3 | 4 = 0
    let count = 0
    
    if (activity > 0.8) {
      level = 4
      count = Math.floor(Math.random() * 10) + 10
    } else if (activity > 0.6) {
      level = 3
      count = Math.floor(Math.random() * 8) + 5
    } else if (activity > 0.3) {
      level = 2
      count = Math.floor(Math.random() * 5) + 2
    } else if (activity > 0.1) {
      level = 1
      count = 1
    }
    
    data.push({
      date: date.toISOString().split('T')[0],
      level,
      count
    })
  }
  
  return data
}
