package com.youzhirong.fbslock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.youzhirong.fbslock.dto.UserDTO;
import com.youzhirong.fbslock.service.UserService;

import io.swagger.annotations.ApiOperation;

@RestController
public class TestController {

	@Autowired
	private UserService userService;
	
	@ApiOperation(value = "重现并发问题添加用户产生多插入的问题接口")
	@PostMapping("/add/user")
	public void addUser(@RequestBody UserDTO userDTO) {
		userService.saveByDTO(userDTO);
	}
	
	@ApiOperation(value = "使用redisson分布式锁解决并发重复添加用户的接口")
	@PostMapping("/add/user/lock")
	public void addUserLock(@RequestBody UserDTO userDTO) {
		userService.saveByDTOLock(userDTO);
	}
	
	@ApiOperation(value = "查询用户的接口")
	@GetMapping("/select/user/{loginId}")
	public UserDTO selectUser(@PathVariable(name = "loginId") String loginId) {
		UserDTO userDTO = userService.selectByCode(loginId);
		return userDTO;
	}
}
