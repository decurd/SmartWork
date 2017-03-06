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

package com.i2max.i2smartwork.common.work;

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
import android.widget.ProgressBar;
import android.widget.TextView;

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

public class WorkDetailViewFragment extends Fragment {
    static String TAG = WorkDetailViewFragment.class.getSimpleName();

    public CircleImageView mCivCrtUsrPhoto;
    public LinearLayout mLlDocFileList;
    protected TextView mTvTtl, mTvRefeWorkTtl, mTvCrtDttm, mTvCrtUsrNm, mTvGrpCdNm, mTvGrpDtlNm, mTvPgrsRate, mTvStNm, mTvStartDt, mTvEndDt, mTvCntn;
    protected ProgressBar mPbPgrsRate;

    private String mTarObjTp, mTarObjId, mTarObjTtl, mTarCrtUsrID, mCurObjTp, mCurObjId, mCurCrtUsrID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_work_detail, container, false);
        mTvTtl = (TextView) view.findViewById(R.id.tv_ttl);
        mTvRefeWorkTtl = (TextView) view.findViewById(R.id.tv_refe_work_ttl);
        mTvCrtDttm = (TextView) view.findViewById(R.id.tv_crt_dttm);
        mCivCrtUsrPhoto = (CircleImageView) view.findViewById(R.id.civ_crt_usr_photo);
        mTvCrtUsrNm = (TextView) view.findViewById(R.id.tv_crt_usr_nm);
        mTvGrpCdNm = (TextView) view.findViewById(R.id.tv_grp_cd_nm);
        mTvGrpDtlNm = (TextView) view.findViewById(R.id.tv_grp_dtl_nm);
        mTvStNm = (TextView) view.findViewById(R.id.tv_st_nm);
        mPbPgrsRate = (ProgressBar) view.findViewById(R.id.pb_pgrs_rate);
        mTvPgrsRate = (TextView) view.findViewById(R.id.tv_pgrs_rate);
        mTvStartDt = (TextView) view.findViewById(R.id.tv_start_dt);
        mTvEndDt = (TextView) view.findViewById(R.id.tv_end_dt);
        mTvCntn = (TextView) view.findViewById(R.id.tv_cntn);

        mLlDocFileList = (LinearLayout) view.findViewById(R.id.ll_doc_file_list);

        return view;
    }

    public void setViewData(LinkedTreeMap<String, Object> statusInfo) {
        final String crtUsrId = FormatUtil.getStringValidate(statusInfo.get("crt_usr_id"));
        final String crtUsrNm = FormatUtil.getStringValidate(statusInfo.get("crt_usr_nm"));
        final String tarObjTp = FormatUtil.getStringValidate(statusInfo.get("tar_obj_tp_cd"));
        final String tarObjId = FormatUtil.getStringValidate(statusInfo.get("up_work_id"));
        final String tarUsrId = FormatUtil.getStringValidate(statusInfo.get("tar_usr_id"));

        mTvTtl.setText(FormatUtil.getStringValidate(statusInfo.get("ttl")));
        //수정이력 있음, 수정자 기준표시, 수정없음, 작성자 기준표시
        if("".equals(FormatUtil.getStringValidate(statusInfo.get("mod_dttm")))) {
            mTvCrtDttm.setText(FormatUtil.getFormattedDateTime(FormatUtil.getStringValidate(statusInfo.get("crt_dttm")))); //만든시간 표시
        } else {
            mTvCrtDttm.setText(FormatUtil.getFormattedDateTime(FormatUtil.getStringValidate(statusInfo.get("mod_dttm")))); //수정시간 표시
        }

        Glide.with(mCivCrtUsrPhoto.getContext())
                .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(statusInfo.get("crt_usr_photo"))))
                .error(R.drawable.ic_no_usr_photo)
                .fitCenter()
                .into(mCivCrtUsrPhoto);

        mTvCrtUsrNm.setText(crtUsrNm);

        mTvGrpCdNm.setText(FormatUtil.getStringValidate(statusInfo.get("grp_cd_nm")));
        mTvGrpDtlNm.setText(FormatUtil.getStringValidate(statusInfo.get("grp_dtl_cd_nm")));


        int intPgrsRate = 0;
        String pgrsRate = FormatUtil.getStringValidate(statusInfo.get("pgrs_rate"));
        Log.e("", "pgrsRate = " + pgrsRate);
        if(!TextUtils.isEmpty(pgrsRate) && !"null".equals(pgrsRate)) intPgrsRate = (int)Float.parseFloat(pgrsRate);
        mPbPgrsRate.setProgress(intPgrsRate);
        mTvPgrsRate.setText(intPgrsRate+"%");

        mTvStNm.setText(FormatUtil.getStringValidate(statusInfo.get("pgrs_st")));


        mTvStartDt.setText(FormatUtil.getFormattedDate5(FormatUtil.getStringValidate(statusInfo.get("start_dt")))); //상시일시 표시
        mTvEndDt.setText(FormatUtil.getFormattedDate5(FormatUtil.getStringValidate(statusInfo.get("end_dt")))); //완료일시 표시

        //첨부파일
        setFilesLayout("첨부파일", mLlDocFileList, statusInfo.get("doc_file_list"));

        mCivCrtUsrPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SNSDetailProfileActivity.class);
                intent.putExtra(SNSDetailProfileActivity.USR_ID, crtUsrId);
                intent.putExtra(SNSDetailProfileActivity.USR_NM, crtUsrNm);
                getActivity().startActivity(intent);
            }
        });

        mTvCntn.setText(FormatUtil.getStringValidate(statusInfo.get("cntn")));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void loadRecyclerView(String tarObjTp, String tarObjId, String tarObjTtl, String tarCrtUsrId, String curObjTp, String curObjId, String curCrtUsrId) {
        mTarObjTp = tarObjTp;
        mTarObjId = tarObjId;
        mTarObjTtl = tarObjTtl;
        mTarCrtUsrID = tarCrtUsrId;
        mCurObjTp = curObjTp;
        mCurObjId = curObjId;
        mCurCrtUsrID = curCrtUsrId;

        I2ConnectApi.requestJSON2Map(getActivity(), I2UrlHelper.Work.getViewSnsWork(mCurObjId))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Map<String, Object>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.Memo.getViewSnsMemo onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.Memo.getViewSnsMemo onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(getActivity(), e);
                    }

                    @Override
                    public void onNext(Map<String, Object> status) {
                        Log.d(TAG, "I2UrlHelper.Memo.getViewSnsMemo onNext");
                        LinkedTreeMap<String, Object> statusInfo = (LinkedTreeMap<String, Object>) status.get("statusInfo");
                        setViewData(statusInfo);
                    }
                });
    }



    public void setFilesLayout(String title, LinearLayout targetLayout, Object object) {
        final List<LinkedTreeMap<String, String>> filesList = (List<LinkedTreeMap<String, String>>)object;
        if(filesList == null || filesList.size() <= 0) {
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
                final String downloadURL = I2UrlHelper.File.getDownloadFile(
                        FormatUtil.getStringValidate(fileMap.get("file_id")));

                fileView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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


                            }
                        });

                        targetLayout.addView(fileView);
                    }

                }
            }



}
