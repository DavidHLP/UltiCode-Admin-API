export enum LanguageType {
  JAVA,
  PYTHON,
  CPP,
  C,
  GO,
  JAVASCRIPT,
  TYPESCRIPT,
  RUST,
  KOTLIN,
  SCALA,
  RUBY,
  PHP,
  CSHARP,
}

// 后端 LanguageType 名称 -> Monaco 语言ID 映射
const LANGUAGE_TO_MONACO: Record<string, string> = {
  JAVA: 'java',
  PYTHON: 'python',
  CPP: 'cpp',
  C: 'c',
  GO: 'go',
  JAVASCRIPT: 'javascript',
  TYPESCRIPT: 'typescript',
  RUST: 'rust',
  KOTLIN: 'kotlin',
  SCALA: 'scala',
  RUBY: 'ruby',
  PHP: 'php',
  CSHARP: 'csharp',
}

/**
 * 将后端语言枚举名（如 'JAVA'）映射为 Monaco 语言ID（如 'java'）
 * 若未匹配，则降级为小写字符串
 */
export const toMonacoLanguage = (lang: string): string => {
  return LANGUAGE_TO_MONACO[lang] ?? lang.toLowerCase()
}

/**
 * 语言选项项：后端名称、显示名、Monaco 语言ID
 */
export interface LanguageOption {
  /** 后端枚举名称（如 'JAVA'） */
  name: string
  /** 展示名称（如 'Java'、'C++'、'C#'） */
  display: string
  /** Monaco 语言ID（如 'java'） */
  monaco: string
}

// 显示名映射（未命中则自动降级为首字母大写 + 其余小写）
const DISPLAY_NAME_MAP: Record<string, string> = {
  JAVA: 'Java',
  PYTHON: 'Python',
  CPP: 'C++',
  C: 'C',
  GO: 'Go',
  JAVASCRIPT: 'JavaScript',
  TYPESCRIPT: 'TypeScript',
  RUST: 'Rust',
  KOTLIN: 'Kotlin',
  SCALA: 'Scala',
  RUBY: 'Ruby',
  PHP: 'PHP',
  CSHARP: 'C#',
}

// 根据枚举定义顺序生成有序语言名称列表
const ORDERED_LANGUAGE_NAMES: string[] = Object.keys(LanguageType)
  .filter((k) => isNaN(Number(k)))
  .map((k) => k)

/**
 * 前端统一使用的语言选项列表（顺序与枚举一致）
 */
export const LANGUAGE_OPTIONS: LanguageOption[] = ORDERED_LANGUAGE_NAMES.map((name) => {
  const display = DISPLAY_NAME_MAP[name] ?? name.charAt(0) + name.slice(1).toLowerCase()
  const monaco = LANGUAGE_TO_MONACO[name] ?? name.toLowerCase()
  return { name, display, monaco }
})
