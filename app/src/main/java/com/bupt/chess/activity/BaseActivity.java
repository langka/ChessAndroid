package com.bupt.chess.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bupt.chess.R;
import com.bupt.chess.manager.EventManager;
import com.bupt.chess.manager.MessageManager;
import com.wang.avi.AVLoadingIndicatorView;

import org.greenrobot.eventbus.Subscribe;

import java.math.BigDecimal;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xusong on 2018/1/10.
 * About:
 */

public class BaseActivity extends FragmentActivity {


    protected MessageManager messageManager;
    AnimationDrawable loadingAnimationDrawable;
    AnimationDrawable dialogAnimationDrawable;
    long totalTimeSeconds = 30;
    long beginTime = -1;//记录验证码发送时间

    private Handler shareHandler = new Handler();
    private View loadingView;


    @Subscribe
    public void onEvent(String event) {
        Log.d("Event", "default handler");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventManager.GetInstance().getEventBus().register(this);
        messageManager = MessageManager.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // activity state

        // umeng stat
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.GetInstance().getEventBus().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void setHeaderDividerVisibility(boolean visibility) {
        if (visibility) {
            findViewById(R.id.header_divide).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.header_divide).setVisibility(View.GONE);
        }
    }

    public void setHeaderBg(int res) {
        View header = findViewById(R.id.header);
        header.setBackgroundColor(res);
    }

    public View addBackBtn() {
        View back = findViewById(R.id.header_left_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        back.setVisibility(View.VISIBLE);
        return back;
    }

    public ImageButton setBackBtn(int resId) {
        ImageButton imageButton = (ImageButton) findViewById(R.id.header_left_btn);
        imageButton.setVisibility(View.VISIBLE);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imageButton.setImageResource(resId);
        return imageButton;
    }

    public void setTitle(String title) {
        TextView textView = (TextView) findViewById(R.id.header_title);
        textView.setVisibility(View.VISIBLE);
        if (title != null) {
            textView.setText(title);
        } else {
            textView.setText("");
        }
    }

    public TextView getTitleTextView() {
        TextView textView = (TextView) findViewById(R.id.header_title);
        return textView;
    }

    public void setHeaderLeftImage(int resId, View.OnClickListener listener) {
        ImageButton button = (ImageButton) findViewById(R.id.header_left_btn);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(listener);
        button.setImageDrawable(getResources().getDrawable(resId));
    }

    public void setHeaderLeftImageWithSize(int resId, int width, int height, View.OnClickListener listener) {
        ImageButton button = (ImageButton) findViewById(R.id.header_left_btn);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(listener);
        button.setImageDrawable(getResources().getDrawable(resId));
        ViewGroup.LayoutParams layoutParams = button.getLayoutParams();
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        button.setLayoutParams(layoutParams);
    }

    public TextView getRightTextView() {
        TextView right = (TextView) findViewById(R.id.header_right_text_btn);
        right.setVisibility(View.VISIBLE);
        return right;
    }

    public ImageView getRightImage() {
        ImageView imageView = (ImageView) findViewById(R.id.header_right_btn);
        imageView.setVisibility(View.VISIBLE);
        return imageView;
    }

    public void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        imm.showSoftInput(findViewById(R.id.root), InputMethodManager.SHOW_FORCED);
        imm.hideSoftInputFromWindow(findViewById(R.id.root).getWindowToken(), 0); //强制隐藏键盘
    }
    public void showLoadingView() {
        View v = findViewById(R.id.loadingview);
        v.setVisibility(View.VISIBLE);
        AVLoadingIndicatorView loadingIndicatorView = (AVLoadingIndicatorView) v.findViewById(R.id.loading_loader);
        loadingIndicatorView.show();
    }

    public void hideLoadingView() {
        View v = findViewById(R.id.loadingview);
        v.setVisibility(View.INVISIBLE);
        AVLoadingIndicatorView loadingIndicatorView = (AVLoadingIndicatorView) v.findViewById(R.id.loading_loader);
        loadingIndicatorView.hide();
    }


    public void showText(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

}
