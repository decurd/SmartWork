package com.i2max.i2smartwork.component;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.common.conference.ConferenceWriteActivity;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.FileUtil;
import com.i2max.i2smartwork.utils.FormatUtil;
import com.i2max.i2smartwork.utils.IntentUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * TODO write 페이지 parent class
 */
public class BaseWriteActivity extends BaseAppCompatActivity {
    protected static String TAG = ConferenceWriteActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() { super.onPause(); }

    public void setFilesLayout(Context context, LinearLayout targetLayout, JSONArray array) {
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
                    final String downloadURL = I2UrlHelper.File.getDownloadFile(fileId);
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
                                    intent = IntentUtil.getI2ViewerIntent(
                                            FormatUtil.getStringValidate(fileId),
                                            FormatUtil.getStringValidate(fileNm));
                                } else if ("mp4".equalsIgnoreCase(fileExt) || "fla".equalsIgnoreCase(fileExt)) { //video
                                    intent = IntentUtil.getVideoPlayIntent(downloadURL);
                                } else {
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadURL));
                                    Bundle bundle = new Bundle();
                                    bundle.putString("Authorization", I2UrlHelper.getTokenAuthorization()); //인증처리
                                    intent.putExtra(Browser.EXTRA_HEADERS, bundle);
                                    Log.d(TAG, "intent:" + intent.toString());
                                }
                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                                Toast.makeText(BaseWriteActivity.this, "I2뷰어 앱이 설치되어 있지않습니다.\nI2뷰어를 설치하시기 바랍니다.", Toast.LENGTH_LONG).show();
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
}

