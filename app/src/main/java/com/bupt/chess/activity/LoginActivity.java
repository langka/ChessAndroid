package com.bupt.chess.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bupt.chess.R;
import com.bupt.chess.manager.UserManager;
import com.bupt.chess.msg.data.response.AccountResponse;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    // 三方账号没有绑定手机号的code
    private static final int ERROR_TOKEN_CODE = 100119;

    @BindView(R.id.innerroot)
    RelativeLayout root;

    @BindView(R.id.header)
    RelativeLayout header;

    @BindView(R.id.content_container)
    LinearLayout moverView;

    @BindView(R.id.login_account)
    EditText accountEditText;//手机号码域

    @BindView(R.id.login_pwd)
    EditText pwdEditText;

    @BindView(R.id.login_submit)
    TextView submitTextView;//登录按钮

    @BindView(R.id.login_register)
    TextView registerTextView;//注册文字
    @BindView(R.id.login_other_container)
    View otherWay;

    Handler handler = new Handler();

    //静态启动方法
    public static void Start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initView();
        controlKeyboardLayout(root, moverView);
    }


    //初始化其他变量代码
    private void initData() {
    }

    private void initView() {//注册事件监听器
        setTitle("登录");
        accountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && pwdEditText.getText().length() > 0) {
                    submitTextView.setTextColor(getResources().getColor(R.color.B1));
                } else {
                    submitTextView.setTextColor(getResources().getColor(R.color.TRANS_W1));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        pwdEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && accountEditText.getText().length() > 0) {
                    submitTextView.setTextColor(getResources().getColor(R.color.W1));
                } else {
                    submitTextView.setTextColor(getResources().getColor(R.color.TRANS_W1));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        otherWay.setOnClickListener(view -> showText("其它登录方式暂不支持！"));
        pwdEditText.setOnKeyListener((view, keyCode, keyEvent) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                login();
                return true;
            }
            return false;
        });

        registerTextView.setOnClickListener(new View.OnClickListener() {//跳转至注册
            @Override
            public void onClick(View v) {
            }
        });
        submitTextView.setOnClickListener(new View.OnClickListener() {//启动登录流程
            @Override
            public void onClick(View v) {//登录按钮
                login();
            }
        });

    }

    private void login() {
        showLoadingView();
        String account = accountEditText.getText().toString().trim();
        String pwd = pwdEditText.getText().toString().trim();
        messageManager.sendLoginMessage(account,pwd,(resp)->{
            hideLoadingView();
            if(!resp.data.success){
                showText("登陆失败:"+resp.data.info);
            }else {
                UserManager.getInstance().setUser(resp.data);
              RoomListActivity.Start(LoginActivity.this);
            }
        });

    }


    private void controlKeyboardLayout(final View root, final View targetView) {
        root.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect rect = new Rect();
                        root.getWindowVisibleDisplayFrame(rect);
                        int rootInvisibleHeight = root.getRootView()
                                .getHeight() - rect.bottom;
                        Log.i(TAG, "最外层的高度" + root.getRootView().getHeight());
                        // 若rootInvisibleHeight高度大于100，则说明当前视图上移了，说明软键盘弹出了
                        //给它上移90
                        if (rootInvisibleHeight > 100) {
                            targetView.scrollTo(0, 90);
                        } else {
                            // 软键盘没有弹出来的时候
                            targetView.scrollTo(0, 0);
                        }
                    }
                });
    }


}
