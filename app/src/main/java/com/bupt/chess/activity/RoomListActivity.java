package com.bupt.chess.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bupt.chess.R;
import com.bupt.chess.manager.UserManager;
import com.bupt.chess.msg.data.RoomData;
import com.bupt.chess.msg.data.response.AccountResponse;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xusong on 2018/1/12.
 * About:
 */
class RoomListActivity extends BaseActivity {

    public static void Start(Context context) {
        Intent intent = new Intent(context, RoomListActivity.class);
        context.startActivity(intent);
    }

    View header;
    ImageView portrait;
    TextView name;
    TextView win;
    TextView lost;
    View refresh;

    @BindView(R.id.roomlist)
    ListView roomListView;
    @BindView(R.id.createroom)
    TextView createTextView;

    RoomListAdapter roomAdapter;
    AccountResponse accountResponse;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roomlist);
        ButterKnife.bind(this);
        accountResponse = UserManager.getInstance().getUser();
        header = LayoutInflater.from(this).inflate(R.layout.header_roolist, null);
        portrait = (ImageView) header.findViewById(R.id.head_icon);
        name = (TextView) header.findViewById(R.id.head_name);
        name.setText(accountResponse.name);
        win = (TextView) header.findViewById(R.id.head_win);
        win.setText(accountResponse.win+"");
        lost = (TextView) header.findViewById(R.id.head_lost);
        lost.setText(accountResponse.lost+"");
        refresh = header.findViewById(R.id.refresh);
        refresh.setOnClickListener(view -> getData());
        roomAdapter = new RoomListAdapter(new ArrayList<>(), this);
        roomListView.setAdapter(roomAdapter);
        roomListView.addHeaderView(header);
        roomListView.setOnItemClickListener((adapterView, view, i, l) -> {
            RoomData data = (RoomData) roomAdapter.getItem(i - 1);
            messageManager.joinRoom(data.key, response -> {
                if (response.data.success) {//成功
                    RoomActivity.Start(RoomListActivity.this, response.data);
                } else {
                    showText("进入房间失败: " + response.data.info);
                }
            });

        });
        getData();
    }

    void getData() {
        showLoadingView();
        messageManager.sendRoomListMessage(response -> {
            hideLoadingView();
            roomAdapter.updateData(response.data.rooms);
        });
    }

    private static class RoomListAdapter extends BaseAdapter {

        private List<RoomData> datas;
        private Context context;

        public void updateData(List<RoomData> datas) {
            if (datas != null)
                this.datas = datas;
            else this.datas = new ArrayList<>();
            notifyDataSetChanged();
        }

        public RoomListAdapter(List<RoomData> datas, Context context) {
            this.datas = datas;
            this.context = context;
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int i) {
            return datas.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if (view == null) {
                holder = new ViewHolder();
                view = LayoutInflater.from(context).inflate(R.layout.item_room, null);
                holder.roomName = (TextView) view.findViewById(R.id.item_room_name);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            RoomData current = datas.get(i);
            holder.roomName.setText(current.name);
            return view;
        }

        static class ViewHolder {
            TextView roomName;
        }

    }
}

