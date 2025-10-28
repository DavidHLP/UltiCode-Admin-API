package com.david.admin.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.david.admin.service.SensitiveOperationGuard;
import com.david.admin.service.UserManagementService;
import com.david.core.exception.GlobalExceptionHandler;
import com.david.core.forward.ForwardedUser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 覆盖 UserAdminController 的核心接口： - GET /api/admin/users - GET /api/admin/users/{userId} - POST
 * /api/admin/users - PUT /api/admin/users/{userId}
 *
 * <p>说明： 1) 使用 @SpringBootTest 以加载真实的参数解析器（@CurrentForwardedUser）与安全配置； 2) 通过 @MockBean 屏蔽掉 Service
 * 与 Guard 的真实逻辑； 3) 仅断言状态码与交互行为，避免对响应体结构的强耦合；
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandler.class)
class UserAdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UserManagementService userManagementService;
    @MockBean private SensitiveOperationGuard sensitiveOperationGuard;

    @Test
    @DisplayName("【listUsers】管理员可查询用户列表")
    @WithMockUser(roles = "admin")
    void listUsers_ok() throws Exception {
        // 不关心返回体内容，避免对 DTO 结构的强依赖
        when(userManagementService.listUsers(anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(mock()); // 返回一个 Mockito mock 即可

        mockMvc.perform(
                        get("/api/admin/users")
                                .param("page", "1")
                                .param("size", "10")
                                .param("keyword", "jack")
                                .param("status", "1")
                                .param("roleId", "2"))
                .andExpect(status().isOk());

        verify(userManagementService).listUsers(1, 10, "jack", 1, 2L);
        verifyNoMoreInteractions(userManagementService);
    }

    // ======== 成功路径 ========

    @Test
    @DisplayName("【getUser】管理员可获取用户详情")
    @WithMockUser(roles = "admin")
    void getUser_ok() throws Exception {
        when(userManagementService.getUser(100L)).thenReturn(mock());

        mockMvc.perform(get("/api/admin/users/{userId}", 100)).andExpect(status().isOk());

        verify(userManagementService).getUser(100L);
        verifyNoMoreInteractions(userManagementService);
    }

    @Test
    @DisplayName("【createUser】管理员可创建用户（校验敏感操作令牌）")
    @WithMockUser(roles = "admin")
    void createUser_ok() throws Exception {
        // Guard 只需验证被调用即可
        doNothing().when(sensitiveOperationGuard).ensureValid(anyLong(), eq("SEC-TOKEN-123"));

        when(userManagementService.createUser(
                        ArgumentMatchers.any(ForwardedUser.class), ArgumentMatchers.any()))
                .thenReturn(mock());

        String body =
                """
                {
                  "username": "alice",
                  "email": "alice@example.com",
                  "password": "P@ssw0rd!"
                }
                """;

        mockMvc.perform(
                        post("/api/admin/users")
                                .header("X-Sensitive-Action-Token", "SEC-TOKEN-123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isCreated());

        // 验证 Guard 与 Service 被正常调用
        verify(sensitiveOperationGuard).ensureValid(anyLong(), eq("SEC-TOKEN-123"));
        verify(userManagementService).createUser(any(ForwardedUser.class), any());
        verifyNoMoreInteractions(userManagementService, sensitiveOperationGuard);
    }

    @Test
    @DisplayName("【updateUser】管理员可更新用户（校验敏感操作令牌）")
    @WithMockUser(roles = "admin")
    void updateUser_ok() throws Exception {
        doNothing().when(sensitiveOperationGuard).ensureValid(anyLong(), eq("SEC-TOKEN-XYZ"));

        when(userManagementService.updateUser(
                        ArgumentMatchers.any(ForwardedUser.class),
                        eq(200L),
                        ArgumentMatchers.any()))
                .thenReturn(mock());

        String body =
                """
                {
                  "email": "new.email@example.com",
                  "displayName": "New Name",
                  "status": 1
                }
                """;

        mockMvc.perform(
                        put("/api/admin/users/{userId}", 200)
                                .header("X-Sensitive-Action-Token", "SEC-TOKEN-XYZ")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isOk());

        verify(sensitiveOperationGuard).ensureValid(anyLong(), eq("SEC-TOKEN-XYZ"));
        verify(userManagementService).updateUser(any(ForwardedUser.class), eq(200L), any());
        verifyNoMoreInteractions(userManagementService, sensitiveOperationGuard);
    }

    @Test
    @DisplayName("【权限】非管理员访问接口 -> 403")
    @WithMockUser(roles = "user")
    void forbidden_for_non_admin() throws Exception {
        mockMvc.perform(get("/api/admin/users")).andExpect(status().isForbidden());

        // 不应触发任何服务调用
        verifyNoInteractions(userManagementService, sensitiveOperationGuard);
    }

    // ======== 权限控制 ========

    @TestConfiguration
    static class TestWebConfig implements WebMvcConfigurer {

        @Bean
        HandlerMethodArgumentResolver forwardedUserResolverBean() {
            return new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return ForwardedUser.class.isAssignableFrom(parameter.getParameterType());
                }

                @Override
                public Object resolveArgument(
                        MethodParameter parameter,
                        ModelAndViewContainer mavContainer,
                        NativeWebRequest webRequest,
                        WebDataBinderFactory binderFactory) {
                    // 提供一个稳定的“已认证网关用户”
                    return new ForwardedUser(1L, "admin", List.of("admin"));
                }
            };
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            // 关键：显式注册
            resolvers.add(forwardedUserResolverBean());
        }
    }

    // ======== 参数校验/错误分支 ========

    @Nested
    @DisplayName("参数校验错误")
    class ValidationErrors {

        @Test
        @DisplayName("listUsers: page < 1 -> 400")
        @WithMockUser(roles = "admin")
        void listUsers_page_lt_1() throws Exception {
            mockMvc.perform(get("/api/admin/users").param("page", "0").param("size", "10"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(userManagementService);
        }

        @Test
        @DisplayName("listUsers: size > 100 -> 400")
        @WithMockUser(roles = "admin")
        void listUsers_size_gt_100() throws Exception {
            mockMvc.perform(get("/api/admin/users").param("page", "1").param("size", "101"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(userManagementService);
        }

        @Test
        @DisplayName("createUser: 缺少 X-Sensitive-Action-Token -> 400")
        @WithMockUser(roles = "admin")
        void createUser_missing_sensitive_header() throws Exception {
            String body =
                    """
                    {
                      "username": "bob",
                      "email": "bob@example.com",
                      "password": "P@ssw0rd!"
                    }
                    """;

            mockMvc.perform(
                            post("/api/admin/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(body))
                    .andExpect(status().isBadRequest()); // 缺失必填请求头

            verifyNoInteractions(sensitiveOperationGuard, userManagementService);
        }

        @Test
        @DisplayName("updateUser: 缺少 X-Sensitive-Action-Token -> 400")
        @WithMockUser(roles = "admin")
        void updateUser_missing_sensitive_header() throws Exception {
            String body =
                    """
                    {
                      "email": "x@example.com"
                    }
                    """;

            mockMvc.perform(
                            put("/api/admin/users/{userId}", 1)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(body))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(sensitiveOperationGuard, userManagementService);
        }
    }
}
