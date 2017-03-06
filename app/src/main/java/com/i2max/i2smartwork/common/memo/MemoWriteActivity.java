package com.i2max.i2smartwork.common.memo;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.internal.LinkedTreeMap;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.common.conference.ConferenceDetailActivity;
import com.i2max.i2smartwork.common.sns.SNSPersonSearchActivity;
import com.i2max.i2smartwork.common.task.TaskDetailActivity;
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

/**
 * Created by shlee on 2015. 10. 16..
 */
public class MemoWriteActivity extends BaseAppCompatActivity {
    static String TAG = MemoWriteActivity.class.getSimpleName();

    public static final int REQUEST_APPR_MEMBER_SEARCH = 3001;
    public static final int REQUEST_REFC_MEMBER_SEARCH = 3002;
    protected final int REQUEST_FILE_KITKAT_INTENT_CALLED = 2103; //키캣 이상 문서파일 열기 처리
    protected final int REQUEST_FILE = 2104; //키캣 이전버젼 문파서일 열기처리

    protected LinearLayout mLlMemoFile;
    protected EditText mEtTtl, mEtCntn;
    protected TextView mTvCrtDttm, mTvEmptyApprMember, mTvEmptyRefcMember, mTvEmptyFile, mTvMemoSt, mTvTarObjTtl, mTvCrtUsrNM;
    protected ImageView mIbAddApprMember, mIbAddRefcMember, mIbAddFiles, mIbSave;
    protected CircleImageView mCivCrtUsrPhoto;
    protected RecyclerView mRvApprMember, mRvRefcMember;
    protected MemoMemberRecyclerViewAdapter mApprAdapter, mRefcAdapter;

    private int mUploadedCnt = 0;
    private List<List> mTotalList;
    private List<Map<String, String>> mApprMemberList = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> mRefcMemberList = new ArrayList<Map<String, String>>();
    private List<Map<String, String>> mFileList = new ArrayList<Map<String, String>>();

    protected String mTarObjTp = "", mTarObjId = "", mTarObjTtl = "", mTarCrtUsrId = "", mObjId = "", mCrtUsrId = "", mMemoStCd = "";
    protected Calendar now;
    int mTabPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_memo_write);

        Bundle extra = getIntent().getExtras();
        String title = getString(R.string.task_add);
        if (extra != null) {
            title = extra.getString(CodeConstant.TITLE, getString(R.string.memo_add));
            mTarObjTp = extra.getString(CodeConstant.TAR_OBJ_TP, "");
            mTarObjId = extra.getString(CodeConstant.TAR_OBJ_ID, "");
            mTarObjTtl = extra.getString(CodeConstant.TAR_OBJ_TTL, "");
            mTarCrtUsrId = extra.getString(CodeConstant.TAR_CRT_USR_ID, "");
            mObjId = extra.getString(CodeConstant.TASK_ID, "");
            mCrtUsrId = extra.getString(CodeConstant.CRT_USR_ID, "");
            mTabPos = extra.getInt(CodeConstant.TAB_POS, 1);
            Log.e(TAG, "mTarObjTp = " + mTarObjTp + " /mTarObjId = " + mTarObjId + " /mTarObjTtl = " + mTarObjTtl + " /mTarCrtUsrId= " + mTarCrtUsrId);
            Log.e(TAG, "TASK_ID= " + mObjId + " /CRT_USR_ID= " + mCrtUsrId + " /mTabPos = " + mTabPos);
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(title);

        mEtTtl = (EditText) findViewById(R.id.et_ttl);
        mCivCrtUsrPhoto = (CircleImageView) findViewById(R.id.civ_crt_usr_photo);
        mTvCrtUsrNM = (TextView) findViewById(R.id.tv_crt_usr_nm);
        mTvTarObjTtl = (TextView) findViewById(R.id.tv_tar_obj_ttl);
        mEtCntn = (EditText) findViewById(R.id.et_cntn);
        mTvMemoSt = (TextView) findViewById(R.id.tv_memo_st);
        mTvCrtDttm = (TextView) findViewById(R.id.tv_crt_dttm);

        mIbAddApprMember = (ImageView) findViewById(R.id.ib_add_appr_member);
        mIbAddRefcMember = (ImageView) findViewById(R.id.ib_add_refc_member);
        mRvApprMember = (RecyclerView) findViewById(R.id.rv_appr_member);
        mRvRefcMember = (RecyclerView) findViewById(R.id.rv_refc_member);
        mTvEmptyApprMember = (TextView) findViewById(R.id.tv_empty_appr_member);
        mTvEmptyRefcMember = (TextView) findViewById(R.id.tv_empty_refc_member);

        mIbAddFiles = (ImageView) findViewById(R.id.ib_add_file);
        mLlMemoFile = (LinearLayout) findViewById(R.id.ll_memo_file_list);
        mTvEmptyFile = (TextView) findViewById(R.id.tv_memo_empty_file);

        mIbSave = (ImageView) findViewById(R.id.iv_bt_save);

        GridLayoutManager layoutManager1 = new GridLayoutManager(this, 5);
        layoutManager1.setOrientation(LinearLayoutManager.VERTICAL);
        mRvApprMember.setLayoutManager(layoutManager1);
        GridLayoutManager layoutManager2 = new GridLayoutManager(this, 5);
        layoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        mRvRefcMember.setLayoutManager(layoutManager2);

        initView();
    }

    public void initView() {
        now = Calendar.getInstance();

        mIbAddApprMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mApprMemberList != null && mApprMemberList.size() > 0) { //결재자 1명만 선택처리
                    Toast.makeText(MemoWriteActivity.this, "결재자를 이미 선택하셨습니다.\n삭제하시고 선택하시기 바랍니다.", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    //Search
                    Intent intent = new Intent(MemoWriteActivity.this, SNSPersonSearchActivity.class);
                    intent.putExtra(SNSPersonSearchActivity.USR_ID, PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
                    intent.putExtra(SNSPersonSearchActivity.MODE, CodeConstant.MODE_TASK_MEMBER_ADD);
                    startActivityForResult(intent, REQUEST_APPR_MEMBER_SEARCH);
                }
            }
        });

        mIbAddRefcMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Search
                Intent intent = new Intent(MemoWriteActivity.this, SNSPersonSearchActivity.class);
                intent.putExtra(SNSPersonSearchActivity.USR_ID, PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
                intent.putExtra(SNSPersonSearchActivity.MODE, CodeConstant.MODE_TASK_MEMBER_ADD);
                startActivityForResult(intent, REQUEST_REFC_MEMBER_SEARCH);
            }
        });

        //생성자
        mTvCrtUsrNM.setText(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_NM));
        Glide.with(mCivCrtUsrPhoto.getContext())
                .load(I2UrlHelper.File.getUsrImage(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_PHOTO)))
                .error(R.drawable.ic_no_usr_photo)
                .fitCenter()
                .into(mCivCrtUsrPhoto);

        //결재자
        mApprMemberList = new ArrayList<Map<String, String>>();
        mApprAdapter = new MemoMemberRecyclerViewAdapter(MemoWriteActivity.this, mApprMemberList);
        mRvApprMember.setAdapter(mApprAdapter);
        //참조자
        mRefcMemberList = new ArrayList<Map<String, String>>();
        mRefcAdapter = new MemoMemberRecyclerViewAdapter(MemoWriteActivity.this, mRefcMemberList);
        mRvRefcMember.setAdapter(mRefcAdapter);

        mIbAddFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser(REQUEST_FILE, REQUEST_FILE_KITKAT_INTENT_CALLED);
            }
        });

        mIbSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePrevProc();
            }
        });

        if (!TextUtils.isEmpty(mObjId)) {
            loadEditViewMemo();
        } else {
            mTvCrtDttm.setText(FormatUtil.getSendableFormatNow());

            mEtTtl.setText("메모 (" + PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_NM) + ") " + FormatUtil.getSendableFormatNow());
            mEtCntn.setText("");

//            Map<String, String> person = new HashMap<String, String>();
//            person.put("post_usr_id", "");
//            person.put("post_id", "");
//            person.put("usr_id", PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
//            person.put("usr_nm", PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_NM));
//            person.put("photo_img_min", PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_PHOTO));
//            person.put("appr_usr_id", PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
//            person.put("fnl_appr_yn", "N"); //신규 참가자

            //작성자 결재자 중복체크
//            for (int i = 0; i < mApprMemberList.size(); i++) {
//                if (person.get("usr_id").equals(mApprMemberList.get(i).get("usr_id"))) {
//                    Toast.makeText(MemoWriteActivity.this, "중복된 참가자입니다", Toast.LENGTH_LONG).show();
//                    return;
//                }
//            }

            //작성자 참조자 중복체크
//            for (int i = 0; i < mRefcMemberList.size(); i++) {
//                if (person.get("usr_id").equals(mRefcMemberList.get(i).get("usr_id"))) {
//                    Toast.makeText(MemoWriteActivity.this, "작성자가 참조자로 중복된 참가자입니다", Toast.LENGTH_LONG).show();
//                    return;
//                }
//            }

            //결재자 visible처리
            if (mApprMemberList == null) mApprMemberList = new ArrayList<Map<String, String>>();
            if (mApprMemberList == null || mApprMemberList.size() < 1) {
                mRvApprMember.setVisibility(View.GONE);
                mTvEmptyApprMember.setVisibility(View.VISIBLE);
            } else {
                mRvApprMember.setVisibility(View.VISIBLE);
                mTvEmptyApprMember.setVisibility(View.GONE);
            }
            mApprAdapter.notifyDataSetChanged();

            //결재자 visible처리
            if (mRefcMemberList == null) mRefcMemberList = new ArrayList<Map<String, String>>();
            if (mRefcMemberList == null || mRefcMemberList.size() < 1) {
                mRvRefcMember.setVisibility(View.GONE);
                mTvEmptyRefcMember.setVisibility(View.VISIBLE);
            } else {
                mRvRefcMember.setVisibility(View.VISIBLE);
                mTvEmptyRefcMember.setVisibility(View.GONE);
            }
            mRefcAdapter.notifyDataSetChanged();
        }

    }

    public void loadEditViewMemo() {
        I2ConnectApi.requestJSON2Map(MemoWriteActivity.this, I2UrlHelper.Memo.getViewSnsMemo(mObjId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Memo.getViewSnsMemo onCompleted");
                        loadEditViewFile();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Memo.getViewSnsMemo onError");
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(MemoWriteActivity.this, e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Memo.getViewSnsMemo onNext");
                        LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");
                        setViewData(statusInfo);
                    }
                });
    }

    public void loadEditViewFile() {
        I2ConnectApi.requestJSON2Map(MemoWriteActivity.this, I2UrlHelper.Task.getListSnsTaskFile(mObjId))
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
                        DialogUtil.showErrorDialogWithValidateSession(MemoWriteActivity.this, e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Task.getListSnsTaskFile onNext");
                        LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");
                        setFileData(statusInfo);
                    }
                });
    }


    public void setViewData(LinkedTreeMap<String, Object> statusInfo) {
        final String crtUsrId = FormatUtil.getStringValidate(statusInfo.get("crt_usr_id"));
        final String crtUsrNm = FormatUtil.getStringValidate(statusInfo.get("crt_usr_nm"));
        final String tarObjTp = FormatUtil.getStringValidate(statusInfo.get("tar_obj_tp_cd"));
        final String tarObjId = FormatUtil.getStringValidate(statusInfo.get("tar_obj_id"));
        final String tarUsrId = FormatUtil.getStringValidate(statusInfo.get("tar_usr_id"));

        mEtTtl.setText(statusInfo.get("ttl").toString());
        mEtCntn.setText(FormatUtil.getStringValidate(statusInfo.get("cntn")));

        mTvCrtDttm.setText(FormatUtil.getFormattedDateTime(FormatUtil.getStringValidate(statusInfo.get("crt_dttm")))); //만든시간 표시
        //상태
        mTvMemoSt.setText(FormatUtil.getStringValidate(statusInfo.get("appr_st_nm")));

        //결재자 표시 1명만 가능
        Map<String, String> apprUsr = new HashMap<>();
        apprUsr.put("usr_id", FormatUtil.getStringValidate(statusInfo.get("appr_usr_id")));
        apprUsr.put("fnl_appr_yn", FormatUtil.getStringValidate(statusInfo.get("fnl_appr_yn"))); //"Y"기존 참가자, "N"신규
        mApprMemberList.add(apprUsr);
        mApprAdapter.notifyDataSetChanged();

        //참조자 표시
        List<Map<String, String>> userList = (List<Map<String, String>>) statusInfo.get("ref_user_list");
        for (int i = 0; i < userList.size(); i++) {
            userList.get(i).put("usr_id", userList.get(i).get("usr_id"));
            String fnlApprYn = FormatUtil.getStringValidate(userList.get(i).get("fnl_appr_yn"));
            userList.get(i).put("fnl_appr_yn", TextUtils.isDigitsOnly(fnlApprYn)?"Y":fnlApprYn);  //"Y"기존 참가자, "N"신규
        }
        mRefcMemberList.addAll(userList);
        mRefcAdapter.notifyDataSetChanged();

        //파일ㄹ 리스트 처리
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
                                intent = new Intent(MemoWriteActivity.this, ConferenceDetailActivity.class);
                            } else if (CodeConstant.TYPE_TASK.equals(tarObjTp)) {
                                intent = new Intent(MemoWriteActivity.this, TaskDetailActivity.class);
                            } else if (CodeConstant.TYPE_MEMO.equals(tarObjTp)) {
                                intent = new Intent(MemoWriteActivity.this, MemoDetailActivity.class);
                            } else if (CodeConstant.TYPE_WORK.equals(tarObjTp)) {
                                intent = new Intent(MemoWriteActivity.this, WorkDetailActivity.class);
                            }
                            intent.putExtra(CodeConstant.CUR_OBJ_TP, tarObjTp);
                            intent.putExtra(CodeConstant.CUR_OBJ_ID, tarObjId);
                            intent.putExtra(CodeConstant.CRT_USR_ID, tarUsrId);
                            startActivity(intent);

                        }
                    });
            mTvTarObjTtl.setText(tarObjTtl);
            LinkBuilder.on(mTvTarObjTtl)
                    .addLink(tarObjLink)
                    .build();
        } else {
            mTvTarObjTtl.setText(tarObjTtl);
        }

    }

    public void setFileData(LinkedTreeMap<String, Object> statusInfo) {
        //파일공유자료
        setFilesLayoutByEdit(mLlMemoFile, mTvEmptyFile, REQUEST_FILE, statusInfo.get("doc_file_list"));
    }

    public void savePrevProc() {

        if ("".equals(mEtTtl.getText().toString().trim())) {
            DialogUtil.showInformationDialog(MemoWriteActivity.this, "제목을 입력하시기 바랍니다.");
            return;
        }

        if ("".equals(mEtCntn.getText().toString().trim())) {
            DialogUtil.showInformationDialog(MemoWriteActivity.this, "내용을 입력하시기 바랍니다.");
            return;
        }

        mMemoStCd = mTvMemoSt.getText().toString().trim();
        if ("(처리중)상신".equals(mMemoStCd)) {
            mMemoStCd = "PRCS";
        } else if ("진행".equals(mMemoStCd)) {
            mMemoStCd = "REF";
        } else if ("지연".equals(mMemoStCd)) {
            mMemoStCd = "CPLT";
        } else if ("종료".equals(mMemoStCd)) {
            mMemoStCd = "TEMP";
        } else if ("종료".equals(mMemoStCd)) {
            mMemoStCd = "RPT";
        } else if ("종료".equals(mMemoStCd)) {
            mMemoStCd = "FIN";
        }


        mTotalList = new ArrayList<List>();
        mTotalList.add(mFileList);

        Log.e(TAG, "mTotalList.size() = " + mTotalList.size());
        List<Map<Integer, Integer>> indexList = new ArrayList<>();
        for (int i = 0; i < mTotalList.size(); i++) {
            Log.e(TAG, "i = " + i + " / filesList = " + mTotalList.get(i).size());
            if (mTotalList.get(i) == null || mTotalList.get(i).size() < 1) continue;

            for (int j = 0; j < mTotalList.get(i).size(); i++) {
                Map<String, String> fileMap = (Map<String, String>)mTotalList.get(i).get(j);
                Log.e(TAG, "i = " + i + " /j = " + j + " / fileMap = " + fileMap.get("file_nm"));
                if (fileMap == null) break;

                if (CodeConstant.ATTACH_NEW.equals(fileMap.get(CodeConstant.ATTACH_ST))) {
                    Map<Integer, Integer> index = new HashMap<>();
                    index.put(1, i);
                    index.put(2, j);
                    indexList.add(index);
                    Log.e(TAG, "FILE UPLOAD INDEX i= "+i+" / j = " + j);
                }
            }
        }
        Log.e(TAG, "total  upload file cnt = "+indexList.size());
        if (indexList.size() > 0) {
            uplaodFiles(indexList, mTotalList);
        } else {
            save();
        }
    }

    public void save() {
        DialogUtil.showCircularProgressDialog(MemoWriteActivity.this);

        String restCntn = "";

//        FormEncodingBuilder memoFormBuilder = I2UrlHelper.Memo.getMemoFormBuilder(
//                mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrId,
//                mObjId, mEtTtl.getText().toString(), mMemoStCd,
//                mEtCntn.getText().toString());
//
//        //mTotalList
//        I2ConnectApi.requestJSON2Map(I2UrlHelper.Task.getSaveTask(mObjId, memoFormBuilder, mApprMemberList, mRefcMemberList, null))
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Map<String, Object>>() {
//                    @Override
//                    public void onCompleted() {
//                        Log.d(TAG, "I2UrlHelper.Task.getSaveTask onCompleted");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d(TAG, "I2UrlHelper.Task.getSaveTask onError");
//                        DialogUtil.removeCircularProgressDialog();
//                        //Error dialog 표시
//                        DialogUtil.showErrorDialog(MemoWriteActivity.this, e.getMessage());
//                    }
//
//                    @Override
//                    public void onNext(Map<String, Object> status) {
//                        Log.d(TAG, "I2UrlHelper.Task.getSaveTask onNext");
//                        LinkedTreeMap<String, String> statusInfo = (LinkedTreeMap<String, String>) status.get("statusInfo");
//                        if (statusInfo != null) {
//                            mObjId = statusInfo.get("task_id");
//                            mCrtUsrId = statusInfo.get("crt_usr_id");
//                            completeWrite(mObjId, mCrtUsrId);
//                        }
//                    }
//                });
    }

    public void clear() {
        mTotalList.clear();
        mApprMemberList.clear();
        mRefcMemberList.clear();
        mFileList.clear();
    }

    public void completeWrite(final String memoId, final String crtUsrId) {
        DialogUtil.removeCircularProgressDialog();

        DialogUtil.showDialog(MemoWriteActivity.this, "안내", "작업생성이 완료되었습니다.",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clear();
                        //수정처리
                        Log.e(TAG, "completeWrite  memoId =" + memoId);
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
//                        } else {
                            intent = new Intent(MemoWriteActivity.this, MemoDetailActivity.class);
                            intent.putExtra(CodeConstant.TAR_OBJ_TP, mTarObjTp);
                            intent.putExtra(CodeConstant.TAR_OBJ_ID, mTarObjId);
                            intent.putExtra(CodeConstant.TAR_OBJ_TTL, mTarObjTtl);
                            intent.putExtra(CodeConstant.TAR_CRT_USR_ID, mTarCrtUsrId);
                            intent.putExtra(CodeConstant.CUR_OBJ_TP, CodeConstant.TYPE_TASK);
                            intent.putExtra(CodeConstant.CUR_OBJ_ID, memoId);
                            intent.putExtra(CodeConstant.CRT_USR_ID, crtUsrId);
                            intent.putExtra(CodeConstant.TAB_POS, TaskDetailActivity.VIEW_TASK_DETAIL);
                            startActivity(intent);
//                        }

                        finish();
                    }
                });
    }

    public void uplaodFiles(final List<Map<Integer, Integer>> indexList, final List<List> totalList) {

        if (mUploadedCnt < indexList.size()) {
            DialogUtil.showCircularProgressDialog(MemoWriteActivity.this);

            Map<Integer, Integer> index = indexList.get(mUploadedCnt);
            final int listIndex = index.get(1);
            final int fileIndex = index.get(2);
            Map<String, String> fileMap = (Map<String, String>)totalList.get(listIndex).get(fileIndex);
            Uri uri = Uri.parse(fileMap.get("uri"));
            File file = new File(FileUtil.getPath(this, uri));
            String fileNm = fileMap.get("file_nm");
            String mimeType = FileUtil.getMimeType(this, uri);

            I2ConnectApi.uploadFile(MemoWriteActivity.this, fileNm, mimeType, file)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<JSONObject>() {
                        @Override
                        public void onCompleted() {
                            Log.d(TAG, "uploadFile onCompleted");
                            DialogUtil.removeCircularProgressDialog();
                            mUploadedCnt++;
                            if (mUploadedCnt >= indexList.size()) {
                                save();
                            } else {
                                uplaodFiles(indexList, totalList);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "uploadFile onError");
                            DialogUtil.removeCircularProgressDialog();
                            e.printStackTrace();
                            //Error dialog 표시
                            DialogUtil.showErrorDialogWithValidateSession(MemoWriteActivity.this, e);
                        }

                        @Override
                        public void onNext(JSONObject jsonObject) {
                            Log.d(TAG, "uploadFile onNext");
                            if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                                JSONArray statusInfoArray = I2ResponseParser.getStatusInfoArray(jsonObject);
                                try {
                                    if (statusInfoArray.length() > 0) {
                                        // 기존 리스트에 결과값 추가
                                        ((Map<String, String>)totalList.get(listIndex).get(fileIndex)).put("attach_file_id", statusInfoArray.getJSONObject(0).getString("fileId"));
                                        ((Map<String, String>)totalList.get(listIndex).get(fileIndex)).put("attach_nm", statusInfoArray.getJSONObject(0).getString("fileName"));
                                        ((Map<String, String>)totalList.get(listIndex).get(fileIndex)).put("physcl_file_nm", statusInfoArray.getJSONObject(0).getString("systemFileName"));
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
            String fileNm = FormatUtil.getStringValidate(listMap.get(i).get("attach_nm"));
            file.put("uri", "");
            file.put("file_nm", fileNm);
            file.put("attach_nm", fileNm);
            file.put("attach_file_id", listMap.get(i).get("attach_file_id"));
            file.put("attach_tp_cd", listMap.get(i).get("attach_tp_cd"));
            file.put("tar_tp", listMap.get(i).get("tar_tp"));
            file.put("tar_sub_tp", listMap.get(i).get("tar_sub_tp"));
            file.put("physcl_file_path", listMap.get(i).get("physcl_file_path"));
            file.put(CodeConstant.ATTACH_ST, CodeConstant.ATTACHED);

            switch (mode) {
                case REQUEST_FILE:
                    if (mFileList == null) mFileList = new ArrayList<Map<String, String>>();
                    mFileList.add(file);
                    Log.e(TAG, "fileList size = " + mFileList.size());
                    break;
            }
            addFilesLayout(targetLayout, tvEmpty, mode, fileNm);
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
            case REQUEST_APPR_MEMBER_SEARCH:
                if (data != null) {
                    Map<String, String> person = new HashMap<String, String>();
                    person.put("usr_id", data.getExtras().getString("usr_id"));
                    person.put("usr_nm", data.getExtras().getString("usr_nm"));
                    person.put("photo_img_min", data.getExtras().getString("usr_img"));
                    person.put("fnl_appr_yn", "N"); //신규 참가자

                    //set image
                    if (mApprMemberList == null) mApprMemberList = new ArrayList<>();
                    mApprMemberList.add(person);
                    if (mApprMemberList == null || mApprMemberList.size() < 1) {
                        mRvApprMember.setVisibility(View.GONE);
                        mTvEmptyApprMember.setVisibility(View.VISIBLE);
                    } else {
                        mRvApprMember.setVisibility(View.VISIBLE);
                        mTvEmptyApprMember.setVisibility(View.GONE);
                    }
                    mApprAdapter.notifyDataSetChanged();
                }
                break;
            case REQUEST_REFC_MEMBER_SEARCH:
                if (data != null) {
                    Map<String, String> person = new HashMap<String, String>();
                    person.put("usr_id", data.getExtras().getString("usr_id"));
                    person.put("usr_nm", data.getExtras().getString("usr_nm"));
                    person.put("photo_img_min", data.getExtras().getString("usr_img"));
                    person.put("fnl_appr_yn", "N"); //신규 참가자

                    //중복체크
                    for (int i = 0; i < mRefcMemberList.size(); i++) {
                        if (person.get("usr_id").equals(mRefcMemberList.get(i).get("usr_id"))) {
                            Toast.makeText(MemoWriteActivity.this, "중복된 참가자입니다", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    //set image
                    if (mRefcMemberList == null) mRefcMemberList = new ArrayList<>();
                    mRefcMemberList.add(person);
                    if (mRefcMemberList == null || mRefcMemberList.size() < 1) {
                        mRvRefcMember.setVisibility(View.GONE);
                        mTvEmptyRefcMember.setVisibility(View.VISIBLE);
                    } else {
                        mRvRefcMember.setVisibility(View.VISIBLE);
                        mTvEmptyRefcMember.setVisibility(View.GONE);
                    }
                    mRefcAdapter.notifyDataSetChanged();
                }
                break;

            case REQUEST_FILE: //file 첨부
            case REQUEST_FILE_KITKAT_INTENT_CALLED:
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
                    llFileList = mLlMemoFile;
                    tvEmpty = mTvEmptyFile;
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
                    llFileList = mLlMemoFile;
                    tvEmpty = mTvEmptyFile;
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
        file.put("physcl_file_path", "");

        int pos = -1;
        switch (mode) {
            case REQUEST_FILE:
            case REQUEST_FILE_KITKAT_INTENT_CALLED:
                if (mFileList == null) mFileList = new ArrayList<Map<String, String>>();
                mFileList.add(file);
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
                        ((ViewGroup) fileView.getParent()).invalidate();
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
                    mLlMemoFile.setVisibility(View.GONE);
                    mTvEmptyFile.setVisibility(View.VISIBLE);
                } else {
                    mLlMemoFile.setVisibility(View.VISIBLE);
                    mTvEmptyFile.setVisibility(View.GONE);
                }
                break;
        }
    }

    public class MemoMemberRecyclerViewAdapter
            extends RecyclerView.Adapter<MemoMemberRecyclerViewAdapter.ViewHolder> {

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

        public MemoMemberRecyclerViewAdapter(Context context, List<Map<String, String>> items) {
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
            final String usrCfrcUserFlag = person.get("cfrc_usr_flag");
            if ("CRTR".equals(usrCfrcUserFlag)) {
                // 주최자 //통합?
            } else if ("HOST".equals(usrCfrcUserFlag)) {
                // 사회자
            } else { //GNER
                // 참가자
            }
            holder.mUsrID = usrId;
            Log.e(TAG, "usr name = " + usrNm);
            holder.mTvUsrNm.setText(usrNm);
            Glide.with(holder.mCivCrtUsrPhoto.getContext())
                    .load(I2UrlHelper.File.getUsrImage(usrImg))
                    .error(R.drawable.ic_no_usr_photo)
                    .fitCenter()
                    .into(holder.mCivCrtUsrPhoto);

            holder.mIvDelmember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG, "remove size =" + pos);
                    //((MemoWriteActivity) mContext).mMemberList.remove(pos); //todo
                    mValues.remove(pos); //todo
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
