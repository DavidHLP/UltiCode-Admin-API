/// <reference types="vite/client" />
interface ImportMetaEnv {
  // 用户头像
  readonly VITE_DEFAULT_USER_AVATAR: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
