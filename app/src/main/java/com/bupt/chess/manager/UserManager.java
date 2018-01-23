package com.bupt.chess.manager;

import com.bupt.chess.msg.data.response.AccountResponse;

/**
 * Created by xusong on 2018/1/22.
 * About:
 */

public class UserManager {
    private static UserManager instance = new UserManager();

    private UserManager() {

    }

    public static UserManager getInstance() {
        return instance;
    }
    AccountResponse accountResponse;

    public void setUser(AccountResponse r){
        accountResponse = r;
    }
    public AccountResponse getUser(){
        return accountResponse;
    }
}
