package com.bupt.chess.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.bupt.chess.R;
import com.bupt.chess.msg.data.response.RoomResponse;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;

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

    static Gson gson = new Gson();
    public static void Start(Context context, RoomResponse response){
        Intent intent = new Intent(context,RoomActivity.class);
        String resp=gson.toJson(response);
        intent.putExtra("response",resp);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        // TODO: 2018/1/13 对于创建房间的人，需要监听他人加入房间的事件。对于加入房间的人则不需要；双方都需要监听离开，开始，交换的事件
        //当这个activityfinish时，取消监听
    }


}
