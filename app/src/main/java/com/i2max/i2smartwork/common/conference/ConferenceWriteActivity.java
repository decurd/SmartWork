package com.i2max.i2smartwork.common.conference;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.internal.LinkedTreeMap;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.common.sns.SNSPersonSearchActivity;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FileUtil;
import com.i2max.i2smartwork.utils.FormatUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;
import com.squareup.okhttp.FormEncodingBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by shlee on 15. 9. 16..
 */
public class ConferenceWriteActivity extends BaseAppCompatActivity {
    static String TAG = ConferenceWriteActivity.class.getSimpleName();

    private final String PHOTO_PATTERN = "([^\\s]+(\\.(?i)(jpg|png))$)";
    private final String DOC_PATTERN = "([^\\s]+(\\.(?i)(hwp|gul|txt|doc|docx|ppt|pptx|xls|xlsx|pdf|jpg|png|flv))$)";
    private final String MOV_PATTERN = "([^\\s]+(\\.(?i)(flv|mp4))$)";
    private final String FILE_PATTERN = "([^\\s]+(\\.(?i)(hwp|gul|txt|doc|docx|ppt|pptx|xls|xlsx|pdf|jpg|png))$)";

    protected final int REQUEST_RESERVE_ROOM = 2000;
    protected final int REQUEST_DOC_KITKAT_INTENT_CALLED = 2023; //키캣 이상 문서파일 열기 처리
    protected final int REQUEST_DOC = 2024; //키캣 이전버젼 문파서일 열기처리
    protected final int REQUEST_MOV_KITKAT_INTENT_CALLED = 2013; //키캣 이상 동영상파일 열기 처리
    protected final int REQUEST_MOV = 2014; //키캣 이전버젼 파일 열기처리서
    protected final int REQUEST_FILE_KITKAT_INTENT_CALLED = 2003; //키캣 이상 파일 열기 처리
    protected final int REQUEST_FILE = 2004; //키캣 이전버젼 파일 열기처리
    protected final int REQUEST_REST_KITKAT_INTENT_CALLED = 2033; //키캣 이상 문서파일 열기 처리
    protected final int REQUEST_REST = 2034; //키캣 이전버젼 회의결과자료파일 열기처리
    public static final String CFRC_ONLINE = "cfrc_online";

    protected RelativeLayout mRlCfrcRoom, mRlCfrcEdit, mRlCfrcRestCntn, mRlCfrcRestList;
    protected EditText mEtTtl, mEtCntn, mEtRestCntn;
    protected TextView mTvDt, mTvPriodeTm, mTvStNm, mTvCfrcRoomNm, mTvCfrcRoomTpNm, mTvCfrcStNm, mTvCfrcCrtDttm,
            mTvEmptyMember, mTvEmptyFile, mTvEmptyMov, mTvEmptyDoc, mTvEmptyRest;
    protected Spinner mSpCfrcTp;
    protected SwitchCompat mScCfrcRoomTp;
    protected Button mBtReserveRoom;
    protected LinearLayout mLlCfrcFile, mLlCfrcMov, mLlCfrcDoc, mLlCfrcRest;
    protected ImageView mIbAddMember, mIbAddFiles, mIbAddDocs, mIbAddMovies, mIbAddRest, mIbSave;
    protected CheckBox mCbNoti, mCbPlan;
    protected String mStartTm = "", mEndTm = "", mRoomId = "", mCfrcId = "", mCrtUsrId = "";
    protected RecyclerView mRvCfrcMember;
    protected CfrcMemberRecyclerViewAdapter mAdapter;

    private String mCfrcSt, mCfrcRoomTp, mCfrcTp, mCfrcNotiYn, mCfrcPlanYn;

    private int mUploadedCnt = 0;
    private List<List> mTotalList;
    private List<Map<String, String>> mMemberList = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> mFileList = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> mMovList = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> mDocList = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> mRestList = new ArrayList<Map<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference_write);

        //편집처리
        Bundle extra = getIntent().getExtras();
        String title = getString(R.string.cfrc_add);
        if (extra != null) {
            title = extra.getString(CodeConstant.TITLE, getString(R.string.cfrc_add));
            mCfrcId = extra.getString(CodeConstant.CFRC_ID, "");
            mCrtUsrId = extra.getString(CodeConstant.CRT_USR_ID, "");
            Log.e(TAG, "CFRC_ID= " + mCfrcId + ", CRT_USR_ID= " + mCrtUsrId);
        }


        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(title);

        mEtTtl = (EditText) findViewById(R.id.et_ttl);

        mRlCfrcEdit = (RelativeLayout) findViewById(R.id.rl_cfrc_edit);
        mTvCfrcStNm = (TextView) findViewById(R.id.tv_cfrc_st_nm);
        mTvCfrcCrtDttm = (TextView) findViewById(R.id.tv_cfrc_crt_dttm);

        mTvCfrcRoomNm = (TextView) findViewById(R.id.tv_cfrc_room_nm);
        mTvCfrcRoomTpNm = (TextView) findViewById(R.id.tv_cfrc_room_tp_nm);
        mScCfrcRoomTp = (SwitchCompat) findViewById(R.id.sc_cfrc_room_tp);
        mSpCfrcTp = (Spinner) findViewById(R.id.sp_cfrc_tp);

        mTvDt = (TextView) findViewById(R.id.tv_dt);
        mTvPriodeTm = (TextView) findViewById(R.id.tv_priode_tm);
        mTvStNm = (TextView) findViewById(R.id.tv_st_nm);

        mEtCntn = (EditText) findViewById(R.id.et_cntn);
        mBtReserveRoom = (Button) findViewById(R.id.bt_reserve_room);

        mRlCfrcRoom = (RelativeLayout) findViewById(R.id.rl_cfrc_room);
        mCbNoti = (CheckBox) findViewById(R.id.cb_auto_push);
        mCbPlan = (CheckBox) findViewById(R.id.cb_auto_plan);

        mIbAddMember = (ImageView) findViewById(R.id.ib_add_member);
        mRvCfrcMember = (RecyclerView) findViewById(R.id.rv_cfrc_member);
        mTvEmptyMember = (TextView) findViewById(R.id.tv_cfrc_empty_member);

        mIbAddFiles = (ImageView) findViewById(R.id.ib_add_file);
        mLlCfrcFile = (LinearLayout) findViewById(R.id.ll_cfrc_file_list);
        mTvEmptyFile = (TextView) findViewById(R.id.tv_cfrc_empty_file);

        mIbAddMovies = (ImageView) findViewById(R.id.ib_add_mov);
        mLlCfrcMov = (LinearLayout) findViewById(R.id.ll_cfrc_mov_list);
        mTvEmptyMov = (TextView) findViewById(R.id.tv_cfrc_empty_mov);

        mIbAddDocs = (ImageView) findViewById(R.id.ib_add_doc);
        mLlCfrcDoc = (LinearLayout) findViewById(R.id.ll_cfrc_doc_list);
        mTvEmptyDoc = (TextView) findViewById(R.id.tv_cfrc_empty_doc);

        mRlCfrcRestCntn = (RelativeLayout) findViewById(R.id.rl_cfrc_rest_cntn);
        mRlCfrcRestList = (RelativeLayout) findViewById(R.id.rl_cfrc_rest_list);
        mEtRestCntn = (EditText) findViewById(R.id.et_rest_cntn);
        mIbAddRest = (ImageView) findViewById(R.id.ib_add_rest);
        mLlCfrcRest = (LinearLayout) findViewById(R.id.ll_cfrc_rest_list);
        mTvEmptyRest = (TextView) findViewById(R.id.tv_cfrc_empty_rest);
        mIbSave = (ImageView) findViewById(R.id.iv_bt_save);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 5);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRvCfrcMember.setLayoutManager(layoutManager);

        initView();
    }

    public void initView() {

        if (mCfrcId != null && !"".equals(mCfrcId)) {
            loadEditViewCfrc();
        } else {
            //default offline
            setVisibleOnline(true);
            mScCfrcRoomTp.setChecked(true);
            mSpCfrcTp.setSelection(1); //디폴트 영상
        }

        mScCfrcRoomTp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setVisibleOnline(isChecked);
            }
        });

        mBtReserveRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConferenceWriteActivity.this, ConferenceRoomScheduleActivity.class);
                Log.e(TAG, "swich online = " + mScCfrcRoomTp.isChecked());
                if (mScCfrcRoomTp.isChecked()) {
                    intent.putExtra(CFRC_ONLINE, "Y");
                    intent.putExtra(CodeConstant.TITLE, "회의일시 예약");
                } else {
                    intent.putExtra(CodeConstant.TITLE, "회의실 예약");
                }
                startActivityForResult(intent, REQUEST_RESERVE_ROOM);
            }
        });

        mIbAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Search
                Intent intent = new Intent(ConferenceWriteActivity.this, SNSPersonSearchActivity.class);
                intent.putExtra(SNSPersonSearchActivity.USR_ID, PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
                intent.putExtra(SNSPersonSearchActivity.MODE, CodeConstant.MODE_CFRC_MEMBER_ADD);
                startActivityForResult(intent, SNSPersonSearchActivity.REQUEST_FRIEND_SEARCH);
            }
        });

        mMemberList = new ArrayList<Map<String, String>>();
        mAdapter = new CfrcMemberRecyclerViewAdapter(this, mMemberList);
        mRvCfrcMember.setAdapter(mAdapter);

        mIbAddDocs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser(REQUEST_DOC, REQUEST_DOC_KITKAT_INTENT_CALLED);
            }
        });

        mIbAddMovies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser(REQUEST_MOV, REQUEST_MOV_KITKAT_INTENT_CALLED);
            }
        });

        mIbAddFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser(REQUEST_MOV, REQUEST_FILE_KITKAT_INTENT_CALLED);
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
                saveCfrcProc();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
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
            case REQUEST_RESERVE_ROOM:
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    mRoomId = bundle.getString("cfrc_room_id", "");
                    mStartTm = bundle.getString("cfrc_start_tm", "");
                    mEndTm = bundle.getString("cfrc_end_tm", "");
                    mTvDt.setText(bundle.getString("cfrc_dt", ""));
                    mTvCfrcRoomNm.setText(bundle.getString("cfrc_room_nm", ""));
                    mTvPriodeTm.setText(mStartTm + "~" + mEndTm);
                }
                break;
            case SNSPersonSearchActivity.REQUEST_FRIEND_SEARCH:
                if (data != null) {
                    Map<String, String> person = new HashMap<String, String>();
                    person.put("usr_id", data.getExtras().getString(CodeConstant.USR_ID));
                    person.put("usr_nm", data.getExtras().getString(CodeConstant.USR_NM));
                    person.put("crt_usr_photo", data.getExtras().getString(CodeConstant.USR_IMG));
                    person.put("cfrc_usr_flag", "GNER"); //사회자|참가자  HOST|GNER 설정가능
                    person.put("cfrc_usr_flag_nm", "참가자");

                    //중복체크
                    for (int i = 0; i < mMemberList.size(); i++) {
                        if (person.get("usr_id").equals(mMemberList.get(i).get("usr_id"))) {
                            Toast.makeText(ConferenceWriteActivity.this, "중복된 참가자입니다", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    //set image
                    if (mMemberList == null) mMemberList = new ArrayList<Map<String, String>>();
                    mMemberList.add(person);
                    setVisibleMember();
                    mAdapter.notifyDataSetChanged();
                }
                break;

            case REQUEST_DOC: //file 첨부
            case REQUEST_MOV:
            case REQUEST_FILE:
            case REQUEST_REST:
            case REQUEST_DOC_KITKAT_INTENT_CALLED:
            case REQUEST_MOV_KITKAT_INTENT_CALLED:
            case REQUEST_FILE_KITKAT_INTENT_CALLED:
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
            case REQUEST_DOC:
                if (llFileList == null) {
                    llFileList = mLlCfrcDoc;
                    tvEmpty = mTvEmptyDoc;
                }
            case REQUEST_MOV:
                if (llFileList == null) {
                    llFileList = mLlCfrcMov;
                    tvEmpty = mTvEmptyMov;
                }
            case REQUEST_FILE:
                if (llFileList == null) {
                    tvEmpty = mTvEmptyFile;
                }
            case REQUEST_REST:
                if (llFileList == null) {
                    llFileList = mLlCfrcRest;
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
            case REQUEST_DOC_KITKAT_INTENT_CALLED:
                if (llFileList == null) {
                    llFileList = mLlCfrcDoc;
                    tvEmpty = mTvEmptyDoc;
                }
            case REQUEST_MOV_KITKAT_INTENT_CALLED:
                if (llFileList == null) {
                    llFileList = mLlCfrcMov;
                    tvEmpty = mTvEmptyMov;
                }
            case REQUEST_FILE_KITKAT_INTENT_CALLED:
                if (llFileList == null) {
                    llFileList = mLlCfrcFile;
                    tvEmpty = mTvEmptyFile;
                }
            case REQUEST_REST_KITKAT_INTENT_CALLED:
                if (llFileList == null) {
                    llFileList = mLlCfrcRest;
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

    public void setFileVisible(int mode) {
        switch (mode) {
            case REQUEST_DOC:
            case REQUEST_DOC_KITKAT_INTENT_CALLED:
                if (mDocList == null || mDocList.size() < 1) {
                    mLlCfrcDoc.setVisibility(View.GONE);
                    mTvEmptyDoc.setVisibility(View.VISIBLE);
                } else {
                    mLlCfrcDoc.setVisibility(View.VISIBLE);
                    mTvEmptyDoc.setVisibility(View.GONE);
                }
                break;
            case REQUEST_MOV:
            case REQUEST_MOV_KITKAT_INTENT_CALLED:
                if (mMovList == null || mMovList.size() < 1) {
                    mLlCfrcMov.setVisibility(View.GONE);
                    mTvEmptyMov.setVisibility(View.VISIBLE);
                } else {
                    mLlCfrcMov.setVisibility(View.VISIBLE);
                    mTvEmptyMov.setVisibility(View.GONE);
                }
                break;
            case REQUEST_FILE:
            case REQUEST_FILE_KITKAT_INTENT_CALLED:
                if (mFileList == null || mFileList.size() < 1) {
                    mLlCfrcFile.setVisibility(View.GONE);
                    mTvEmptyFile.setVisibility(View.VISIBLE);
                } else {
                    mLlCfrcFile.setVisibility(View.VISIBLE);
                    mTvEmptyFile.setVisibility(View.GONE);
                }
                break;
            case REQUEST_REST:
            case REQUEST_REST_KITKAT_INTENT_CALLED:
                if (mRestList == null || mRestList.size() < 1) {
                    mLlCfrcRest.setVisibility(View.GONE);
                    mTvEmptyRest.setVisibility(View.VISIBLE);
                } else {
                    mLlCfrcRest.setVisibility(View.VISIBLE);
                    mTvEmptyRest.setVisibility(View.GONE);
                }
                break;
        }
    }

    public boolean validateFileExt(int mode, String fileName) {
        boolean result = false;
        Pattern pattern;
        Matcher matcher;
        switch (mode) {
            case REQUEST_DOC:
            case REQUEST_DOC_KITKAT_INTENT_CALLED:
                // hwp|gul|txt|doc|docx|ppt|pptx|xls|xlsx|pdf|jpg|png|flv
                pattern = Pattern.compile(DOC_PATTERN);
                matcher = pattern.matcher(fileName);
                result = matcher.matches();
                break;
            case REQUEST_MOV:
            case REQUEST_MOV_KITKAT_INTENT_CALLED:
                // flv|mp4만 가능
                pattern = Pattern.compile(MOV_PATTERN);
                matcher = pattern.matcher(fileName);
                result = matcher.matches();
                break;
            case REQUEST_FILE:
            case REQUEST_FILE_KITKAT_INTENT_CALLED:
                // 문서만 가능 hwp|gul|txt|doc|docx|ppt|pptx|xls|xlsx|pdf|jpg|png
                pattern = Pattern.compile(FILE_PATTERN);
                matcher = pattern.matcher(fileName);
                result = matcher.matches();
                break;
            case REQUEST_REST:
            case REQUEST_REST_KITKAT_INTENT_CALLED:
                // 문서만 가능 hwp|gul|txt|doc|docx|ppt|pptx|xls|xlsx|pdf|jpg|png
                pattern = Pattern.compile(FILE_PATTERN);
                matcher = pattern.matcher(fileName);
                result = matcher.matches();
                break;
        }

        return result;
    }

    public void addFilesLayout(LinearLayout targetLayout, TextView tvEmpty, int mode, Uri uri) {
        if (TextUtils.isEmpty(uri.toString())) return;

        Map<String, String> file = new HashMap<String, String>();
        String fileNm = FileUtil.getFileName(this, uri);
        file.put("uri", uri.toString());
        file.put("file_id", "");
        file.put("file_nm", fileNm);
        file.put(CodeConstant.ATTACH_ST, CodeConstant.ATTACH_NEW);
        file.put("tar_obj_tp_cd", "CFRC");

        int pos = -1;
        switch (mode) {
            case REQUEST_DOC:
            case REQUEST_DOC_KITKAT_INTENT_CALLED:
                if (mDocList == null) mDocList = new ArrayList<Map<String, String>>();
                // hwp|gul|txt|doc|docx|ppt|pptx|xls|xlsx|pdf|jpg|png|flv
                if(validateFileExt(mode, fileNm)) {
                    file.put("tar_file_tp_cd", "DOC");
                    mDocList.add(file);
                } else {
                    Toast.makeText(ConferenceWriteActivity.this, "지원하지 않는 파일입니다.\n다음에 해당하는 회의문서자료를 업로드해주시기 바랍니다.\n(hwp,gul,txt,doc,docx,ppt,pptx,xls,xlsx,pdf,jpg,png,flv)", Toast.LENGTH_LONG).show();
                    return;
                }
                Log.e(TAG, "mDocList size = " + mDocList.size());
                break;
            case REQUEST_MOV:
            case REQUEST_MOV_KITKAT_INTENT_CALLED:
                // flv|mp4만 가능
                if (mMovList == null) mMovList = new ArrayList<Map<String, String>>();
                if(validateFileExt(mode, fileNm)) {
                    file.put("tar_file_tp_cd", "MOV");
                    mMovList.add(file);
                } else {
                    Toast.makeText(ConferenceWriteActivity.this, "지원하지 않는 파일입니다.\n" +
                            "다음에 해당하는 동영상자료 업로드해주시기 바랍니다.\n(flv,mp4)", Toast.LENGTH_LONG).show();
                    return;
                }
                Log.e(TAG, "mMovList size = " + mMovList.size());
                break;
            case REQUEST_FILE:
            case REQUEST_FILE_KITKAT_INTENT_CALLED:
                // 문서만 가능 hwp|gul|txt|doc|docx|ppt|pptx|xls|xlsx|pdf|jpg|png
                if (mFileList == null) mFileList = new ArrayList<Map<String, String>>();
                if(validateFileExt(mode, fileNm)) {
                    file.put("tar_file_tp_cd", "GNR"); //TASK GNL
                    mFileList.add(file);
                } else {
                    Toast.makeText(ConferenceWriteActivity.this, "지원하지 않는 파일입니다.\n다음에 해당하는 파일자료를 업로드해주시기 바랍니다.\n(hwp,gul,txt,doc,docx,ppt,pptx,xls,xlsx,pdf,jpg,png)", Toast.LENGTH_LONG).show();
                    return;
                }
                Log.e(TAG, "fileList size = " + mFileList.size());
                break;
            case REQUEST_REST:
            case REQUEST_REST_KITKAT_INTENT_CALLED:
                // 문서만 가능 hwp|gul|txt|doc|docx|ppt|pptx|xls|xlsx|pdf|jpg|png
                if (mRestList == null) mRestList = new ArrayList<Map<String, String>>();
                if(validateFileExt(mode, fileNm)) {
                    file.put("tar_file_tp_cd", "REST");
                    mRestList.add(file);
                } else {
                    Toast.makeText(ConferenceWriteActivity.this, "지원하지 않는 파일입니다.\n다음에 해당하는 회의결과자료를 업로드해주시기 바랍니다.\n(hwp,gul,txt,doc,docx,ppt,pptx,xls,xlsx,pdf,jpg,png)", Toast.LENGTH_LONG).show();
                    return;
                }
                Log.e(TAG, "fileList size = " + mFileList.size());  
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
                    case REQUEST_DOC:
                    case REQUEST_DOC_KITKAT_INTENT_CALLED:
                        positoin = ((ViewGroup) fileView.getParent()).indexOfChild(fileView);
                        Log.e(TAG, "mDocList remove pos = " + positoin);
                        //서버업로드 기준 서버삭제
                        if (CodeConstant.ATTACHED.equals(mDocList.get(positoin).get(CodeConstant.ATTACH_ST))) {
                            mDocList.get(positoin).put(CodeConstant.ATTACH_ST, CodeConstant.DEL_ATTACHED);
                        } else { //서버 업로드 전이면 클라이언트 파리일스트 삭제
                            mDocList.remove(positoin);
                        }
                        ((ViewGroup) fileView.getParent()).removeView(fileView);
                        break;
                    case REQUEST_MOV:
                    case REQUEST_MOV_KITKAT_INTENT_CALLED:
                        positoin = ((ViewGroup) fileView.getParent()).indexOfChild(fileView);
                        Log.e(TAG, "mMovList remove pos = " + positoin);
                        //서버업로드 기준 서버삭제
                        if (CodeConstant.ATTACHED.equals(mMovList.get(positoin).get(CodeConstant.ATTACH_ST))) {
                            mMovList.get(positoin).put(CodeConstant.ATTACH_ST, CodeConstant.DEL_ATTACHED);
                        } else { //서버 업로드 전이면 클라이언트 파리일스트 삭제
                            mMovList.remove(positoin);
                        }
                        ((ViewGroup) fileView.getParent()).removeView(fileView);
                        break;
                    case REQUEST_FILE:
                    case REQUEST_FILE_KITKAT_INTENT_CALLED:
                        positoin = ((ViewGroup) fileView.getParent()).indexOfChild(fileView);
                        Log.e(TAG, "mFileList remove pos = " + positoin);
                        //서버업로드 기준 서버삭제
                        if (CodeConstant.ATTACHED.equals(mFileList.get(positoin).get(CodeConstant.ATTACH_ST))) {
                            mFileList.get(positoin).put(CodeConstant.ATTACH_ST, CodeConstant.DEL_ATTACHED);
                        } else { //서버 업로드 전이면 클라이언트 파리일스트 삭제
                            mFileList.remove(positoin);
                        }
                        ((ViewGroup) fileView.getParent()).removeView(fileView);
                        break;
                    case REQUEST_REST: //i2conference 앱에서만 등록/수정?
                    case REQUEST_REST_KITKAT_INTENT_CALLED:
                        positoin = ((ViewGroup) fileView.getParent()).indexOfChild(fileView);
                        Log.e(TAG, "m remove pos = " + positoin);
                        //서버업로드 기준 서버삭제
                        if (CodeConstant.ATTACHED.equals(mRestList.get(positoin).get(CodeConstant.ATTACH_ST))) {
                            mRestList.get(positoin).put(CodeConstant.ATTACH_ST, CodeConstant.DEL_ATTACHED);
                        } else { //서버 업로드 전이면 클라이언트 파리일스트 삭제
                            mRestList.remove(positoin);
                        }
                        ((ViewGroup) fileView.getParent()).removeView(fileView);
                        break;
                }
                setFileVisible(mode);
            }
        });

        targetLayout.addView(fileView);
        setFileVisible(mode);
    }


    public void setVisibleOnline(boolean bool) {
        if (bool) {
            mTvCfrcRoomTpNm.setText("온라인");
            mSpCfrcTp.setVisibility(View.VISIBLE);
            mRlCfrcRoom.setVisibility(View.GONE);
            mBtReserveRoom.setText("회의일시예약");
        } else {
            mTvCfrcRoomTpNm.setText("오프라인");
            mSpCfrcTp.setVisibility(View.INVISIBLE);
            mRlCfrcRoom.setVisibility(View.VISIBLE);
            mBtReserveRoom.setText("회의실예약");
        }
    }

    public void saveCfrcProc() {

        if ("".equals(mEtTtl.getText().toString().trim())) {
            DialogUtil.showInformationDialog(ConferenceWriteActivity.this, "제목을 입력하시기 바랍니다.");
            return;
        }

        if ("".equals(mEtCntn.getText().toString().trim())) {
            DialogUtil.showInformationDialog(ConferenceWriteActivity.this, "내용을 입력하시기 바랍니다.");
            return;
        }

        mCfrcSt = mTvCfrcStNm.getText().toString();
        if ("".equals(mCfrcSt) || "대기".equals(mCfrcSt)) {
            mCfrcSt = "RDY";
        } else if ("종료".equals(mCfrcSt)) {
            mCfrcSt = "FIN";
        }

        if (mScCfrcRoomTp.isChecked()) {
            mCfrcRoomTp = "ONLINE";
            switch (mSpCfrcTp.getSelectedItemPosition()) {
                case 1:
                    mCfrcTp = "MOV";
                    break;
                case 2:
                    mCfrcTp = "MOVDOC";
                    break;
                case 3:
                    mCfrcTp = "DOC";
                    break;
                default:
                    DialogUtil.showInformationDialog(ConferenceWriteActivity.this, "회의타입을 선택하시기 바랍니다.");
                    return;
            }
        } else {
            if ("".equals(mRoomId)) {
                DialogUtil.showInformationDialog(ConferenceWriteActivity.this, "회의실정보가 잘못되었습니다.\n회의타입을 선택하시기 바랍니다.");
                return;
            }
            mCfrcRoomTp = "OFFLINE";
            mCfrcTp = "GEN";

        }

        if ("".equals(mTvDt.getText().toString()) || !FormatUtil.isThisDateValid(mTvDt.getText().toString(), "yyyy-MM-dd")) {
            if (mScCfrcRoomTp.isChecked())
                DialogUtil.showInformationDialog(ConferenceWriteActivity.this, "회의일시예약을 하시기 바랍니다.");
            else
                DialogUtil.showInformationDialog(ConferenceWriteActivity.this, "회의날짜가 잘못되었습니다.\n회의실예약을 하시기 바랍니다.");
            return;
        }

        Log.e(TAG, "mStartTm = " + mStartTm + " / mEndTm = " + mEndTm);
        if ("".equals(mStartTm) || "".equals(mEndTm)) {
            if (mScCfrcRoomTp.isChecked())
                DialogUtil.showInformationDialog(ConferenceWriteActivity.this, "회의일시예약을 하시기 바랍니다.");
            else
                DialogUtil.showInformationDialog(ConferenceWriteActivity.this, "회의시간이 잘못되었습니다.\n회의실예약을 하시기 바랍니다.");
            return;
        } else {
            mStartTm = mStartTm.replace(":", "");
            mEndTm = mEndTm.replace(":", "");
        }

        mCfrcNotiYn = mCbNoti.isChecked() ? "Y" : "N";
        mCfrcPlanYn = mCbPlan.isChecked() ? "Y" : "N";


        mTotalList = new ArrayList<List>();
        mTotalList.add(mDocList);
        mTotalList.add(mMovList);
        mTotalList.add(mFileList);
        mTotalList.add(mRestList);

        List<Map<Integer, Integer>> indexList = new ArrayList<>();
        for (int i = 0; i < mTotalList.size(); i++) {
//            Log.e(TAG, "first  i = " + i + " / filesList size = " + mTotalList.get(i).size());
            List<Map> listFileMap = (List<Map>)mTotalList.get(i);
            if (listFileMap == null || (listFileMap != null && listFileMap.size() < 1)) continue;

            for (int j = 0; j < listFileMap.size(); j++) {
                Map<String, String> fileMap = (Map<String, String>)listFileMap.get(j);

                if (fileMap != null && CodeConstant.ATTACH_NEW.equals(fileMap.get(CodeConstant.ATTACH_ST))) {
                    Log.e(TAG, "second  i = " + i + " /j = " + j + " / fileMap = " + fileMap.toString());
                    Map<Integer, Integer> index = new HashMap<>();
                    index.put(1, i);
                    index.put(2, j);
                    indexList.add(index);
                }
            }
        }
        Log.e(TAG, "total  upload file cnt = "+indexList.size());
        if (indexList.size() > 0) {
            uplaodFiles(indexList);
        } else {
            saveCfrc();
        }
    }

    public void saveCfrc() {
        DialogUtil.showCircularProgressDialog(ConferenceWriteActivity.this);

        FormEncodingBuilder cfrcFormBuilder = I2UrlHelper.Cfrc.getCfrcFormBuilder(
                mCfrcId, mEtTtl.getText().toString(), mCfrcSt,
                mTvDt.getText().toString(), mStartTm, mEndTm,
                mCfrcRoomTp, mRoomId, mCfrcTp, mEtCntn.getText().toString(),
                mCfrcNotiYn, mCfrcPlanYn, mEtRestCntn.getText().toString());


        I2ConnectApi.requestJSON2Map(ConferenceWriteActivity.this, I2UrlHelper.Cfrc.getSaveConference(mCfrcId, cfrcFormBuilder, mMemberList, mTotalList))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Conference.getSaveConference onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Conference.getSaveConference onError");
                        DialogUtil.removeCircularProgressDialog();
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(ConferenceWriteActivity.this, e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Conference.getSaveConference onNext");

                        if (I2ResponseParser.checkReponseStatus(status)) {
                            LinkedTreeMap<String, String> statusInfo = (LinkedTreeMap<String, String>) status.get("statusInfo");

                            if (statusInfo != null) {
                                mCfrcId = FormatUtil.getStringValidate(statusInfo.get("cfrc_id"));
                                mCrtUsrId = statusInfo.get(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
                                Log.e(TAG, "saveCfrc CFRC_ID= " + mCfrcId + ", CRT_USR_ID= " + mCrtUsrId);
                                completeWrite(mCfrcId, mCrtUsrId, mEtTtl.getText().toString());
                            }
                        }

                    }
                });
    }

    public void clear() {
        mTotalList.clear();
        mMemberList.clear();
        mDocList.clear();
        mMovList.clear();
        mFileList.clear();
        mRestList.clear();
    }

    public void completeWrite(final String cfrcId, final String crtUsrId, final String cfrcTtl) {
        DialogUtil.removeCircularProgressDialog();

        DialogUtil.showDialog(ConferenceWriteActivity.this, "안내", "정상처리하였습니다.",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clear();
                        //수정처리
                        if (getIntent().getExtras() != null
                                && getIntent().getExtras().containsKey("requestCode")
                                && getIntent().getExtras().getInt("requestCode") == CodeConstant.REQUEST_EDIT) { //편집으로 들어왔을 경우
                            Log.e(TAG, "request code = " + getIntent().getExtras().getInt("requestCode") + " / CodeConstant.REQUEST_EDIT = " + CodeConstant.REQUEST_EDIT);
                            Intent returnIntent = new Intent();
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        } else { // 회의 생성 플로팅 버튼으로 들어왔을 경우
                            Log.e(TAG, "completeWrite cfrc cfrcId =" + cfrcId);
                            Intent intent = new Intent(ConferenceWriteActivity.this, ConferenceDetailActivity.class);
                            intent.putExtra(CodeConstant.CUR_OBJ_TP, CodeConstant.TYPE_CFRC);
                            intent.putExtra(CodeConstant.CUR_OBJ_ID, cfrcId);
                            intent.putExtra(CodeConstant.TAR_OBJ_TTL, cfrcTtl);
                            intent.putExtra(CodeConstant.CRT_USR_ID, crtUsrId);
                            intent.putExtra(CodeConstant.TAB_POS, ConferenceDetailActivity.VIEW_CFRC_DETAIL);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    public void uplaodFiles(final List<Map<Integer, Integer>> indexList) {

        if (mUploadedCnt < indexList.size()) {
            DialogUtil.showCircularProgressDialog(ConferenceWriteActivity.this);

            Map<Integer, Integer> index = indexList.get(mUploadedCnt);
            final int listIndex = index.get(1);
            final int fileIndex = index.get(2);
            Map<String, String> fileMap = (Map<String, String>)mTotalList.get(listIndex).get(fileIndex);
            Uri uri = Uri.parse(fileMap.get("uri"));
            File file = new File(FileUtil.getPath(this, uri));
            String fileNm = fileMap.get("file_nm");
            String mimeType = FileUtil.getMimeType(this, uri);

            I2ConnectApi.uploadFile(ConferenceWriteActivity.this, fileNm, mimeType, file)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<JSONObject>() {
                        @Override
                        public void onCompleted() {
                            Log.d(TAG, "uploadFile onCompleted");
                            DialogUtil.removeCircularProgressDialog();
                            mUploadedCnt++;
                            if (mUploadedCnt >= indexList.size()) {
                                saveCfrc();
                            } else {
                                uplaodFiles(indexList);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "uploadFile onError");
                            DialogUtil.removeCircularProgressDialog();
                            e.printStackTrace();
                            //Error dialog 표시
                            DialogUtil.showErrorDialogWithValidateSession(ConferenceWriteActivity.this, e);
                        }

                        @Override
                        public void onNext(JSONObject jsonObject) {
                            Log.d(TAG, "uploadFile onNext");
                            if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                                JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                                JSONArray fileList = I2ResponseParser.getJsonArray(statusInfo, "file_list");
                                try {
                                    if (fileList.length() > 0) {
                                        // 기존 리스트에 결과값 추가
                                        ((Map<String, String>)mTotalList.get(listIndex).get(fileIndex)).put("file_id", fileList.getJSONObject(0).getString("file_id"));
                                        ((Map<String, String>)mTotalList.get(listIndex).get(fileIndex)).put("file_nm", fileList.getJSONObject(0).getString("file_nm"));
                                        ((Map<String, String>)mTotalList.get(listIndex).get(fileIndex)).put("file_tp_cd", fileList.getJSONObject(0).getString("file_tp_cd"));
                                        ((Map<String, String>)mTotalList.get(listIndex).get(fileIndex)).put("phscl_file_nm", fileList.getJSONObject(0).getString("phscl_file_nm"));
                                        ((Map<String, String>)mTotalList.get(listIndex).get(fileIndex)).put("file_ext", fileList.getJSONObject(0).getString("file_ext"));
                                        ((Map<String, String>)mTotalList.get(listIndex).get(fileIndex)).put("file_size", fileList.getJSONObject(0).getString("file_size"));
//                                        ((Map<String, String>)mTotalList.get(listIndex).get(fileIndex)).put("file_cont_type", fileList.getJSONObject(0).getString("file_cont_type"));
//                                        ((Map<String, String>)mTotalList.get(listIndex).get(fileIndex)).put("charset", fileList.getJSONObject(0).getString("charset"));
//                                        ((Map<String, String>)mTotalList.get(listIndex).get(fileIndex)).put("file_path", fileList.getJSONObject(0).getString("file_path"));
//                                        ((Map<String, String>)mTotalList.get(listIndex).get(fileIndex)).put("file_path", fileList.getJSONObject(0).getString("file_path"));
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

    public void loadEditViewCfrc() {
        I2ConnectApi.requestJSON2Map(ConferenceWriteActivity.this, I2UrlHelper.Cfrc.getViewSnsConference(mCfrcId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Conference.getViewSnsConference onCompleted");
                        loadEditViewCfrcFile();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Conference.getViewSnsConference onError");
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(ConferenceWriteActivity.this, e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Conference.getViewSnsConference onNext");
                        LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");
                        setCfrcViewData(statusInfo);
                    }
                });
    }

    public void loadEditViewCfrcFile() {
        I2ConnectApi.requestJSON2Map(ConferenceWriteActivity.this, I2UrlHelper.Cfrc.getListSnsCfrcFile(mCfrcId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Conference.getViewSnsConference onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Conference.getViewSnsConference onError");
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(ConferenceWriteActivity.this, e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Conference.getViewSnsConference onNext");
                        LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");
                        setCfrcFileData(statusInfo);
                    }
                });
    }

    public void setCfrcViewData(LinkedTreeMap<String, Object> statusInfo) {
        final String crtUsrId = FormatUtil.getStringValidate(statusInfo.get("crt_usr_id"));
        final String crtUsrNm = FormatUtil.getStringValidate(statusInfo.get("crt_usr_nm"));

        mEtTtl.setText(statusInfo.get("cfrc_ttl").toString());

        mRlCfrcEdit.setVisibility(View.VISIBLE);
        //수정이력 있음, 수정자 기준표시, 수정없음, 작성자 기준표시
        if ("".equals(FormatUtil.getStringValidate(statusInfo.get("mod_dttm")))) {
            mTvCfrcCrtDttm.setText(FormatUtil.getFormattedDateTime(FormatUtil.getStringValidate(statusInfo.get("crt_dttm")))); //만든시간 표시
        } else {
            mTvCfrcCrtDttm.setText(FormatUtil.getFormattedDateTime(FormatUtil.getStringValidate(statusInfo.get("mod_dttm")))); //수정시간 표시
        }

        String cfrcRoomTpNm = FormatUtil.getStringValidate(statusInfo.get("cfrc_room_tp"));
        if ("ONLINE".equals(cfrcRoomTpNm)) {
            mScCfrcRoomTp.setChecked(true);
            mSpCfrcTp.setVisibility(View.VISIBLE);
        } else {
            mScCfrcRoomTp.setChecked(false);
            mSpCfrcTp.setVisibility(View.GONE);
            mRoomId = FormatUtil.getStringValidate(statusInfo.get("cfrc_room_id"));
        }
        String cfrcTp = FormatUtil.getStringValidate(statusInfo.get("cfrc_tp"));
        if ("MOV".equals(cfrcTp)) {
            mSpCfrcTp.setSelection(1);
        } else if ("MOVDOC".equals(cfrcTp)) {
            mSpCfrcTp.setSelection(2);
        } else if ("DOC".equals(cfrcTp)) {
            mSpCfrcTp.setSelection(3);
        } else {
            mSpCfrcTp.setSelection(0);
        }

        mTvCfrcStNm.setText(FormatUtil.getStringValidate(statusInfo.get("cfrc_st_nm")));
        mTvCfrcRoomNm.setText(FormatUtil.getStringValidate(statusInfo.get("cfrc_room_nm")));

        mStartTm = FormatUtil.getStringValidate(statusInfo.get("start_tm"));
        mEndTm = FormatUtil.getStringValidate(statusInfo.get("end_tm"));
        mTvDt.setText(FormatUtil.getFormattedDate5(FormatUtil.getStringValidate(statusInfo.get("cfrc_dt"))));
        mTvPriodeTm.setText(FormatUtil.getFormattedCfrcTime(FormatUtil.getStringValidate(statusInfo.get("start_tm"))) + "~" +
                FormatUtil.getFormattedCfrcTime(FormatUtil.getStringValidate(statusInfo.get("end_tm"))));

        mTvCfrcRoomTpNm.setText(FormatUtil.getStringValidate(statusInfo.get("cfrc_room_tp_nm")));

        mEtCntn.setText(FormatUtil.getStringValidate(statusInfo.get("cfrc_cntn")));

        if ("Y".equals(FormatUtil.getStringValidate(statusInfo.get("cfrc_crt_noti_yn")))) {
            mCbNoti.setChecked(true);
        } else {
            mCbNoti.setChecked(false);
        }

        if ("Y".equals(FormatUtil.getStringValidate(statusInfo.get("plan_open_yn")))) {
            mCbPlan.setChecked(true);
        } else {
            mCbPlan.setChecked(false);
        }

        //멤버 표시
        List<Map<String, String>> userList = (List<Map<String, String>>) statusInfo.get("ref_user_list");
        for (int i = 0; i < userList.size(); i++) {
            userList.get(i).put("usr_id", userList.get(i).get("ref_usr_id")); //기존 참가자
            userList.get(i).put("usr_nm", userList.get(i).get("ref_usr_nm"));
            userList.get(i).put("crt_usr_photo", userList.get(i).get("ref_usr_photo"));
            Log.e(TAG, userList.get(i).toString());
        }
        mMemberList.addAll(userList);
        setVisibleMember();
        mAdapter.notifyDataSetChanged();

        //파일 뱔도 처리

        //회의결과자
        if ("종료".equals(mTvCfrcStNm.getText().toString())) {
            mRlCfrcRestCntn.setVisibility(View.VISIBLE);
            mRlCfrcRestList.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(FormatUtil.getStringValidate(statusInfo.get("cfrc_rslt_cntn"))))
                mRlCfrcRestCntn.setVisibility(View.GONE);
            else {
                mRlCfrcRestCntn.setVisibility(View.VISIBLE);
                mEtRestCntn.setText(FormatUtil.getStringValidate(statusInfo.get("cfrc_rslt_cntn")));
            }
        }
    }

    public void setCfrcFileData(LinkedTreeMap<String, Object> statusInfo) {
        //회의문서자료
        setFilesLayoutByEdit(mLlCfrcDoc, mTvEmptyDoc, REQUEST_DOC, statusInfo.get("doc_file_list"));
        //동영상공유자료
        setFilesLayoutByEdit(mLlCfrcMov, mTvEmptyMov, REQUEST_MOV, statusInfo.get("share_mov_list"));
        //파일공유자료
        setFilesLayoutByEdit(mLlCfrcFile, mTvEmptyFile, REQUEST_FILE, statusInfo.get("gnr_file_list"));
        //회의결과자료
        setFilesLayoutByEdit(mLlCfrcRest, mTvEmptyRest, REQUEST_REST, statusInfo.get("rest_file_list"));
    }

    public void setFilesLayoutByEdit(LinearLayout targetLayout, TextView tvEmpty, int mode, Object object) {
        final List<LinkedTreeMap<String, String>> listMap = (List<LinkedTreeMap<String, String>>) object;

        Log.e(TAG, "fileList size =" + listMap.size());
        if (listMap == null || listMap.size() <= 0) {
            targetLayout.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            targetLayout.setVisibility(View.VISIBLE);
            targetLayout.removeAllViews();
            tvEmpty.setVisibility(View.GONE);
        }

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
            file.put("file_ext", listMap.get(i).get("file_ext"));
            file.put("file_size", String.valueOf(listMap.get(i).get("file_size")));
            file.put(CodeConstant.ATTACH_ST, CodeConstant.ATTACHED);
            Log.e(TAG, "listMap.size() setFilesLayoutByEdit / " + file.toString());

            switch (mode) {
                case REQUEST_DOC:
                    if (mDocList == null) mDocList = new ArrayList<Map<String, String>>();
                    mDocList.add(file);
                    Log.e(TAG, "mDocList size = " + mDocList.size());
                    break;
                case REQUEST_MOV:
                    if (mMovList == null) mMovList = new ArrayList<Map<String, String>>();
                    mMovList.add(file);
                    Log.e(TAG, "mMovList size = " + mMovList.size());
                    break;
                case REQUEST_FILE:
                    if (mFileList == null) mFileList = new ArrayList<Map<String, String>>();
                    mFileList.add(file);
                    Log.e(TAG, "fileList size = " + mFileList.size());
                    break;
                case REQUEST_REST:
                    if (mRestList == null) mRestList = new ArrayList<Map<String, String>>();
                    mRestList.add(file);
                    Log.e(TAG, "mRestList size = " + mRestList.size());
                    break;
            }
            addFilesLayout(targetLayout, tvEmpty, mode, fileNm);
        }
    }

    public void setVisibleMember() {
        if (mMemberList == null || mMemberList.size() < 1) {
            mRvCfrcMember.setVisibility(View.GONE);
            mTvEmptyMember.setVisibility(View.VISIBLE);
        } else {
            mRvCfrcMember.setVisibility(View.VISIBLE);
            mTvEmptyMember.setVisibility(View.GONE);
        }
    }

    public class CfrcMemberRecyclerViewAdapter
            extends RecyclerView.Adapter<CfrcMemberRecyclerViewAdapter.ViewHolder> {

        protected Context mContext;
        private List<Map<String, String>> mValues;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public String mUsrID;
            public final CircleImageView mCivCrtUsrPhoto;
            public final TextView mTvUsrNm, mTvCfrcUsrFlagNm;
            public final ImageButton mIvDelmember;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mCivCrtUsrPhoto = (CircleImageView) view.findViewById(R.id.civ_crt_usr_photo);
                mTvUsrNm = (TextView) view.findViewById(R.id.tv_usr_nm);
                mTvCfrcUsrFlagNm = (TextView) view.findViewById(R.id.tv_cfrc_usr_flag_nm);

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

        public CfrcMemberRecyclerViewAdapter(Context context, List<Map<String, String>> items) {
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
            final String cfrcUsrFlagNm = person.get("cfrc_usr_flag_nm");
//            final String usrImg = person.get("crt_usr_photo");
//            final String usrCfrcUserFlag = person.get("cfrc_usr_flag");

            holder.mUsrID = usrId;
            Log.e(TAG, "usr name = " + usrNm);
            holder.mTvUsrNm.setText(usrNm);
            holder.mTvCfrcUsrFlagNm.setText(cfrcUsrFlagNm);

            Glide.with(holder.mCivCrtUsrPhoto.getContext())
                    .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(person.get("crt_usr_photo"))))
                    .error(R.drawable.ic_no_usr_photo)
                    .fitCenter()
                    .into(holder.mCivCrtUsrPhoto);

            holder.mIvDelmember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG, "remove size =" + pos);
                    ((ConferenceWriteActivity) mContext).mMemberList.remove(pos);
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
}
