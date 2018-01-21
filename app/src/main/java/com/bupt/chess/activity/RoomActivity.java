package com.bupt.chess.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.bupt.chess.R;
import com.bupt.chess.msg.data.RoomMsg;
import com.bupt.chess.msg.data.response.GameResponse;
import com.bupt.chess.msg.data.response.RoomResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bupt.chess.msg.Message.*;

/**
 * Created by xusong on 2018/1/13.
 * About:
 */

public class RoomActivity extends BaseActivity {

    @BindView(R.id.room_red)
    TextView redName;
    @BindView(R.id.room_black)
    TextView blackName;
    @BindView(R.id.room_start)
    TextView start;
    @BindView(R.id.room_talk)
    ListView talkMsg;

    //判断是不是主动退出房间
    private boolean isSubjective = true;

    List<RoomMsg> talks = new ArrayList<>();
    RoomResponse data;

    public static Gson gson = new Gson();
    private static final int[] listeningCallBacks = new int[]{TYPE_JOIN_ROOM, TYPE_SWAP_ROOM, TYPE_LEAVE_ROOM,
            TYPE_ROOM_MSG, TYPE_GAME_RESPONSE};

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
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String resp = intent.getStringExtra("response");
        data = gson.fromJson(resp, RoomResponse.class);
        start.setOnClickListener(view -> {
            if (messageManager.getUniqueKey().equals(data.master)) {
                messageManager.startGame(data.roomKey);
            } else {//
                showText("只有房主才能开启游戏！");
            }
        });

        //当这个activityfinish时，取消监听
        messageManager.registerSpecialCallBack(listeningCallBacks, (response1 -> {
            switch (response1.type) {
                case TYPE_JOIN_ROOM:
                case TYPE_SWAP_ROOM:
                    data = (RoomResponse) response1.data;
                    updatePlayerView();
                    break;
                case TYPE_LEAVE_ROOM:
                    RoomResponse r = (RoomResponse) response1.data;
                    String uniqueKey = messageManager.getUniqueKey();
                    if (uniqueKey.equals(r.red) || uniqueKey.equals(r.black)) {
                        data = r;
                        updatePlayerView();
                    } else {//我已经不在房间里
                        showText("您退出了房间 " + data.info);
                        isSubjective = false;
                        finish();
                    }
                    break;
                case TYPE_ROOM_MSG:
                    RoomMsg msg = (RoomMsg) response1.data;
                    talks.add(msg);
                    updateTalkingView();
                    break;
                case TYPE_GAME_RESPONSE:
                    GameResponse response = (GameResponse) response1.data;
                    if (response.success) {

                    } else {
                        showText("游戏开始失败！" + response.info);
                    }

            }
        }));

    }

    private void updatePlayerView() {
        redName.setText(data.red);
        blackName.setText(data.black);

    }

    private void updateTalkingView() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isSubjective) {//主动适用back键或者离开按钮
            messageManager.leaveRoom(data.roomKey);
        }
        messageManager.removeCallBacks(listeningCallBacks);
    }
}
