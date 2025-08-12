// 与后端实体对齐的最小类型集合
export interface TestCaseInput {
  id?: number
  testCaseOutputId?: number
  testCaseName: string
  // 与后端枚举 InputType 的 name 对齐，如 INT、STRING 等
  inputType?: string
  inputContent: string
  orderIndex: number
}

export interface TestCaseOutput {
  id?: number
  problemId: number
  output: string
  score?: number
  isSample?: boolean
  outputType?: string
}

// 后端 TestCase
export interface TestCase {
  id: number
  problemId: number
  testCaseOutput: TestCaseOutput
  testCaseInput: TestCaseInput[]
}

export  const InputType = [
  'STRING',
  'CHAR',
  'BOOLEAN',
  'BYTE',
  'SHORT',
  'INT',
  'LONG',
  'FLOAT',
  'DOUBLE',
  'BIG_INTEGER',
  'BIG_DECIMAL',
  'STRING_ARRAY',
  'CHAR_ARRAY',
  'BOOLEAN_ARRAY',
  'INT_ARRAY',
  'LONG_ARRAY',
  'FLOAT_ARRAY',
  'DOUBLE_ARRAY',
  'STRING_2D_ARRAY',
  'CHAR_2D_ARRAY',
  'BOOLEAN_2D_ARRAY',
  'INT_2D_ARRAY',
  'LONG_2D_ARRAY',
  'FLOAT_2D_ARRAY',
  'DOUBLE_2D_ARRAY',
  'LIST_STRING',
  'LIST_CHAR',
  'LIST_BOOLEAN',
  'LIST_INT',
  'LIST_LONG',
  'LIST_FLOAT',
  'LIST_DOUBLE',
  'LIST_LIST_INT',
  'LIST_LIST_STRING',
  'SET_INT',
  'SET_LONG',
  'SET_STRING',
  'MAP_STRING_STRING',
  'MAP_STRING_INT',
  'MAP_INT_INT',
  'MAP_INT_STRING',
  'LIST_NODE_INT',
  'LIST_NODE_STRING',
  'TREE_NODE_INT',
  'TREE_NODE_STRING',
  'GRAPH_ADJ_LIST',
  'GRAPH_ADJ_MATRIX',
  'INTERVAL_INT_ARRAY',
  'POINT_INT',
  'CUSTOM',
]

export const OutputType = InputType
