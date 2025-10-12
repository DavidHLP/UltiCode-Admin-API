package com.david.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.entity.user.AuthUser;
import com.david.service.IUserService;
import com.david.utils.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * 分页查询用户（按关键字匹配用户名或邮箱）
     *
     * @param page    第几页（从1开始）
     * @param size    每页大小
     * @param keyword 可选关键字（用户名/邮箱 模糊匹配）
     * @return 分页数据
     */
    @GetMapping("/page")
    public ResponseResult<Page<AuthUser>> pageUsers(
            @RequestParam int page,
            @RequestParam(name = "pageSize") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Boolean status) {
        Page<AuthUser> result = userService.pageUsers(page, size, keyword, roleId, status);
        return ResponseResult.success("分页获取用户成功", result);
    }

    /**
     * 新增用户
     *
     * @param user 用户实体
     * @return 统一响应：成功或失败信息
     */
    @PostMapping
    public ResponseResult<Void> createUser(@RequestBody AuthUser user) {
        if (userService.save(user)) {
            return ResponseResult.success("用户创建成功");
        }
        return ResponseResult.fail(500, "用户创建失败");
    }

    /**
     * 更新指定ID的用户信息
     *
     * @param id   用户ID
     * @param user 待更新的用户信息（以ID为准）
     * @return 统一响应：成功或失败信息
     */
    @PutMapping("/{id}")
    public ResponseResult<Void> updateUser(@PathVariable Long id, @RequestBody AuthUser user) {
        user.setUserId(id);
        if (userService.updateById(user)) {
            return ResponseResult.success("用户信息更新成功");
        }
        return ResponseResult.fail(500, "用户信息更新失败");
    }

    /**
     * 删除指定ID的用户
     *
     * @param id 用户ID
     * @return 统一响应：成功或失败信息
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteUser(@PathVariable Long id) {
        if (userService.removeById(id)) {
            return ResponseResult.success("用户删除成功");
        }
        return ResponseResult.fail(500, "用户删除失败");
    }

    @GetMapping("/ids")
    public ResponseResult<List<AuthUser>> getUserByIds(@RequestParam("ids") List<Long> ids) {
        List<AuthUser> users = userService.listByIds(ids);
        if (users.isEmpty()) {
            return ResponseResult.fail(404, "用户不存在");
        }
        return ResponseResult.success("获取用户信息成功", users);
    }

    @GetMapping("/id")
    public ResponseResult<AuthUser> getUserById(@RequestParam("id") Long id) {
        AuthUser user = userService.getById(id);
        if (user == null) {
            return ResponseResult.fail(404, "用户不存在");
        }
        return ResponseResult.success("获取用户信息成功", user);
    }
}
