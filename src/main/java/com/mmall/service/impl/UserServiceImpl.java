package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount= userMapper.checkUsername(username);
        if(resultCount==0){
            return ServerResponse.createByErrorMessage("用户名不存在！");
        }
        //todo 密码登陆MD5
        String md5=MD5Util.MD5EncodeUtf8(password);
        User user=userMapper.selectLogin(username,md5);
        if(user==null){
            return ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return  ServerResponse.createBySuccess("登陆成功",user);
    }

    @Override
    public ServerResponse<String> register(User user){
//        int resultCount=userMapper.checkUsername(user.getUsername());
//        if(resultCount>0){
//            return ServerResponse.createByErrorMessage("用户以存在。");
//            }
         ServerResponse validResponse=this.checkValid(user.getUsername(),Const.USERNAME);
         if(!validResponse.isSuccess()){
             return  validResponse;
         }
         validResponse=this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return  validResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int   resultCount=userMapper.insert(user);
        if(resultCount==0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServerResponse<String> checkValid(String str,String type){
       if(StringUtils.isBlank(type)){
           //开始校验
           if(Const.CURRENT_USER.equals(str)){
               int resultCount=userMapper.checkEamil(str);
               if(resultCount>0){
                   return ServerResponse.createBySuccessMessage("校验成功");
               }
           }
       }else {
           return ServerResponse.createByErrorMessage("参数错误");
       }
       return ServerResponse.createBySuccessMessage("校验成功");
    }

    @Override
    public ServerResponse selectQuestion(String username){
        ServerResponse validResponse=this.checkValid(username,Const.CURRENT_USER);
        if(validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question=userMapper.selectQuestionByUsername(username);
        if(StringUtils.isBlank(question)){
            return ServerResponse.createBySuccess(question);

        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }


    @Override
    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount=userMapper.checkAnswer(username,question,answer);
        if(resultCount>0){
            //说明问题及问题答案是这个用户的，并且正确
            String forgetToken=UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误");
        }
        ServerResponse validResponse=this.checkValid(username,Const.CURRENT_USER);
        if(validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token=TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效，或过期");
        }
        if(StringUtils.equals(forgetToken,token)){
          String md5Password=MD5Util.MD5EncodeUtf8(passwordNew);
          int rowCount=userMapper.updatePasswordByUsername(username,md5Password);
          if(rowCount>0){
              return ServerResponse.createBySuccessMessage("修改成功");
          }
        }else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取");
        }
        return ServerResponse.createByErrorMessage("修改密码错误");
    }

    @Override
    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        //防止横向越权，要检验一下这个用户的旧密码，一定要指定是这个用户，因为我们会查询一个count（1），如果不指定id，那么结果就是true，count>0
        int resultCount=userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount==0){
            return ServerResponse.createByErrorMessage("旧密码错误");

        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount=userMapper.updateByPrimaryKeySelective(user);
        if(updateCount>0){
            return ServerResponse.createBySuccessMessage("密码修改成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
       }

}