import request from "@/utils/request";

export interface SubmitCodeRequest {
  problemId: number;
  language: string; // e.g. 'JAVA' | 'CPP' | 'PYTHON' ... must match backend LanguageType code
  sourceCode: string;
}

/**
 * 提交代码以触发判题，返回 submissionId
 */
export const submitCode = (payload: SubmitCodeRequest): Promise<number> => {
  return request({
    url: "/judge/api/view/submit",
    method: "post",
    data: payload,
  }) as Promise<number>;
};
