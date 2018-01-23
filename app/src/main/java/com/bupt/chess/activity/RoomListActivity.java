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
public class RoomListActivity extends BaseActivity {

    public static void Start(Context context) {
        Intent intent = new Intent(context, RoomListActivity.class);
        context.startActivity(intent);
    }

    View header;
    ImageView portrait;
    TextView name;
    TextView win;
    TextView lvText;
    TextView lost;
    View refresh;
    View add;

    @BindView(R.id.roomlist)
    ListView roomListView;
    @BindView(R.id.previous)
    View previous;
    @BindView(R.id.next)
    View next;

    RoomListAdapter roomAdapter;
    AccountResponse accountResponse;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roomlist);
        ButterKnife.bind(this);
        accountResponse = UserManager.getInstance().getUser();
        header = LayoutInflater.from(this).inflate(R.layout.header_roolist, null);
        portrait = (ImageView) header.findViewById(R.id.head_portrait);
        name = (TextView) header.findViewById(R.id.head_name);
        name.setText(accountResponse.name);
        lvText = (TextView) header.findViewById(R.id.head_level);
        lvText.setText("lv "+(accountResponse.win-accountResponse.lost));
        win = (TextView) header.findViewById(R.id.head_win);
        win.setText(accountResponse.win + "");
        lost = (TextView) header.findViewById(R.id.head_lost);
        lost.setText(accountResponse.lost + "");
        refresh = header.findViewById(R.id.head_refresh);
        add = header.findViewById(R.id.head_add);
        refresh.setOnClickListener(view -> getData());
        roomAdapter = new RoomListAdapter(new ArrayList<>(), this);
        roomListView.setAdapter(roomAdapter);
        roomListView.addHeaderView(header);
        roomListView.setOnItemClickListener((adapterView, view, i, l) -> {
            if (i!=0) {
                RoomData data = (RoomData) roomAdapter.getItem(i - 1);
                messageManager.joinRoom(data.key, response -> {
                    if (response.data.success) {//成功
                        RoomActivity.Start(RoomListActivity.this, response.data);
                    } else {
                        showText("进入房间失败: " + response.data.info);
                    }
                });
            }

        });
        add.setOnClickListener(view -> {
            messageManager.createRoom(response -> {
                showText("create room success");
                RoomActivity.Start(RoomListActivity.this,response.data);
            });
        });
        previous.setOnClickListener(view -> roomAdapter.previousPage());
        next.setOnClickListener(view -> roomAdapter.nextPage());
        getData();
    }

    void getData() {
        showLoadingView();
        messageManager.sendRoomListMessage(response -> {
            hideLoadingView();
            roomAdapter.updateData(response.data.rooms);
            if(response.data.rooms==null||response.data.rooms.size()==0)
                showText("当前没有房间！");
        });
    }

    private static class RoomListAdapter extends BaseAdapter {

        private List<RoomData> datas;
        private Context context;
        private int pageCount = 5;
        private int currentPage = 0;

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
            int x = datas.size();
            return x >= pageCount ? pageCount : x;
        }


        public void nextPage() {
            int total = datas.size();
            int all = total / (pageCount + 1) + 1;
            if (currentPage < all) {
                currentPage++;
                notifyDataSetChanged();
            }
        }

        public void previousPage() {
            if (currentPage > 0) {
                currentPage--;
                notifyDataSetChanged();
            }
        }

        @Override
        public Object getItem(int i) {
            return datas.get(pageCount * currentPage + i);
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
            RoomData current = datas.get(i + pageCount * currentPage);
            holder.roomName.setText(current.name);
            return view;
        }

        static class ViewHolder {
            TextView roomName;
        }

    }
}

