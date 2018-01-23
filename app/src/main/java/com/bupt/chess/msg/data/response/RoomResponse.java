package com.bupt.chess.msg.data.response;


import com.bupt.chess.msg.UserInfo;

/**
 * Created by xusong on 2018/1/5.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 * join-leave-swap-create
 */
public class RoomResponse {
    public String roomName;
    public String roomKey;
    public boolean success;
    public String red;
    public String black;
    public String master;
    public int type;
    public String info;

    public AccountResponse r;
    public AccountResponse b;

}
