package com.bupt.chess.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bupt.chess.ChessView;
import com.bupt.chess.R;
import com.bupt.chess.manager.EventConsts;
import com.bupt.chess.msg.Message;
import com.bupt.chess.msg.data.MoveRequest;
import com.bupt.chess.msg.data.response.RoomResponse;

import org.greenrobot.eventbus.Subscribe;

import static com.bupt.chess.activity.RoomActivity.gson;

/**
 * Created by xusong on 2018/1/14.
 * About:
 */

public class GameActivity extends BaseActivity {

    public static void Start(Context context, RoomResponse response) {
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra("room", gson.toJson(response));
        context.startActivity(intent);
    }

    ChessView chessView;
    //初始化数据
    RoomResponse data;

    @Subscribe
    public void onEventMainThread(EventConsts ev) {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        chessView = (ChessView) findViewById(R.id.chessview);
        Intent intent = getIntent();
        String resp = intent.getStringExtra("response");
        // data = gson.fromJson(resp, RoomResponse.class);
        chessView.init(false);
        chessView.setStepMoveListener((x, y, w, z) -> {
            chessView.setOk(false);
            Message<MoveRequest> msg = new Message<>();
            msg.type = Message.TYPE_MOVE_REQUEST;
            messageManager.movePiece(data.roomKey, x, y, w, z);
        });
    }
}
