package com.i2max.i2smartwork.common.sns;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.ImageView;
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
import com.i2max.i2smartwork.utils.PreferenceUtil;

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

public class SNSConfigProfileActivity extends BaseAppCompatActivity {
    static String TAG = SNSConfigProfileActivity.class.getSimpleName();

    protected final int REQUEST_GALLERY_KITKAT_INTENT_CALLED = 2003; //키캣 이상 파일 열기 처리
    protected final int REQUEST_GALLERY = 2004; //키캣 이전버젼 파일 열기처리
    protected final int REQUEST_TAKE_PHOTO = 2005;

    protected CircleImageView civUsrPhoto;
    protected ImageView ivProfileBg;
    protected TextView tvUsrNM, tvPosNm, tvDeptNm, tvUsrID, tvMobile, tvPhone, tvDept;
    protected EditText etIntro;

    protected boolean isProfileEdited;
    protected String originIntro;
    protected Map<String, Object> mapProfileImgInfo = new HashMap<String, Object>();

    private String mPhotoPath;// = "i2_take_picture_profile.png";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_config_profile);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        civUsrPhoto = (CircleImageView)findViewById(R.id.civ_usr_photo);
        ivProfileBg = (ImageView)findViewById(R.id.iv_profile_bg);
        tvUsrNM = (TextView)findViewById(R.id.tv_usr_nm);
        tvPosNm = (TextView)findViewById(R.id.tv_pos_nm);
        tvDeptNm = (TextView)findViewById(R.id.tv_dept_nm);
        tvUsrID = (TextView)findViewById(R.id.tv_usr_id);
        tvMobile = (TextView)findViewById(R.id.tv_mobile);
        tvPhone = (TextView)findViewById(R.id.tv_phone);
        tvDept = (TextView)findViewById(R.id.tv_dept);
        etIntro = (EditText) findViewById(R.id.et_intro);

        Button btnProfileImg = (Button) findViewById(R.id.btn_profile_img);
        btnProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SNSConfigProfileActivity.this);
                builder.setTitle("이미지 선택");

                final CharSequence[] choiceList =
                        {"사진찍기", "앨범가져오기" , "취소" };

                builder.setItems(choiceList,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: // 사진찍기
//                                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
//                                        }
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
                if (isProfileEdited) {
                    DialogUtil.showConfirmDialog(SNSConfigProfileActivity.this, "알림", "변경된 프로필이 있습니다. 취소하시겠습니까?",
                            new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            }, new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                } else {
                    finish();
                }
            }
        });
        Button btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isProfileEdited) {
                    uplaodProfileImageFiles();
                } else {
                    updateProfileIntro();
                }
            }
        });

        tvUsrID.setText(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_LOGIN_ID));

        I2ConnectApi.requestJSON(SNSConfigProfileActivity.this, I2UrlHelper.SNS.getViewSNSUserProfile(PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID)))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSNSUser onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSNSUser onError");
                        //Error dialog 표시
                        DialogUtil.showErrorDialog(SNSConfigProfileActivity.this, e.getMessage());
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.getViewSNSUser onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            JSONObject statusInfo = I2ResponseParser.getStatusInfo(jsonObject);

                            try {
                                tvUsrNM.setText(FormatUtil.getStringValidate(statusInfo.getString("usr_nm")));
                                tvPosNm.setText(FormatUtil.getStringValidate(statusInfo.getString("pos_nm")));
                                tvDeptNm.setText(FormatUtil.getStringValidate(statusInfo.getString("dept_nm")));
                                tvMobile.setText(FormatUtil.getStringValidate(statusInfo.getString("phn_num")));
                                tvPhone.setText(FormatUtil.getStringValidate(statusInfo.getString("tel_num")));
                                tvDept.setText(FormatUtil.getStringValidate(statusInfo.getString("dept_nm")));
                                if (!statusInfo.isNull("self_intro")) {
                                    etIntro.setText(statusInfo.getString("self_intro"));
                                    etIntro.setSelection(etIntro.getText().length());
                                }

                                originIntro = etIntro.getText().toString();

                                Glide.with(civUsrPhoto.getContext())
                                        .load(I2UrlHelper.File.getUsrImage(FormatUtil.getStringValidate(statusInfo.getString("usr_photo_url"))))
                                        .error(R.drawable.ic_no_usr_photo)
                                        .fitCenter()
                                        .into(civUsrPhoto);

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

    public void updateProfileIntro() {
        I2ConnectApi.requestJSON(SNSConfigProfileActivity.this, I2UrlHelper.SNS.updateSnsUserProfile(
                PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID),"", "", "", etIntro.getText().toString()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "I2UrlHelper.SNS.updateSnsUserProfile onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "I2UrlHelper.SNS.updateSnsUserProfile onError");
                        //Error dialog 표시
                        DialogUtil.showErrorDialog(SNSConfigProfileActivity.this, e.getMessage());
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.updateSnsUserProfile onNext");
                        if (I2ResponseParser.checkReponseStatus(jsonObject)) {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    public void updateProfileImage() {

        I2ConnectApi.requestJSON(SNSConfigProfileActivity.this, I2UrlHelper.SNS.updateProfilePhoto(
                PreferenceUtil.getInstance().getString(PreferenceUtil.PREF_USR_ID),
                (String) mapProfileImgInfo.get("phscl_file_nm"), "0", "0",
                (String) mapProfileImgInfo.get("profile_img_w"), (String) mapProfileImgInfo.get("profile_img_h")))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        DialogUtil.removeCircularProgressDialog();
                        Log.d(TAG, "I2UrlHelper.SNS.updateProfilePhoto onCompleted");
                        if (originIntro.equals(etIntro.getText().toString())) {
                            Toast.makeText(getBaseContext(), "프로필사진이 정상적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            updateProfileIntro();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        DialogUtil.removeCircularProgressDialog();
                        Log.d(TAG, "I2UrlHelper.SNS.updateProfilePhoto onError");
                        //Error dialog 표시
                        e.printStackTrace();
                        DialogUtil.showErrorDialogWithValidateSession(SNSConfigProfileActivity.this, e);
                    }

                    @Override
                    public void onNext(JSONObject jsonObject) {
                        Log.d(TAG, "I2UrlHelper.SNS.updateProfilePhoto onNext");
                        if (!I2ResponseParser.checkReponseStatus(jsonObject)) {
                            Toast.makeText(getBaseContext(), I2ResponseParser.getStatusMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void uplaodProfileImageFiles() {

        DialogUtil.showCircularProgressDialog(SNSConfigProfileActivity.this);

        Uri uri = (Uri)mapProfileImgInfo.get("profile_img_uri");
        File file = new File(FileUtil.getPath(this, uri));

        String fileNm = (String)mapProfileImgInfo.get("profile_img_nm");
        String mimeType = FileUtil.getMimeType(this, uri);

        Log.d(TAG, "uplaodProfileImageFiles file = " + file.exists());
        Log.d(TAG, "uplaodProfileImageFiles filePath = " + FileUtil.getPath(this, uri));
        Log.d(TAG, "uplaodProfileImageFiles fileName = " + fileNm + ", mimeType =" + mimeType +  ", fileSize(kb) =" + file.length()/1024);

        I2ConnectApi.uploadFile(SNSConfigProfileActivity.this, fileNm, mimeType, file)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JSONObject>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "uploadFile onCompleted");

                        updateProfileImage();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "uploadFile onError");
                        DialogUtil.removeCircularProgressDialog();
                        e.printStackTrace();
                        //Error dialog 표시
                        DialogUtil.showErrorDialogWithValidateSession(SNSConfigProfileActivity.this, e);
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

        String fileNm = (String)mapProfileImgInfo.get("profile_img_nm");
        String mimeType = FileUtil.getMimeType(this, uri);
        Log.d(TAG, "uri = " + uri);
        Log.d(TAG, w + " / " + h + " uplaodProfileImageFiles filePath = " + FileUtil.getPath(this, uri));
        Log.d(TAG, "uplaodProfileImageFiles fileName = " + fileNm + ", mimeType =" + mimeType);

        Glide.with(civUsrPhoto.getContext())
                .load(uri)
                .error(R.drawable.ic_no_usr_photo)
                .fitCenter()
                .into(civUsrPhoto);

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case REQUEST_GALLERY:
                Uri uri = data.getData();
                beginCrop(uri);
                break;
            case REQUEST_TAKE_PHOTO:
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File file = new File(path, mPhotoPath);

                Uri takePhotoUri = Uri.fromFile(file);
                beginCrop(takePhotoUri);

                break;
            case REQUEST_GALLERY_KITKAT_INTENT_CALLED :
                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                Uri takeUri = data.getData();
                getContentResolver().takePersistableUriPermission(takeUri, takeFlags);
                beginCrop(takeUri);

                break;

            case Crop.REQUEST_CROP:
                addProfileFromPicture(Crop.getOutput(data));
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
