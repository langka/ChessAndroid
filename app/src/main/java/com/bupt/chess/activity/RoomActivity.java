package com.bupt.chess.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bupt.chess.R;
import com.bupt.chess.manager.MessageManager;
import com.bupt.chess.msg.data.NormalMessage;
import com.bupt.chess.msg.data.response.AccountResponse;
import com.bupt.chess.msg.data.response.GameResponse;
import com.bupt.chess.msg.data.response.RoomResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bupt.chess.R.id.head_win;
import static com.bupt.chess.msg.Message.*;

/**
 * Created by xusong on 2018/1/13.
 * About:
 */

public class RoomActivity extends BaseActivity {
    @BindView(R.id.red_container)
    View red;
    @BindView(R.id.black_container)
    View black;
    @BindView(R.id.swap)
    View swap;
    @BindView(R.id.leave)
    View leave;
    @BindView(R.id.kick)
    View kick;
    @BindView(R.id.start)
    View start;
    @BindView(R.id.send)
    TextView send;
    @BindView(R.id.input_message)
    EditText input;
    @BindView(R.id.room_talk)
    ListView talkMsg;

    RoomResponse data;
    public static Gson gson = new Gson();
    private static final int[] listeningCallBacks = new int[]{TYPE_JOIN_ROOM, TYPE_SWAP_ROOM, TYPE_LEAVE_ROOM, TYPE_GAME_RESPONSE};
    private List<NormalMessage> talks = new ArrayList<>();
    private boolean isSubjective = true;
    //监视房间普通消息
    private MessageManager.OnMessageResponse<NormalMessage> talkListener = r -> {
        NormalMessage msg = r.data;
        talks.add(msg);
        updateTalkingView();
    };
    //监听功能消息
    private MessageManager.OnMessageResponse roomListener = response1 -> {
        switch (response1.type) {
            //roomresponse总是返回服务端当前的房间状态，如果两个人都被踢出，那么roomresponse的red何black都为null
            case TYPE_JOIN_ROOM:
            case TYPE_SWAP_ROOM:
                data = (RoomResponse) response1.data;
                updatePlayerView();
                break;
            case TYPE_LEAVE_ROOM://受到其他人离开房屋的消息
                RoomResponse r = (RoomResponse) response1.data;
                String uniqueKey = messageManager.getUniqueKey();
                if (uniqueKey.equals(r.red) || uniqueKey.equals(r.black)) {//如果这个房间里还有我
                    data = r;
                    updatePlayerView();
                } else {//我已经不在房间里
                    showText("您退出了房间 " + data.info);
                    isSubjective = false;
                    finish();
                }
                break;
            case TYPE_GAME_RESPONSE:
                GameResponse response = (GameResponse) response1.data;
                if (response.success) {
                    GameActivity.Start(RoomActivity.this,data);
                } else {
                    showText("游戏开始失败！" + response.info);
                }

        }
    };


    public static void Start(Context context, RoomResponse response) {
        Intent intent = new Intent(context, RoomActivity.class);
        String resp = gson.toJson(response);
        intent.putExtra("response", resp);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        eventManager.getEventBus().register(this);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String resp = intent.getStringExtra("response");
        data = gson.fromJson(resp, RoomResponse.class);
        swap.setOnClickListener(view -> showText("等会实现!"));
        leave.setOnClickListener(view -> messageManager.leaveRoom(data.roomKey));
        start.setOnClickListener(view -> messageManager.startGame(data.roomKey));
        kick.setOnClickListener(view -> showText("无法踢出！"));

        send.setOnClickListener(view -> {
            String text = input.getText().toString().trim();
            if(text==null||text.equals("")){
                showText("无法发送空消息");
            }
            else{
                // TODO: 2018/1/23 send the message,show in the list
            }
        });
        messageManager.registerSpecialCallBack(listeningCallBacks, roomListener);
        messageManager.subscribeTalkMessage(talkListener);
    }

    private void updatePlayerView() {
        updateUserView(red,data.r);
        updateUserView(black,data.b);
        String key = messageManager.getUniqueKey();
        if(!key.equals(data.master)){
            kick.setVisibility(View.INVISIBLE);
            start.setVisibility(View.INVISIBLE);
        }

    }

    private void updateUserView(View v, AccountResponse r) {
        if (r != null) {
            v.findViewById(R.id.header_info_container).setVisibility(View.VISIBLE);
            v.findViewById(R.id.no_enemy).setVisibility(View.INVISIBLE);
            TextView name = (TextView) v.findViewById(R.id.head_name);
            TextView level = (TextView) v.findViewById(R.id.head_level);
            TextView win = (TextView) v.findViewById(head_win);
            TextView lost = (TextView) v.findViewById(R.id.head_lost);
            name.setText(r.name);
            win.setText(r.win + "");
            lost.setText(r.lost + "");
            level.setText("lv " + (r.win - r.lost));
        } else {
            v.findViewById(R.id.header_info_container).setVisibility(View.INVISIBLE);
            v.findViewById(R.id.no_enemy).setVisibility(View.VISIBLE);
        }
    }

    private void updateTalkingView() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eventManager.getEventBus().unregister(this);
        if (isSubjective) {//主动适用back键或者离开按钮
            messageManager.leaveRoom(data.roomKey);
        }
        messageManager.removeCallBacks(listeningCallBacks);
    }
}
