package com.i2max.i2smartwork.common.sns;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.Tools;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FileUtil;
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

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SNSWriteActivity extends BaseAppCompatActivity {
    static String TAG = SNSWriteActivity.class.getSimpleName();

    public static final String MODE = "mode";
    public static final String GROUP_ID = "group_id";
    public static final String TARGET_NM = "target_nm";

    public static final int MODE_TARGET_USER = 11;
    public static final int MODE_TARGET_GROUP = 12;
    public static final int MODE_TARGET_OBJECT = 13;

    protected final int REQUEST_GALLERY_KITKAT_INTENT_CALLED = 2003; //키캣 이상 파일 열기 처리
    protected final int REQUEST_GALLERY = 2004; //키캣 이전버젼 파일 열기처리
    protected final int REQUEST_TAKE_PHOTO = 2005;
    protected final int REQUEST_M_CAMERA_PERMIT = 91;
    protected final int REQUEST_M_STORAGE_PERMIT = 92;

    protected final int MODE_NO_ADD = 0;
    protected final int MODE_FILE_ADD = 1;
    protected final int MODE_SURVEY_ADD = 2;
    protected final int MODE_LINK_ADD = 3;
    protected final int MODE_TAKE_PHOTO = 4;

    protected String mTargetNM, mObjectTp, mObjectId, mTarObjTtl;
    protected int mModeTarget, mTotalGrpCnt;
    protected EditText etBody;
    protected Spinner spGroup;
    protected List<JSONObject> mGroupArray = new ArrayList<>();
    protected int modeWrite, groupPage, selectedGroupPos, surveyAddCnt;
    protected LinearLayout llAddBtns, llFileFrame, llSurveyFrame, llSurveys;
    protected RelativeLayout rlLinkFrame;
    protected CheckBox etSurveyUsrOpen;

    protected int uploadedCnt;
    protected Map<String, Object> uploadFileInfo = new HashMap<String, Object>();

    protected List<LinearLayout> llSurveyLayoutList = new ArrayList<>();
    protected List<EditText> etSurveyList = new ArrayList<>();
    protected List<Button> btnSurveyPlusList = new ArrayList<>();
    protected List<Button> btnSurveyMinusList = new ArrayList<>();

    // 링크추가 Views
    protected EditText etLink;
    protected TextView tvLink, tvTarNm;
    protected Button btnLinkPlus, btnLinkMinus;

    protected List<String> linkUserIdList = new ArrayList<>();
    protected List<String> linkUserNmList = new ArrayList<>();
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_write);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("글쓰기");

        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            mModeTarget = extra.getInt(MODE);
            mTargetNM = extra.getString(TARGET_NM, "");
            mObjectTp = extra.getString(CodeConstant.CUR_OBJ_TP, "");
            mObjectId = extra.getString(CodeConstant.CUR_OBJ_ID, "");
            mTarObjTtl = extra.getString(CodeConstant.TAR_OBJ_TTL, "");
        }

        etBody = (EditText) findViewById(R.id.et_body);
        spGroup = (Spinner) findViewById(R.id.sp_group);
        tvTarNm = (TextView) findViewById(R.id.tv_tar_nm);
        llAddBtns = (LinearLayout) findViewById(R.id.ll_add_btns);
        llFileFrame = (LinearLayout) findViewById(R.id.ll_file_frame);
        llSurveyFrame = (LinearLayout) findViewById(R.id.ll_survey_frame);
        llSurveys = (LinearLayout) findViewById(R.id.ll_surveys);
        etSurveyUsrOpen = (CheckBox) findViewById(R.id.cb_usr_open);
        rlLinkFrame = (RelativeLayout) findViewById(R.id.rl_link_frame);
        etLink = (EditText) findViewById(R.id.et_link);
        tvLink = (TextView) findViewById(R.id.tv_link);
        btnLinkPlus = (Button) findViewById(R.id.btn_link_plus);
        btnLinkMinus = (Button) findViewById(R.id.btn_link_minus);

        List<ImageView> ivFileAddList = new ArrayList<ImageView>();
        ivFileAddList.add((ImageView) findViewById(R.id.iv_file_add1));
        ivFileAddList.add((ImageView) findViewById(R.id.iv_file_add2));
        ivFileAddList.add((ImageView) findViewById(R.id.iv_file_add3));
        ivFileAddList.add((ImageView) findViewById(R.id.iv_file_add4));
        ivFileAddList.add((ImageView) findViewById(R.id.iv_file_add5));
        List<TextView> tvFileAddList = new ArrayList<TextView>();
        tvFileAddList.add((TextView) findViewById(R.id.tv_file_add1));
        tvFileAddList.add((TextView) findViewById(R.id.tv_file_add2));
        tvFileAddList.add((TextView) findViewById(R.id.tv_file_add3));
        tvFileAddList.add((TextView) findViewById(R.id.tv_file_add4));
        tvFileAddList.add((TextView) findViewById(R.id.tv_file_add5));

        uploadFileInfo.put("uri_list", new ArrayList<Uri>());
        uploadFileInfo.put("nm_list", new ArrayList<String>());
        uploadFileInfo.put("file_ext_list", new ArrayList<String>());
        uploadFileInfo.put("attach_tp_cd_list", new ArrayList<String>());
        uploadFileInfo.put("attach_nm_list", new ArrayList<String>());
        uploadFileInfo.put("phscl_file_nm_list", new ArrayList<String>());
        uploadFileInfo.put("file_id_list", new ArrayList<String>());
        uploadFileInfo.put("file_size_list", new ArrayList<String>());
        uploadFileInfo.put("file_path_list", new ArrayList<String>());
        uploadFileInfo.put("iv_file_add_list", ivFileAddList);
        uploadFileInfo.put("tv_file_add_list", tvFileAddList);

        initButtons();

        // 2개의 설문항목 추가
        addSurveyLayout();
        addSurveyLayout();

        selectedGroupPos = 0;
        groupPage = 1;
        mTotalGrpCnt = 0;
        loadGroups(groupPage);

        if (mModeTarget == MODE_TARGET_USER || mModeTarget == MODE_TARGET_GROUP || mModeTarget == MODE_TARGET_OBJECT) {
            spGroup.setVisibility(View.GONE);
            tvTarNm.setVisibility(View.VISIBLE);
            String tarNm = "대상 : " + mTargetNM;
            if (mModeTarget == MODE_TARGET_OBJECT) {
                if (CodeConstant.TYPE_CFRC.equals(mObjectTp)) tarNm = "회의 : " + mTargetNM;
                else if (CodeConstant.TYPE_TASK.equals(mObjectTp)) tarNm = "작업 : " + mTargetNM;
                else if (CodeConstant.TYPE_MEMO.equals(mObjectTp)) tarNm = "메모보고 : " + mTargetNM;
                else if (CodeConstant.TYPE_WORK.equals(mObjectTp)) tarNm = "과제관리 : " + mTargetNM;
            }
            tvTarNm.setText(tarNm);
        } else {
            spGroup.setVisibility(View.VISIBLE);
            tvTarNm.setVisibility(View.GONE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_M_CAMERA_PERMIT);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_M_STORAGE_PERMIT);
        }

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void initButtons() {
        Button btnUserSearch = (Button) findViewById(R.id.btn_user_search);
        btnUserSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SNSWriteActivity.this, SNSPersonSearchActivity.class);
                intent.putExtra(SNSPersonSearchActivity.USR_ID, PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
                intent.putExtra(SNSPersonSearchActivity.MODE, CodeConstant.MODE_LINK_ADD);
                startActivityForResult(intent, SNSPersonSearchActivity.REQUEST_FRIEND_SEARCH);
            }
        });

        final Button btnFilePlus = (Button) findViewById(R.id.btn_file_plus);
        btnFilePlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < 19) {
                    Intent intent = new Intent();
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "파일 선택"), REQUEST_GALLERY);
                } else {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    startActivityForResult(intent, REQUEST_GALLERY_KITKAT_INTENT_CALLED);
                }
            }
        });

        Button btnCameraAdd = (Button) findViewById(R.id.btn_camera_add);
        btnCameraAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modeWrite != MODE_FILE_ADD) {
                    takePhotoIntent();
                    llFileFrame.setVisibility(View.VISIBLE);
                    llSurveyFrame.setVisibility(View.GONE);
                    rlLinkFrame.setVisibility(View.GONE);
                    llAddBtns.setVisibility(View.GONE);
//                    btnFilePlus.setVisibility(View.GONE);
                    modeWrite = MODE_FILE_ADD;
                } else {
                    llFileFrame.setVisibility(View.GONE);
                    modeWrite = MODE_NO_ADD;
                }
            }
        });

        Button btnFileAdd = (Button) findViewById(R.id.btn_file_add);
        btnFileAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modeWrite != MODE_FILE_ADD) {
                    llFileFrame.setVisibility(View.VISIBLE);
                    llSurveyFrame.setVisibility(View.GONE);
                    rlLinkFrame.setVisibility(View.GONE);
                    llAddBtns.setVisibility(View.GONE);
                    modeWrite = MODE_FILE_ADD;
                } else {
                    llFileFrame.setVisibility(View.GONE);
                    modeWrite = MODE_NO_ADD;
                }
            }
        });

        Button btnSurveyAdd = (Button) findViewById(R.id.btn_survey_add);
        btnSurveyAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modeWrite != MODE_SURVEY_ADD) {
                    llSurveyFrame.setVisibility(View.VISIBLE);
                    rlLinkFrame.setVisibility(View.GONE);
                    llAddBtns.setVisibility(View.GONE);
                    modeWrite = MODE_SURVEY_ADD;
                } else {
                    llSurveyFrame.setVisibility(View.GONE);
                    modeWrite = MODE_NO_ADD;
                }
            }
        });

        Button btnLinkAdd = (Button) findViewById(R.id.btn_link_add);
        btnLinkAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (modeWrite != MODE_LINK_ADD) {
                    rlLinkFrame.setVisibility(View.VISIBLE);
                    llSurveyFrame.setVisibility(View.GONE);
                    llAddBtns.setVisibility(View.GONE);
                    modeWrite = MODE_LINK_ADD;
                } else {
                    rlLinkFrame.setVisibility(View.GONE);
                    modeWrite = MODE_NO_ADD;
                }
            }
        });


        Button btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (llAddBtns.getVisibility() == View.GONE) {
                    llAddBtns.setVisibility(View.VISIBLE);
                    llFileFrame.setVisibility(View.GONE);
                    llSurveyFrame.setVisibility(View.GONE);
                    rlLinkFrame.setVisibility(View.GONE);
                    modeWrite = MODE_NO_ADD;
                    clearAttachs();
                } else {
                    finish();
                }
            }
        });

        btnLinkPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getViewWebsite(etLink.getText().toString());
            }
        });

        btnLinkMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etLink.setText("");
                etLink.setVisibility(View.VISIBLE);
                tvLink.setText("");
                tvLink.setVisibility(View.GONE);
                btnLinkPlus.setVisibility(View.VISIBLE);
                btnLinkMinus.setVisibility(View.GONE);
            }
        });

        Button btnConfirm = (Button) findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePost();
            }
        });


        List<ImageView> ivFileAddList = ((List<ImageView>) uploadFileInfo.get("iv_file_add_list"));
        View.OnClickListener fileRemoveListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = (int) v.getTag();
                ImageView iv = (ImageView) v;

                int addCnt = ((List<Uri>) uploadFileInfo.get("uri_list")).size();

                if (addCnt > pos && iv.getDrawable() != null) {
                    DialogUtil.showConfirmDialog(SNSWriteActivity.this, "제거", "추가된 파일을 제거하시겠습니까?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // 삭제위치만 제거 후 Copy 완전 클리어후 순서대로 다시 추가
                            ((List<Uri>) uploadFileInfo.get("uri_list")).remove(pos);

                            List<Uri> uriList = new ArrayList<Uri>((List<Uri>) uploadFileInfo.get("uri_list"));

                            ((List<Uri>) uploadFileInfo.get("uri_list")).clear();
                            ((List<String>) uploadFileInfo.get("nm_list")).clear();

                            List<ImageView> ivList = ((List<ImageView>) uploadFileInfo.get("iv_file_add_list"));
                            List<TextView> tvList = ((List<TextView>) uploadFileInfo.get("tv_file_add_list"));
                            for (int i = 0; i < ivList.size(); i++) {
                                ivList.get(i).setImageBitmap(null);
                                ivList.get(i).destroyDrawingCache();
                                tvList.get(i).setText("");
                                ((RelativeLayout) tvList.get(i).getParent()).setVisibility(View.GONE);
                            }

                            for (int i = 0; i < uriList.size(); i++) {
                                addFilesFromPicture(uriList.get(i));
                            }
                        }
                    });
                }

            }
        };

        for (int i = 0; i < ivFileAddList.size(); i++) {
            ImageView iv = ivFileAddList.get(i);
            iv.setTag(i);
            iv.setOnClickListener(fileRemoveListener);
        }
    }

    public void clearAttachs() {
        // 파일첨부 클리어
        uploadedCnt = 0;
        ((ArrayList<Uri>) uploadFileInfo.get("uri_list")).clear();
        ((ArrayList<String>) uploadFileInfo.get("nm_list")).clear();
        ((ArrayList<String>) uploadFileInfo.get("attach_tp_cd_list")).clear();
        ((ArrayList<String>) uploadFileInfo.get("attach_nm_list")).clear();
        ((ArrayList<String>) uploadFileInfo.get("phscl_file_nm_list")).clear();
        ((ArrayList<String>) uploadFileInfo.get("file_ext_list")).clear();
        ((ArrayList<String>) uploadFileInfo.get("file_id_list")).clear();
        ((ArrayList<String>) uploadFileInfo.get("file_size_list")).clear();
        ((ArrayList<String>) uploadFileInfo.get("file_path_list")).clear();

        List<ImageView> ivFileAddList = (List<ImageView>) uploadFileInfo.get("iv_file_add_list");
        List<TextView> tvFileAddList = (List<TextView>) uploadFileInfo.get("tv_file_add_list");
        for (int i = 0; i < ivFileAddList.size(); i++) {
            ivFileAddList.get(i).setImageBitmap(null);
            tvFileAddList.get(i).setText("");
            ((RelativeLayout) tvFileAddList.get(i).getParent()).setVisibility(View.GONE);
        }

        // 설문조사 초기화
        surveyAddCnt = 0;
        llSurveyLayoutList.clear();
        etSurveyList.clear();
        btnSurveyPlusList.clear();
        btnSurveyMinusList.clear();
        llSurveys.removeAllViews();

        // 두개는 기본 생성
        addSurveyLayout();
        addSurveyLayout();

        // 링크 초기화
        btnLinkMinus.callOnClick();
    }

    public void loadGroups(int page) {
        I2ConnectApi.requestJSON(SNSWriteActivity.this,
                I2UrlHelper.SNS.getListUserGroup(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID), String.format("%d", page), ""))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getListUserGroup onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListUserGroup onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSWriteActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getListUserGroup onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            List<JSONObject> statusInfoArrayAsList = I2ResponseParser.getJsonArrayAsList(statusInfo, "list_data");

                            if (statusInfoArrayAsList != null && statusInfoArrayAsList.size() > 0) {
                                try {
                                    mTotalGrpCnt = statusInfo.getInt("list_count");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                mGroupArray.addAll(statusInfoArrayAsList);

                                if (mTotalGrpCnt > mGroupArray.size()) {
                                    groupPage++;
                                    loadGroups(groupPage);
                                } else {
                                    setDataSpinner();
                                }
                            } else {
                                groupPage = 1;
                                setDataSpinner();
                            }

                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void setDataSpinner() {
        List<String> list = new ArrayList<>();
        list.add("전체공개");
        for (int i = 0; i < mGroupArray.size(); i++) {
            try {
                list.add(mGroupArray.get(i).getString("grp_nm"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(SNSWriteActivity.this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGroup.setAdapter(dataAdapter);
        spGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroupPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void addSurveyLayout() {

        if (surveyAddCnt > 0) {
            int pos = surveyAddCnt - 1;
            btnSurveyPlusList.get(pos).setVisibility(View.GONE);
            btnSurveyMinusList.get(pos).setVisibility(View.VISIBLE);
        }

        LinearLayout llHorizon = new LinearLayout(this);
        llHorizon.setOrientation(LinearLayout.HORIZONTAL);
        llHorizon.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams pllHor = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        pllHor.setMargins(0, (int) Tools.fromDpToPx(7), 0, 0);

        ContextThemeWrapper newContext = new ContextThemeWrapper(this, R.style.TextSNSWriteLinkSmall);

        EditText etSurvey = new EditText(newContext);
        etSurvey.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_text_add));
        etSurvey.setPadding((int) Tools.fromDpToPx(24), 0, 0, 0);
        etSurvey.setSingleLine(true);
        etSurvey.setHintTextColor(getResources().getColor(R.color.text_color_light));
        etSurvey.setHint("설문항목을 입력해주세요.");
        LinearLayout.LayoutParams pEtSurvey = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        pEtSurvey.weight = 1.0f;

        llHorizon.addView(etSurvey, pEtSurvey);

        Button btnSurveyPlus = new Button(this);
        btnSurveyPlus.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_plus));
        LinearLayout.LayoutParams pBtnSurveyPlus = new LinearLayout.LayoutParams((int) Tools.fromDpToPx(31), (int) Tools.fromDpToPx(31));
        pBtnSurveyPlus.setMargins((int) Tools.fromDpToPx(12), 0, 0, 0);

        llHorizon.addView(btnSurveyPlus, pBtnSurveyPlus);

        Button btnSurveyMinus = new Button(this);
        btnSurveyMinus.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_minus));
        LinearLayout.LayoutParams pBtnSurveyMinus = new LinearLayout.LayoutParams((int) Tools.fromDpToPx(31), (int) Tools.fromDpToPx(31));
        pBtnSurveyMinus.setMargins((int) Tools.fromDpToPx(12), 0, 0, 0);
        btnSurveyMinus.setVisibility(View.GONE);

        llHorizon.addView(btnSurveyMinus, pBtnSurveyMinus);

        llSurveys.addView(llHorizon, pllHor);

        llSurveyLayoutList.add(llHorizon);
        etSurveyList.add(etSurvey);
        btnSurveyPlusList.add(btnSurveyPlus);
        btnSurveyMinusList.add(btnSurveyMinus);

        surveyAddCnt++;

        btnSurveyPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSurveyLayout();
            }
        });

        btnSurveyMinus.setTag(surveyAddCnt);
        btnSurveyMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeSurveyLayout((int) v.getTag() - 1);
            }
        });
    }

    public void removeSurveyLayout(int pos) {
        etSurveyList.remove(pos);
        btnSurveyPlusList.remove(pos);
        btnSurveyMinusList.remove(pos);
        LinearLayout ll = llSurveyLayoutList.get(pos);
        ll.removeAllViews();
        ViewGroup viewParent = (ViewGroup) ll.getParent();
        viewParent.removeView(ll);
        llSurveyLayoutList.remove(pos);
        for (int i = 0; i < btnSurveyMinusList.size(); i++) {
            btnSurveyMinusList.get(i).setTag(i + 1);
        }
        surveyAddCnt--;
    }

    public void getViewWebsite(String linkURL) {
        DialogUtil.showCircularProgressDialog(SNSWriteActivity.this);

        WebView wvLink = new WebView(SNSWriteActivity.this);
        wvLink.loadUrl(linkURL);

        wvLink.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                tvLink.setText(view.getTitle());
                tvLink.setVisibility(View.VISIBLE);
                btnLinkMinus.setVisibility(View.VISIBLE);
                etLink.setVisibility(View.GONE);
                btnLinkPlus.setVisibility(View.GONE);

                DialogUtil.removeCircularProgressDialog();
            }
        });
    }

    public FormEncodingBuilder getSnsPostFormBuilder(String postTpTd) {
        String objTp = "";
        String objID = "";
        String objNM = "";

        objID = mObjectId;
        objNM = mTargetNM;

        if (mModeTarget == MODE_TARGET_USER) {
            objTp = "USER";
        } else if (mModeTarget == MODE_TARGET_GROUP) {
            objTp = "GROUP";
        } else if (mModeTarget == MODE_TARGET_OBJECT) {
            objTp = mObjectTp;
        } else {
            objTp = getOjbTp();
            objID = mObjectId;
            objNM = mTargetNM;
        }

        return I2UrlHelper.SNS.getSnsPostFormBuilder(postTpTd, objTp, objID, objNM, etBody.getText().toString());
    }

    public void getUserLinkBuilder(FormEncodingBuilder formBuilder) {

        if (linkUserIdList.size() > 0) {
            for (int i = linkUserIdList.size() - 1; i >= 0; i--) {
                String body = etBody.getText().toString();
                if (!body.contains(linkUserNmList.get(i))) {
                    linkUserIdList.remove(i);
                    linkUserNmList.remove(i);
                }
            }

            I2UrlHelper.SNS.getLinkUserFormBuilder(formBuilder, linkUserIdList, linkUserNmList);
        }
    }

    public String getOjbTp() {
        String objTp = "GROUP";
        if (selectedGroupPos != 0) {
            try {
                mObjectId = mGroupArray.get(selectedGroupPos - 1).getString("grp_id");
                mTargetNM = mGroupArray.get(selectedGroupPos - 1).getString("grp_nm");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (selectedGroupPos == 0) {
            objTp = "USER";
            mObjectId = PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID);
            mTargetNM = PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_NM);
        }
        return objTp;
    }

    public void savePost() {
        if (etBody.getText().length() == 0) {
            DialogUtil.showInformationDialog(SNSWriteActivity.this, "내용을 입력해주십시오.");
            return;
        }

        FormEncodingBuilder snsPostFormBuilder;

        switch (modeWrite) {
            case MODE_NO_ADD:

                snsPostFormBuilder = getSnsPostFormBuilder("FEED");
                getUserLinkBuilder(snsPostFormBuilder);

                DialogUtil.showCircularProgressDialog(SNSWriteActivity.this);
                I2ConnectApi.requestJSON(SNSWriteActivity.this, I2UrlHelper.SNS.saveSnsPostNoAdd(snsPostFormBuilder))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<JSONObject>() {
                            @Override
                            public void onCompleted() {
                                Log.d(TAG, "I2UrlHelper.SNS.saveSnsPostNoAdd onCompleted");
                                completeWritePost();

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "I2UrlHelper.SNS.saveSnsPostNoAdd onError" + e.getMessage());
                                DialogUtil.removeCircularProgressDialog();
                                //Error dialog 표시
                                e.printStackTrace();
                                DialogUtil.showErrorDialogWithValidateSession(SNSWriteActivity.this, e);
                            }

                            @Override
                            public void onNext(JSONObject jsonObject) {
                                Log.d(TAG, "I2UrlHelper.SNS.saveSnsPostNoAdd onNext");
                                if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                                    //JSONArray statusInfoArray = I2ResponseParser.getStatusInfoArray(jsonObject);

                                } else {
                                    Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;

            case MODE_FILE_ADD:
                if (((List<Uri>) uploadFileInfo.get("uri_list")).size() == 0) {
                    DialogUtil.showInformationDialog(SNSWriteActivity.this, "첨부파일은 하나 이상 올려주세요.");
                } else {
                    uploadedCnt = 0;
                    uplaodImageFiles();
                }

                break;

            case MODE_SURVEY_ADD:

                if (surveyAddCnt < 2) {
                    DialogUtil.showInformationDialog(SNSWriteActivity.this, "설문항목을 두개 이상 입력해주십시오.");
                } else {
                    DialogUtil.showCircularProgressDialog(SNSWriteActivity.this);

                    List<String> surveyNmList = new ArrayList<>();

                    for (int i = 0; i < surveyAddCnt; i++) {
                        String surveyStr = etSurveyList.get(i).getText().toString();
                        if (!TextUtils.isEmpty(surveyStr))
                            surveyNmList.add(surveyStr);
                    }

                    if (surveyNmList.size() <= 1) {
                        DialogUtil.showInformationDialog(SNSWriteActivity.this, "설문항목을 두개 이상 입력이 되지않았습니다.");
                    } else {
                        String surveyOpenYN = "Y";
                        if (etSurveyUsrOpen.isChecked())
                            surveyOpenYN = "N";

                        snsPostFormBuilder = getSnsPostFormBuilder("POLL");
                        getUserLinkBuilder(snsPostFormBuilder);

                        I2ConnectApi.requestJSON(SNSWriteActivity.this, I2UrlHelper.SNS.saveSnsSurvey( snsPostFormBuilder, surveyOpenYN, surveyNmList) )
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<JSONObject>() {
                                    @Override
                                    public void onCompleted() {
                                        Log.d(TAG, "I2UrlHelper.SNS.saveSnsSurvey onCompleted");
                                        completeWritePost();

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.d(TAG, "I2UrlHelper.SNS.saveSnsSurvey onError");
                                        DialogUtil.removeCircularProgressDialog();
                                        //Error dialog 표시
                                        e.printStackTrace();
                                        DialogUtil.showErrorDialogWithValidateSession(SNSWriteActivity.this, e);
                                    }

                                    @Override
                                    public void onNext(JSONObject jsonObject) {
                                        Log.d(TAG, "I2UrlHelper.SNS.saveSnsSurvey onNext");
                                        if (!I2ResponseParser.checkReponseStatus(jsonObject)) {
                                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }

                }

                break;

            case MODE_LINK_ADD:

                if (tvLink.getText().toString().length() == 0) {
                    DialogUtil.showInformationDialog(SNSWriteActivity.this, "링크주소를 입력해주십시오.");
                } else {
                    DialogUtil.showCircularProgressDialog(SNSWriteActivity.this);

                    snsPostFormBuilder = getSnsPostFormBuilder("LINK");
                    getUserLinkBuilder(snsPostFormBuilder);

                    I2ConnectApi.requestJSON(SNSWriteActivity.this, I2UrlHelper.SNS.saveSnsPostLinks(
                            snsPostFormBuilder, tvLink.getText().toString(), etLink.getText().toString()))
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<JSONObject>() {
                                @Override
                                public void onCompleted() {
                                    Log.d(TAG, "I2UrlHelper.SNS.saveSnsPostLinks onCompleted");
                                    completeWritePost();

                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.d(TAG, "I2UrlHelper.SNS.saveSnsPostLinks onError");
                                    DialogUtil.removeCircularProgressDialog();
                                    //Error dialog 표시
                                    e.printStackTrace();
                                    DialogUtil.showErrorDialogWithValidateSession(SNSWriteActivity.this, e);
                                }

                                @Override
                                public void onNext(JSONObject jsonObject) {
                                    Log.d(TAG, "I2UrlHelper.SNS.saveSnsPostLinks onNext");
                                    if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                                        //JSONArray statusInfoArray = I2ResponseParser.getStatusInfoArray(jsonObject);

                                    } else {
                                        Toast.makeText(SNSWriteActivity.this, I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

                break;
        }
    }

    private String mPhotoPath = "i2_take_picture.jpg";

    private void takePhotoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File file = new File(path, mPhotoPath);
            if (!path.exists()) path.mkdirs();

            Uri uri = Uri.fromFile(file);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    public void saveFilesPost() {

        DialogUtil.showCircularProgressDialog(SNSWriteActivity.this);

        FormEncodingBuilder snsPostFormBuilder = getSnsPostFormBuilder("FILE");
        getUserLinkBuilder(snsPostFormBuilder);

        Log.d(TAG, "uploadFileInfo = " + uploadFileInfo.toString());

        I2ConnectApi.requestJSON(SNSWriteActivity.this, I2UrlHelper.SNS.saveSnsFilesPost(snsPostFormBuilder, uploadFileInfo))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.saveSnsFilesPost onCompleted");
                        completeWritePost();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.saveSnsFilesPost onError");
                        DialogUtil.removeCircularProgressDialog();
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSWriteActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.saveSnsFilesPost onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {

                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void uplaodImageFiles() {

        if (uploadedCnt < ((List<Uri>) uploadFileInfo.get("uri_list")).size()) {
            DialogUtil.showCircularProgressDialog(SNSWriteActivity.this);

            Uri uri = Uri.parse(((List<Uri>) uploadFileInfo.get("uri_list")).get(uploadedCnt).toString());
            File file = new File(FileUtil.getPath(this, uri));

            String fileNm = ((List<String>) uploadFileInfo.get("nm_list")).get(uploadedCnt);
            String mimeType = FileUtil.getMimeType(this, uri);

//            Log.d(TAG, "uplaodImageFiles file = " + file.exists());
//            Log.d(TAG, "uplaodImageFiles filePath = " + FileUtil.getPath(this, uri));
//            Log.d(TAG, "uplaodImageFiles fileName = " + fileNm + ", mimeType =" + mimeType +  ", fileSize(kb) =" + file.length()/1024);

            I2ConnectApi.uploadFile(SNSWriteActivity.this, fileNm, mimeType, file)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<JSONObject>() {
                        @Override
                        public void onCompleted() {
                            Log.d(TAG, "uploadFile onCompleted");
                            DialogUtil.removeCircularProgressDialog();
                            uploadedCnt++;
                            if (uploadedCnt >= ((List<Uri>) uploadFileInfo.get("uri_list")).size()) {
                                saveFilesPost();
                            } else {
                                uplaodImageFiles();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "uploadFile onError");
                            DialogUtil.removeCircularProgressDialog();
                            //Error dialog 표시
                            e.printStackTrace();
                            DialogUtil.showErrorDialogWithValidateSession(SNSWriteActivity.this, e);
                        }

                        @Override
                        public void onNext(JSONObject jsonObject) {
                            Log.d(TAG, "uploadFile onNext");
                            if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                                JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                                JSONArray statusInfoArray = I2ResponseParser.getJsonArray(statusInfo, "file_list");

                                try {
                                    if (statusInfoArray.length() > 0) {
                                        ((List<String>) uploadFileInfo.get("attach_nm_list")).add(statusInfoArray.getJSONObject(0).getString("file_nm"));
                                        ((List<String>) uploadFileInfo.get("phscl_file_nm_list")).add(statusInfoArray.getJSONObject(0).getString("phscl_file_nm"));
                                        ((List<String>) uploadFileInfo.get("attach_tp_cd_list")).add(statusInfoArray.getJSONObject(0).getString("file_tp_cd"));
                                        ((List<String>) uploadFileInfo.get("file_ext_list")).add(statusInfoArray.getJSONObject(0).getString("file_ext"));
                                        ((List<String>) uploadFileInfo.get("file_id_list")).add(statusInfoArray.getJSONObject(0).getString("file_id"));
                                        ((List<String>) uploadFileInfo.get("file_size_list")).add(statusInfoArray.getJSONObject(0).getString("file_size"));
                                        ((List<String>) uploadFileInfo.get("file_path_list")).add(statusInfoArray.getJSONObject(0).getString("file_path"));

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

    public void completeWritePost() {
        DialogUtil.removeCircularProgressDialog();

        DialogUtil.showDialog(SNSWriteActivity.this, "안내", "글쓰기가 완료되었습니다.",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (CodeConstant.TYPE_CFRC.equals(mObjectTp)) {
//                            ConferenceDetailActivity.isChangedList = true;
                        } else if (CodeConstant.TYPE_TASK.equals(mObjectTp)) {
//                            TaskDetailActivity.isChangedList = true;
                        } else if (CodeConstant.TYPE_MEMO.equals(mObjectTp)) {
//                            MemoDetailActivity.isChangedList = true;
                        } else {
                            SNSMainFragment.isChangedList = true;
                        }

                        Intent returnIntent = new Intent();
                        setResult(RESULT_OK, returnIntent);
                        finish();

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

    public void addFilesFromPicture(Uri uri) {
        try {
            int pos = ((List<Uri>) uploadFileInfo.get("uri_list")).size();
            if (pos >= ((List<ImageView>) uploadFileInfo.get("iv_file_add_list")).size()) {
                DialogUtil.showInformationDialog(SNSWriteActivity.this, String.format("모바일에서는 %d개 이상 파일을 추가할 수 없습니다.", pos));
                return;
            }
            //Log.d(TAG, "imgNm = " + FileUtil.getFileName(this, uri));

            Bitmap bitmap;
            if (FileUtil.isImageFile(this, uri)) {
                //대용량일 경우 리사이징 표시 처리
                bitmap = FileUtil.getBitmap(this, uri);
                ((List<ImageView>) uploadFileInfo.get("iv_file_add_list")).get(pos).setImageBitmap(
                        FileUtil.scaleCenterCrop(bitmap, (int) Tools.fromDpToPx(100), (int) Tools.fromDpToPx(100)));
                //((List<String>) uploadFileInfo.get("attach_tp_cd_list")).add("PHOT");
            } else { //이미지가 아닌 파일일 경우
                FileUtil.setFileExtIcon(((List<ImageView>) uploadFileInfo.get("iv_file_add_list")).get(pos), FileUtil.getFileName(this, uri));

                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_file_doc);
                //((List<String>) uploadFileInfo.get("attach_tp_cd_list")).add("FILE");
            }

            if (bitmap != null) {
                ((List<String>) uploadFileInfo.get("nm_list")).add(FileUtil.getFileName(this, uri));
                ((List<Uri>) uploadFileInfo.get("uri_list")).add(uri);
                TextView tvFileNm = ((List<TextView>) uploadFileInfo.get("tv_file_add_list")).get(pos);
                tvFileNm.setText(FileUtil.getFileName(this, uri));
                ((RelativeLayout) tvFileNm.getParent()).setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Toast.makeText(SNSWriteActivity.this, "인식되지 않는 파일입니다.\n파일을 확인하시고 다시 선택하시기 바랍니다 ", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }


    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        ClipData clipdata;
        switch (requestCode) {
            case REQUEST_GALLERY:
                clipdata = data.getClipData();
                if (clipdata == null) {
                    Uri uri = data.getData();
                    addFilesFromPicture(uri);

                } else {
                    for (int i = 0; i < clipdata.getItemCount(); i++) {
                        Uri uri = clipdata.getItemAt(i).getUri();
                        addFilesFromPicture(uri);
                    }
                }
                break;
            case REQUEST_GALLERY_KITKAT_INTENT_CALLED:
                clipdata = data.getClipData();

                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                if (clipdata == null) {
                    Uri uri = data.getData();
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);
                    addFilesFromPicture(uri);
                } else {
                    for (int i = 0; i < clipdata.getItemCount(); i++) {
                        Uri uri = clipdata.getItemAt(i).getUri();
                        getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        addFilesFromPicture(uri);
                    }
                }
                break;

            case REQUEST_TAKE_PHOTO:
                try {
                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    File file = new File(path, mPhotoPath);

                    Uri uri = Uri.fromFile(file);
                    addFilesFromPicture(uri);
                } catch (Exception ie) {
                    ie.printStackTrace();
                }

                break;

            case SNSPersonSearchActivity.REQUEST_FRIEND_SEARCH:
                if (data != null) {
                    String temp = etBody.getText().toString();
                    linkUserIdList.add(data.getExtras().getString("usr_id"));
                    linkUserNmList.add(data.getExtras().getString("usr_nm"));
                    temp = temp + "@[" + data.getExtras().getString("usr_nm") + "]";

                    etBody.setText(temp);

                    etBody.setSelection(etBody.getText().length());
                }
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_M_CAMERA_PERMIT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this, "11ㅇㅋㄷㅋ", Toast.LENGTH_SHORT).show();
                } else {
                    Button btnCameraAdd = (Button) findViewById(R.id.btn_camera_add);
                    btnCameraAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DialogUtil.showInformationDialog(SNSWriteActivity.this, "설정>I2Connect>앱권한>카메라 권한을 허용하여 주십시오.");
                        }
                    });

                }
                break;

            case REQUEST_M_STORAGE_PERMIT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "22ㅇㅋㄷㅋ", Toast.LENGTH_SHORT).show();
                } else {
                    Button btnFileAdd = (Button) findViewById(R.id.btn_file_add);
                    btnFileAdd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DialogUtil.showInformationDialog(SNSWriteActivity.this, "설정>I2Connect>앱권한>저장 권한을 허용하여 주십시오.");
                        }
                    });
                }
                break;
        }
    }


}
