package com.david.admin.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.david.admin.service.PermissionManagementService;
import com.david.admin.service.RoleManagementService;
import com.david.admin.service.SensitiveOperationGuard;
import com.david.core.exception.GlobalExceptionHandler;
import com.david.core.forward.ForwardedUser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandler.class)
class RoleAdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private RoleManagementService roleManagementService;
    @MockBean private PermissionManagementService permissionManagementService;
    @MockBean private SensitiveOperationGuard sensitiveOperationGuard;

    // ======== 成功路径 ========

    @Test
    @DisplayName("【listRoles】管理员可查询角色列表")
    @WithMockUser(roles = "admin")
    void listRoles_ok() throws Exception {
        when(roleManagementService.listRoles("editor")).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/roles").param("keyword", "editor"))
                .andExpect(status().isOk());

        verify(roleManagementService).listRoles("editor");
        verifyNoMoreInteractions(roleManagementService);
    }

    @Test
    @DisplayName("【listRoleOptions】管理员可查询角色选项")
    @WithMockUser(roles = "admin")
    void listRoleOptions_ok() throws Exception {
        when(roleManagementService.listRoleOptions()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/roles/options")).andExpect(status().isOk());

        verify(roleManagementService).listRoleOptions();
        verifyNoMoreInteractions(roleManagementService);
    }

    @Test
    @DisplayName("【listRolePermissions】管理员可查询权限列表")
    @WithMockUser(roles = "admin")
    void listRolePermissions_ok() throws Exception {
        when(permissionManagementService.listPermissionsDto("user:")).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/roles/permissions").param("keyword", "user:"))
                .andExpect(status().isOk());

        verify(permissionManagementService).listPermissionsDto("user:");
        verifyNoMoreInteractions(permissionManagementService);
    }

    @Test
    @DisplayName("【getRole】管理员可获取角色详情")
    @WithMockUser(roles = "admin")
    void getRole_ok() throws Exception {
        when(roleManagementService.getRole(10L)).thenReturn(mock());

        mockMvc.perform(get("/api/admin/roles/{roleId}", 10)).andExpect(status().isOk());

        verify(roleManagementService).getRole(10L);
        verifyNoMoreInteractions(roleManagementService);
    }

    @Test
    @DisplayName("【createRole】管理员可创建角色（校验敏感操作令牌）")
    @WithMockUser(roles = "admin")
    void createRole_ok() throws Exception {
        doNothing().when(sensitiveOperationGuard).ensureValid(anyLong(), eq("TOKEN-123"));
        when(roleManagementService.createRole(any(ForwardedUser.class), any())).thenReturn(mock());

        String body =
                """
                {
                  "code": "new_role",
                  "name": "New Role",
                  "description": "A brand new role"
                }
                """;

        mockMvc.perform(
                        post("/api/admin/roles")
                                .header("X-Sensitive-Action-Token", "TOKEN-123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isCreated());

        verify(sensitiveOperationGuard).ensureValid(anyLong(), eq("TOKEN-123"));
        verify(roleManagementService).createRole(any(ForwardedUser.class), any());
        verifyNoMoreInteractions(roleManagementService, sensitiveOperationGuard);
    }

    @Test
    @DisplayName("【updateRole】管理员可更新角色（校验敏感操作令牌）")
    @WithMockUser(roles = "admin")
    void updateRole_ok() throws Exception {
        doNothing().when(sensitiveOperationGuard).ensureValid(anyLong(), eq("TOKEN-456"));
        when(roleManagementService.updateRole(any(ForwardedUser.class), eq(20L), any()))
                .thenReturn(mock());

        String body =
                """
                {
                  "name": "Updated Role Name",
                  "permissionIds": [1, 2, 3]
                }
                """;

        mockMvc.perform(
                        put("/api/admin/roles/{roleId}", 20)
                                .header("X-Sensitive-Action-Token", "TOKEN-456")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isOk());

        verify(sensitiveOperationGuard).ensureValid(anyLong(), eq("TOKEN-456"));
        verify(roleManagementService).updateRole(any(ForwardedUser.class), eq(20L), any());
        verifyNoMoreInteractions(roleManagementService, sensitiveOperationGuard);
    }

    @Test
    @DisplayName("【deleteRole】管理员可删除角色（校验敏感操作令牌）")
    @WithMockUser(roles = "admin")
    void deleteRole_ok() throws Exception {
        doNothing().when(sensitiveOperationGuard).ensureValid(anyLong(), eq("TOKEN-789"));
        doNothing().when(roleManagementService).deleteRole(any(ForwardedUser.class), eq(30L));

        mockMvc.perform(
                        delete("/api/admin/roles/{roleId}", 30)
                                .header("X-Sensitive-Action-Token", "TOKEN-789"))
                .andExpect(status().isOk());

        verify(sensitiveOperationGuard).ensureValid(anyLong(), eq("TOKEN-789"));
        verify(roleManagementService).deleteRole(any(ForwardedUser.class), eq(30L));
        verifyNoMoreInteractions(roleManagementService, sensitiveOperationGuard);
    }

    // ======== 权限控制 ========

    @Test
    @DisplayName("【权限】非管理员访问接口 -> 403")
    @WithMockUser(roles = "user")
    void forbidden_for_non_admin() throws Exception {
        // GET：无请求体, 直接403
        mockMvc.perform(get("/api/admin/roles")).andExpect(status().isForbidden());

        // ★ createRole：提供最小合法payload，避免触发 @Valid 400
        String createBody =
                """
                {
                  "code": "role_min",
                  "name": "Role Min"
                }
                """;
        mockMvc.perform(
                        post("/api/admin/roles")
                                .header("X-Sensitive-Action-Token", "DUMMY-TOKEN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createBody))
                .andExpect(status().isForbidden());

        // ★ updateRole：同理，提供合法可更新字段
        String updateBody =
                """
                {
                  "name": "Updated Name",
                  "permissionIds": [1]
                }
                """;
        mockMvc.perform(
                        put("/api/admin/roles/{roleId}", 1L)
                                .header("X-Sensitive-Action-Token", "DUMMY-TOKEN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody))
                .andExpect(status().isForbidden());

        // ★ deleteRole：无需 body；只要带上必须的敏感操作头，避免 MissingRequestHeader 400
        mockMvc.perform(
                        delete("/api/admin/roles/{roleId}", 1L)
                                .header("X-Sensitive-Action-Token", "DUMMY-TOKEN"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(
                roleManagementService, sensitiveOperationGuard, permissionManagementService);
    }

    // ======== 参数校验/错误分支 ========

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
            resolvers.add(forwardedUserResolverBean());
        }
    }

    // ======== 模拟 @CurrentForwardedUser ========

    @Nested
    @DisplayName("参数校验错误")
    class ValidationErrors {

        @Test
        @DisplayName("createRole: 缺少 X-Sensitive-Action-Token -> 400")
        @WithMockUser(roles = "admin")
        void createRole_missing_sensitive_header() throws Exception {

            String body =
                    """
                    {
                      "code": "test"
                    }
                    """;
            mockMvc.perform(
                            post("/api/admin/roles")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(body))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(sensitiveOperationGuard, roleManagementService);
        }

        @Test
        @DisplayName("updateRole: 缺少 X-Sensitive-Action-Token -> 400")
        @WithMockUser(roles = "admin")
        void updateRole_missing_sensitive_header() throws Exception {
            String body =
                    """
                    {
                      "name": "test"
                    }
                    """;
            mockMvc.perform(
                            put("/api/admin/roles/{roleId}", 1)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(body))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(sensitiveOperationGuard, roleManagementService);
        }

        @Test
        @DisplayName("deleteRole: 缺少 X-Sensitive-Action-Token -> 400")
        @WithMockUser(roles = "admin")
        void deleteRole_missing_sensitive_header() throws Exception {
            mockMvc.perform(delete("/api/admin/roles/{roleId}", 1))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(sensitiveOperationGuard, roleManagementService);
        }
    }
}
