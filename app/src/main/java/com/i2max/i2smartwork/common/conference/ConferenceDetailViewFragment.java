/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.i2max.i2smartwork.common.conference;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Browser;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.internal.LinkedTreeMap;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.common.sns.SNSDetailProfileActivity;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.DisplayUtil;
import com.i2max.i2smartwork.utils.FileUtil;
import com.i2max.i2smartwork.utils.FormatUtil;
import com.i2max.i2smartwork.utils.IntentUtil;

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ConferenceDetailViewFragment extends Fragment {
    static String TAG = ConferenceDetailViewFragment.class.getSimpleName();

    public static final int LIST_CFRC_MEMBER = 0;
    public static final int LIST_CFRC_DETAIL = 1;
    public static final int LIST_CFRC_TASK = 2;
    public static final int LIST_CFRC_FEED = 3;

    public CircleImageView mCivCrtUsrPhoto;
    public ImageView mIvCfrcPushYn, mIvCfrcPlanShare;
    public LinearLayout mLlcfrcfileList, mLlcfrcMovList, mLlcfrcDocList, mLlcfrcRestList;
    public RelativeLayout mRlCfrcRecode;
    protected TextView mTvCfrcTtl, mTvCrtDttm, mTvCrtUsrNm, mTvCfrcStNm, mTvCfrcRoomNm, mTvCfrcTerm, mTvCfrcRoomTpNm, mTvCfrcTpNm, mTvCfrcCntn, mTvCfrcRecode;

    private String mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conference_detail, container, false);
        mTvCfrcTtl = (TextView) view.findViewById(R.id.tv_cfrc_ttl);
        mTvCrtDttm = (TextView) view.findViewById(R.id.tv_crt_dttm);
        mCivCrtUsrPhoto = (CircleImageView) view.findViewById(R.id.civ_crt_usr_photo);
        mTvCrtUsrNm = (TextView) view.findViewById(R.id.tv_crt_usr_nm);
        mTvCfrcStNm = (TextView) view.findViewById(R.id.tv_cfrc_st_nm);
        mTvCfrcRoomNm = (TextView) view.findViewById(R.id.tv_cfrc_room_nm);
        mTvCfrcTerm = (TextView) view.findViewById(R.id.tv_cfrc_term);
        mTvCfrcRoomTpNm = (TextView) view.findViewById(R.id.tv_cfrc_room_tp_nm);
        mTvCfrcTpNm = (TextView) view.findViewById(R.id.tv_cfrc_tp_nm);
        mIvCfrcPushYn = (ImageView) view.findViewById(R.id.iv_cfrc_push_yn_ic);
        mIvCfrcPlanShare = (ImageView) view.findViewById(R.id.iv_cfrc_plan_share_yn_ic);
        mTvCfrcCntn = (TextView) view.findViewById(R.id.tv_cfrc_cntn);

        mLlcfrcfileList = (LinearLayout) view.findViewById(R.id.ll_cfrc_file_list);
        mLlcfrcMovList = (LinearLayout) view.findViewById(R.id.ll_cfrc_mov_list);
        mLlcfrcDocList = (LinearLayout) view.findViewById(R.id.ll_cfrc_doc_list);
        mRlCfrcRecode = (RelativeLayout)view.findViewById(R.id.rl_cfrc_recode);
        mLlcfrcRestList = (LinearLayout) view.findViewById(R.id.ll_cfrc_rest_list);
        mTvCfrcRecode = (TextView) view.findViewById(R.id.tv_cfrc_recode);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void loadRecyclerView(String tarObjTp, String tarObjId, String tarObjTtl, String tarCrtUsrId) {
        mTarObjTp = tarObjTp;
        mTarObjId = tarObjId;
        mTarObjTtl = tarObjTtl;
        mTarCrtUsrId = tarCrtUsrId;
        Log.e(TAG, "mTarObjTp = " + mTarObjTp + "mTarObjId = " + mTarObjId + "mTarObjTtl = " + mTarObjTtl + "mTarCrtUsrId = " + mTarCrtUsrId);

        I2ConnectApi.requestJSON2Map(getActivity(), I2UrlHelper.Cfrc.getViewSnsConference(mTarObjId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Conference.getViewSnsConference onCompleted");
                        loadEditViewCfrcFile(mTarObjId);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Conference.getViewSnsConference onError");
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(getActivity(), e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Conference.getViewSnsConference onNext");
                        LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");
                        setCfrcViewData(statusInfo);
                    }
                });
    }

    public void loadEditViewCfrcFile(String mTarObjId) {
        I2ConnectApi.requestJSON2Map(getActivity(), I2UrlHelper.Cfrc.getListSnsCfrcFile(mTarObjId))
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
                        DialogUtil.showErrorDialogWithValidateSession(getActivity(), e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Conference.getViewSnsConference onNext");
                        LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");
                        setCfrcFileData(statusInfo);
                    }
                });
    }

    public void setCfrcViewData(LinkedTreeMap<String, Object> item) {
        final String crtUsrId = FormatUtil.getStringValidate(item.get("crt_usr_id"));
        final String crtUsrNm = FormatUtil.getStringValidate(item.get("crt_usr_nm"));

        mTvCfrcTtl.setText(FormatUtil.getStringValidate(item.get("cfrc_ttl")));
        //수정이력 있음, 수정자 기준표시, 수정없음, 작성자 기준표시
        if("".equals(FormatUtil.getStringValidate(item.get("mod_dttm")))) {
            mTvCrtDttm.setText(FormatUtil.getFormattedDateTime(FormatUtil.getStringValidate(item.get("crt_dttm")))); //만든시간 표시
        } else {
            mTvCrtDttm.setText(FormatUtil.getFormattedDateTime(FormatUtil.getStringValidate(item.get("mod_dttm")))); //수정시간 표시
        }
        Glide.with(mCivCrtUsrPhoto.getContext())
                .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(item.get("crt_usr_photo"))))
                .error(R.drawable.ic_no_usr_photo)
                .fitCenter()
                .into(mCivCrtUsrPhoto);
        mCivCrtUsrPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SNSDetailProfileActivity.class);
                intent.putExtra(SNSDetailProfileActivity.USR_ID, crtUsrId);
                intent.putExtra(SNSDetailProfileActivity.USR_NM, crtUsrNm);
                getActivity().startActivity(intent);
            }
        });
        mTvCrtUsrNm.setText(crtUsrNm);

        mTvCfrcStNm.setText(FormatUtil.getStringValidate(item.get("cfrc_st_nm")));
        mTvCfrcRoomNm.setText(FormatUtil.getStringValidate(item.get("cfrc_room_nm")));

        String cfrcDt = FormatUtil.getFormattedDate5(FormatUtil.getStringValidate(item.get("cfrc_dt")));
        mTvCfrcTerm.setText(cfrcDt + " "+
                FormatUtil.getFormattedCfrcTime(FormatUtil.getStringValidate(item.get("start_tm"))) + "~" +
                FormatUtil.getFormattedCfrcTime(FormatUtil.getStringValidate(item.get("end_tm"))));

        mTvCfrcRoomTpNm.setText(FormatUtil.getStringValidate(item.get("cfrc_room_tp")));

        if("오프라인".equals(mTvCfrcRoomTpNm.getText().toString())) mTvCfrcTpNm.setVisibility(View.GONE);
        else mTvCfrcTpNm.setText(FormatUtil.getStringValidate(item.get("cfrc_tp_nm")));

        mTvCfrcCntn.setText(FormatUtil.getStringValidate(item.get("cfrc_cntn")));

        int drawableIconPushYn = R.drawable.ic_icon_check_off;
        if("Y".equals(FormatUtil.getStringValidate(item.get("cfrc_crt_noti_yn")))) {
            drawableIconPushYn = R.drawable.ic_icon_check_on;
        }
        mIvCfrcPushYn.setImageResource(drawableIconPushYn);

        int drawableIconPlanOpenYn = R.drawable.ic_icon_check_off;
        if("Y".equals(FormatUtil.getStringValidate(item.get("plan_open_yn")))) {
            drawableIconPlanOpenYn = R.drawable.ic_icon_check_on;
        }
        mIvCfrcPlanShare.setImageResource(drawableIconPlanOpenYn);



        //회의결과자
        if(TextUtils.isEmpty(FormatUtil.getStringValidate(item.get("cfrc_rslt_cntn")))) mRlCfrcRecode.setVisibility(View.GONE);
        else {
            mRlCfrcRecode.setVisibility(View.VISIBLE);
            mTvCfrcRecode.setText(FormatUtil.getStringValidate(item.get("cfrc_rslt_cntn")));
        }

    }

    public void setCfrcFileData(LinkedTreeMap<String, Object> item) {
        //회의문서자료
        setFilesLayout("회의문서자료", mLlcfrcDocList, item.get("doc_file_list"));
        //동영상공유자료
        setFilesLayout("동영상공유자료", mLlcfrcMovList, item.get("share_mov_list"));
        //파일공유자료
        setFilesLayout("파일공유자료", mLlcfrcfileList, item.get("gnr_file_list"));
        //회의결과자료
        setFilesLayout("회의결과자료", mLlcfrcRestList, item.get("rest_file_list"));
    }

    public void setFilesLayout(String title, LinearLayout targetLayout, Object object) {

        final List<LinkedTreeMap<String, String>> filesList = (List<LinkedTreeMap<String, String>>)object;
        if(filesList == null || (filesList != null && filesList.size() <= 0)) {
            targetLayout.setVisibility(View.GONE);
        } else {
            Log.e(TAG, "fileList size ="+filesList.size());
            targetLayout.setVisibility(View.VISIBLE);
            targetLayout.removeAllViews();
            //addTitleView
            LinearLayout.LayoutParams tvParam = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            tvParam.setMargins(0, DisplayUtil.dip2px(getActivity(), 12), 0, DisplayUtil.dip2px(getActivity(), 10));

            TextView tvTitle = new TextView(getActivity());
            tvTitle.setLayoutParams(tvParam);
            if (Build.VERSION.SDK_INT < 23) {
                tvTitle.setTextAppearance(getActivity(), android.R.style.TextAppearance_Material_Medium);
            } else {
                tvTitle.setTextAppearance(android.R.style.TextAppearance_Material_Medium);
            }
            tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            tvTitle.setTextColor(getResources().getColor(R.color.text_color_black));
            tvTitle.setText(title);

            targetLayout.addView(tvTitle);

            //addFilesView
            for (int i=0; i<filesList.size(); i++) {
                final LinkedTreeMap<String, String> fileMap = filesList.get(i);
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View fileView = inflater.inflate(R.layout.view_item_file, null);

                ImageView ivIcFileExt = (ImageView) fileView.findViewById(R.id.iv_ic_file_ext);
                TextView tvFileNm = (TextView) fileView.findViewById(R.id.tv_file_nm);

                //확장자에 따른 아이콘 변경처리
                ivIcFileExt.setImageResource(R.drawable.ic_file_doc);
                String fileNm = FormatUtil.getStringValidate(fileMap.get("file_nm"));
                tvFileNm.setText(fileNm);
                FileUtil.setFileExtIcon(ivIcFileExt, fileNm);
                final String fileExt = FileUtil.getFileExtsion(fileNm);
                final String downloadURL = I2UrlHelper.File.getDownloadFile(FormatUtil.getStringValidate(fileMap.get("file_id")));

                fileView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {
                            Intent intent = null;
                            if ("Y".equals(FormatUtil.getStringValidate(fileMap.get("conv_yn")))) {
                                //i2viewer 연동 (문서중 conv_yn='Y'값만)
                                intent = IntentUtil.getI2ViewerIntent(
                                        FormatUtil.getStringValidate(fileMap.get("file_id")),
                                        FormatUtil.getStringValidate(fileMap.get("file_nm")));
                                getActivity().startActivity(intent);
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
                            getActivity().startActivity(intent);
                        }catch (ActivityNotFoundException e) {
                            Toast.makeText(getActivity(), "I2Viewer앱이 설치되어 있지 않습니다.\n설치후 다시 시도해주시기 바랍니다.", Toast.LENGTH_LONG).show();
                            //TODO 다이알로그 문의 및 앱 설치 URL연결처리
                        }
                        }
                    });

                    targetLayout.addView(fileView);
                }

            }
    }



}
