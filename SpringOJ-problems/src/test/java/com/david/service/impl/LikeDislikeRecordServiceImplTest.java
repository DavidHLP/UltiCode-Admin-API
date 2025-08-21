package com.david.service.impl;

import com.david.ProblemsServiceSpringBootApplication;
import com.david.calendar.enums.ActionType;
import com.david.calendar.enums.TargetType;
import com.david.calendar.vo.LikeDislikeRecordVo;
import com.david.service.ILikeDislikeRecordService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
        classes = ProblemsServiceSpringBootApplication.class,
        properties = {
                // 禁用外部依赖，避免测试时连接外部服务
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.sentinel.enabled=false",
                "spring.kafka.listener.auto-startup=false",
                // 排除有问题的自动配置：系统指标与方法校验
                "spring.autoconfigure.exclude=org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration,org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration"
        }
)
@ActiveProfiles("test")
@Transactional // 每个测试方法事务内执行，默认回滚
@Rollback // 明确标注回滚，确保数据库最终无变更
class LikeDislikeRecordServiceImplTest {

    @Autowired
    private ILikeDislikeRecordService service;

    // 选取在 like_dislike_record 表中无外键约束的目标，避免额外依赖
    private static final long TEST_USER_ID = 999_999_001L;
    private static final long TEST_TARGET_ID = 888_888_001L;
    private static final TargetType TARGET_TYPE = TargetType.SOLUTION;

    @Test
    void testPerformActionAndStatsRollbackSafe() {
        // 1) 记录初始统计
        LikeDislikeRecordVo before = service.getStats(TARGET_TYPE, TEST_TARGET_ID);
        long baseLike = before.getLikeCount();
        long baseDislike = before.getDislikeCount();
        long baseTotal = before.getTotalCount();

        // 2) 点赞
        LikeDislikeRecordVo afterLike = service.performAction(TEST_USER_ID, TARGET_TYPE, TEST_TARGET_ID, ActionType.LIKE);
        Assertions.assertEquals("LIKE", afterLike.getUserAction());
        Assertions.assertEquals(baseLike + 1, afterLike.getLikeCount());
        Assertions.assertEquals(baseDislike, afterLike.getDislikeCount());
        Assertions.assertEquals(baseTotal + 1, afterLike.getTotalCount());

        // 3) 再次点赞，等价于取消操作
        LikeDislikeRecordVo afterCancel = service.performAction(TEST_USER_ID, TARGET_TYPE, TEST_TARGET_ID, ActionType.LIKE);
        Assertions.assertEquals("NONE", afterCancel.getUserAction());
        Assertions.assertEquals(baseLike, afterCancel.getLikeCount());
        Assertions.assertEquals(baseDislike, afterCancel.getDislikeCount());
        Assertions.assertEquals(baseTotal, afterCancel.getTotalCount());

        // 4) 点踩
        LikeDislikeRecordVo afterDislike = service.performAction(TEST_USER_ID, TARGET_TYPE, TEST_TARGET_ID, ActionType.DISLIKE);
        Assertions.assertEquals("DISLIKE", afterDislike.getUserAction());
        Assertions.assertEquals(baseLike, afterDislike.getLikeCount());
        Assertions.assertEquals(baseDislike + 1, afterDislike.getDislikeCount());
        Assertions.assertEquals(baseTotal + 1, afterDislike.getTotalCount());

        // 5) 切换到点赞（从点踩 -> 点赞）
        LikeDislikeRecordVo afterSwitch = service.performAction(TEST_USER_ID, TARGET_TYPE, TEST_TARGET_ID, ActionType.LIKE);
        Assertions.assertEquals("LIKE", afterSwitch.getUserAction());
        Assertions.assertEquals(baseLike + 1, afterSwitch.getLikeCount());
        Assertions.assertEquals(baseDislike, afterSwitch.getDislikeCount());
        Assertions.assertEquals(baseTotal + 1, afterSwitch.getTotalCount());

        // 注意：由于有 @Transactional + @Rollback，测试结束后这些更改不会提交到数据库
    }
}
