package com.bupt.chess.msg.data;

/**
 * Created by xusong on 2018/1/6.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 * 普通聊天消息
 */
public class NormalMessage {
    public String from;
    public String content;

    public NormalMessage(){

    }

    public NormalMessage(String from, String content) {
        this.from = from;
        this.content = content;
    }
}
