package com.bupt.chess;

import android.app.Application;

import com.bupt.chess.manager.MessageManager;

/**
 * Created by xusong on 2018/1/10.
 * About:
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MessageManager.getInstance().init(this);
    }

}
