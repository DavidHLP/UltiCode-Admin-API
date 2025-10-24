package com.david.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PermissionChangeNotifier {

    public void notifyRoleChange(Long roleId, String roleCode) {
        log.info("已触发权限变更通知，roleId={}, roleCode={}", roleId, roleCode);
        // 可接入消息队列/邮件/站内信，这里先记录日志。
    }
}
