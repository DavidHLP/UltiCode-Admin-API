package com.david.admin.controller;

import com.david.admin.service.SensitiveOperationGuard;
import com.david.admin.service.UserManagementService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class UserAdminControllerTest {
    @Resource @MockBean private UserManagementService userManagementService;

    @Resource @MockBean private SensitiveOperationGuard sensitiveOperationGuard;
}
