export interface SolutionComment {
  id: number;
  solutionId: number;
  userId: number;
  username: string;
  avatar: string;
  content: string;
  parentId: number | null;
  rootId: number | null;
  replyToUserId: number | null;
  replyToUsername: string | null;
  upvotes: number;
  downvotes: number;
  createdAt: string;
  children?: SolutionComment[];
}

export interface SolutionCommentDto {
  solutionId: number;
  content: string;
  parentId?: number;
  replyToUserId?: number;
}

export type SolutionCommentVo = SolutionComment;
