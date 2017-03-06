package com.i2max.i2smartwork.utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.i2max.i2smartwork.R;
import com.i2max.i2smartwork.constant.AppConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FileUtil {

    // save file
    public final static String FILE_SAVE_HEAD = "i2connec_image_";
    public final static String FILE_SAMPLE_HEAD = "i2connect_sample_";
    public final static String FILE_EXT_JPG = "jpg";
    public final static String FILE_EXT_PNG = "png";
    public final static String FILE_EXT_GIF = "gif";

    public final static String STR_TAKE_PHOTO_TYPE = "take_photo_type";
    public final static String TAKE_PHOTO_TYPE_CROP = "CROP";

    public static String getRootPathFromExternalSD(String fileName) {
        File sdPath = Environment.getExternalStorageDirectory();
        StringBuilder sb = new StringBuilder();

        sb.append(sdPath.getAbsolutePath());
        if (TextUtils.isEmpty(fileName)) {
            sb.append(AppConstant.ROOT_PATH);
        } else {
            sb.append(fileName);
        }

        return sb.toString();
    }

    public static boolean makeDir(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return dir.exists() && dir.isDirectory();
    }

    public static String getSaveFileNameWithoutExt(String prefix) {
        String timeStamp = new SimpleDateFormat("yyyyMMddhhmmss", Locale.KOREA).format(new Date());
        return prefix + timeStamp;
    }

    public static String getSaveFileName(String prefix, String suffix) {
        String timeStamp = new SimpleDateFormat("yyyyMMddhhmmss", Locale.KOREA).format(new Date());
        return prefix + timeStamp + "." + suffix;
    }

    public static Uri changeContentUriToFileUri(Context context, Uri uri) {
        Log.e("android uri", " Scheme : " + uri.getScheme() + " Host : " + uri.getHost());

        Cursor cursor = context.getContentResolver().query(Uri.parse(uri.getScheme() + "://" + uri.getHost() + uri.getPath()), null, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
        Uri fileUri = Uri.fromFile(new File(path));
        cursor.close();

        Log.e("URI", uri.toString());
        return fileUri;
    }

    public static File createImageFile() throws IOException {
        File imageFile = null;
        String imageFileName = getSaveFileName(FILE_SAVE_HEAD, FILE_EXT_JPG);
        if (FileUtil.makeDir(AppConstant.ROOT_DOWNLOAD_IMAGE_PATH)) {
            imageFile = new File(AppConstant.ROOT_DOWNLOAD_IMAGE_PATH, imageFileName);
            Log.e("imageFile", "imageFile = " + imageFile.getAbsolutePath());
        }
        return imageFile;
    }

    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(Context context, Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // Check Google Drive.
        if(isGooglePhotoUri(uri)) {
            return uri.getLastPathSegment();
        }

        // DocumentProvider 1. 안드로이드 버전 체크
        // com.android.providers.media.documents/document/image :: uri로 전달 받는 경로가 킷캣으로 업데이트 되면서 변경 됨.
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }

        // content://media/external/images/media/....
        // 안드로이드 버전에 관계없이 경로가 com.android... 형식으로 집히지 않을 수 도 있음. [ 겔럭시S4 테스트 확인 ]
        if(isPathSDCardType(uri)) {

            final String selection = MediaStore.Images.Media._ID + "=?";
            final String[] selectionArgs = new String[] {
                    uri.getLastPathSegment()
            };

            return getDataColumn(context.getApplicationContext(),  MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
        }

        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    /**
     * URI 를 받아서 Column 데이터 접근.
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     * 킷캣에서 추가된  document식 Path
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     * 기본 경로 ( 킷캣 이전버전 ) Path : external/images/media/ID(1234...)
     */
    public static boolean isPathSDCardType(Uri uri) {

        return "external".equals(uri.getPathSegments().get(0));
    }

    /**
     * @param uri The Uri to check.
     * 구글 드라이브를 통한 업로드 여부 체크.
     */
    public static boolean isGooglePhotoUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String getMimeType(Context context, Uri uri) {
        String mimeType = null;

        Log.e("FileUtil", "uri = "+ uri.toString());
        String extension = MimeTypeMap.getFileExtensionFromUrl(getPath(context, uri));
        Log.e("FileUtil", "extension = "+ extension);
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        //미지원 마임타입
        if(mimeType == null || "null".equals(mimeType)) {
            if("hwp".equals(extension)) mimeType = "application/hwp";
            else if("doc".equals(extension) || "docx".equals(extension)) mimeType = "application/msword";
            else if("xls".equals(extension) || "xlsx".equals(extension)
                    || "xlsm".equals(extension)) mimeType = "application/vnd.ms-excel";
            else if("ppt".equals(extension) || "pptx".equals(extension)) mimeType = "application/vnd.ms-powerpoint";
            //else if(".gul".equals(fileExtension)) mimeType = ""; // 훈민정음은 mimeType이 없음
        }
        Log.e("FileUtil", "mimetype = "+ mimeType);
        return mimeType;
    }

    public static boolean isImageFile(Context context, Uri uri) {
        boolean result = false;

        String type = null;
        try {
            type = getMimeType(context, uri);
        } catch(Exception e) {
            type = "";
        }
        result = type.indexOf("image") > -1;

        return result;
    }

    public static Bitmap getBitmap(Context context, Uri uri) {

        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 1200000; // 1.2MP
            in = context.getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();


            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d("FileUtil.getBitmap", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

            Bitmap b = null;
            in = context.getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = b.getHeight();
                int width = b.getWidth();
                Log.d("FileUtil.getBitmap", "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x,
                        (int) y, true);
                b.recycle();
                b = scaledBitmap;

                System.gc();
            } else {
                b = BitmapFactory.decodeStream(in);
            }
            in.close();

            Log.d("FileUtil.getBitmap", "bitmap size - width: " + b.getWidth() + ", height: " +
                    b.getHeight());
            return b;
        } catch (IOException e) {
            Log.e("FileUtil.getBitmap", e.getMessage(), e);
            return null;
        }
    }


    public static Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }

    public static String getFileExtsion(String fileNm) {
        return fileNm.substring(fileNm.lastIndexOf(".")+1);
    }
    /**
     * 파일 확장자 이미지 처리
     * @param iv
     * @param fileNm
     */
    public static void setFileExtIcon(ImageView iv, String fileNm) {
        String ext = getFileExtsion(fileNm);
        Log.e("FileUtil", "file extsion = " + ext);

        if("pdf".equalsIgnoreCase(ext)){ //문서
            iv.setImageResource(R.drawable.ic_icon_file_pdf);
        } else if("doc".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_doc);
        } else if("docx".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_docx);
        } else if("ppt".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_ppt);
        } else if("pptx".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_pptx);
        } else if("xls".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_xls);
        } else if("xlsx".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_xlsx);
        } else if("txt".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_txt);
        } else if("hwp".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_hwp);
        } else if("gul".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_gul);
        } else if("ai".equalsIgnoreCase(ext)){ //이미지
            iv.setImageResource(R.drawable.ic_icon_file_ai);
        } else if("bmp".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_bmp);
        } else if("jpg".equalsIgnoreCase(ext) || "jpeg".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_jpg);
        } else if("png".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_png);
        } else if("mp3".equalsIgnoreCase(ext)){ //오디오
            iv.setImageResource(R.drawable.ic_icon_file_mp3);
        } else if("mp4".equalsIgnoreCase(ext)){  //동영상
            iv.setImageResource(R.drawable.ic_icon_file_mp4);
        } else if("avi".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_avi);
        } else if("fla".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_fla);
        } else if("wmv".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_wmv);
        } else if("html".equalsIgnoreCase(ext)){  //웹
            iv.setImageResource(R.drawable.ic_icon_file_html);
        } else if("css".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_css);
        } else if("js".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_js);
        } else if("php".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_php);
        } else if("zip".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_zip);
        } else if("rar".equalsIgnoreCase(ext)){
            iv.setImageResource(R.drawable.ic_icon_file_rar);
        } else {
            iv.setImageResource(R.drawable.ic_icon_file_etc);
        }
    }

    public static JSONObject loadJSONFromAsset(Context context, String fileName) {
        JSONObject obj = null;

        try {

            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");
            obj = new JSONObject(json);

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    public static Map<String, Object> readMenuFromAsset(Context context, String fileName) {
        Map<String, Object> status = null;

        try {

            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");

            Gson gson = new Gson();
            status = new HashMap<String, Object>();
            status = (Map<String, Object>) gson.fromJson(json, status.getClass());


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return status;
    }
}
