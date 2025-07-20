import type { TestCase } from './testCase';

export interface Problem {
  id: number;
  title: string;
  description: string;
  inputFormat: string;
  outputFormat: string;
  sampleInput: string;
  sampleOutput: string;
  hint: string;
  timeLimit: number;
  memoryLimit: number;
  difficulty: 'Easy' | 'Medium' | 'Hard';
  tags: string;
  solvedCount: number;
  submissionCount: number;
  createdBy: number;
  isVisible: boolean;
  createdAt: string;
  updatedAt: string;
  testCases?: TestCase[];
}
