import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class debug_output {
    public static void main(String[] args) {
        try {
            // 模拟错误答案代码
            List<Integer> result = Arrays.asList(0, 0); // 错误答案
            List<Integer> expected = Arrays.asList(0, 1); // 正确答案
            
            ObjectMapper mapper = new ObjectMapper();
            
            // 创建输出格式
            Map<String, Object> testResult = new HashMap<>();
            testResult.put("testCaseIndex", 0);
            testResult.put("success", result.equals(expected));
            testResult.put("actualOutput", mapper.writeValueAsString(result));
            testResult.put("expectedOutput", mapper.writeValueAsString(expected));
            
            List<Map<String, Object>> results = Arrays.asList(testResult);
            
            System.out.println(mapper.writeValueAsString(results));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
