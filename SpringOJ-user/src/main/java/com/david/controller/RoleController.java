package com.david.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.david.entity.role.Role;
import com.david.service.IRoleService;
import com.david.utils.ResponseResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user/api/role")
@RequiredArgsConstructor
public class RoleController {

	private final IRoleService roleService;

	@GetMapping("/page")
	public ResponseResult<Page<Role>> pageUsers(
			@RequestParam int page,
			@RequestParam int size,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) Integer status) {
		Page<Role> result = roleService.pageRoles(page, size, keyword, status);
		return ResponseResult.success("分页获取角色成功", result);
	}

	@GetMapping
	public ResponseResult<List<Role>> getAllRoles() {
		List<Role> roles = roleService.list();
		return ResponseResult.success("获取角色列表成功", roles);
	}

	@GetMapping("/{id}")
	public ResponseResult<Role> getRoleById(@PathVariable Long id) {
		Role role = roleService.getById(id);
		if (role != null) {
			return ResponseResult.success("获取角色信息成功", role);
		}
		return ResponseResult.fail(404, "未找到对应角色");
	}

	@PostMapping
	public ResponseResult<Void> createRole(@RequestBody Role role) {
		boolean isSaved = roleService.save(role);
		if (isSaved) {
			return ResponseResult.success("角色创建成功");
		}
		return ResponseResult.fail(500, "角色创建失败");
	}

	@PutMapping("/{id}")
	public ResponseResult<Void> updateRole(@PathVariable Long id, @RequestBody Role role) {
		role.setId(id);
		boolean isUpdated = roleService.updateById(role);
		if (isUpdated) {
			return ResponseResult.success("角色信息更新成功");
		}
		return ResponseResult.fail(500, "角色信息更新失败");
	}

	@DeleteMapping("/{id}")
	public ResponseResult<Void> deleteRole(@PathVariable Long id) {
		boolean isDeleted = roleService.removeById(id);
		if (isDeleted) {
			return ResponseResult.success("角色删除成功");
		}
		return ResponseResult.fail(500, "角色删除失败");
	}
}