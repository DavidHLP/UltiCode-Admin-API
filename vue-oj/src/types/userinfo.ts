export interface UserProfile {
  username: string
  handle: string
  avatar: string
  ranking: number
  location: string
  education: string
  gender: string
  school: string
  skills: string[]
}

export interface UserBioItem {
  icon: string
  content: string
}

export interface Achievement {
  id: string
  name: string
  icon: string
  value: string | number
  color: string
}

export interface ProgrammingLanguage {
  name: string
  color: string
  percentage: number
}

export interface SkillData {
  name: string
  level: string
  value: number
}

export interface StatCard {
  title: string
  value: string | number
  change: string
  percentage?: string
  type: 'heat' | 'solved' | 'ranking' | 'streak'
}

export interface ActivityDay {
  date: string
  level: 0 | 1 | 2 | 3 | 4
  count: number
}

export interface ProjectItem {
  id: string
  title: string
  description?: string
  time: string
  difficulty?: 'easy' | 'medium' | 'hard'
  tags?: string[]
}

export interface UserInfoData {
  profile: UserProfile
  bio: UserBioItem[]
  achievements: Achievement[]
  languages: ProgrammingLanguage[]
  skills: SkillData[]
  stats: StatCard[]
  activity: ActivityDay[]
  projects: ProjectItem[]
  tabs: Array<{
    key: string
    label: string
    icon: string
    active: boolean
  }>
}
