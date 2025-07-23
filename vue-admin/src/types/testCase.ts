export interface TestCase {
  id: number;
  problemId: number;
  inputFile: string;
  outputFile: string;
  score: number;
  isSample: boolean;
  createdAt: string;
  inputContent?: string;
  outputContent?: string;
}

export interface TestCaseContent {
  inputContent: string;
  outputContent: string;
}
