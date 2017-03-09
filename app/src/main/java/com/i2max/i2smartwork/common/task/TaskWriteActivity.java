package com.i2max.i2smartwork.common.task;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.datetimepicker.date.DatePickerDialog;
import com.bumptech.glide.Glide;
import com.google.gson.internal.LinkedTreeMap;
import com.i2max.i2smartwork.Manifest;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.common.conference.ConferenceDetailActivity;
import com.i2max.i2smartwork.common.memo.MemoDetailActivity;
import com.i2max.i2smartwork.common.sns.SNSPersonSearchActivity;
import com.i2max.i2smartwork.common.work.WorkDetailActivity;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FileUtil;
import com.i2max.i2smartwork.utils.FormatUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;
import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;
import com.squareup.okhttp.FormEncodingBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.R.attr.mode;

/**
 * Created by shlee on 2015. 10. 16..
 */
public class TaskWriteActivity extends BaseAppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 3;
    static String TAG = TaskWriteActivity.class.getSimpleName();

    protected final int REQUEST_FILE_KITKAT_INTENT_CALLED = 2103; //키캣 이상 문서파일 열기 처리
    protected final int REQUEST_FILE = 2104; //키캣 이전버젼 문파서일 열기처리
    protected final int REQUEST_REST_KITKAT_INTENT_CALLED = 2113; //키캣 이상 문서파일 열기 처리
    protected final int REQUEST_REST = 2114; //키캣 이전버젼 문파서일 열기처리

    protected RelativeLayout mRlRestCntn;
    protected LinearLayout mLlTaskFile, mLlTaskRest;
    protected EditText mEtTtl, mEtTaskOrd, mEtCntn, mEtRestCntn;
    protected TextView mTvCrtDttm, mTvEmptyMember, mTvEmptyFile, mTvEmptyRest, mTvTarObjTtl;
    protected Spinner mSpTaskSt, mSpPriority;
    protected Button mBtStartDt, mBtEndDt;
    protected ImageView mIbAddMember, mIbAddFiles, mIbAddRest, mIbSave;
    protected RecyclerView mRvMember;
    protected TaskMemberRecyclerViewAdapter mAdapter;

    private int mUploadedCnt = 0;
    private List<List> mTotalList;
    private List<Map<String, String>> mMemberList = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> mFileList = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> mRestList = new ArrayList<Map<String, String>>();

    final String mObjTp = CodeConstant.TYPE_TASK;
    protected String mTarObjTp = "", mTarObjId = "", mTarObjTtl = "", mTarCrtUsrId = "", mObjId = "", mCrtUsrId = "", mTaskSt = "", mImpTp = "";
    protected Calendar now;
    int mTabPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_task_write);

        Bundle extra = getIntent().getExtras();
        String title = getString(R.string.task_add);
        if (extra != null) {
            title = extra.getString(CodeConstant.TITLE, getString(R.string.task_add));
            mTarObjTp = extra.getString(CodeConstant.TAR_OBJ_TP, "");
            mTarObjId = extra.getString(CodeConstant.TAR_OBJ_ID, "");
            mTarObjTtl = extra.getString(CodeConstant.TAR_OBJ_TTL, "");
            mTarCrtUsrId = extra.getString(CodeConstant.TAR_CRT_USR_ID, "");
            mObjId = extra.getString(CodeConstant.TASK_ID, "");
            mCrtUsrId = extra.getString(CodeConstant.CRT_USR_ID, "");
            mTabPos = extra.getInt(CodeConstant.TAB_POS, 1);
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(title);

        mEtTtl = (EditText) findViewById(R.id.et_ttl);
        mTvTarObjTtl = (TextView) findViewById(R.id.tv_tar_obj_ttl);
        mEtTaskOrd = (EditText) findViewById(R.id.et_priority_no);
        mEtCntn = (EditText) findViewById(R.id.et_cntn);

        mTvCrtDttm = (TextView) findViewById(R.id.tv_crt_dttm);

        mSpTaskSt = (Spinner) findViewById(R.id.sp_task_st);
        mSpPriority = (Spinner) findViewById(R.id.sp_task_priority);
        mBtStartDt = (Button) findViewById(R.id.bt_start_dt);
        mBtEndDt = (Button) findViewById(R.id.bt_end_dt);

        mIbAddMember = (ImageView) findViewById(R.id.ib_add_member);
        mRvMember = (RecyclerView) findViewById(R.id.rv_task_member);
        mTvEmptyMember = (TextView) findViewById(R.id.tv_task_empty_member);

        mIbAddFiles = (ImageView) findViewById(R.id.ib_add_file);
        mLlTaskFile = (LinearLayout) findViewById(R.id.ll_task_file_list);
        mTvEmptyFile = (TextView) findViewById(R.id.tv_task_empty_file);

        mRlRestCntn = (RelativeLayout) findViewById(R.id.rl_task_rest_list);
        mEtRestCntn = (EditText) findViewById(R.id.et_rest_cntn);
        mIbAddRest = (ImageView) findViewById(R.id.ib_add_rest);
        mLlTaskRest = (LinearLayout) findViewById(R.id.ll_task_rest_list);
        mTvEmptyRest = (TextView) findViewById(R.id.tv_task_empty_rest);

        mIbSave = (ImageView) findViewById(R.id.iv_bt_save);


        GridLayoutManager layoutManager = new GridLayoutManager(this, 5);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRvMember.setLayoutManager(layoutManager);

        initView();
    }

    public void initView() {
        now = Calendar.getInstance();

        mBtStartDt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.newInstance(TaskWriteActivity.this, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show(getFragmentManager(), "start_dt");
            }
        });


        mBtEndDt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.newInstance(TaskWriteActivity.this, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show(getFragmentManager(), "end_dt");
            }
        });

        mIbAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Search
                Intent intent = new Intent(TaskWriteActivity.this, SNSPersonSearchActivity.class);
                intent.putExtra(SNSPersonSearchActivity.USR_ID, PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
                intent.putExtra(SNSPersonSearchActivity.MODE, CodeConstant.MODE_TASK_MEMBER_ADD);
                startActivityForResult(intent, SNSPersonSearchActivity.REQUEST_FRIEND_SEARCH);
            }
        });

        mMemberList = new ArrayList<Map<String, String>>();
        mAdapter = new TaskMemberRecyclerViewAdapter(TaskWriteActivity.this, mMemberList);
        mRvMember.setAdapter(mAdapter);

        mIbAddFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toast.makeText(TaskWriteActivity.this, "클릭하였습니다", Toast.LENGTH_SHORT).show();
                // 갤러리 사용권한 체크(사용권한이 없을 경우 -1)
                if (ContextCompat.checkSelfPermission(TaskWriteActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 사용권한이 없을 경우

                    // 최초 권한 요청인지, 혹은 사용자에 의한 재요청인지 확인
                    if (ActivityCompat.shouldShowRequestPermissionRationale(TaskWriteActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // 사용자가 임의로 권한을 취소시킨 경우
                        // 권한 재요청
                        ActivityCompat.requestPermissions(TaskWriteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_STORAGE);
                    } else {
                        // 최초로 권한을 요청하는 경우 (첫실행)
                        ActivityCompat.requestPermissions(TaskWriteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_STORAGE);
                    }
                } else {
                    // 사용 권한이 있음을 확인한 경우
                    openFileChooser(REQUEST_FILE, REQUEST_FILE_KITKAT_INTENT_CALLED);
                }
            }
        });

        mIbAddRest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser(REQUEST_REST, REQUEST_REST_KITKAT_INTENT_CALLED);
            }
        });

        mIbSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTaskProc();
            }
        });
        if (!TextUtils.isEmpty(mObjId)) {
            loadEditViewTask();
        } else {
            mTvCrtDttm.setText(FormatUtil.getSendableFormatNow());

            mEtTtl.setText("작업 (" + PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_NM) + ") " + FormatUtil.getSendableFormatNow());
            mSpTaskSt.setSelection(1); //기본 대기
            mSpPriority.setSelection(2); // 기본 중간
            mEtCntn.setText("To-Do");

            mBtStartDt.setText(FormatUtil.getSendableFormat3Today());
            mBtEndDt.setText(FormatUtil.getSendableFormat3Today());
            Map<String, String> person = new HashMap<String, String>();
            person.put("post_usr_id", "");
            person.put("post_id", "");
            person.put("usr_id", PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
            person.put("usr_nm", PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_NM));
            person.put("photo_img_min", PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_PHOTO));
            person.put("task_usr_id", PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
            person.put("fnl_appr_yn", "N"); //신규 참가자

            //중복체크
            for (int i = 0; i < mMemberList.size(); i++) {
                if (person.get("usr_id").equals(mMemberList.get(i).get("usr_id"))) {
                    Toast.makeText(TaskWriteActivity.this, "중복된 참가자입니다", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            //set image
            if (mMemberList == null) mMemberList = new ArrayList<Map<String, String>>();
            mMemberList.add(person);
            if (mMemberList == null || mMemberList.size() < 1) {
                mRvMember.setVisibility(View.GONE);
                mTvEmptyMember.setVisibility(View.VISIBLE);
            } else {
                mRvMember.setVisibility(View.VISIBLE);
                mTvEmptyMember.setVisibility(View.GONE);
            }
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                // 갤러리 사용권한에 대한 콜백을 받음
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한 동의 버튼 선택
                    openFileChooser(REQUEST_FILE, REQUEST_FILE_KITKAT_INTENT_CALLED);
                } else {
                    // 사용자가 권한 동의를 안 함
                    // 권한 동의안함 버튼 선택
                    Toast.makeText(this, "권한 사용을 동의해 주셔야 이용이 가능합니다", Toast.LENGTH_LONG).show();
                    // finish(); // 확인해 봐야 한다.
                }
                return;
            }
            // 예외 케이스
        }
    }

    public void loadEditViewTask() {
        I2ConnectApi.requestJSON2Map(TaskWriteActivity.this, I2UrlHelper.Task.getViewSnsTask(mObjId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Task.getViewSnsTask onCompleted");
                        loadEditViewTaskFile();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Task.getViewSnsTask onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(TaskWriteActivity.this, e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Task.getViewSnsTask onNext");
                        LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");
                        setTaskViewData(statusInfo);
                    }
                });
    }

    public void loadEditViewTaskFile() {
        I2ConnectApi.requestJSON2Map(TaskWriteActivity.this, I2UrlHelper.Task.getListSnsTaskFile(mObjId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Task.getListSnsTaskFile onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Task.getListSnsTaskFile onError");
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(TaskWriteActivity.this, e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Task.getListSnsTaskFile onNext");
                        LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");
                        setTaskFileData(statusInfo);    // 서버에서 등록된 파일리스트 정보를 받아서 setTaskFileData 호출
                    }
                });
    }


    public void setTaskViewData(LinkedTreeMap<String, Object> statusInfo) {
        final String crtUsrId = FormatUtil.getStringValidate(statusInfo.get("crt_usr_id"));
        final String crtUsrNm = FormatUtil.getStringValidate(statusInfo.get("crt_usr_nm"));
        final String tarObjTp = FormatUtil.getStringValidate(statusInfo.get("tar_obj_tp_cd"));
        final String tarObjId = FormatUtil.getStringValidate(statusInfo.get("tar_obj_id"));
        final String tarUsrId = FormatUtil.getStringValidate(statusInfo.get("tar_usr_id"));

        mEtTtl.setText(statusInfo.get("ttl").toString());
        mEtTaskOrd.setText(FormatUtil.getStringValidate(statusInfo.get("task_ord")));
        mTvCrtDttm.setText(FormatUtil.getFormattedDateTime(FormatUtil.getStringValidate(statusInfo.get("crt_dttm")))); //만든시간 표시

        if ("대기".equals(FormatUtil.getStringValidate(statusInfo.get("task_st_nm")))) {
            mSpTaskSt.setSelection(1);
        } else if ("진행".equals(FormatUtil.getStringValidate(statusInfo.get("task_st_nm")))) {
            mSpTaskSt.setSelection(2);
        } else if ("종료".equals(FormatUtil.getStringValidate(statusInfo.get("task_st_nm")))) {
            mSpTaskSt.setSelection(3);
        }

        if ("낮음".equals(FormatUtil.getStringValidate(statusInfo.get("impt_tp_nm")))) {
            mSpPriority.setSelection(1);
        } else if ("중간".equals(FormatUtil.getStringValidate(statusInfo.get("impt_tp_nm")))) {
            mSpPriority.setSelection(2);
        } else if ("종료".equals(FormatUtil.getStringValidate(statusInfo.get("impt_tp_nm")))) {
            mSpPriority.setSelection(3);
        }

        mBtStartDt.setText(FormatUtil.getFormattedDateTime3(FormatUtil.getStringValidate(statusInfo.get("start_dttm"))));
        mBtEndDt.setText(FormatUtil.getFormattedDateTime3(FormatUtil.getStringValidate(statusInfo.get("end_dttm"))));

        mEtCntn.setText(FormatUtil.getStringValidate(statusInfo.get("cntn")));

        //멤버 표시
        List<Map<String, String>> userList = (List<Map<String, String>>) statusInfo.get("ref_user_list");
        for (int i = 0; i < userList.size(); i++) {


            userList.get(i).put("usr_id", userList.get(i).get("ref_usr_id"));
            userList.get(i).put("usr_nm", userList.get(i).get("ref_usr_nm"));
            userList.get(i).put("task_usr_id", userList.get(i).get("ref_usr_id"));
            userList.get(i).put("photo_img_min", userList.get(i).get("ref_usr_photo"));

            String fnlApprYn = FormatUtil.getStringValidate(userList.get(i).get("fnl_appr_yn"));
            userList.get(i).put("fnl_appr_yn", TextUtils.isDigitsOnly(fnlApprYn)?"Y":fnlApprYn);  //"Y"기존 참가자, "N"신규
        }
        mMemberList.addAll(userList);
        if (mMemberList == null || mMemberList.size() < 1) {
            mRvMember.setVisibility(View.GONE);
            mTvEmptyMember.setVisibility(View.VISIBLE);
        } else {
            mRvMember.setVisibility(View.VISIBLE);
            mTvEmptyMember.setVisibility(View.GONE);
        }

        mAdapter.notifyDataSetChanged();

        String tarObjTtl = FormatUtil.getStringValidate(statusInfo.get("tar_obj_ttl"));

        if (CodeConstant.TYPE_CFRC.equals(tarObjTp) || CodeConstant.TYPE_TASK.equals(tarObjTp)
            || CodeConstant.TYPE_MEMO.equals(tarObjTp) || CodeConstant.TYPE_WORK.equals(tarObjTp)) {
            Link tarObjLink = new Link(tarObjTtl);
            tarObjLink.setTypeface(Typeface.DEFAULT_BOLD)
                    .setOnClickListener(new Link.OnClickListener() {
                        @Override
                        public void onClick(String clickedText) {
                            Intent intent = null;
                            if (CodeConstant.TYPE_CFRC.equals(tarObjTp)) {
                                intent = new Intent(TaskWriteActivity.this, ConferenceDetailActivity.class);
                            } else if (CodeConstant.TYPE_TASK.equals(tarObjTp)) {
                                intent = new Intent(TaskWriteActivity.this, TaskDetailActivity.class);
                            } else if (CodeConstant.TYPE_MEMO.equals(tarObjTp)) {
                                intent = new Intent(TaskWriteActivity.this, MemoDetailActivity.class);
                            } else if (CodeConstant.TYPE_WORK.equals(tarObjTp)) {
                                intent = new Intent(TaskWriteActivity.this, WorkDetailActivity.class);
                            }
                            intent.putExtra(CodeConstant.CUR_OBJ_TP, tarObjTp);
                            intent.putExtra(CodeConstant.CUR_OBJ_ID, tarObjId);
                            intent.putExtra(CodeConstant.CRT_USR_ID, tarUsrId);
                            startActivity(intent);

                        }
                    });
            if (mTvTarObjTtl != null) {
                mTvTarObjTtl.setText(tarObjTtl);
                LinkBuilder.on(mTvTarObjTtl)
                        .addLink(tarObjLink)
                        .build();
            }
        } else {
            if (mTvTarObjTtl != null)
                mTvTarObjTtl.setText(tarObjTtl);
        }

        //작업결과 표시
        if ("종료".equals(mSpTaskSt.getSelectedItem().toString())) {
            mRlRestCntn.setVisibility(View.VISIBLE);
            mLlTaskRest.setVisibility(View.VISIBLE);
            mEtRestCntn.setText(FormatUtil.getStringValidate(statusInfo.get("rest_cntn")));
        } else {
            mRlRestCntn.setVisibility(View.GONE);
            mLlTaskRest.setVisibility(View.GONE);
        }
    }

    public void setTaskFileData(LinkedTreeMap<String, Object> statusInfo) {
        //파일공유자료
        setFilesLayoutByEdit(mLlTaskFile, mTvEmptyFile, REQUEST_FILE, statusInfo.get("doc_file_list"));
        //결과자료
        // setFilesLayoutByEdit(mLlTaskRest, mTvEmptyRest, REQUEST_REST, statusInfo.get("rest_file_list"));
        // setFilesLayoutByEdit(mLlTaskRest, mTvEmptyRest, REQUEST_REST, statusInfo.get("file_list"));
        setFilesLayoutByEdit(mLlTaskRest, mTvEmptyRest, REQUEST_REST_KITKAT_INTENT_CALLED, statusInfo.get("file_list"));
    }


    public void saveTaskProc() {

        if ("".equals(mEtTtl.getText().toString().trim())) {
            DialogUtil.showInformationDialog(TaskWriteActivity.this, "제목을 입력하시기 바랍니다.");
            return;
        }

        if ("".equals(mEtCntn.getText().toString().trim())) {
            DialogUtil.showInformationDialog(TaskWriteActivity.this, "내용을 입력하시기 바랍니다.");
            return;
        }

        mTaskSt = mSpTaskSt.getSelectedItem().toString().trim();
        if ("".equals(mTaskSt) || "작업상태".equals(mTaskSt)) {
            DialogUtil.showInformationDialog(TaskWriteActivity.this, "상태를 선택하시기 바랍니다.");
            return;
        } else if ("대기".equals(mTaskSt)) { //NST|FIN|DLY|RDY
            mTaskSt = "NST";
        } else if ("진행".equals(mTaskSt)) {
            mTaskSt = "ING";
        } else if ("지연".equals(mTaskSt)) {
            mTaskSt = "DLY";
        } else if ("종료".equals(mTaskSt)) {
            mTaskSt = "FIN";
        }

        // LOW|MIDD|HIGH
        mImpTp = mSpPriority.getSelectedItem().toString().trim();
        if (TextUtils.isEmpty(mImpTp) || "중요도".equals(mImpTp)) {
            DialogUtil.showInformationDialog(TaskWriteActivity.this, "중요도를 선택하시기 바랍니다.");
            return;
        } else if ("낮음".equals(mImpTp)) {
            mImpTp = "LOW";
        } else if ("중간".equals(mImpTp)) {
            mImpTp = "MIDD";
        } else if ("높음".equals(mImpTp)) {
            mImpTp = "HIGH";
        }

        if ("".equals(mBtStartDt.getText().toString()) || !FormatUtil.isThisDateValid(mBtStartDt.getText().toString(), "yyyy-MM-dd")) {
            DialogUtil.showInformationDialog(TaskWriteActivity.this, "시작일자를 선택하시기 바랍니다.");
            return;
        }

        if ("".equals(mBtEndDt.getText().toString()) || !FormatUtil.isThisDateValid(mBtEndDt.getText().toString(), "yyyy-MM-dd")) {
            //시간비교 종료일자가 시작일자보다 빠른지 여부
            DialogUtil.showInformationDialog(TaskWriteActivity.this, "종료일자를 선택하시기 바랍니다.");
            return;
        }

        mTotalList = new ArrayList<List>();
        mTotalList.add(mFileList);
        mTotalList.add(mRestList);

        List<Map<Integer, Integer>> indexList = new ArrayList<>();
        for (int i = 0; i < mTotalList.size(); i++) {
            if (mTotalList.get(i) == null || mTotalList.get(i).size() < 1) continue;

            for (int j = 0; j < mTotalList.get(i).size(); j++) {
                Map<String, String> fileMap = (Map<String, String>)mTotalList.get(i).get(j);
                if (fileMap == null) break;

                if (CodeConstant.ATTACH_NEW.equals(fileMap.get(CodeConstant.ATTACH_ST))) {  // attach_mode = "NEW" 일 때
                    Map<Integer, Integer> index = new HashMap<>();
                    index.put(1, i);
                    index.put(2, j);
                    indexList.add(index);
                    Log.e(TAG, "FILE UPLOAD INDEX i= "+i+" / j = " + j);
                }
            }
        }
        if (indexList.size() > 0) { // 첨부파일이 있을 때 (실제 업로드 되는 파일)
            uplaodFiles(indexList, mTotalList);
        } else {
            saveTask();
        }
    }

    public void saveTask() {
        DialogUtil.showCircularProgressDialog(TaskWriteActivity.this);

        String restCntn = "";
        if (mEtRestCntn != null) restCntn = mEtRestCntn.getText().toString();


        FormEncodingBuilder taskFormBuilder = I2UrlHelper.Task.getTaskFormBuilder(
                mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrId,
                mObjId, mEtTtl.getText().toString(), mTaskSt,
                mBtStartDt.getText().toString().trim(), mBtEndDt.getText().toString().trim(),
                mImpTp, mEtTaskOrd.getText().toString().trim(),
                mEtCntn.getText().toString(), mTaskSt, restCntn);



        I2ConnectApi.requestJSON2Map(TaskWriteActivity.this, I2UrlHelper.Task.getSaveTask(mObjId, taskFormBuilder, mMemberList, mTotalList))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Task.getSaveTask onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Task.getSaveTask onError");
                        DialogUtil.removeCircularProgressDialog();
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(TaskWriteActivity.this, e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Task.getSaveTask onNext");
                        LinkedTreeMap<String, String> statusInfo = (LinkedTreeMap<String, String>) status.get("statusInfo");
                        if (statusInfo != null) {
                            mObjId = statusInfo.get("task_id");
                            completeWrite(mObjId);
                        }
                    }
                });
    }

    public void clear() {
        mTotalList.clear();
        mMemberList.clear();
        mFileList.clear();
        mRestList.clear();
    }

    public void completeWrite(final String taskId) {
        DialogUtil.removeCircularProgressDialog();

        DialogUtil.showDialog(TaskWriteActivity.this, "안내", "작업생성이 완료되었습니다.",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clear();
                        //수정처리
                        Log.e(TAG, "completeWrite  taskId =" + taskId);
                        Intent intent = null;
//                        if (CodeConstant.CFRC_TP.equals(mTarObjTp)) {
//                            intent = new Intent(TaskWriteActivity.this, ConferenceDetailActivity.class);
//                            intent.putExtra(CodeConstant.CUR_OBJ_TP, mTarObjTp);
//                            intent.putExtra(CodeConstant.CUR_OBJ_ID, mTarObjId);
//                            intent.putExtra(CodeConstant.TAR_OBJ_TTL, mTarObjTtl);
//                            intent.putExtra(CodeConstant.CRT_USR_ID, mTarCrtUsrId);
//                            intent.putExtra(CodeConstant.TAB_POS, ConferenceDetailActivity.LIST_CFRC_TASK);
//                            startActivity(intent);
//                        } else if (CodeConstant.WORK_TP.equals(mTarObjTp)) {
//                            //TODO 과제 연동
                        //수정처리
                        if (getIntent().getExtras() != null
                                && getIntent().getExtras().containsKey("requestCode")
                                && getIntent().getExtras().getInt("requestCode") == CodeConstant.REQUEST_EDIT) { //편집으로 들어왔을 경우
                            Log.e(TAG, "request code = " + getIntent().getExtras().getInt("requestCode") + " / CodeConstant.REQUEST_EDIT = " + CodeConstant.REQUEST_EDIT);
                            Intent returnIntent = new Intent();
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        } else { // 작업 생성 플로팅 버튼으로 들어왔을 경우
                            intent = new Intent(TaskWriteActivity.this, TaskDetailActivity.class);
//                            intent.putExtra(CodeConstant.TAR_OBJ_TP, mTarObjTp);
//                            intent.putExtra(CodeConstant.TAR_OBJ_ID, mTarObjId);
//                            intent.putExtra(CodeConstant.TAR_OBJ_TTL, mTarObjTtl);
//                            intent.putExtra(CodeConstant.TAR_CRT_USR_ID, mTarCrtUsrId);
                            intent.putExtra(CodeConstant.CUR_OBJ_TP, CodeConstant.TYPE_TASK);
                            intent.putExtra(CodeConstant.CUR_OBJ_ID, taskId);
                            intent.putExtra(CodeConstant.TAB_POS, TaskDetailActivity.VIEW_TASK_DETAIL);
                            startActivity(intent);
                        }

                        finish();
                    }
                });
    }

    public void uplaodFiles(final List<Map<Integer, Integer>> indexList, final List<List> totalList) {
        if (mUploadedCnt < indexList.size()) {
            DialogUtil.showCircularProgressDialog(TaskWriteActivity.this);

            Map<Integer, Integer> index = indexList.get(mUploadedCnt);
            final int listIndex = index.get(1); // 로컬파일맵 + Rest파일맵
            final int fileIndex = index.get(2); // 파일맵
            Map<String, String> fileMap = (Map<String, String>)totalList.get(listIndex).get(fileIndex);
            Log.e(TAG, "uri: " + fileMap.get("uri") );
            Uri uri = Uri.parse(fileMap.get("uri"));
            File file = new File(FileUtil.getPath(this, uri));
            String fileNm = fileMap.get("file_nm");
            String mimeType = FileUtil.getMimeType(this, uri);

            I2ConnectApi.uploadFile(TaskWriteActivity.this, fileNm, mimeType, file)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<JSONObject>() {
                        @Override
                        public void onCompleted() {
                            Log.d(TAG, "uploadFile onCompleted");
                            DialogUtil.removeCircularProgressDialog();
                            mUploadedCnt++;
                            if (mUploadedCnt >= indexList.size()) {
                                saveTask();
                            } else {
                                uplaodFiles(indexList, totalList);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "uploadFile onError");
                            DialogUtil.removeCircularProgressDialog();
                            //Error dialog 표시
                            e.printStackTrace();
                            DialogUtil.showErrorDialogWithValidateSession(TaskWriteActivity.this, e);
                        }

                        @Override
                        public void onNext(JSONObject jsonObject) {
                            Log.d(TAG, "uploadFile onNext");
                            if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                                JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                                JSONArray statusInfoArray = I2ResponseParser.getJsonArray(statusInfo, "file_list");
                                try {
                                    if (statusInfoArray.length() > 0) {
                                        // 파일을 업로드하고 난 후 업로드 파일에 대한 메타값을 json에서 받아 맵에 저장(업로드 되는 파일만)
                                        // 여기에 파일 ATTACH_ST 값을 세팅하는 것도 좋은 방법
                                        // http://m.expertbank.co.kr/i2cowork/sns/file/upload.json
                                        // 기존 리스트에 결과값 추가
                                        ((Map<String, String>)totalList.get(listIndex).get(fileIndex)).put("file_id", statusInfoArray.getJSONObject(0).getString("file_id"));
                                        ((Map<String, String>)totalList.get(listIndex).get(fileIndex)).put("file_nm", statusInfoArray.getJSONObject(0).getString("file_nm"));
                                        ((Map<String, String>)totalList.get(listIndex).get(fileIndex)).put("phscl_file_nm", statusInfoArray.getJSONObject(0).getString("phscl_file_nm"));
                                        ((Map<String, String>)totalList.get(listIndex).get(fileIndex)).put("file_ext", statusInfoArray.getJSONObject(0).getString("file_ext"));
                                        ((Map<String, String>)totalList.get(listIndex).get(fileIndex)).put("file_size", statusInfoArray.getJSONObject(0).getString("file_size"));
                                        ((Map<String, String>)totalList.get(listIndex).get(fileIndex)).put("file_tp_cd", statusInfoArray.getJSONObject(0).getString("file_tp_cd"));
                                        ((Map<String, String>)totalList.get(listIndex).get(fileIndex)).put("file_path", statusInfoArray.getJSONObject(0).getString("file_path"));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void setFilesLayoutByEdit(LinearLayout targetLayout, TextView tvEmpty, int mode, Object object) {
        // 서버에서 받은 파일리스트가 있으면 화면에 출력하여 준다

        List<LinkedTreeMap<String, String>> listMap = (List<LinkedTreeMap<String, String>>) object;

        if (listMap == null || listMap.size() <= 0) {
            targetLayout.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            targetLayout.setVisibility(View.VISIBLE);
            targetLayout.removeAllViews();
            tvEmpty.setVisibility(View.GONE);

            for (int i = 0; i < listMap.size(); i++) {
                Map<String, String> file = new HashMap<String, String>();
                String fileNm = FormatUtil.getStringValidate(listMap.get(i).get("file_nm"));
                file.put("uri", "");
                file.put("file_nm", fileNm);
                file.put("file_id", listMap.get(i).get("file_id"));
                file.put("file_tp_cd", listMap.get(i).get("file_tp_cd"));
                file.put("tar_obj_tp_cd", listMap.get(i).get("tar_obj_tp_cd"));
                file.put("tar_file_tp_cd", listMap.get(i).get("tar_file_tp_cd"));
                file.put("phscl_file_nm", listMap.get(i).get("phscl_file_nm"));
                file.put(CodeConstant.ATTACH_ST, "NONE");

                switch (mode) {
                    case REQUEST_FILE:
                        if (mFileList == null) mFileList = new ArrayList<Map<String, String>>();
                        mFileList.add(file);
                        break;
                    case REQUEST_REST_KITKAT_INTENT_CALLED:
                        if (mRestList == null) mRestList = new ArrayList<Map<String, String>>();
                        mRestList.add(file);
                        break;
                }
                addFilesLayout(targetLayout, tvEmpty, mode, fileNm);
            }

        }

    }

    public void openFileChooser(int requestCodePrev, int requestCodeNew) {
        if (Build.VERSION.SDK_INT < 19) {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    Intent intent = new Intent();
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "파일 선택"), requestCodePrev);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, requestCodeNew);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        ClipData clipdata = null;
        int takeFlags = -1;
        switch (requestCode) {
            case SNSPersonSearchActivity.REQUEST_FRIEND_SEARCH:
                if (data != null) {
                    Map<String, String> person = new HashMap<String, String>();
                    person.put("post_usr_id", "");
                    person.put("post_id", "");
                    person.put("usr_id", data.getExtras().getString(CodeConstant.USR_ID));
                    person.put("usr_nm", data.getExtras().getString(CodeConstant.USR_NM));
                    person.put("photo_img_min", data.getExtras().getString(CodeConstant.USR_IMG));

                    person.put("task_usr_id", data.getExtras().getString("usr_id"));
                    person.put("fnl_appr_yn", "N"); //신규 참가자

                    //중복체크
                    for (int i = 0; i < mMemberList.size(); i++) {
                        if (person.get("usr_id").equals(mMemberList.get(i).get("usr_id"))) {
                            Toast.makeText(TaskWriteActivity.this, "중복된 참가자입니다", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    //set image
                    if (mMemberList == null) mMemberList = new ArrayList<Map<String, String>>();
                    mMemberList.add(person);
                    if (mMemberList == null || mMemberList.size() < 1) {
                        mRvMember.setVisibility(View.GONE);
                        mTvEmptyMember.setVisibility(View.VISIBLE);
                    } else {
                        mRvMember.setVisibility(View.VISIBLE);
                        mTvEmptyMember.setVisibility(View.GONE);
                    }
                    mAdapter.notifyDataSetChanged();
                }
                break;

            case REQUEST_FILE: //file 첨부
            case REQUEST_FILE_KITKAT_INTENT_CALLED:
            case REQUEST_REST:
            case REQUEST_REST_KITKAT_INTENT_CALLED:
                addFilesLayout(requestCode, data);
                break;
        }
    }

    @SuppressLint("NewApi")
    public void addFilesLayout(int mode, Intent intent) {
        ClipData clipdata = null;
        LinearLayout llFileList = null;
        TextView tvEmpty = null;

        switch (mode) {
            case REQUEST_FILE:
                if (llFileList == null) {
                    llFileList = mLlTaskFile;
                    tvEmpty = mTvEmptyFile;
                }
            case REQUEST_REST:
                if (llFileList == null) {
                    llFileList = mLlTaskRest;
                    tvEmpty = mTvEmptyRest;
                }

                clipdata = intent.getClipData();
                if (clipdata == null) {
                    Uri uri = intent.getData();
                    addFilesLayout(llFileList, tvEmpty, mode, uri);

                } else {
                    for (int i = 0; i < clipdata.getItemCount(); i++) {
                        Uri uri = clipdata.getItemAt(i).getUri();
                        addFilesLayout(llFileList, tvEmpty, mode, uri);
                    }
                }
                break;
            case REQUEST_FILE_KITKAT_INTENT_CALLED:
                if (llFileList == null) {
                    llFileList = mLlTaskFile;
                    tvEmpty = mTvEmptyFile;
                }
            case REQUEST_REST_KITKAT_INTENT_CALLED:
                if (llFileList == null) {
                    llFileList = mLlTaskRest;
                    tvEmpty = mTvEmptyRest;
                }
                clipdata = intent.getClipData();

                final int takeFlags = intent.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                if (clipdata == null) {
                    Uri uri = intent.getData();
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    addFilesLayout(llFileList, tvEmpty, mode, uri);
                } else {
                    for (int i = 0; i < clipdata.getItemCount(); i++) {
                        Uri uri = clipdata.getItemAt(i).getUri();
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        addFilesLayout(llFileList, tvEmpty, mode, uri);
                    }
                }
                break;
        }
    }

    public void addFilesLayout(LinearLayout targetLayout, TextView tvEmpty, int mode, Uri uri) {

        if (TextUtils.isEmpty(uri.toString())) return;

        Map<String, String> file = new HashMap<String, String>();
        String fileNm = FileUtil.getFileName(this, uri);
        file.put("uri", uri.toString());
        file.put("file_nm", fileNm);
        file.put(CodeConstant.ATTACH_ST, CodeConstant.ATTACH_NEW);
        file.put("phscl_file_nm", "");

        int pos = -1;
        switch (mode) {
            case REQUEST_FILE:
            case REQUEST_FILE_KITKAT_INTENT_CALLED:
                if (mFileList == null) mFileList = new ArrayList<Map<String, String>>();
                mFileList.add(file);
                break;
            case REQUEST_REST:
            case REQUEST_REST_KITKAT_INTENT_CALLED:
                // 문서만 가능 hwp|gul|txt|doc|docx|ppt|pptx|xls|xlsx|pdf|jpg|png
                if (mRestList == null) mRestList = new ArrayList<Map<String, String>>();
                mRestList.add(file);
                break;
        }

        addFilesLayout(targetLayout, tvEmpty, mode, fileNm);

    }

    public void addFilesLayout(final LinearLayout targetLayout, TextView tvEmpty, final int mode, String fileNm) {

        if (TextUtils.isEmpty(fileNm)) return;

        //addFilesView
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View fileView = inflater.inflate(R.layout.view_item_file, null);

        ImageView ivIcFileExt = (ImageView) fileView.findViewById(R.id.iv_ic_file_ext);
        TextView tvFileNm = (TextView) fileView.findViewById(R.id.tv_file_nm);
        ImageButton ibDel = (ImageButton) fileView.findViewById(R.id.ib_del);
        ibDel.setVisibility(View.VISIBLE);
        //확장자에 따른 아이콘 변경처리
        FileUtil.setFileExtIcon(ivIcFileExt, fileNm);
        tvFileNm.setText(fileNm);

        ibDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int positoin = -1;
                Map<String, String> file = null;
                switch (mode) {
                    case REQUEST_FILE:
                    case REQUEST_FILE_KITKAT_INTENT_CALLED:
                        positoin = ((ViewGroup) fileView.getParent()).indexOfChild(fileView);
                        Log.e(TAG, "mFileList remove pos = " + positoin);
                        //서버업로드 기준 서버삭제
                        if (CodeConstant.ATTACHED.equals(mFileList.get(positoin).get(CodeConstant.ATTACH_ST))) {
                            mFileList.get(positoin).put(CodeConstant.ATTACH_ST, CodeConstant.DEL_ATTACHED);
                            //Toast.makeText(TaskWriteActivity.this, mFileList.get(positoin)+"", Toast.LENGTH_SHORT).show();
                        } else { //서버 업로드 전이면 클라이언트 파리일스트 삭제
                            mFileList.remove(positoin);
                        }
                        ((ViewGroup) fileView.getParent()).removeView(fileView);
                        // ((ViewGroup) fileView.getParent()).invalidate(); // 해당 로직을 제거하니 에러가 없어짐
                        break;
                    case REQUEST_REST: //i2conference 앱에서만 등록/수정?
                    case REQUEST_REST_KITKAT_INTENT_CALLED:
                        positoin = ((ViewGroup) fileView.getParent()).indexOfChild(fileView);
                        Log.e(TAG, "m remove pos = " + positoin);
                        //서버업로드 기준 서버삭제
                        // Toast.makeText(TaskWriteActivity.this, "ATTACH_MODE=" + mRestList.get(positoin).get(CodeConstant.ATTACH_ST), Toast.LENGTH_SHORT).show();
                        if (mRestList.get(positoin).get(CodeConstant.ATTACH_ST).equals("NONE")) {
                            mRestList.get(positoin).put(CodeConstant.ATTACH_ST, "DELETE");
                        } else { //서버 업로드 전이면 클라이언트 파리일스트 삭제
                            mRestList.remove(positoin);
                        }
                        ((ViewGroup) fileView.getParent()).removeView(fileView);
                        // ((ViewGroup) fileView.getParent()).invalidate();
                        break;
                }
                setFileVisible(mode);
            }
        });

        targetLayout.addView(fileView);
        setFileVisible(mode);
    }

    public void setFileVisible(int mode) {

        switch (mode) {
            case REQUEST_FILE:
            case REQUEST_FILE_KITKAT_INTENT_CALLED:
                if (mFileList == null || mFileList.size() < 1) {
                    mLlTaskFile.setVisibility(View.GONE);
                    mTvEmptyFile.setVisibility(View.VISIBLE);
                } else {
                    mLlTaskFile.setVisibility(View.VISIBLE);
                    mTvEmptyFile.setVisibility(View.GONE);
                }
                break;
            case REQUEST_REST:
            case REQUEST_REST_KITKAT_INTENT_CALLED:
                if (mRestList == null || mRestList.size() < 1) {
                    mLlTaskRest.setVisibility(View.GONE);
                    mTvEmptyRest.setVisibility(View.VISIBLE);
                } else {
                    mLlTaskRest.setVisibility(View.VISIBLE);
                    mTvEmptyRest.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        if (dialog.getTag().equals("start_dt")) {
            now.set(year, monthOfYear, dayOfMonth);
            mBtStartDt.setText(FormatUtil.getFormattedDate3(now.getTime()));
        } else if (dialog.getTag().equals("end_dt")) {
            now.set(year, monthOfYear, dayOfMonth);
            mBtEndDt.setText(FormatUtil.getFormattedDate3(now.getTime()));
        }
    }

    public class TaskMemberRecyclerViewAdapter
            extends RecyclerView.Adapter<TaskMemberRecyclerViewAdapter.ViewHolder> {

        protected Context mContext;
        private List<Map<String, String>> mValues;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public String mUsrID;
            public final CircleImageView mCivCrtUsrPhoto;
            public final TextView mTvUsrNm;
            public final ImageButton mIvDelmember;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mCivCrtUsrPhoto = (CircleImageView) view.findViewById(R.id.civ_crt_usr_photo);
                mTvUsrNm = (TextView) view.findViewById(R.id.tv_usr_nm);
                mIvDelmember = (ImageButton) view.findViewById(R.id.ib_del_member);
            }

            @Override
            public String toString() {
                return super.toString();
            }
        }

        public Map<String, String> getValueAt(int position) {
            return mValues.get(position);
        }

        public TaskMemberRecyclerViewAdapter(Context context, List<Map<String, String>> items) {
            mContext = context;
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_add_member, parent, false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                TypedValue outValue = new TypedValue();
                mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                view.setBackgroundResource(outValue.resourceId);
            }
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Map<String, String> person = mValues.get(position);

            final int pos = position;
            final String usrId = person.get("usr_id");
            final String usrNm = person.get("usr_nm");
            final String usrImg = person.get("photo_img_min");
            final String usrCfrcUserFlag = person.get("cfrc_usr_flag"); //??
            if ("CRTR".equals(usrCfrcUserFlag)) {
                // 주최자 //통합?
            } else if ("HOST".equals(usrCfrcUserFlag)) {
                // 사회자
            } else { //GNER
                // 참가자
            }
            holder.mUsrID = usrId;
            holder.mTvUsrNm.setText(usrNm);
            Glide.with(holder.mCivCrtUsrPhoto.getContext())
                    .load(I2UrlHelper.File.getUsrImage(usrImg))
                    .error(R.drawable.ic_no_usr_photo)
                    .fitCenter()
                    .into(holder.mCivCrtUsrPhoto);

            holder.mIvDelmember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((TaskWriteActivity) mContext).mMemberList.remove(pos);
                    removeAt(pos);
                }
            });

        }

        public void removeAt(int position) {
//            mValues.remove(position);
            notifyItemRemoved(position);
            int pos = position - 1 < 0 ? 0 : position - 1;
            notifyItemRangeChanged(pos, mValues.size());
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }
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

}
