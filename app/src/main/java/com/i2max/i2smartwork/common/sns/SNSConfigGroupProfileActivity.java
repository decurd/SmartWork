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

package com.i2max.i2smartwork.common.sns;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.i2api.I2ConnectApi;
import com.i2max.i2smartwork.i2api.I2ResponseParser;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.FileUtil;
import com.i2max.i2smartwork.utils.FormatUtil;
import com.soundcloud.android.crop.Crop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SNSConfigGroupProfileActivity extends BaseAppCompatActivity {
    static String TAG = SNSConfigGroupProfileActivity.class.getSimpleName();

    protected final int REQUEST_GALLERY_KITKAT_INTENT_CALLED = 2003; //키캣 이상 파일 열기 처리
    protected final int REQUEST_GALLERY = 2004; //키캣 이전버젼 파일 열기처리
    protected final int REQUEST_TAKE_PHOTO = 2005;

    public static final String GRP_ID = "tar_grp_id";
    public static final String GRP_NM = "tar_grp_nm";

    protected String mTarGrpID, mTarGrpNm;

    protected CircleImageView mCivCrtUsrPhoto;
    protected TextView tvGrpNM;
    protected EditText etGrpNm, etGrpIntro;
    protected RadioGroup rgIsPublic;

    protected boolean isProfileEdited;
    protected String originNm, originIntro;
    protected Map<String, Object> mapProfileImgInfo = new HashMap<String, Object>();

    private String mPhotoPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_config_group_profile);

        Intent intent = getIntent();
        mTarGrpID = intent.getStringExtra(GRP_ID);
        mTarGrpNm = intent.getStringExtra(GRP_NM);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        mCivCrtUsrPhoto = (CircleImageView)findViewById(R.id.civ_grp_photo);
        tvGrpNM = (TextView)findViewById(R.id.tv_grp_nm);
        etGrpNm = (EditText) findViewById(R.id.et_grp_nm);
        etGrpIntro = (EditText) findViewById(R.id.et_grp_intro);
        rgIsPublic = (RadioGroup) findViewById(R.id.rg_is_public);

        tvGrpNM.setText(mTarGrpNm);
        etGrpNm.setText(mTarGrpNm);

        Button btnProfileImg = (Button) findViewById(R.id.btn_profile_img);
        btnProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SNSConfigGroupProfileActivity.this);
                builder.setTitle("이미지 선택");

                final CharSequence[] choiceList =
                        {"사진찍기", "앨범가져오기", "취소"};

                builder.setItems(choiceList,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: // 사진찍기
                                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                                            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                            mPhotoPath = String.format("%d.png", System.currentTimeMillis());
                                            File file = new File(path, mPhotoPath);
                                            if (!path.exists()) path.mkdirs();

                                            Uri uri = Uri.fromFile(file);
                                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                                        }
                                        break;

                                    case 1: // 앨범가져오기
                                        if (Build.VERSION.SDK_INT < 19) {
                                            Intent intent = new Intent();
                                            intent.setType("image/*");
                                            intent.setAction(Intent.ACTION_GET_CONTENT);
                                            startActivityForResult(Intent.createChooser(intent, "사진 선택"), REQUEST_GALLERY);
                                        } else {
                                            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                                            intent.setType("image/*");
                                            startActivityForResult(intent, REQUEST_GALLERY_KITKAT_INTENT_CALLED);
                                        }
                                        break;
                                }
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });

        Button btnCancel = (Button) findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isProfileEdited && !isEditedProfileInfo() ) {
                    uplaodProfileImageFiles();
                } else {
                    if(isEditedProfileInfo()) {
                        updateGroupProfile();
                    } else {
                        DialogUtil.showInformationDialog(SNSConfigGroupProfileActivity.this, "변경된 내용이 없습니다.");
                    }
                }

            }
        });

        I2ConnectApi.requestJSON(SNSConfigGroupProfileActivity.this, I2UrlHelper.SNS.getViewSnsGroup(mTarGrpID))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSnsGroup onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSnsGroup onError" + e.getMessage());
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSConfigGroupProfileActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSnsGroup onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);

                            try {
                                etGrpNm.setText(statusInfo.getString("grp_nm"));
                                originNm = etGrpNm.getText().toString();

                                if (!statusInfo.isNull("grp_intro"))
                                    etGrpIntro.setText(statusInfo.getString("grp_intro"));

                                originIntro = etGrpIntro.getText().toString();

                                if (statusInfo.getString("open_yn").equals("Y")) {
                                    rgIsPublic.check(R.id.rb_public_on);
                                } else {
                                    rgIsPublic.check(R.id.rb_public_off);
                                }

                                Glide.with(mCivCrtUsrPhoto.getContext())
                                        .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(statusInfo.get("grp_photo_url"))))
                                        .error(R.drawable.ic_no_grp_photo)
                                        .fitCenter()
                                        .into(mCivCrtUsrPhoto);

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

    public boolean isEditedProfileInfo() {
        boolean isEdited;
        isEdited = !(originNm.equals(etGrpNm.getText().toString()) && originIntro.equals(etGrpIntro.getText().toString()));
        return isEdited;
    }

    public void updateGroupProfile() {
        String openYN = null;
        if (rgIsPublic.indexOfChild(findViewById(rgIsPublic.getCheckedRadioButtonId())) == 0) {
            openYN = "Y";
        } else {
            openYN = "N";
        }

        I2ConnectApi.requestJSON(SNSConfigGroupProfileActivity.this, I2UrlHelper.SNS.updateSnsGroup(
                mTarGrpID, etGrpNm.getText().toString(), etGrpIntro.getText().toString(), openYN))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.updateSnsGroup onCompleted");
                        if (isProfileEdited) {
                            uplaodProfileImageFiles();
                        } else {
                            Toast.makeText(getBaseContext(), "그룹 프로필 정보가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.updateSnsGroup onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSConfigGroupProfileActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.updateSnsGroup onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {

                        } else {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void updateSnsGroupImage() {

        I2ConnectApi.requestJSON(SNSConfigGroupProfileActivity.this, I2UrlHelper.SNS.updateSnsGroupImage(
                mTarGrpID, (String)mapProfileImgInfo.get("phscl_file_nm"), "0", "0",
                (String) mapProfileImgInfo.get("profile_img_w"), (String) mapProfileImgInfo.get("profile_img_h")))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        DialogUtil.removeCircularProgressDialog();
                        Log.d(TAG, "I2UrlHelper.SNS.updateSnsGroupImage onCompleted");
                        Toast.makeText(getBaseContext(), "그룹 프로필 사진이 정상적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();

//                        if (originNm.equals(etGrpNm.getText().toString()) && originIntro.equals(etGrpIntro.getText().toString())) {
//                            Toast.makeText(getBaseContext(), "그룹 프로필 사진이 정상적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
//                            finish();
//                        } else {
//                            updateGroupProfile();
//                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.updateSnsGroupImage onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSConfigGroupProfileActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.updateSnsGroupImage onNext");
                        if (!I2ResponseParser.checkReponseStatus(jsonObject)) {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void uplaodProfileImageFiles() {

        DialogUtil.showCircularProgressDialog(SNSConfigGroupProfileActivity.this);

        Uri uri = (Uri)mapProfileImgInfo.get("profile_img_uri");
        File file = new File(FileUtil.getPath(this, uri));

        String fileNm = (String)mapProfileImgInfo.get("profile_img_nm");
        String mimeType = FileUtil.getMimeType(this, uri);

//        Log.d(TAG, "uplaodProfileImageFiles file = " + file.exists());
//        Log.d(TAG, "uplaodProfileImageFiles filePath = " + FileUtil.getPath(this, uri));
//        Log.d(TAG, "uplaodProfileImageFiles fileName = " + fileNm + ", mimeType =" + mimeType +  ", fileSize(kb) =" + file.length()/1024);

        I2ConnectApi.uploadFile(SNSConfigGroupProfileActivity.this, fileNm, mimeType, file)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "uploadFile onCompleted");

                        updateSnsGroupImage();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "uploadFile onError");
                        DialogUtil.removeCircularProgressDialog();
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(SNSConfigGroupProfileActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "uploadFile onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);
                            JSONArray statusInfoArray = I2ResponseParser.getJsonArray(statusInfo, "file_list");

                            try {
                                if (statusInfoArray.length()>0) {
                                    mapProfileImgInfo.put("attach_nm", statusInfoArray.getJSONObject(0).getString("file_nm"));
                                    mapProfileImgInfo.put("phscl_file_nm", statusInfoArray.getJSONObject(0).getString("phscl_file_nm"));
                                    mapProfileImgInfo.put("file_tp_cd", statusInfoArray.getJSONObject(0).getString("file_tp_cd"));
                                    mapProfileImgInfo.put("file_ext", statusInfoArray.getJSONObject(0).getString("file_ext"));
                                    mapProfileImgInfo.put("file_id", statusInfoArray.getJSONObject(0).getString("file_id"));
                                    mapProfileImgInfo.put("file_size", statusInfoArray.getJSONObject(0).getString("file_size"));
                                    mapProfileImgInfo.put("file_path", statusInfoArray.getJSONObject(0).getString("file_path"));
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

    public void addProfileFromPicture(Uri uri) {
        //대용량일 경우 리사이징 표시 처리
        Bitmap bitmap = FileUtil.getBitmap(this, uri);

        String w,h;
        w = String.format("%d", bitmap.getWidth());
        h = String.format("%d", bitmap.getHeight());

        mapProfileImgInfo.put("profile_img_nm", FileUtil.getFileName(this, uri));
        mapProfileImgInfo.put("profile_img_uri", uri);
        mapProfileImgInfo.put("profile_img_w", w);
        mapProfileImgInfo.put("profile_img_h", h);

        Glide.with(mCivCrtUsrPhoto.getContext())
                .load(uri)
                .error(R.drawable.ic_no_usr_photo)
                .fitCenter()
                .into(mCivCrtUsrPhoto);

//        mCivCrtUsrPhoto.setImageURI(uri);
        isProfileEdited = true;
    }

    private void beginCrop(Uri source) {
        String fileNm = FileUtil.getFileName(this, source);
        //String fileNm = "cropped.png";
        File file = new File(getCacheDir(), fileNm);
        if (file.exists())
            file.delete();

        Uri destination = Uri.fromFile(new File(getCacheDir(), fileNm));
        Crop.of(source, destination).asSquare().start(this);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case REQUEST_GALLERY:
                Uri uri = result.getData();
                beginCrop(uri);
                break;
            case REQUEST_TAKE_PHOTO:
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File file = new File(path, mPhotoPath);

                Uri takePhotoUri = Uri.fromFile(file);
                beginCrop(takePhotoUri);

                break;
            case REQUEST_GALLERY_KITKAT_INTENT_CALLED :
                final int takeFlags = result.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                Uri takeUri = result.getData();
                getContentResolver().takePersistableUriPermission(takeUri, takeFlags);
                beginCrop(takeUri);

                break;

            case Crop.REQUEST_CROP:
                addProfileFromPicture(Crop.getOutput(result));
                break;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
