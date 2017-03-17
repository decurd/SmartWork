package com.i2max.i2smartwork.common.sns;

import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Browser;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.db.chart.Tools;
import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.view.HorizontalBarChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.XController;
import com.db.chart.view.YController;
import com.db.chart.view.animation.Animation;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.common.conference.ConferenceDetailActivity;
import com.i2max.i2smartwork.common.memo.MemoDetailActivity;
import com.i2max.i2smartwork.common.task.TaskDetailActivity;
import com.i2max.i2smartwork.common.web.WebviewActivity;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.constant.CodeConstant;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DateCalendarUtil;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FileUtil;
import com.i2max.i2smartwork.utils.FormatUtil;
import com.i2max.i2smartwork.utils.IntentUtil;
import com.i2max.i2smartwork.utils.PreferenceUtil;
import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SNSDetailPostActivity extends BaseAppCompatActivity {
    static String TAG = SNSDetailPostActivity.class.getSimpleName();

    public static final int REQUEST_REPLY_WRITE = 1001;

    protected RecyclerView mRV;
    protected JSONObject postJson;
    protected JSONArray replyArray;
    protected JSONArray likeUserArray;
    protected JSONArray surveyArray;
    protected List<Integer> voteCntArray = new ArrayList<>();
    protected HashMap<String,List<String>> mImageInfoMap = new HashMap<>();

    protected String mUsrID, mPostID, mCrtUsrID, mCrtUsrNM, mTarObjTp, mTarObjId;
    protected View mViewDummy, mViewCover;
    protected CircleImageView mCivCrtUsrPhoto;
    protected TextView mTvCrtTerm, mTvTarObjNm, mTvCrtUsrNm, mTvCrtDttm, mTvCntn, mTvReplyCnt, mTvLikeCnt, mTvVoteCnt, mTvSurveyVoteType, mTvSurveyResultType, mTvLinkTtl, mTvLinkUrl;
    protected ImageView mIvLike;
    protected SliderLayout mSlImages;
    protected LinearLayout mLlLike, mLlReply, mLlLink, mLlFile, mLlFileNms;
    protected Button mBtnDelete;
    protected LinearLayout mLlSurveyVote, mLlVoteResult;
    protected RadioGroup mRgSurvey;
    protected HorizontalBarChartView mChartVote;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_detail_post);

        Intent intent = getIntent();
        mPostID = intent.getStringExtra(CodeConstant.POST_ID);
        mTarObjTp = intent.getStringExtra(CodeConstant.TAR_OBJ_TP);
        mCrtUsrID = intent.getStringExtra(CodeConstant.CRT_USR_ID);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("소통커뮤니티 상세");
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        mUsrID = PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID);

        mCivCrtUsrPhoto = (CircleImageView) findViewById(R.id.civ_crt_usr_photo);
        mTvCrtTerm = (TextView) findViewById(R.id.tv_crt_term);
        mTvTarObjNm = (TextView) findViewById(R.id.tv_crt_usr_nm);
        mTvCrtUsrNm = (TextView) findViewById(R.id.tv_tar_grp_nm);
        mTvCrtDttm = (TextView) findViewById(R.id.tv_crt_dttm);
        mTvCntn = (TextView) findViewById(R.id.tv_cntn);
        mTvReplyCnt = (TextView) findViewById(R.id.tv_reply_cnt);
        mTvLikeCnt = (TextView) findViewById(R.id.tv_like_cnt);
        mIvLike = (ImageView) findViewById(R.id.iv_like);
        mSlImages = (SliderLayout) findViewById(R.id.slider);
        mLlLike = (LinearLayout) findViewById(R.id.ll_like);
        mLlReply = (LinearLayout) findViewById(R.id.ll_reply);
        mBtnDelete = (Button) findViewById(R.id.btn_del);
        mViewDummy = findViewById(R.id.view_dummy);
        mViewCover = findViewById(R.id.view_cover);
        mTvLinkTtl = (TextView) findViewById(R.id.tv_link_ttl);
        mTvLinkUrl = (TextView) findViewById(R.id.tv_link_Url);

        mLlSurveyVote = (LinearLayout) findViewById(R.id.ll_survey_vote);
        mRgSurvey = (RadioGroup) findViewById(R.id.rg_survey);
        Button btnVoteSave = (Button) findViewById(R.id.btn_vote_save);
        btnVoteSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSnsSurveyVote();
            }
        });
        Button btnVoteResult = (Button) findViewById(R.id.btn_vote_result);
        btnVoteResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLlVoteResult.setVisibility(View.VISIBLE);
                mLlSurveyVote.setVisibility(View.GONE);
                loadSnsSurveyVoteResult();
            }
        });

        mTvSurveyVoteType = (TextView) findViewById(R.id.tv_survey_vote_type);
        mTvSurveyResultType = (TextView) findViewById(R.id.tv_survey_result_type);

        mLlVoteResult = (LinearLayout) findViewById(R.id.ll_vote_result);
        mChartVote = (HorizontalBarChartView) findViewById(R.id.chart_vote);
        mTvVoteCnt = (TextView) findViewById(R.id.tv_vote_cnt);
        Button btnVoteRefresh = (Button) findViewById(R.id.btn_vote_refresh);
        btnVoteRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadSnsSurveyVoteResult();
            }
        });
        Button btnVoteAgain = (Button) findViewById(R.id.btn_vote_again);
        btnVoteAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLlSurveyVote.setVisibility(View.VISIBLE);
                mLlVoteResult.setVisibility(View.GONE);
            }
        });

        mLlLink = (LinearLayout) findViewById(R.id.ll_link);
        mLlFile = (LinearLayout) findViewById(R.id.ll_file);
        mLlFileNms = (LinearLayout) findViewById(R.id.ll_file_nms);

        Button btnLikeList = (Button) findViewById(R.id.btn_like_list);
        btnLikeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                I2ConnectApi.requestJSON(SNSDetailPostActivity.this, I2UrlHelper.SNS.getListSnsLikeByUsers( mPostID ))
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<JSONObject>() {
                            @Override
                            public void onCompleted() {
                                Log.d(TAG, "I2UrlHelper.SNS.getListSnsLikeByUsers onCompleted");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, "I2UrlHelper.SNS.getListSnsLikeByUsers onError");
                                //Error dialog 표시
                                e.printStackTrace();
                                DialogUtil.showErrorDialogWithValidateSession(SNSDetailPostActivity.this, e);
                            }

                            @Override
                            public void onNext(JSONObject jsonObject) {
                                Log.d(TAG, "I2UrlHelper.SNS.getListSnsLikeByUsers onNext");
                                if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                                    JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                                    likeUserArray = I2ResponseParser.getJsonArray(statusInfo, "list_users");

                                    try {
                                        CharSequence[] items = new CharSequence[likeUserArray.length()];
                                        for (int i = 0; i < likeUserArray.length(); i++) {
                                            JSONObject jo = likeUserArray.getJSONObject(i);
                                            items[i] = jo.getString("usr_nm");
                                        }

                                        AlertDialog.Builder builder = new AlertDialog.Builder(SNSDetailPostActivity.this);
                                        builder.setTitle("좋아요 목록");
                                        builder.setItems(items, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int item) {
                                                try {
                                                    JSONObject object = likeUserArray.getJSONObject(item);
                                                    Intent intent = new Intent(SNSDetailPostActivity.this, SNSDetailProfileActivity.class);
                                                    intent.putExtra(SNSDetailProfileActivity.USR_ID, object.getString("usr_id"));
                                                    intent.putExtra(SNSDetailProfileActivity.USR_NM, object.getString("usr_nm"));

                                                    startActivity(intent);

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }


                                                dialog.dismiss();
                                            }
                                        });
                                        AlertDialog alert = builder.create();
                                        alert.show();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        onError(e);
                                    }

                                } else {
                                    Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        mRV = (RecyclerView)findViewById(R.id.rv_sns_reply);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRV.setLayoutManager(layoutManager);

        FloatingActionButton fabReplyWrite = (FloatingActionButton) findViewById(R.id.fab_reply_write);
        fabReplyWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SNSDetailPostActivity.this, SNSReplyWriteActivity.class);
                intent.putExtra(CodeConstant.POST_ID, mPostID);
                intent.putExtra(CodeConstant.TAR_OBJ_TP, mTarObjTp);
                intent.putExtra(CodeConstant.TAR_OBJ_ID, mTarObjId);

                startActivityForResult(intent, REQUEST_REPLY_WRITE);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "SNSDetailPostActivity onResume called~!");

        loadDetailPost();
    }

    public void loadDetailPost() {
        I2ConnectApi.requestJSON(SNSDetailPostActivity.this, I2UrlHelper.SNS.getSelectSNSPost(mPostID))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getSNSPost onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getSNSPost onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSDetailPostActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getSNSPost onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);

                            try {
                                postJson = (JSONObject) I2ResponseParser.getJsonArray(statusInfo, "list_post").get(0);
                                setPostData();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void setPostData() {
        try {
            mCrtUsrID = postJson.getString("crt_usr_id");
            mCrtUsrNM = postJson.getString("crt_usr_nm");
            mPostID = postJson.getString("post_id");

            mTvCrtTerm.setText(FormatUtil.getStringValidate(DateCalendarUtil.getStringFromBetweenNow(postJson.getString("crt_dttm"))));

            mTvCrtDttm.setText(DateCalendarUtil.getStringFromYYYYMMDDHHMM(postJson.getString("crt_dttm")));
            mTvCntn.setText(FormatUtil.getStringValidate(postJson.getString("cntn")));
            mTvReplyCnt.setText(FormatUtil.getStringValidate(postJson.getString("reply_cnt")));
            mTvLikeCnt.setText(FormatUtil.getStringValidate(postJson.getString("like_cnt")));

            // tar_obj_tp별 이름셋팅과 링크연결셋팅
            setObjTpNmAndIntent(postJson);

            if ( !postJson.isNull("list_reply") ) {
                replyArray = postJson.getJSONArray("list_reply");
                if (replyArray != null && replyArray.length() > 0) {
                    mRV.setAdapter(new ReplyRecyclerViewAdapter(SNSDetailPostActivity.this, replyArray));
                }
            }

            if (postJson.getString("like_my").equals("Y")) {
                mIvLike.setImageDrawable(ContextCompat.getDrawable(SNSDetailPostActivity.this, R.drawable.ic_like));
            } else {
                mIvLike.setImageDrawable(ContextCompat.getDrawable(SNSDetailPostActivity.this, R.drawable.ic_unlike));
            }

            if ( PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(mCrtUsrID) ) {
                mBtnDelete.setVisibility(View.VISIBLE);
            } else {
                mBtnDelete.setVisibility(View.GONE);
            }

            mBtnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    DialogUtil.showConfirmDialog(SNSDetailPostActivity.this, "삭제안내", "내가 쓴 글을 삭제하시겠습니까?",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    I2ConnectApi.requestJSON(SNSDetailPostActivity.this, I2UrlHelper.SNS.deleteSnsPost(mPostID))
                                            .subscribeOn(Schedulers.newThread())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new Subscriber<JSONObject>() {
                                                @Override
                                                public void onCompleted() {
                                                    Log.d(TAG, "I2UrlHelper.SNS.deleteSnsPost onCompleted");
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    Log.d(TAG, "I2UrlHelper.SNS.deleteSnsPost onError");
                                                    //Error dialog 표시
                                                    e.printStackTrace();
                                                    DialogUtil.showErrorDialogWithValidateSession(SNSDetailPostActivity.this, e);
                                                }

                                                @SuppressLint("NewApi")
                                                @Override
                                                public void onNext(JSONObject jsonObject) {
                                                    Log.d(TAG, "I2UrlHelper.SNS.deleteSnsPost onNext");
                                                    if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                                                        // 해당글 삭제 후 갱신알림 화면종료
                                                        SNSMainFragment.isChangedList = true;

                                                        Intent returnIntent = new Intent();
                                                        setResult(RESULT_OK, returnIntent);
                                                        finish();

                                                    } else {
                                                        Toast.makeText(v.getContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            });
                }
            });

            mLlLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    I2ConnectApi.requestJSON(SNSDetailPostActivity.this, I2UrlHelper.SNS.getToggleSnsLike(mPostID))
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<JSONObject>() {
                                @Override
                                public void onCompleted() {
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.d(TAG, "onError = " + e.getMessage());
                                    //Error dialog 표시
                                    e.printStackTrace();
                                    DialogUtil.showErrorDialogWithValidateSession(SNSDetailPostActivity.this, e);
                                }

                                @Override
                                public void onNext(JSONObject jsonObject) {
                                    JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);

                                    try {
                                        int likeCnt = postJson.getInt("like_cnt");
                                        if("C".equals(statusInfo.getString("crud").toString())) {
                                            postJson.put("like_cnt", Integer.valueOf(++likeCnt));
                                            postJson.put("like_my", "Y");
                                            mIvLike.setImageDrawable(ContextCompat.getDrawable(SNSDetailPostActivity.this, R.drawable.ic_like));
                                        } else {
                                            postJson.put("like_cnt", Integer.valueOf(--likeCnt));
                                            postJson.put("like_my", "N");
                                            mIvLike.setImageDrawable(ContextCompat.getDrawable(SNSDetailPostActivity.this, R.drawable.ic_unlike));
                                        }
                                        mTvLikeCnt.setText(FormatUtil.getStringValidate(String.valueOf(postJson.getInt("like_cnt"))));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                }
            });

            Glide.with(mCivCrtUsrPhoto.getContext())
                    .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(postJson.getString("crt_usr_photo"))))
                    .error(R.drawable.ic_no_usr_photo)
                    .fitCenter()
                    .into(mCivCrtUsrPhoto);

            if (postJson.getString("post_tp_cd").equals("POLL")) {

                surveyArray = postJson.getJSONArray("list_survey");

                Log.d(TAG, "postJson list_survey = " + surveyArray.toString());

                setSurveyRadioButtons();

                if (postJson.getString("voted_yn").equals("Y")) {
                    mLlVoteResult.setVisibility(View.VISIBLE);
                    loadSnsSurveyVoteResult();
                } else {
                    mLlSurveyVote.setVisibility(View.VISIBLE);
                }

                if (postJson.getString("survey_usr_open_yn").equals("Y")) {
                    mTvSurveyVoteType.setText("설문내용 (기명)");
                    mTvSurveyResultType.setText("설문결과 (기명)");
                } else {
                    mTvSurveyVoteType.setText("설문내용 (무기명)");
                    mTvSurveyResultType.setText("설문결과 (무기명)");
                }
            }

            //수정
            if(postJson.isNull("list_file")) {
                mSlImages.setVisibility(View.GONE);
                mViewCover.setVisibility(View.GONE);
                mViewDummy.setVisibility(View.VISIBLE);
                mLlFile.setVisibility(View.GONE);
            } else {
                JSONArray array = postJson.getJSONArray("list_file");

                setFilesLayout(mLlFile, array);
                setPhotoLayout(array);
            }

            if(!postJson.isNull("link_url")) {
                mLlLink.setVisibility(View.VISIBLE);

                String linkTtl = postJson.getString("link_ttl");
                SpannableString spanString = new SpannableString(linkTtl);
                spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
                mTvLinkTtl.setText(spanString);

                final String linkURL = postJson.getString("link_url");
                mTvLinkUrl.setText(linkURL);

                mLlLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkURL));
                        startActivity(browserIntent);
                    }
                });
            } else {
                mLlLink.setVisibility(View.GONE);
            }

            if(!postJson.isNull("list_user")) {
                JSONArray array = postJson.getJSONArray("list_user");

                String cntn_temp = postJson.getString("cntn");

                List<Link> links = new ArrayList<>();

                for (int i=0; i<array.length(); i++) {

                    final String crtUserID = array.getJSONObject(i).getString("usr_id");
                    final String crtUserNM = array.getJSONObject(i).getString("usr_nm");

                    cntn_temp = cntn_temp.replace("@[" + crtUserNM + "]", crtUserNM);
                    Link personLink = new Link( crtUserNM );
                    personLink.setTypeface(Typeface.DEFAULT_BOLD)
                            .setOnClickListener(new Link.OnClickListener() {
                                @Override
                                public void onClick(String clickedText) {
                                    Intent intent = new Intent(SNSDetailPostActivity.this, SNSDetailProfileActivity.class);
                                    intent.putExtra(SNSDetailProfileActivity.USR_ID, crtUserID);
                                    intent.putExtra(SNSDetailProfileActivity.USR_NM, crtUserNM);

                                    startActivity(intent);
                                }
                            });

                    links.add(personLink);

                }

                mTvCntn.setText(cntn_temp);

                LinkBuilder.on(mTvCntn)
                        .addLinks(links)
                        .build();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void loadSnsSurveyVoteResult() {
        I2ConnectApi.requestJSON(SNSDetailPostActivity.this, I2UrlHelper.SNS.getSnsSurveyVoteResult(mPostID))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getSnsSurveyVoteResult onError");
                    }

                    @Override
                    public void onError(Throwable e) {
                        DialogUtil.showErrorDialog(SNSDetailPostActivity.this, e.getMessage());
                        Log.d(TAG, "onError = " + e.getMessage());
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSDetailPostActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        JSONObject statusInfo= I2ResponseParser.getStatusInfo(jsonObject);
                        JSONArray listSurvey = I2ResponseParser.getJsonArray(statusInfo, "list_survey");
                        showChartVote(listSurvey);
                        try {
                            postJson.put("list_survey", listSurvey);
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void saveSnsSurveyVote() {
        if (mRgSurvey.getCheckedRadioButtonId() == 0) {
            DialogUtil.showInformationDialog(SNSDetailPostActivity.this, "투표항목을 선택해주세요.");
            return;
        }

        int index = mRgSurvey.indexOfChild(findViewById(mRgSurvey.getCheckedRadioButtonId()));

        String selectedItemID = "";
        try {
            selectedItemID = surveyArray.getJSONObject(index).getString("survey_itm_id");
            for (int i=0; i<surveyArray.length(); i++) {
                if (i==index) {
                    surveyArray.getJSONObject(i).put("voted_yn", "Y");
                } else {
                    surveyArray.getJSONObject(i).put("voted_yn", "N");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        I2ConnectApi.requestJSON(SNSDetailPostActivity.this, I2UrlHelper.SNS.saveSnsSurveyVote(mPostID, selectedItemID))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.saveSnsSurveyVote onError");
                        mLlVoteResult.setVisibility(View.VISIBLE);
                        mLlSurveyVote.setVisibility(View.GONE);
                        loadSnsSurveyVoteResult();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError = " + e.getMessage());
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSDetailPostActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                    }
                });
    }

    public void setObjTpNmAndIntent(JSONObject jsonObject) throws JSONException {

        mTarObjTp = jsonObject.getString("tar_obj_tp_cd");
        mTarObjId = jsonObject.getString("tar_obj_id");

        View.OnClickListener createrOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUsrID.equals(mCrtUsrID)) {
                    Toast.makeText(v.getContext(), "보고 있는 멤버와 같은 멤버입니다", Toast.LENGTH_SHORT).show();
                } else {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, SNSDetailProfileActivity.class);
                    intent.putExtra(SNSDetailProfileActivity.USR_ID, mCrtUsrID);
                    intent.putExtra(SNSDetailProfileActivity.USR_NM, mCrtUsrNM);

                    context.startActivity(intent);
                }
            }
        };

        mCivCrtUsrPhoto.setOnClickListener(createrOnClickListener);
        mTvCrtUsrNm.setText(FormatUtil.getStringValidate(mCrtUsrNM));
        mTvCrtUsrNm.setOnClickListener(createrOnClickListener);
        if (mTarObjTp.equals(CodeConstant.TYPE_USER)) {
            if (jsonObject.has("tar_usr_id") && !mCrtUsrID.equals(jsonObject.getString("tar_usr_id"))
                    && !TextUtils.isEmpty(jsonObject.getString("tar_usr_id")) && !"null".equals(jsonObject.getString("tar_usr_id"))) {
                mTvTarObjNm.setText(FormatUtil.getStringValidate(jsonObject.getString("tar_usr_nm")));
                final String tarUsrId = jsonObject.getString("tar_usr_id");
                mTvTarObjNm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, SNSDetailProfileActivity.class);
                        intent.putExtra(SNSDetailProfileActivity.USR_ID, tarUsrId);
                        intent.putExtra(SNSDetailProfileActivity.USR_NM, mTvTarObjNm.getText().toString());
                        context.startActivity(intent);
                    }
                });
            }
        } else if (mTarObjTp.equals(CodeConstant.TYPE_GROUP)) {
            mTvTarObjNm.setText(FormatUtil.getStringValidate(jsonObject.getString("tar_obj_ttl")));
            final String tarGrpId = mTarObjId;
            mTvTarObjNm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, SNSDetailGroupActivity.class);
                    intent.putExtra(SNSDetailGroupActivity.GRP_ID, tarGrpId);
                    intent.putExtra(SNSDetailGroupActivity.GRP_NM, mTvTarObjNm.getText().toString());
                    context.startActivity(intent);
                }
            });

        } else {
            final String tarObjId = jsonObject.getString("tar_obj_id");
            final String crtUsrId = jsonObject.getString("crt_usr_id");
            final String tarSelectUrl = jsonObject.getString("tar_select_url");
            String tarObjTtl = "";
            if (!jsonObject.isNull("tar_obj_nm")) {
                tarObjTtl = jsonObject.getString("tar_obj_nm");
                mTvTarObjNm.setText(FormatUtil.getStringValidate(tarObjTtl));
            } else if (!jsonObject.isNull("tar_obj_ttl")) {
                tarObjTtl = jsonObject.getString("tar_obj_ttl");
                mTvTarObjNm.setText(FormatUtil.getStringValidate(tarObjTtl));
            }
            final String tarTtl = tarObjTtl;

            Class clazz = null;
            if(mTarObjTp.equals(CodeConstant.TYPE_CFRC)) {
                clazz = ConferenceDetailActivity.class;
            } else if(mTarObjTp.equals(CodeConstant.TYPE_TASK)) {
                clazz = TaskDetailActivity.class;
            } else if(mTarObjTp.equals(CodeConstant.TYPE_MEMO)) {
                clazz = MemoDetailActivity.class;
            } else {
                // TODO : 다른 타입이 있다면 여기에 추가
                if(!TextUtils.isEmpty(tarSelectUrl)) {
                    clazz = WebviewActivity.class;
                }
            }

            if(clazz == null) return;

            final Class clazzz = clazz;
            View.OnClickListener objOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, clazzz);
                    intent.putExtra(CodeConstant.CUR_OBJ_TP, mTarObjTp);
                    intent.putExtra(CodeConstant.CUR_OBJ_ID, tarObjId);
                    intent.putExtra(CodeConstant.CRT_USR_ID, crtUsrId);
                    intent.putExtra(CodeConstant.TAR_SELECT_URL, tarSelectUrl);
                    intent.putExtra(CodeConstant.TAR_OBJ_ID, tarObjId);
                    intent.putExtra(CodeConstant.TAR_OBJ_TTL, tarTtl);
                    intent.putExtra(CodeConstant.TITLE, tarTtl);
                    context.startActivity(intent);
                }
            };

            mTvCntn.setOnClickListener(objOnClickListener);
            mTvTarObjNm.setOnClickListener(objOnClickListener);
        }
    }

    public void setPhotoLayout(JSONArray array) {
        mSlImages.setVisibility(View.VISIBLE);
        mViewDummy.setVisibility(View.GONE);

        mSlImages.removeAllSliders();
        mSlImages.setBackgroundColor(ContextCompat.getColor(SNSDetailPostActivity.this, R.color.colorPrimaryLight));

        mImageInfoMap.clear();
        List<String> imageNmList = new ArrayList<>();
        List<String> imageUrlList = new ArrayList<>();

        int cnt = 0;
        for (int i=0; i<array.length(); i++) {
            try {
                if (!"PHOTO".equals(array.getJSONObject(i).getString("file_tp_cd"))) continue;

                String photo_nm = array.getJSONObject(i).getString("file_nm");
                String photo_path = I2UrlHelper.File.getPhotoImage(array.getJSONObject(i).getString("file_id"));
                Log.e(TAG, "nm :" + photo_nm + " , image : " + photo_path);
                DefaultSliderView sliderView = new DefaultSliderView(SNSDetailPostActivity.this);
                sliderView.setPicasso(I2UrlHelper.buildPicassoAddTokenHeader(SNSDetailPostActivity.this));
                sliderView
                        .image(photo_path)
                        .setScaleType(BaseSliderView.ScaleType.CenterInside)
                        .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                            @Override
                            public void onSliderClick(BaseSliderView baseSliderView) {
                                Intent intent = new Intent(SNSDetailPostActivity.this, SNSImageSliderActivity.class);
                                intent.putExtra(SNSImageSliderActivity.IMAGE_INFO_MAP, mImageInfoMap);
                                startActivity(intent);
                            }
                        });

                imageNmList.add(photo_nm);
                imageUrlList.add(photo_path);
                mSlImages.addSlider(sliderView);

                cnt++;
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mImageInfoMap.put(SNSImageSliderActivity.IMAGE_NM_LIST, imageNmList);
        mImageInfoMap.put(SNSImageSliderActivity.IMAGE_URL_LIST, imageUrlList);

        if(cnt <=0) {
            mSlImages.setVisibility(View.GONE);
            mViewCover.setVisibility(View.GONE);
            mViewDummy.setVisibility(View.VISIBLE);
        } else if (cnt==1) {
            mSlImages.setPresetTransformer(SliderLayout.Transformer.Default);
            mSlImages.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
            mViewCover.setVisibility(View.VISIBLE);
            mViewCover.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int action = motionEvent.getAction();

                    if (action == MotionEvent.ACTION_UP) {
                        Intent intent = new Intent(SNSDetailPostActivity.this, SNSImageSliderActivity.class);
                        intent.putExtra(SNSImageSliderActivity.IMAGE_INFO_MAP, mImageInfoMap);
                        startActivity(intent);
                    }
                    return true;
                }
            });
            mSlImages.stopAutoCycle();
        } else {
            mSlImages.setPresetTransformer(SliderLayout.Transformer.ZoomOutSlide);
            mSlImages.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
            mSlImages.setCustomAnimation(new DescriptionAnimation());
            mSlImages.setDuration(4000);
            mSlImages.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Visible);
            mViewCover.setVisibility(View.GONE);
            mSlImages.startAutoCycle();
        }

    }

    public void setFilesLayout(LinearLayout targetLayout, JSONArray array) {
        final JSONArray listFiles = array;
        Log.e(TAG, "fileList size ="+listFiles.length());
        if(listFiles.length() <= 0) {
            targetLayout.setVisibility(View.GONE);
        } else {
            targetLayout.setVisibility(View.VISIBLE);
            targetLayout.removeAllViews();

            int cnt = 0;
            //addFilesView
            for (int i=0; i<listFiles.length(); i++) {
                try {
                    final JSONObject jsonFile = listFiles.getJSONObject(i);
                    if(!"FILE".equals(jsonFile.getString("file_tp_cd"))) continue;

                    final String fileNm = jsonFile.getString("file_nm");
                    final String fileExt = FileUtil.getFileExtsion(fileNm);
                    final String fileId = FormatUtil.getStringValidate(jsonFile.getString("file_id"));
                    final String downloadURL = I2UrlHelper.File.getDownloadFile(fileId, PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
                    final String convertYn = jsonFile.getString("conv_yn");

                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View fileView = inflater.inflate(R.layout.view_item_file, null);

                    ImageView ivIcFileExt = (ImageView) fileView.findViewById(R.id.iv_ic_file_ext);
                    TextView tvFileNm = (TextView) fileView.findViewById(R.id.tv_file_nm);

                    //확장자에 따른 아이콘 변경처리
                    ivIcFileExt.setImageResource(R.drawable.ic_file_doc);
                    tvFileNm.setText(fileNm);
                    FileUtil.setFileExtIcon(ivIcFileExt, fileNm);

                    fileView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent;
                            try {
                                if ("Y".equals(FormatUtil.getStringValidate(convertYn))) { //i2viewer
                                    //i2viewer 연동 (문서중 conv_yn='Y'값만)
                                    /*intent = IntentUtil.getI2ViewerIntent(
                                            FormatUtil.getStringValidate(fileId),
                                            FormatUtil.getStringValidate(fileNm));*/

                                    // 첨부파일 링크를 바로  다운로드 할 수 있게 처리해 달라는 요청이 있어서 삽입
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadURL));
                                    Bundle bundle = new Bundle();
                                    bundle.putString("Authorization", I2UrlHelper.getTokenAuthorization());
                                    intent.putExtra(Browser.EXTRA_HEADERS, bundle);
                                } else if ("mp4".equalsIgnoreCase(fileExt) || "fla".equalsIgnoreCase(fileExt)) { //video
                                    intent = IntentUtil.getVideoPlayIntent(downloadURL);
                                } else {
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadURL));
                                    Bundle bundle = new Bundle();
                                    bundle.putString("Authorization", I2UrlHelper.getTokenAuthorization());
                                    intent.putExtra(Browser.EXTRA_HEADERS, bundle);
                                    Log.d(TAG, "intent:" + intent.toString());
                                }
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                                Toast.makeText(SNSDetailPostActivity.this, "I2뷰어 앱이 설치되어 있지않습니다.\nI2뷰어를 설치하시기 바랍니다.", Toast.LENGTH_LONG).show();
                                //TODO i2viewer 설치 링크 처리?
                            }
                        }
                    });
                    targetLayout.addView(fileView);
                    cnt++;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            if(cnt <= 0) targetLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_REPLY_WRITE) {
            if(resultCode == RESULT_OK){
            }
        }
    }

    public void setSurveyRadioButtons() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        mRgSurvey.removeAllViews();

        for (int i=0; i<surveyArray.length(); i++) {
            try {
                ContextThemeWrapper newContext = new ContextThemeWrapper(this, R.style.TextListSmall);
                RadioButton radioButton = new RadioButton(newContext);
                radioButton.setText(surveyArray.getJSONObject(i).getString("survey_itm_nm"));
                mRgSurvey.addView(radioButton, p);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void showChartVote(JSONArray jsonArray){
        BarSet barSet = new BarSet();
        Bar bar;
        List<Integer> orderArray = new ArrayList<>();

        mChartVote.getData().clear();
        voteCntArray.clear();

        int tatalCnt = 0, maxCnt = 0;

        for(int i = jsonArray.length()-1; i >= 0; i--){
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String label = jsonObject.getString("survey_itm_nm");
                Integer cnt = jsonObject.getInt("vote_cnt");

                if (label.length() > 10) {
                    label = label.substring(0, 10) + "...";
                }

                if (maxCnt < cnt) {
                    maxCnt = cnt;
                }

                bar = new Bar(label, cnt);

                if (surveyArray.getJSONObject(i).getString("voted_yn").equals("Y")) {
                    bar.setColor(Color.parseColor("#fc7f47"));
                } else {
                    bar.setColor(Color.parseColor("#607d8b"));
                }

                voteCntArray.add(cnt);
                orderArray.add(jsonObject.getInt("sort_ord"));
                tatalCnt += cnt;

                barSet.addBar(bar);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mChartVote.addData(barSet);
        mChartVote.setBarSpacing(Tools.fromDpToPx(12));

        mChartVote.setBorderSpacing(0)
                .setAxisBorderValues(0, maxCnt, 1) // 2)
                .setXAxis(false)
                .setLabelsColor(Color.parseColor("#FF8E8A84"))
                .setYLabels(YController.LabelPosition.OUTSIDE)
                .setXLabels(XController.LabelPosition.NONE);

        Collections.reverse(voteCntArray);

        int[] order = new int[orderArray.size()];
        int i = 0;
        for (Integer e : orderArray) order[i++] = e.intValue();

        Runnable tooltipAction = new Runnable() {
            @Override
            public void run() {
                showTooltip();
            }
        };
        mChartVote.show(
                new Animation()
                        .setOverlap(.5f, order)
                        .setEndAction(tooltipAction)
        );

        mTvVoteCnt.setText(String.format("총 %d표", tatalCnt));
    }


    private void showTooltip(){
        ArrayList<ArrayList<Rect>> areas = new ArrayList<>();
        areas.add(mChartVote.getEntriesArea(0));

        mChartVote.dismissAllTooltips();

        for (int i = 0; i < areas.get(0).size(); i++) {

            Tooltip tooltip = new Tooltip(SNSDetailPostActivity.this, R.layout.chart_tooltip, R.id.value);
            tooltip.prepare(areas.get(0).get(i), voteCntArray.get(i));

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                tooltip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1));
                tooltip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0));
            }

            mChartVote.showTooltip(tooltip, true);
        }
    }

    public static class ReplyRecyclerViewAdapter
            extends RecyclerView.Adapter<ReplyRecyclerViewAdapter.ViewHolder> {

        private Context mContext;
        private final TypedValue mTypedValue = new TypedValue();
        private int mBackground;
        private JSONArray mValues;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;

            public String mPostID;

            public final CircleImageView mCivCrtUsrPhoto;
            public final TextView mTvCrtUsrNm, mTvCrtDttm, mTvCntn, mTvLikeCnt;
            public final ImageView mIvLike;
            public final Button mBtnReplyDelete;
            public final LinearLayout mLlLike;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mCivCrtUsrPhoto = (CircleImageView) view.findViewById(R.id.civ_crt_usr_photo);
                mTvCrtUsrNm = (TextView) view.findViewById(R.id.tv_crt_usr_nm);
                mTvCrtDttm = (TextView) view.findViewById(R.id.tv_crt_dttm);
                mTvCntn = (TextView) view.findViewById(R.id.tv_cntn);
                mTvLikeCnt = (TextView) view.findViewById(R.id.tv_like_cnt);
                mIvLike = (ImageView) view.findViewById(R.id.iv_like);
                mBtnReplyDelete = (Button) view.findViewById(R.id.btn_reply_delete);
                mLlLike = (LinearLayout) view.findViewById(R.id.ll_like);
            }

            @Override
            public String toString() {
                return super.toString();
            }
        }

        public JSONObject getValueAt(int position) throws JSONException {
            return mValues.getJSONObject(position);
        }

        public ReplyRecyclerViewAdapter(Context context, JSONArray items) {
            mContext = context;
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_reply, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            try {
                final JSONObject jsonObject = mValues.getJSONObject(position);

                holder.mPostID = jsonObject.getString("post_id");
                holder.mTvCrtUsrNm.setText(FormatUtil.getStringValidate(jsonObject.getString("crt_usr_nm")));
                holder.mTvCrtDttm.setText(DateCalendarUtil.getStringFromYYYYMMDDHHMM(jsonObject.getString("crt_dttm")));
                holder.mTvCntn.setText(FormatUtil.getStringValidate(jsonObject.getString("cntn")));
                holder.mTvLikeCnt.setText(FormatUtil.getStringValidate(jsonObject.getString("like_cnt")));

                if (jsonObject.getString("like_my").equals("Y")) {
                    holder.mIvLike.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_like));
                } else {
                    holder.mIvLike.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_unlike));
                }

                holder.mLlLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        I2ConnectApi.requestJSON(mContext, I2UrlHelper.SNS.getToggleSnsLike(holder.mPostID))
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<JSONObject>() {
                                    @Override
                                    public void onCompleted() {
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        DialogUtil.showErrorDialog(mContext, e.getMessage());
                                        Log.d(TAG, "onError = " + e.getMessage());
                                        //Error dialog 표시
                                        e.printStackTrace();
                                        DialogUtil.showErrorDialogWithValidateSession(mContext, e);
                                    }

                                    @Override
                                    public void onNext(JSONObject jsonObject) {
                                        JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);

                                        try {
                                            for (int i = 0; i < mValues.length(); i++) {
                                                if (mValues.getJSONObject(i).getString("post_id").equals(holder.mPostID)) {
                                                    int likeCnt = mValues.getJSONObject(i).getInt("like_cnt");
                                                    if("C".equals(statusInfo.getString("crud").toString())) {
                                                        mValues.getJSONObject(i).put("like_cnt", Integer.toString(++likeCnt));
                                                        mValues.getJSONObject(i).put("like_my", "Y");
                                                    } else {
                                                        mValues.getJSONObject(i).put("like_cnt", Integer.toString(--likeCnt));
                                                        mValues.getJSONObject(i).put("like_my", "N");
                                                    }
                                                    break;
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        notifyDataSetChanged();
                                    }
                                });
                    }
                });

                if ( PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(jsonObject.getString("crt_usr_id")) ) {
                    holder.mBtnReplyDelete.setVisibility(View.VISIBLE);
                } else {
                    holder.mBtnReplyDelete.setVisibility(View.GONE);
                }

                if(!jsonObject.isNull("list_user")) {
                    JSONArray array = jsonObject.getJSONArray("list_user");

                    String cntn_temp = jsonObject.getString("cntn");

                    List<Link> links = new ArrayList<>();

                    for (int i=0; i<array.length(); i++) {

                        final String crtUserID = array.getJSONObject(i).getString("usr_id");
                        final String crtUserNM = array.getJSONObject(i).getString("usr_nm");

                        cntn_temp = cntn_temp.replace("@[" + crtUserNM + "]", crtUserNM);
                        Link personLink = new Link( crtUserNM );
                        personLink.setTypeface(Typeface.DEFAULT_BOLD)
                                .setOnClickListener(new Link.OnClickListener() {
                                    @Override
                                    public void onClick(String clickedText) {
                                        Intent intent = new Intent(mContext, SNSDetailProfileActivity.class);
                                        intent.putExtra(SNSDetailProfileActivity.USR_ID, crtUserID);
                                        intent.putExtra(SNSDetailProfileActivity.USR_NM, crtUserNM);

                                        mContext.startActivity(intent);
                                    }
                                });

                        links.add(personLink);

                    }

                    holder.mTvCntn.setText(FormatUtil.getStringValidate(cntn_temp));

                    LinkBuilder.on(holder.mTvCntn)
                            .addLinks(links)
                            .build();
                }

                holder.mBtnReplyDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        DialogUtil.showConfirmDialog(v.getContext(), "삭제 안내", "내가 쓴 댓글을 삭제하시겠습니까?",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        I2ConnectApi.requestJSON(mContext, I2UrlHelper.SNS.deleteSnsPost(holder.mPostID))
                                                .subscribeOn(Schedulers.newThread())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Subscriber<JSONObject>() {
                                                    @Override
                                                    public void onCompleted() {
                                                        Log.d(TAG, "I2UrlHelper.SNS.deleteSnsPost onCompleted");
                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {
                                                        Log.d(TAG, "I2UrlHelper.SNS.deleteSnsPost onError");
                                                        //Error dialog 표시
                                                        e.printStackTrace();
                                                        DialogUtil.showErrorDialogWithValidateSession(mContext, e);
                                                    }

                                                    @SuppressLint("NewApi")
                                                    @Override
                                                    public void onNext(JSONObject jsonObject) {
                                                        Log.d(TAG, "I2UrlHelper.SNS.deleteSnsPost onNext");
                                                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                                                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);

                                                            for (int i=0; i<mValues.length(); i++) {
                                                                try {
                                                                    if ( mValues.getJSONObject(i).getString("post_id").equals(statusInfo.getString("post_id")) ) {
                                                                        mValues.remove(i);
                                                                        notifyDataSetChanged();
                                                                        break;
                                                                    }
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }

                                                        } else {
                                                            Toast.makeText(v.getContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                });

                Glide.with(holder.mCivCrtUsrPhoto.getContext())
                        .load(I2UrlHelper.File.getUsrImage(jsonObject.getString("crt_usr_photo")))
                        .error(R.drawable.ic_no_usr_photo)
                        .fitCenter()
                        .into(holder.mCivCrtUsrPhoto);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



        @Override
        public int getItemCount() {
            return mValues.length();
        }
    }
}
