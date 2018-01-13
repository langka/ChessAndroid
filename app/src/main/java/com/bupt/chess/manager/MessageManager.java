package com.bupt.chess.manager;

import android.os.Handler;

import com.bupt.chess.conn.JsonConnection;
import com.bupt.chess.msg.Message;
import com.bupt.chess.msg.UserInfo;
import com.bupt.chess.msg.data.response.AccountResponse;
import com.bupt.chess.msg.data.response.StaticsResponse;

import java.io.IOException;
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
    private AtomicInteger id = new AtomicInteger(0);
    //需要回复的才有id，不需要response的id一律为负数
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
                if (r != null)
                    handler.post(() -> r.onResponse(m));
                else {

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

    public void init() {
        handler = new Handler();
        connection = JsonConnection.createConnection(null);
        receiverThread.submit(receiveWorker);
        callBackHandlerService.submit(messageHandler);
        callBackHandlerService.submit(messageHandler);
    }
}
