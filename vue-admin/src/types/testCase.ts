export interface TestCase {
  id: number;
  problemId: number;
  inputFile: string;
  outputFile: string;
  score: number;
  isSample: boolean;
  createdAt: string;
}
