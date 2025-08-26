/**
 * 错误码映射配置
 * 统一后端ResponseCode与前端错误处理的映射关系
 */

// 后端响应码枚举（与Java后端保持一致）
export const RESPONSE_CODES = {
  // 成功响应
  SUCCESS: 200,

  // 系统级错误
  OPERATION_FAILED: 999,
  SERVICE_DEGRADED: 201,
  HOT_PARAM_LIMIT: 202,
  SYSTEM_RULE_NOT_SATISFIED: 203,
  AUTH_RULE_FAILED: 204,

  // 客户端错误
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  METHOD_NOT_ALLOWED: 405,

  // 服务器错误
  INTERNAL_SERVER_ERROR: 500,
  MATH_OPERATION_ERROR: 375,

  // 认证相关错误
  INVALID_TOKEN: 2001,
  EXPIRED_TOKEN: 2002,
  ACCESS_DENIED: 2003,
  CLIENT_AUTH_FAILED: 1001,
  USERNAME_PASSWORD_ERROR: 1002,
  UNSUPPORTED_GRANT_TYPE: 1003,
  BUSINESS_ERROR: 1004,
} as const

// 错误类型分类
export const ERROR_TYPES = {
  AUTH: 'auth',           // 认证相关
  PERMISSION: 'permission', // 权限相关
  VALIDATION: 'validation', // 参数验证
  BUSINESS: 'business',     // 业务逻辑
  SYSTEM: 'system',        // 系统错误
  NETWORK: 'network',      // 网络错误
} as const

// 错误码与错误类型的映射
export const ERROR_CODE_TYPE_MAP: Record<number, string> = {
  [RESPONSE_CODES.INVALID_TOKEN]: ERROR_TYPES.AUTH,
  [RESPONSE_CODES.EXPIRED_TOKEN]: ERROR_TYPES.AUTH,
  [RESPONSE_CODES.CLIENT_AUTH_FAILED]: ERROR_TYPES.AUTH,
  [RESPONSE_CODES.USERNAME_PASSWORD_ERROR]: ERROR_TYPES.AUTH,
  [RESPONSE_CODES.UNSUPPORTED_GRANT_TYPE]: ERROR_TYPES.AUTH,
  [RESPONSE_CODES.UNAUTHORIZED]: ERROR_TYPES.AUTH,

  [RESPONSE_CODES.ACCESS_DENIED]: ERROR_TYPES.PERMISSION,
  [RESPONSE_CODES.FORBIDDEN]: ERROR_TYPES.PERMISSION,
  [RESPONSE_CODES.AUTH_RULE_FAILED]: ERROR_TYPES.PERMISSION,

  [RESPONSE_CODES.BAD_REQUEST]: ERROR_TYPES.VALIDATION,
  [RESPONSE_CODES.METHOD_NOT_ALLOWED]: ERROR_TYPES.VALIDATION,

  [RESPONSE_CODES.BUSINESS_ERROR]: ERROR_TYPES.BUSINESS,

  [RESPONSE_CODES.INTERNAL_SERVER_ERROR]: ERROR_TYPES.SYSTEM,
  [RESPONSE_CODES.MATH_OPERATION_ERROR]: ERROR_TYPES.SYSTEM,
  [RESPONSE_CODES.OPERATION_FAILED]: ERROR_TYPES.SYSTEM,
  [RESPONSE_CODES.SERVICE_DEGRADED]: ERROR_TYPES.SYSTEM,
  [RESPONSE_CODES.HOT_PARAM_LIMIT]: ERROR_TYPES.SYSTEM,
  [RESPONSE_CODES.SYSTEM_RULE_NOT_SATISFIED]: ERROR_TYPES.SYSTEM,

  [RESPONSE_CODES.NOT_FOUND]: ERROR_TYPES.NETWORK,
}

// 错误码与用户友好消息的映射
export const ERROR_MESSAGE_MAP: Record<number, string> = {
  // 认证相关
  [RESPONSE_CODES.INVALID_TOKEN]: '登录状态异常，请重新登录',
  [RESPONSE_CODES.EXPIRED_TOKEN]: '登录已过期，请重新登录',
  [RESPONSE_CODES.CLIENT_AUTH_FAILED]: '客户端认证失败',
  [RESPONSE_CODES.USERNAME_PASSWORD_ERROR]: '用户名或密码错误',
  [RESPONSE_CODES.UNSUPPORTED_GRANT_TYPE]: '不支持的认证模式',
  [RESPONSE_CODES.UNAUTHORIZED]: '请先登录',

  // 权限相关
  [RESPONSE_CODES.ACCESS_DENIED]: '权限不足，请联系管理员',
  [RESPONSE_CODES.FORBIDDEN]: '权限不足，请联系管理员',
  [RESPONSE_CODES.AUTH_RULE_FAILED]: '权限验证失败，请联系管理员',

  // 参数验证
  [RESPONSE_CODES.BAD_REQUEST]: '请求参数错误，请检查输入',
  [RESPONSE_CODES.METHOD_NOT_ALLOWED]: '请求方法不被允许',

  // 业务逻辑
  [RESPONSE_CODES.BUSINESS_ERROR]: '业务处理失败，请稍后重试',

  // 系统错误
  [RESPONSE_CODES.INTERNAL_SERVER_ERROR]: '服务器内部错误，请稍后重试',
  [RESPONSE_CODES.MATH_OPERATION_ERROR]: '计算错误，请稍后重试',
  [RESPONSE_CODES.OPERATION_FAILED]: '操作失败，请稍后重试',
  [RESPONSE_CODES.SERVICE_DEGRADED]: '服务繁忙，请稍后重试',
  [RESPONSE_CODES.HOT_PARAM_LIMIT]: '访问频率过高，请稍后重试',
  [RESPONSE_CODES.SYSTEM_RULE_NOT_SATISFIED]: '系统繁忙，请稍后重试',

  // 网络相关
  [RESPONSE_CODES.NOT_FOUND]: '请求的资源不存在',
}

// 错误处理策略配置
export const ERROR_HANDLING_STRATEGY = {
  // 需要清除认证状态的错误码
  CLEAR_AUTH_CODES: [
    RESPONSE_CODES.INVALID_TOKEN,
    RESPONSE_CODES.EXPIRED_TOKEN,
    RESPONSE_CODES.CLIENT_AUTH_FAILED,
    RESPONSE_CODES.UNAUTHORIZED,
    // 将无权限也视为需要重新登录以避免循环跳转
    RESPONSE_CODES.ACCESS_DENIED,
    RESPONSE_CODES.FORBIDDEN,
  ],

  // 需要跳转到登录页的错误码
  REDIRECT_LOGIN_CODES: [
    RESPONSE_CODES.INVALID_TOKEN,
    RESPONSE_CODES.EXPIRED_TOKEN,
    RESPONSE_CODES.CLIENT_AUTH_FAILED,
    RESPONSE_CODES.UNAUTHORIZED,
    RESPONSE_CODES.ACCESS_DENIED,
    RESPONSE_CODES.FORBIDDEN,
  ],

  // 需要显示警告而非错误的错误码
  WARNING_CODES: [
    RESPONSE_CODES.INVALID_TOKEN,
    RESPONSE_CODES.EXPIRED_TOKEN,
    RESPONSE_CODES.ACCESS_DENIED,
    RESPONSE_CODES.FORBIDDEN,
    RESPONSE_CODES.SERVICE_DEGRADED,
    RESPONSE_CODES.HOT_PARAM_LIMIT,
    RESPONSE_CODES.SYSTEM_RULE_NOT_SATISFIED,
  ],

  // 不需要显示消息提示的错误码（静默处理）
  SILENT_CODES: [] as number[],
} as const

/**
 * 获取错误类型
 */
export const getErrorType = (code: number): string => {
  return ERROR_CODE_TYPE_MAP[code] || ERROR_TYPES.SYSTEM
}

/**
 * 获取用户友好的错误消息
 */
export const getErrorMessage = (code: number, defaultMessage?: string): string => {
  return ERROR_MESSAGE_MAP[code] || defaultMessage || '未知错误，请稍后重试'
}

/**
 * 检查是否需要清除认证状态
 */
export const shouldClearAuth = (code: number): boolean => {
  return (ERROR_HANDLING_STRATEGY.CLEAR_AUTH_CODES as readonly number[]).includes(code)
}

/**
 * 检查是否需要跳转到登录页
 */
export const shouldRedirectToLogin = (code: number): boolean => {
  return (ERROR_HANDLING_STRATEGY.REDIRECT_LOGIN_CODES as readonly number[]).includes(code)
}

/**
 * 检查是否应该显示为警告消息
 */
export const shouldShowWarning = (code: number): boolean => {
  return (ERROR_HANDLING_STRATEGY.WARNING_CODES as readonly number[]).includes(code)
}

/**
 * 检查是否应该静默处理（不显示消息）
 */
export const shouldHandleSilently = (code: number): boolean => {
  return ERROR_HANDLING_STRATEGY.SILENT_CODES.includes(code)
}

/**
 * 统一错误处理函数
 */
export interface ErrorHandlingResult {
  type: string
  message: string
  shouldClearAuth: boolean
  shouldRedirectToLogin: boolean
  shouldShowWarning: boolean
  shouldHandleSilently: boolean
}

export const handleError = (code: number, originalMessage?: string): ErrorHandlingResult => {
  return {
    type: getErrorType(code),
    message: getErrorMessage(code, originalMessage),
    shouldClearAuth: shouldClearAuth(code),
    shouldRedirectToLogin: shouldRedirectToLogin(code),
    shouldShowWarning: shouldShowWarning(code),
    shouldHandleSilently: shouldHandleSilently(code),
  }
}
