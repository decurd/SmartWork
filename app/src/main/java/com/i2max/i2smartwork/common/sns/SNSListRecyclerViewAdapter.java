package com.i2max.i2smartwork.common.sns;

import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
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

/**
 * Created by berserk1147 on 15. 8. 9..
 */
public class SNSListRecyclerViewAdapter
        extends RecyclerView.Adapter<SNSListRecyclerViewAdapter.ViewHolder> {
    static String TAG = SNSListRecyclerViewAdapter.class.getSimpleName();

    private Context mContext;
    private final TypedValue mTypedValue = new TypedValue();
    private int mBackground;
    private List<JSONObject> mValues;
    private String mUsrID = "";

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public JSONArray surveyArray;
        protected List<Integer> voteCntArray = new ArrayList<>();

        public String mCrtUsrID, mPostID, mTarObjTp, mTarObjId, mListReply;
        public HashMap<String,List<String>> mImageInfoMap = new HashMap<>();
        public final View mView, mViewDummy, mViewCover;
        public final CircleImageView mCivCrtUsrPhoto;
        public final TextView mTvCrtTerm, mTvCrtUsrNm, mTvTarGrpNm, mTvCrtDttm, mTvCntn, mTvReplyCnt, mTvLikeCnt, mTvVoteCnt, mTvSurveyVoteType, mTvSurveyResultType, mTvLink;
        public final ImageView mIvLike;
        public final LinearLayout mLlLike, mLlReply;
        public final Button mBtnDelete;
        public final LinearLayout mLlSurveyVote, mLlVoteResult, mLlLink, mLlFile, mLlFileNms;
        public final SliderLayout mSlImages;
        public final RadioGroup mRgSurvey;
        public final HorizontalBarChartView mChartVote;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mCivCrtUsrPhoto = (CircleImageView) view.findViewById(R.id.civ_crt_usr_photo);
            mTvCrtTerm = (TextView) view.findViewById(R.id.tv_crt_term);
            mTvCrtUsrNm = (TextView) view.findViewById(R.id.tv_crt_usr_nm);
            mTvTarGrpNm = (TextView) view.findViewById(R.id.tv_tar_grp_nm);
            mTvCrtDttm = (TextView) view.findViewById(R.id.tv_crt_dttm);
            mTvCntn = (TextView) view.findViewById(R.id.tv_cntn);
            mTvReplyCnt = (TextView) view.findViewById(R.id.tv_reply_cnt);
            mTvLikeCnt = (TextView) view.findViewById(R.id.tv_like_cnt);
            mIvLike = (ImageView) view.findViewById(R.id.iv_like);
            mSlImages = (SliderLayout) view.findViewById(R.id.slider);
            mLlLike = (LinearLayout) view.findViewById(R.id.ll_like);
            mLlReply = (LinearLayout) view.findViewById(R.id.ll_reply);
            mBtnDelete = (Button) view.findViewById(R.id.btn_del);
            mViewDummy = view.findViewById(R.id.view_dummy);
            mViewCover = view.findViewById(R.id.view_cover);

            mLlSurveyVote = (LinearLayout) view.findViewById(R.id.ll_survey_vote);
            mRgSurvey = (RadioGroup) view.findViewById(R.id.rg_survey);
            Button btnVoteSave = (Button) view.findViewById(R.id.btn_vote_save);
            btnVoteSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveSnsSurveyVote();
                }
            });
            Button btnVoteResult = (Button) view.findViewById(R.id.btn_vote_result);
            btnVoteResult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLlVoteResult.setVisibility(View.VISIBLE);
                    mLlSurveyVote.setVisibility(View.GONE);
                    loadSnsSurveyVoteResult();
                }
            });

            mTvSurveyVoteType = (TextView) view.findViewById(R.id.tv_survey_vote_type);
            mTvSurveyResultType = (TextView) view.findViewById(R.id.tv_survey_result_type);

            mLlVoteResult = (LinearLayout) view.findViewById(R.id.ll_vote_result);
            mChartVote = (HorizontalBarChartView) view.findViewById(R.id.chart_vote);
            mTvVoteCnt = (TextView) view.findViewById(R.id.tv_vote_cnt);
            Button btnVoteRefresh = (Button) view.findViewById(R.id.btn_vote_refresh);
            btnVoteRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadSnsSurveyVoteResult();
                }
            });
            Button btnVoteAgain = (Button) view.findViewById(R.id.btn_vote_again);
            btnVoteAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLlSurveyVote.setVisibility(View.VISIBLE);
                    mLlVoteResult.setVisibility(View.GONE);
                }
            });

            mTvLink = (TextView) view.findViewById(R.id.tv_link);

            mLlLink = (LinearLayout) view.findViewById(R.id.ll_link);
            mLlFile = (LinearLayout) view.findViewById(R.id.ll_file);
            mLlFileNms = (LinearLayout) view.findViewById(R.id.ll_file_nms);
        }

        @Override
        public String toString() {
            return super.toString();
        }

        public void saveSnsSurveyVote() {
            if (mRgSurvey.getCheckedRadioButtonId() == 0) {
                DialogUtil.showInformationDialog(mView.getContext(), "투표항목을 선택해주세요.");
                return;
            }

            int index = mRgSurvey.indexOfChild(mView.findViewById(mRgSurvey.getCheckedRadioButtonId()));

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

            I2ConnectApi.requestJSON(mView.getContext(), I2UrlHelper.SNS.saveSnsSurveyVote(mPostID, selectedItemID))
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
                            DialogUtil.showErrorDialogWithValidateSession(mView.getContext(), e);
                        }

                        @Override
                        public void onNext(JSONObject jsonObject) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);


                        }
                    });
        }

        public void loadSnsSurveyVoteResult() {
            I2ConnectApi.requestJSON(mView.getContext(), I2UrlHelper.SNS.getSnsSurveyVoteResult(mPostID))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<JSONObject>() {
                        @Override
                        public void onCompleted() {
                            Log.d(TAG, "I2UrlHelper.SNS.getSnsSurveyVoteResult onError");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "onError = " + e.getMessage());
                            //Error dialog 표시
                            e.printStackTrace();
                            DialogUtil.showErrorDialogWithValidateSession(mView.getContext(), e);

                        }

                        @Override
                        public void onNext(JSONObject jsonObject) {
                            JSONArray statusInfoArray = I2ResponseParser.getStatusInfoArray(jsonObject);

                            showChartVote(statusInfoArray);
                        }
                    });
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
                    .setAxisBorderValues(0, maxCnt, 1)
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

                Tooltip tooltip = new Tooltip(mView.getContext(), R.layout.chart_tooltip, R.id.value);
                tooltip.prepare(areas.get(0).get(i), voteCntArray.get(i));

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    tooltip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1));
                    tooltip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0));
                }

                mChartVote.showTooltip(tooltip, true);
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
                    ContextThemeWrapper newContext = new ContextThemeWrapper(mView.getContext(), R.style.TextListSmall);
                    RadioButton radioButton = new RadioButton(newContext);
                    radioButton.setText(surveyArray.getJSONObject(i).getString("survey_itm_nm"));
                    mRgSurvey.addView(radioButton, p);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public JSONObject getValueAt(int position) throws JSONException {
        return mValues.get(position);
    }

    public SNSListRecyclerViewAdapter(Context context, List<JSONObject> items) {
        mContext = context;
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        mValues = items;
    }

    public SNSListRecyclerViewAdapter(Context context, List<JSONObject> items, String usrID) {
        mContext = context;
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        mValues = items;
        mUsrID = usrID;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_sns, parent, false);
        view.setBackgroundResource(mBackground);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        try {
            JSONObject jsonObject = mValues.get(position);

            holder.mCrtUsrID = jsonObject.getString("crt_usr_id");
            holder.mPostID = jsonObject.getString("post_id");
            holder.mTarObjTp = jsonObject.getString("tar_obj_tp_cd");

            if ( !jsonObject.isNull("list_reply") )
                holder.mListReply = jsonObject.getJSONArray("list_reply").toString();

            if (jsonObject.getString("post_tp_cd").equals("POLL")) {

                holder.surveyArray = jsonObject.getJSONArray("list_survey");

                holder.setSurveyRadioButtons();

                if (jsonObject.getString("voted_yn").equals("Y")) {
                    holder.mLlVoteResult.setVisibility(View.VISIBLE);
                    holder.loadSnsSurveyVoteResult();
                } else {
                    holder.mLlSurveyVote.setVisibility(View.VISIBLE);
                }

                if (jsonObject.getString("survey_usr_open_yn").equals("Y")) {
                    holder.mTvSurveyVoteType.setText("설문내용 (기명)");
                    holder.mTvSurveyResultType.setText("설문결과 (기명)");
                } else {
                    holder.mTvSurveyVoteType.setText("설문내용 (무기명)");
                    holder.mTvSurveyResultType.setText("설문결과 (무기명)");
                }
            } else {
                holder.mLlVoteResult.setVisibility(View.GONE);
                holder.mLlSurveyVote.setVisibility(View.GONE);
            }

            holder.mTvCrtTerm.setText(DateCalendarUtil.getStringFromBetweenNow(jsonObject.getString("crt_dttm")));
            holder.mTvCrtUsrNm.setText(jsonObject.getString("crt_usr_nm"));
            if ( CodeConstant.TYPE_GROUP.equals(jsonObject.isNull("tar_obj_tp_cd"))) {
                holder.mTarObjId = jsonObject.getString("tar_obj_id");
                holder.mTvTarGrpNm.setText(jsonObject.getString("tar_grp_nm"));
                holder.mTvTarGrpNm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, SNSDetailGroupActivity.class);
                        intent.putExtra(SNSDetailGroupActivity.GRP_ID, holder.mTarObjId);
                        intent.putExtra(SNSDetailGroupActivity.GRP_NM, holder.mTvTarGrpNm.getText().toString());

                        context.startActivity(intent);
                    }
                });
            }
            holder.mTvCrtDttm.setText(DateCalendarUtil.getStringFromYYYYMMDDHHMM(jsonObject.getString("crt_dttm")));
            holder.mTvCntn.setText(jsonObject.getString("cntn"));
            holder.mTvReplyCnt.setText(jsonObject.getString("reply_cnt"));
            holder.mTvLikeCnt.setText(jsonObject.getString("like_cnt"));

            if (jsonObject.getString("like_my").equals("Y")) {
                holder.mIvLike.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_like));
            } else {
                holder.mIvLike.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_unlike));
            }

            View.OnClickListener profileOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mUsrID.equals(holder.mCrtUsrID)) {
                        Toast.makeText(v.getContext(), "보고 있는 멤버와 같은 멤버입니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, SNSDetailProfileActivity.class);
                        intent.putExtra(SNSDetailProfileActivity.USR_ID, holder.mCrtUsrID);
                        intent.putExtra(SNSDetailProfileActivity.USR_NM, holder.mTvCrtUsrNm.getText().toString());

                        context.startActivity(intent);
                    }
                }
            };

            holder.mCivCrtUsrPhoto.setOnClickListener(profileOnClickListener);
            holder.mTvCrtUsrNm.setOnClickListener(profileOnClickListener);

            if ( PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID).equals(holder.mCrtUsrID) ) {
                holder.mBtnDelete.setVisibility(View.VISIBLE);
            } else {
                holder.mBtnDelete.setVisibility(View.GONE);
            }

            holder.mBtnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    DialogUtil.showConfirmDialog(v.getContext(), "삭제 안내", "내가 쓴 글을 삭제하시겠습니까?",
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

                                                        for (int i = 0; i < mValues.size(); i++) {
                                                            try {
                                                                if (mValues.get(i).getString("post_id").equals(statusInfo.getString("post_id"))) {
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
                                    Log.d(TAG, "onError = " + e.getMessage());
                                    DialogUtil.showErrorDialogWithValidateSession(mContext, e);
                                }

                                @Override
                                public void onNext(JSONObject jsonObject) {
                                    JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);

                                    try {

                                        for (int i = 0; i < mValues.size(); i++) {
                                            if (mValues.get(i).getString("post_id").equals(holder.mPostID)) {
                                                int likeCnt = mValues.get(i).getInt("like_cnt");
                                                if("C".equals(statusInfo.getString("crud").toString())) { //crud C:좋아요, D:좋아요 취소
                                                    mValues.get(i).put("like_cnt", Integer.valueOf(++likeCnt));
                                                    mValues.get(i).put("like_my", "Y");
                                                } else {
                                                    mValues.get(i).put("like_cnt", Integer.valueOf(--likeCnt));
                                                    mValues.get(i).put("like_my", "N");
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


            View.OnClickListener onClickListenerToDetailPost = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, SNSDetailPostActivity.class);
                    intent.putExtra(CodeConstant.POST_ID, holder.mPostID);
                    intent.putExtra(CodeConstant.TAR_OBJ_TP, holder.mTarObjTp);
                    intent.putExtra(CodeConstant.CRT_USR_ID, holder.mCrtUsrID);

                    context.startActivity(intent);
                }
            };

            holder.mTvCntn.setOnClickListener(onClickListenerToDetailPost);
            holder.mLlReply.setOnClickListener(onClickListenerToDetailPost);

            Glide.with(holder.mCivCrtUsrPhoto.getContext())
                    .load(I2UrlHelper.File.getUsrImage(jsonObject.getString("crt_usr_photo")))
                    .error(R.drawable.ic_no_usr_photo)
                    .fitCenter()
                    .into(holder.mCivCrtUsrPhoto);

            if(jsonObject.isNull("list_photo")) {
                holder.mSlImages.setVisibility(View.GONE);
                holder.mViewCover.setVisibility(View.GONE);
                holder.mViewDummy.setVisibility(View.VISIBLE);
            } else {
                holder.mSlImages.setVisibility(View.VISIBLE);
                holder.mViewDummy.setVisibility(View.GONE);

                JSONArray array = jsonObject.getJSONArray("list_photo");

                holder.mSlImages.removeAllSliders();
                holder.mSlImages.setBackgroundColor(Color.GRAY);

                holder.mImageInfoMap.clear();
                List<String> imageNmList = new ArrayList<>();
                List<String> imageUrlList = new ArrayList<>();

                int cnt = 0;
                for (int i=0; i<array.length(); i++) {

                    DefaultSliderView sliderView = new DefaultSliderView(mContext);
                    sliderView
                            .image(I2UrlHelper.File.getPhotoImage(array.getJSONObject(i).getString("file_id")))
                            .setScaleType(BaseSliderView.ScaleType.CenterInside)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView baseSliderView) {
                                    Intent intent = new Intent(mContext, SNSImageSliderActivity.class);
                                    intent.putExtra(SNSImageSliderActivity.IMAGE_INFO_MAP, holder.mImageInfoMap);
                                    mContext.startActivity(intent);
                                }
                            });

                    imageNmList.add(array.getJSONObject(i).getString("attach_nm"));
                    imageUrlList.add(I2UrlHelper.File.getPhotoImage(array.getJSONObject(i).getString("file_id")));
                    holder.mSlImages.addSlider(sliderView);

                    cnt++;
                }

                holder.mImageInfoMap.put(SNSImageSliderActivity.IMAGE_NM_LIST, imageNmList);
                holder.mImageInfoMap.put(SNSImageSliderActivity.IMAGE_URL_LIST, imageUrlList);

                if (cnt<=1) {
                    holder.mSlImages.setPresetTransformer(SliderLayout.Transformer.Default);
                    holder.mSlImages.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
                    holder.mSlImages.stopAutoCycle();
                    holder.mViewCover.setVisibility(View.VISIBLE);
                    holder.mViewCover.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            int action = motionEvent.getAction();

                            if (action == MotionEvent.ACTION_UP) {
                                Intent intent = new Intent(mContext, SNSImageSliderActivity.class);
                                intent.putExtra(SNSImageSliderActivity.IMAGE_INFO_MAP, holder.mImageInfoMap);
                                mContext.startActivity(intent);
                            }

                            return true;
                        }
                    });
                } else {
                    holder.mSlImages.setPresetTransformer(SliderLayout.Transformer.ZoomOutSlide);
                    holder.mSlImages.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
                    holder.mSlImages.setCustomAnimation(new DescriptionAnimation());
                    holder.mSlImages.setDuration(4000);
                    holder.mSlImages.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Visible);
                    holder.mViewCover.setVisibility(View.GONE);
                }
            }

            if(!jsonObject.isNull("list_link")) {
                holder.mLlLink.setVisibility(View.VISIBLE);
                JSONArray array = jsonObject.getJSONArray("list_link");
                if (array.length() > 0) {
                    holder.mTvLink.setText(array.getJSONObject(0).getString("attach_nm"));
                    final String linkURL = array.getJSONObject(0).getString("usr_link");
                    holder.mLlLink.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkURL));
                            mContext.startActivity(browserIntent);
                        }
                    });
                }
            } else {
                holder.mLlLink.setVisibility(View.GONE);
            }

            if(!jsonObject.isNull("list_file")) {
                setFilesLayout(holder.mLlFile, jsonObject.getJSONArray("list_file"));
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

                holder.mTvCntn.setText(cntn_temp);

                LinkBuilder.on(holder.mTvCntn)
                        .addLinks(links)
                        .build();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setFilesLayout(LinearLayout targetLayout, JSONArray array) {
        final JSONArray listFiles = array;
        Log.e(TAG, "fileList size ="+listFiles.length());
        if(listFiles == null || listFiles.length() <= 0) {
            targetLayout.setVisibility(View.GONE);
        } else {
            targetLayout.setVisibility(View.VISIBLE);
            targetLayout.removeAllViews();

            //addFilesView
            for (int i=0; i<listFiles.length(); i++) {
                try {
                    final JSONObject jsonFile = listFiles.getJSONObject(i);
                    final String fileNm = jsonFile.getString("file_nm");
                    final String fileId = FormatUtil.getStringValidate(jsonFile.getString("file_id"));
                    final String fileExt = FileUtil.getFileExtsion(fileNm);
                    final String downloadURL = I2UrlHelper.File.getDownloadFile(fileId, PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID));
                    final String convertYn = jsonFile.getString("conv_yn");


                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                            Intent intent = null;
                            if ("Y".equals(FormatUtil.getStringValidate(convertYn))) {
                                //i2viewer 연동 (문서중 conv_yn='Y'값만)
                                intent = IntentUtil.getI2ViewerIntent(
                                        FormatUtil.getStringValidate(fileId),
                                        FormatUtil.getStringValidate(fileNm));
                                mContext.startActivity(intent);
                            } else if ("mp4".equalsIgnoreCase(fileExt) || "fla".equalsIgnoreCase(fileExt) ||
                                    "wmv".equalsIgnoreCase(fileExt) || "avi".equalsIgnoreCase(fileExt)) { //video
                                intent = IntentUtil.getVideoPlayIntent(downloadURL);
                            } else {
                                //토큰 인증처리
                                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadURL));
                                Bundle bundle = new Bundle();
                                bundle.putString("Authorization", I2UrlHelper.getTokenAuthorization());
                                intent.putExtra(Browser.EXTRA_HEADERS, bundle);
                                Log.d(TAG, "intent:" + intent.toString());
                            }
                            mContext.startActivity(intent);
                        }
                    });

                    targetLayout.addView(fileView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
