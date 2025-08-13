declare module 'splitpanes' {
  import type { DefineComponent } from 'vue'
  export const Splitpanes: DefineComponent<Record<string, unknown>, Record<string, unknown>, unknown>
  export const Pane: DefineComponent<Record<string, unknown>, Record<string, unknown>, unknown>
  const _default: {
    Splitpanes: typeof Splitpanes
    Pane: typeof Pane
  }
  export default _default
}
