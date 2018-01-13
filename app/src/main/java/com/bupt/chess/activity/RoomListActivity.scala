package com.bupt.chess.activity

import java.util

import android.app.Activity
import android.content.{Context, Intent}
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{BaseAdapter, ListView, TextView}
import butterknife.{BindView, ButterKnife}
import com.bupt.chess.R
import com.bupt.chess.manager.MessageManager.OnMessageResponse
import com.bupt.chess.msg.Message
import com.bupt.chess.msg.data.RoomData
import com.bupt.chess.msg.data.response.StaticsResponse

/**
  * Created by xusong on 2018/1/12.
  * About:
  */
class RoomListActivity extends BaseActivity {

  @BindView(R.id.roomlist)
  var roomListView: ListView = _
  @BindView(R.id.createroom)
  var createTextView: TextView = _

  var roomAdapter:RoomListAdapter = _

  type MResp = Message[StaticsResponse] => Unit

  implicit def function2CallBack(f: MResp): OnMessageResponse[StaticsResponse] = new OnMessageResponse[StaticsResponse] {
    override def onResponse(response: Message[StaticsResponse]): Unit = f(response)
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_roomlist)
    ButterKnife.bind(this)
    roomAdapter = new RoomListAdapter(this)
    roomListView.setAdapter(roomAdapter)
  }

  def initData(): Unit = {
    showLoadingView()
    messageManager.sendRoomListMessage((resp => {
      showText("数据加载完成")
      hideLoadingView()
      roomAdapter.rooms = resp.data.rooms
      roomAdapter.notifyDataSetChanged()
    }): MResp)
  }
}
object RoomListActivity{
  def Start(context:Context):Unit = {
    val intent = new Intent(context,classOf[RoomListActivity])
    context.startActivity(intent)
  }
}

class RoomListAdapter(context: Context) extends BaseAdapter {
  type JList[A] = java.util.List[A]
  val inflater = LayoutInflater.from(context)
  var rooms: JList[RoomData] = new util.ArrayList[RoomData]()

  def setNewData(rooList: java.util.List[RoomData]): Unit = rooms = rooList

  override def getItemId(i: Int): Long = 0

  override def getCount: Int = rooms.size

  override def getView(i: Int, view: View, viewGroup: ViewGroup): View = {
    val a = if (view == null) {
      val v = inflater.inflate(R.layout.item_room, null)
      val holder = new ViewHolder
      holder.name = v.findViewById(R.id.item_room_name).asInstanceOf[TextView]
      v.setTag(holder)
      (v, holder)
    } else {
      val holder: ViewHolder = view.getTag.asInstanceOf[ViewHolder]
      (view, holder)
    }
    val data = rooms.get(i)
    a._2.name.setText(data.name + "   " + data.key)
    a._1
  }

  override def getItem(i: Int): AnyRef = rooms.get(i)
}

class ViewHolder {
  var name: TextView = _
}