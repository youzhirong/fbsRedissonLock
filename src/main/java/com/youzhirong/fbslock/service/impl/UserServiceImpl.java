package com.youzhirong.fbslock.service.impl;

import com.youzhirong.fbslock.dao.UserDO;
import com.youzhirong.fbslock.dto.UserDTO;
import com.youzhirong.fbslock.mapper.UserMapper;
import com.youzhirong.fbslock.service.RedissonService;
import com.youzhirong.fbslock.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * [说明]用户表ServiceImpl
 * @author youzhirong
 * @version 创建时间： 2020-04-08
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
	
	@Resource
	private RedissonClient redissonClient;
	
	@Resource
	private RedissonService redissonService;
 
    @Resource
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
    
    @Override
    public void saveByDTOFairLock(UserDTO userDTO) {
    	Boolean isAdd = Boolean.FALSE;
    	RLock rLock = redissonClient.getFairLock(userDTO.getLoginId());
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
    
    //重复提交
    private void test1(String userId) {
    	String lockKey = userId;
    	// 公平加锁，60秒后锁自动释放
    	boolean isLocked = false;
    	try {
    		isLocked = redissonService.fairLock(lockKey, TimeUnit.SECONDS, 3, 60);
    		if (isLocked) { // 如果成功获取到锁就继续执行
    			// 执行业务代码操作
    			//成功
    		} else { // 未获取到锁
    			//请勿重复点击
    		}
    	} catch (Exception e) {
    		//异常
    	} finally {
    		if (isLocked) { // 如果锁还存在，在方法执行完成后，释放锁
    			redissonService.unlock(lockKey);
    		}
    	}
    }
 
}