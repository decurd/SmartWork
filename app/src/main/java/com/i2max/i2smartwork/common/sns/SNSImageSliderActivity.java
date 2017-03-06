package com.i2max.i2smartwork.common.sns;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.component.BaseAppCompatActivity;
import com.i2max.i2smartwork.i2api.I2UrlHelper;
import com.i2max.i2smartwork.utils.DialogUtil;
import com.i2max.i2smartwork.utils.ExtendedViewPager;
import com.i2max.i2smartwork.utils.TouchImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

public class SNSImageSliderActivity extends BaseAppCompatActivity {
    static String TAG = SNSImageSliderActivity.class.getSimpleName();

    public static final String IMAGE_INFO_MAP = "image_info_map";
    public static final String IMAGE_NM_LIST = "image_nm_list";
    public static final String IMAGE_URL_LIST = "image_url_list";

    protected HashMap<String,List<String>> imageInfoMap;
    protected ExtendedViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sns_image_slider);

        Intent intent = getIntent();
        imageInfoMap = (HashMap<String,List<String>>)intent.getSerializableExtra(IMAGE_INFO_MAP);

//        List<String> imageNmList = imageInfoMap.get(IMAGE_NM_LIST);
        List<String> imageURLList = imageInfoMap.get(IMAGE_URL_LIST);

        mViewPager = (ExtendedViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new TouchImageAdapter(imageURLList));

        ImageView ivDownload = (ImageView) findViewById(R.id.iv_download);
        ivDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtil.showConfirmDialog(SNSImageSliderActivity.this, "알림", "이미지를 저장하시겠습니까?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveImageFile();
                            }
                        });
            }
        });

        ImageView ivClose = (ImageView) findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void saveImageFile() {

        List<String> imageURLList = imageInfoMap.get(IMAGE_URL_LIST);
        String filePath = imageURLList.get( mViewPager.getCurrentItem() );
        final String fileName = filePath.split("=")[1] + ".jpg";

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        File file = new File(
                                Environment.getExternalStorageDirectory().getPath()
                                        + File.separator + fileName);
                        try {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                            ostream.close();

                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {}

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };

        Picasso pcs = I2UrlHelper.buildPicassoAddTokenHeader(this);
        RequestCreator rq = pcs.load(filePath);
        rq.into(target);

        DialogUtil.showCircularProgressDialog(SNSImageSliderActivity.this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                DialogUtil.removeCircularProgressDialog();
                DialogUtil.showInformationDialog(SNSImageSliderActivity.this, "이미지 저장이 완료되었습니다.");
            }
        }, 4000);
    }

    static class TouchImageAdapter extends PagerAdapter {

        private List<String> imageURLList;

        public TouchImageAdapter(List<String> list) {
            imageURLList = list;
        }

        @Override
        public int getCount() {
            return imageURLList.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            final TouchImageView img = new TouchImageView(container.getContext());

            Picasso pcs = I2UrlHelper.buildPicassoAddTokenHeader(container.getContext());
            RequestCreator rq = pcs.load(imageURLList.get(position));
            rq.into(img, new Callback() {
                public void onSuccess() {
                    img.setZoom(1);
                }

                public void onError() {
                }
            });

            container.addView(img, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

            return img;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

}
