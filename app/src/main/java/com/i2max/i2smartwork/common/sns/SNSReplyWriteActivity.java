package com.i2max.i2smartwork.common.sns;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;
import com.squareup.okhttp.FormEncodingBuilder;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SNSReplyWriteActivity extends BaseAppCompatActivity {
    static String TAG = SNSReplyWriteActivity.class.getSimpleName();

    protected String mPostID, mTarObjTp, mTarObjId;
    protected EditText etReply;

    protected List<String> linkUserIdList = new ArrayList<>();
    protected List<String> linkUserNmList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_reply_write);

        mPostID = getIntent().getStringExtra(CodeConstant.POST_ID);
        mTarObjTp = getIntent().getStringExtra(CodeConstant.TAR_OBJ_TP);
        mTarObjId = getIntent().getStringExtra(CodeConstant.TAR_OBJ_ID);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("댓글 쓰기");

        etReply = (EditText) findViewById(R.id.et_reply);

        etReply.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (v.getId() == etReply.getId() && actionId == EditorInfo.IME_ACTION_DONE) {
                    saveReply();
                }

                return false;
            }
        });

        etReply.postDelayed(new Runnable() {

            public void run() {
                etReply.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                etReply.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));

            }
        }, 200);

        Button btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnConfirm = (Button) findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveReply();
            }
        });

        Button btnUserSearch = (Button) findViewById(R.id.btn_user_search);
        btnUserSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SNSReplyWriteActivity.this, SNSPersonSearchActivity.class);
                intent.putExtra(SNSPersonSearchActivity.USR_ID, PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
                intent.putExtra(SNSPersonSearchActivity.MODE, CodeConstant.MODE_LINK_ADD);
                startActivityForResult(intent, SNSPersonSearchActivity.REQUEST_FRIEND_SEARCH);
            }
        });

    }

    public void saveReply() {
        if (etReply.getText().length() == 0) {
            DialogUtil.showInformationDialog(SNSReplyWriteActivity.this, "내용을 입력해주십시오.");
            return;
        }


        FormEncodingBuilder userLinkBuilder = null;

        if (linkUserIdList.size() > 0) {
            for (int i=linkUserIdList.size()-1; i>=0; i--) {
                String body = etReply.getText().toString();
                if (!body.contains(linkUserNmList.get(i))){
                    linkUserIdList.remove(i);
                    linkUserNmList.remove(i);
                }
            }

            userLinkBuilder = I2UrlHelper.SNS.getLinkUserFormBuilder(linkUserIdList, linkUserNmList);
        }

        I2ConnectApi.requestJSON(SNSReplyWriteActivity.this, I2UrlHelper.SNS.saveSnsPostReply(mPostID, mTarObjTp, mTarObjId, etReply.getText().toString(), userLinkBuilder))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.saveSnsPost onCompleted");

                        DialogUtil.showDialog(SNSReplyWriteActivity.this, "안내", "댓글 쓰기가 완료되었습니다.",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SNSMainFragment.isChangedList = true;

                                        Intent returnIntent = new Intent();
                                        setResult(RESULT_OK,returnIntent);
                                        finish();
                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.saveSnsPost onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSReplyWriteActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.saveSnsPost onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            //JSONArray statusInfoArray = I2ResponseParser.getStatusInfoArray(jsonObject);

                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected boolean bringKeyboard(EditText view) {
        if (view == null) {
            return false;
        }
        try {
            String value = view.getText().toString();
            if (value == null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "decideFocus. Exception", e);
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {

            case SNSPersonSearchActivity.REQUEST_FRIEND_SEARCH:
                if (data != null) {
                    String temp = etReply.getText().toString();
                    linkUserIdList.add(data.getExtras().getString("usr_id"));
                    linkUserNmList.add(data.getExtras().getString("usr_nm"));
                    temp = temp + "@[" + data.getExtras().getString("usr_nm") + "]";

                    etReply.setText(temp);

                    etReply.setSelection(etReply.getText().length());
                }
                break;

        }
    }
}
