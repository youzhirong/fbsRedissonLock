package com.youzhirong.fbslock.service.impl;
 
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.youzhirong.fbslock.dao.UserDO;
import com.youzhirong.fbslock.dto.UserDTO;
import com.youzhirong.fbslock.mapper.UserMapper;
import com.youzhirong.fbslock.service.UserService;

import lombok.extern.slf4j.Slf4j;

/**
 * [说明]用户表ServiceImpl
 * @author youzhirong
 * @version 创建时间： 2020-04-08
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
	
	@Autowired
	private RedissonClient redissonClient;
 
    @Autowired
    private UserMapper userMapper;
 
    @Override
    public UserDTO selectByCode(String code){
    	UserDO userDO = new UserDO();
    	userDO.setLoginId(code);
    	userDO = userMapper.select(userDO).stream().findFirst().orElse(null);
    	if(userDO == null) {
    		return null;
    	}
    	UserDTO userDTO = new UserDTO();
    	BeanUtils.copyProperties(userDO, userDTO);
        return userDTO;
    }
 
    @Override
    public void saveByDTO(UserDTO userDTO) {
    	Boolean isAdd = Boolean.FALSE;
    	UserDTO source = selectByCode(userDTO.getLoginId());
        if (source == null) {
            isAdd = Boolean.TRUE;
            userDTO.setId(null);
            userDTO.setObjectVersionNumber(1L);
        }else {
        	userDTO.setId(source.getId());
        	userDTO.setObjectVersionNumber(source.getObjectVersionNumber());
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userDTO, userDO);
        if (isAdd) {
            userMapper.insertSelective(userDO);
        } else {
            userMapper.updateByPrimaryKey(userDO);
        }
    }
    
    
    @Override
    public void saveByDTOLock(UserDTO userDTO) {
    	Boolean isAdd = Boolean.FALSE;
    	RLock rLock = redissonClient.getLock(userDTO.getLoginId());
    	rLock.lock();
    	UserDTO source = selectByCode(userDTO.getLoginId());
        if (source == null) {
            isAdd = Boolean.TRUE;
            userDTO.setId(null);
            userDTO.setObjectVersionNumber(1L);
        }else {
        	userDTO.setId(source.getId());
        	userDTO.setObjectVersionNumber(source.getObjectVersionNumber());
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userDTO, userDO);
        if (isAdd) {
            userMapper.insertSelective(userDO);
        } else {
            userMapper.updateByPrimaryKey(userDO);
        }
        rLock.unlock();
    }
 
}