/**
 * 외부 앱 연동 Util
 * FileSelector : 파일 탐색기
 * VideoPlayer : 동영상 재생
 */
package com.i2max.i2smartwork.utils;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.i2max.i2smartwork.i2api.I2UrlHelper;

import java.io.File;

public class IntentUtil {

	/**
	 * url 통핸 Intent 생성
	 * @param url
	 * @return Intent
	 *
	 */
	public static Intent getIntentByUrl(String url) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.addCategory(Intent.CATEGORY_BROWSABLE);
		intent.setData(Uri.parse(url));
		return intent;
	}

	/**
	 * i2conferencer 파일뷰어 앱 연동 함수
	 *
	 * @param cfrcId
	 * @return i2conferencer인텐트
	 */
	public static Intent getI2ConferenceIntent(String cfrcId) {
		String url = I2UrlHelper.I2App.getI2conferencePakageUrl(cfrcId);
		Log.e("getI2cfrcPakageUrl", "I2cfrc url =" + url);
		return getIntentByUrl(url);
	}

	/**
	 * i2livechat 연동
	 * @param type 유저타입 일반 user | 전문가 master | 웹통한 direct
	 * @param tarId 상담ID (네이티브에서는 빈값)
	 * @return
	 */
	public static Intent getI2LiveChatIntent(String type, String tarId) {
		String url = I2UrlHelper.I2App.getI2livechatPakageUrl(type, tarId);
		Log.e("getI2LiveChatIntent", "livechat type =" + type+" / tar_id =" + tarId);
		return getIntentByUrl(url);
	}

	/**
	 * i2viewer 파일뷰어 앱 연동 함수
	 *
	 * @param fileId
	 * @param fileNm
	 * @return	i2viewer인텐트
	 */
	public static Intent getI2ViewerIntent(String fileId, String fileNm) {
		String url = I2UrlHelper.I2App.getI2viewerPakageUrl(fileId, fileNm);
		Log.e("getI2viewerPakageUrl", "I2viewer url =" + url);
		return getIntentByUrl(url);
	}

	/**
	 * 비디오 플레이 인텐트
	 *
	 * @param videoPath
	 * @return
	 */
	public static Intent getVideoPlayIntent(String videoPath) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse(videoPath), "video/*");
		return intent;
	}

	/**
	 * 파일 선택 앱 기동 처리
	 */
	public static Intent getFile(String filePath) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setData(Uri.parse("file://" + filePath));
		intent.setType("file/*");
		return intent;
	}

	public static Intent getFileDefault() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		Intent i = Intent.createChooser(intent, "File");
		return i;
	}
	
	/**
	 * doesn't work sdk4.3 2014 samsung galaxy 
	 */
	public static Intent getSamsungFileExplorer() {
		Intent intent = new Intent();
		intent.setAction("com.sec.android.app.myfiles.PICK_DATA");
		intent.putExtra("CONTENT_TYPE", "*/*");
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setData(Uri.parse("file://"));
		return intent;
	}

	public static Intent getFileExplorer() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		Intent.createChooser(intent, "Select a File to Upload");
		return intent;
	}

	public static Intent getVideo() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("video/*");
		return intent;
	}

	public static Intent getYoutubeVideo(String videoId) {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
		intent.putExtra("VIDEO_ID", videoId);
		intent.putExtra("force_fullscreen", true);
		return intent;
	}

	public static Intent getWebPage(String aUrl) {
		Uri uri = Uri.parse(aUrl);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		return intent;
	}

	/**
	 * 푸쉬 수신시 app icon위에 badge 숫자 표시 처리
	 */
	public static Intent setPushBadge(int badgeCount, String aPackageName, String aClassName) {
		Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
		intent.putExtra("badge_count", badgeCount);
		// 메인 메뉴에 나타나는 어플의 패키지 명
		intent.putExtra("badge_count_package_name", aPackageName);
		// 메인메뉴에 나타나는 어플의 클래스 명
		intent.putExtra("badge_count_class_name", aClassName);
		return intent;
	}
	
	public static Intent installApk(String aApkPath) {
		File apkFile = new File(aApkPath);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
		
		return intent;
	}

	public static Intent sendCall(String aPhoneNumber) {
		Uri uri = Uri.parse("tel:" + aPhoneNumber);
		// Intent intent = new Intent(Intent.ACTION_DIAL, uri);
		Intent intent = new Intent(Intent.ACTION_CALL, uri);
		return intent;
	}

	public static Intent sendEmail(String aEmailAddress) {
		Uri uri = Uri.parse("mailto:" + aEmailAddress);
		Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
		return intent;
	}

	/**
	 * 사진 파일 자르기
	 * @param aUriFileTemp 임시저장패스
	 * @return
	 */
	public static Intent getIntentToCropedFile(Uri aUriFileTemp)
	{
	    Intent kIntent = new Intent("com.android.camera.action.CROP");
	    kIntent.setDataAndType(aUriFileTemp, "image/*");

        // crop한 이미지를 저장할때 200x200 크기로 저장
	    kIntent.putExtra("outputX", 100); // crop한 이미지의 x축 크기
        kIntent.putExtra("outputY", 100); // crop한 이미지의 y축 크기
        kIntent.putExtra("aspectX", 1); // crop 박스의 x축 비율 
        kIntent.putExtra("aspectY", 1); // crop 박스의 y축 비율
        kIntent.putExtra("scale", true);
        kIntent.putExtra("return-data", true);
        kIntent.putExtra(MediaStore.EXTRA_OUTPUT, aUriFileTemp);
        return kIntent;
	}
	
	/**
	 * 앨범에서 사진 가저오기
	 * @param aUriFileTemp 임시 저장 위치
	 * @param aIsCrop
	 * @return
	 */
	public static Intent getIntentToAlbum(Uri aUriFileTemp, boolean aIsCrop)
	{
	    Intent kIntent = new Intent(Intent.ACTION_PICK,      // 또는 ACTION_PICK
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        
        kIntent.putExtra(MediaStore.EXTRA_OUTPUT, aUriFileTemp);
        if (aIsCrop) {
            kIntent.putExtra("crop", "true");
            // crop한 이미지를 저장할때 200x200 크기로 저장
            kIntent.putExtra("outputX", 100); // crop한 이미지의 x축 크기
            kIntent.putExtra("outputY", 100); // crop한 이미지의 y축 크기
            kIntent.putExtra("aspectX", 1); // crop 박스의 x축 비율 
            kIntent.putExtra("aspectY", 1); // crop 박스의 y축 비율
            kIntent.putExtra("scale", true);
            kIntent.putExtra("return-data", true);
            kIntent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        }
        return kIntent;
	}
	
	public static Intent getIntentToPickPhoto(Uri aUriFileTemp)
	{
	    Intent kIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        kIntent.putExtra(MediaStore.EXTRA_OUTPUT, aUriFileTemp);
        kIntent.putExtra("return-data", true);
        return kIntent;
	}

}
