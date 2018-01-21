package com.bupt.chess.manager;

import android.os.Handler;

import com.bupt.chess.conn.JsonConnection;
import com.bupt.chess.msg.Message;
import com.bupt.chess.msg.UserInfo;
import com.bupt.chess.msg.data.response.AccountResponse;
import com.bupt.chess.msg.data.response.RoomResponse;
import com.bupt.chess.msg.data.response.StaticsResponse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xusong on 2018/1/10.
 * About:
 */

public class MessageManager {

    private static MessageManager manager = new MessageManager();

    public static MessageManager getInstance() {
        return manager;
    }

    private MessageManager() {
    }

    private Handler handler;
    private volatile boolean end = false;
    private AtomicInteger id = new AtomicInteger(600);
    //请求响应模式的id一律在600以上，一次相应后直接remove，否则在600以下以type判断是否有监听，并且不主动remove监听
    public String uniqueKey;//conn message时返回的key
    private JsonConnection connection;
    private LinkedBlockingQueue<Message> queue = new LinkedBlockingQueue<>();
    private ConcurrentHashMap<Integer, OnMessageResponse> responsePool = new ConcurrentHashMap<>();
    private ExecutorService sendingService = Executors.newFixedThreadPool(5);
    private ExecutorService receiverThread = Executors.newSingleThreadExecutor();
    private ExecutorService callBackHandlerService = Executors.newFixedThreadPool(2);
    Runnable messageHandler = () -> {
        while (!end) {
            try {
                Message m = queue.take();
                OnMessageResponse r = responsePool.get(m.id);
                if (r != null) {//请求响应模式
                    handler.post(() -> r.onResponse(m));
                    responsePool.remove(m.id);
                }
                else {//非请求响应模式，下次仍然可以用
                    int type = m.type;
                    OnMessageResponse response = responsePool.get(type);
                    if(response!=null){
                        handler.post(()->r.onResponse(m));
                    }

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
    Runnable receiveWorker = () -> {
        while (!end) {
            Message message = connection.readMessage();
            try {
                queue.put(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };


    public interface OnMessageResponse<T> {
        void onResponse(Message<T> response);
    }


    public void registerSpecialCallBack(int[] types,OnMessageResponse response){
        if(types!=null&&types.length!=0){
            for(int i:types){
                responsePool.put(i,response);
            }
        }
    }
    public void registerSpecialCallBack(Map<Integer,OnMessageResponse> callbacks){
        responsePool.putAll(callbacks);
    }

    public void removeCallBacks(int[] types){
        if(types!=null&&types.length>0){
            for(int i:types){
                responsePool.remove(i);
            }
        }
    }

    public void sendLoginMessage(String name, String pwd, OnMessageResponse<AccountResponse> response) {
        sendingService.submit(() -> {
            try {
                int identity = id.getAndDecrement();
                Message m = Message.createLoginMessage(new UserInfo(name, pwd));
                m.id = identity;
                responsePool.put(identity, response);
                connection.writeJson(m.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendRoomListMessage(OnMessageResponse<StaticsResponse> response) {
        sendingService.submit(()->{
            try {
                int identity = id.incrementAndGet();
                Message m = Message.createStaticsRequest(uniqueKey);
                responsePool.put(identity,response);
                connection.writeJson(m.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public void joinRoom(String roomKey, OnMessageResponse<RoomResponse> r){
        sendingService.submit(()->{
            int identity = id.incrementAndGet();
            Message m = Message.createJoinRoomRequest(roomKey,uniqueKey);
            responsePool.put(identity,r);
            try {
                connection.writeJson(m.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void leaveRoom(String roomKey){
        sendingService.submit(()->{
            Message m = Message.createLeaveRoomRequest(roomKey,uniqueKey);
            try {
                connection.writeJson(m.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void startGame(String roomKey){
        sendingService.submit(()->{
            Message m = Message.createGameStartRequest(roomKey,uniqueKey);
            try {
                connection.writeJson(m.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void init() {
        handler = new Handler();
        connection = JsonConnection.createConnection(null);
        receiverThread.submit(receiveWorker);
        callBackHandlerService.submit(messageHandler);
        callBackHandlerService.submit(messageHandler);
    }

    public String getUniqueKey() {
        return uniqueKey;
    }
}
