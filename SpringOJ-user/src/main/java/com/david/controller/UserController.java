package com.david.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.david.entity.user.User;
import com.david.service.IUserService;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user/api")
@RequiredArgsConstructor
public class UserController {

	private final IUserService userService;

	@GetMapping
	public ResponseResult<List<User>> getAllUsers() {
		return ResponseResult.success("用户信息获取成功", userService.list());
	}

	@GetMapping("/{id}")
	public ResponseResult<User> getUserById(@PathVariable Long id) {
		User user = userService.getById(id);
		if (user != null) {
			return ResponseResult.success("用户信息获取成功", user);
		}
		return ResponseResult.fail(404, "用户不存在");
	}

	@PostMapping
	public ResponseResult<Void> createUser(@RequestBody User user) {
		if (userService.save(user)) {
			return ResponseResult.success("用户创建成功");
		}
		return ResponseResult.fail(500, "用户创建失败");
	}

	@PutMapping("/{id}")
	public ResponseResult<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
		user.setUserId(id);
		if (userService.updateById(user)) {
			return ResponseResult.success("用户信息更新成功");
		}
		return ResponseResult.fail(500, "用户信息更新失败");
	}

	@DeleteMapping("/{id}")
	public ResponseResult<Void> deleteUser(@PathVariable Long id) {
		if (userService.removeById(id)) {
			return ResponseResult.success("用户删除成功");
		}
		return ResponseResult.fail(500, "用户删除失败");
	}
}
