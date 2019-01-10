package com.mmall.common;

public class Const {

    public static final String CURRENT_USER="currentUser";

    public static final String EMAIL="email";
    public static final String USERNAME="username";


    public interface  Role{
        //普通用户
        int ROLE_CUSTOMER=0;
        //管理员
        int ROLE_ADMIN=1;
    }

}
