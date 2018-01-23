package com.bupt.chess.manager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.bupt.chess.conn.JsonConnection;
import com.bupt.chess.msg.Message;
import com.bupt.chess.msg.UserInfo;
import com.bupt.chess.msg.data.MoveRequest;
import com.bupt.chess.msg.data.NormalMessage;
import com.bupt.chess.msg.data.response.AccountResponse;
import com.bupt.chess.msg.data.response.RoomResponse;
import com.bupt.chess.msg.data.response.StaticsResponse;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by xusong on 2018/1/10.
 * About:
 */

public class MessageManager {


    public static final String TAG = "MESSAGEMANAGER";
    private static MessageManager manager = new MessageManager();

    public static MessageManager getInstance() {
        return manager;
    }

    private MessageManager() {
    }

    private Handler handler;

    private volatile boolean inited = false;
    private volatile boolean end = false;
    private AtomicInteger id = new AtomicInteger(600);
    //请求响应模式的id一律在600以上，一次相应后直接remove，否则在600以下以type判断是否有监听，并且不主动remove监听
    public String uniqueKey;//conn message时返回的key
    private Set<OnMessageResponse<NormalMessage>> msgListeners = new HashSet<>(6);
    private JsonConnection connection;
    private LinkedBlockingQueue<Message> queue = new LinkedBlockingQueue<>();
    private ConcurrentHashMap<Integer, OnMessageResponse> responsePool = new ConcurrentHashMap<>();
    private ExecutorService sendingService = Executors.newFixedThreadPool(2);
    private ExecutorService receiverThread = Executors.newSingleThreadExecutor();
    private ExecutorService callBackHandlerService = Executors.newFixedThreadPool(2);
    Runnable messageHandler = () -> {
        while (!end) {
            try {
                Message m = queue.take();
                if (m != null) {
                    if (m.type == Message.TYPE_ROOM_MSG) {
                        if (msgListeners.size() != 0)
                            for (OnMessageResponse r : msgListeners) {
                                r.onResponse(m);
                            }
                        continue;
                    }
                    OnMessageResponse r = responsePool.get(m.id);
                    if (r != null) {//请求响应模式
                        handler.post(() -> r.onResponse(m));
                        responsePool.remove(m.id);
                    } else {//非请求响应模式，下次仍然可以用
                        int type = m.type;
                        OnMessageResponse response = responsePool.get(type);
                        if (response != null) {
                            handler.post(() -> r.onResponse(m));
                        }else{
                            log("find a message without callback id: "+m.id+" type:"+type);
                        }

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
            log("new message arrived");
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


    public void registerSpecialCallBack(int[] types, OnMessageResponse response) {
        if (types != null && types.length != 0) {
            for (int i : types) {
                responsePool.put(i, response);
            }
        }
    }

    public void registerSpecialCallBack(Map<Integer, OnMessageResponse> callbacks) {
        responsePool.putAll(callbacks);
    }

    public void removeCallBacks(int[] types) {
        if (types != null && types.length > 0) {
            for (int i : types) {
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
                m.key = uniqueKey;
                responsePool.put(identity, response);
                connection.writeJson(m.toString());
            } catch (IOException e) {
                Log.d(TAG,"send login failed");
                e.printStackTrace();
            }
        });
    }

    public void sendRoomListMessage(OnMessageResponse<StaticsResponse> response) {
        sendingService.submit(() -> {
            try {
                int identity = id.incrementAndGet();
                Message m = Message.createStaticsRequest(uniqueKey);
                m.id = identity;
                responsePool.put(identity, response);
                connection.writeJson(m.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public void createRoom(OnMessageResponse<RoomResponse> r){
        sendingService.submit(() -> {
            int identity = id.incrementAndGet();
            Message m = Message.createCreateRoomRequest( uniqueKey,"快来一起战斗吧",0,"");
            m.id = identity;
            responsePool.put(identity, r);
            try {
                connection.writeJson(m.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void joinRoom(String roomKey, OnMessageResponse<RoomResponse> r) {
        sendingService.submit(() -> {
            int identity = id.incrementAndGet();
            Message m = Message.createJoinRoomRequest(roomKey, uniqueKey);
            m.id = identity;
            responsePool.put(identity, r);
            try {
                connection.writeJson(m.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void leaveRoom(String roomKey) {
        sendingService.submit(() -> {
            Message m = Message.createLeaveRoomRequest(roomKey, uniqueKey);
            try {
                connection.writeJson(m.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void movePiece(String roomKey, int fx, int fy, int tx, int ty) {
        sendingService.submit(() -> {
            Message m = new Message();
            m.type = Message.TYPE_MOVE_REQUEST;
            m.key = uniqueKey;
            MoveRequest request = new MoveRequest();
            request.fromX = fx;
            request.fromY = fy;
            request.toX = tx;
            request.toY = ty;
            request.room = roomKey;
            m.data = request;
            try {
                connection.writeJson(m.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }



    public void startGame(String roomKey) {
        sendingService.submit(() -> {
            Message m = Message.createGameStartRequest(roomKey, uniqueKey);
            try {
                connection.writeJson(m.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void init(Context context) {
        handler = new Handler();
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            boolean b = appInfo.metaData.getBoolean("IS_DEBUG");
            if (!b) {
                new Thread(() -> {
                    try {
                        Socket s = new Socket("10.209.8.206", 9876);
                        handler.post(()->Toast.makeText(context,"connected to server",Toast.LENGTH_SHORT).show());
                        connection = JsonConnection.createConnection(s);
                        Message msg=Message.createConnMessage();
                        connection.writeJson(msg.toString());
                        msg = connection.readMessage();
                        uniqueKey = msg.key;
                        log("unikey get: "+uniqueKey);
                        receiverThread.submit(receiveWorker);
                        callBackHandlerService.submit(messageHandler);
                        callBackHandlerService.submit(messageHandler);
                        inited = true;
                    } catch (IOException e) {
                        handler.post(() -> {
                            Toast.makeText(context, "与服务器链接失败", Toast.LENGTH_SHORT).show();
                        });
                        e.printStackTrace();
                    }
                }).start();
            } else {//debug模式，虚拟出数据
                try {
                    connection = JsonConnection.createDemoConn(context);
                    inited = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "加载失败", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void subscribeTalkMessage(OnMessageResponse<NormalMessage> listener) {
        msgListeners.add(listener);
    }

    public void unSubscribeTalkMessage(OnMessageResponse<NormalMessage> listener) {
        msgListeners.remove(listener);
    }

    private void log(String x){
        Log.d(TAG,x);
    }


}
