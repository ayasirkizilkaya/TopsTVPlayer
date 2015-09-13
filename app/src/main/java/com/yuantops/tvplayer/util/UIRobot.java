package com.yuantops.tvplayer.util;

import android.os.Bundle;
import com.yuantops.tvplayer.AppManager;
import com.yuantops.tvplayer.bean.Video;
import com.yuantops.tvplayer.ui.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * 处理与UI相关的工具类，包括Activity跳转、显示Dialog、显示Toast
 * @author admin (Email: yuan.tops@gmail.com)
 * @date 2015-1-7
 */
public class UIRobot {
	public static final String TAG = UIRobot.class.getSimpleName();

	public static void gotoMainPage(Context context, String url) {
        Intent intent = new Intent(context, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        intent.putExtras(bundle);
//		intent.putExtra("url", url);
        context.startActivity(intent);
        AppManager.getInstance().finishActivity((Activity) context);
    }

	/**
	 * 跳转到注册页面
	 * @param mContext
	 */
	public static void gotoRegisterPage(Context mContext) {
		Intent intent = new Intent(mContext, RegisterActivity.class);
		mContext.startActivity(intent);
	}
	
	/**
	 * 跳转到影片详情介绍页
	 * @param mContext
	 * @param video
	 */
	public static void showVideoDetail(Context mContext, Video video){
		if(video == null){
			return;
		}
		Intent intent = new Intent(mContext, VideoDisplayActivity.class);
		intent.putExtra("video", video);
		mContext.startActivity(intent);
		//AppManager.getInstance().finishActivity();
	}

	/**
	 * 跳转到播放器页面
	 * @param mContext
	 * @param video
	 */
	public static void openVideoPlayer(Context mContext, Video video){
		if(video == null){
			return;
		}
		Intent intent = new Intent(mContext, VideoPlayActivity.class);		
		/*if(video.getType().equals(Video.LIVE_BROADCAST_TYPE)){
			intent = new Intent(mContext, VideoPlayActivity_Vitamio.class);
		}else if(video.getType().equals(Video.VIDEO_ON_DEMAND_TYPE)){
			intent = new Intent(mContext, VideoPlayActivity.class);
		}*/
		intent.putExtra("video", video);
		Log.v(TAG, "openVideoPlayer()...");
		mContext.startActivity(intent);
		
		//如果当前的置顶Activity为播放器，那么销毁它
		/*Activity currentActivity = AppManager.getInstance().currentActivity();
		if(currentActivity.getClass().getSimpleName().equals("VideoPlayActivity_Vitamio") || currentActivity.getClass().getSimpleName().equals("VideoPlayActivity")){
			AppManager.getInstance().finishActivity();
		}*/
	}
	
	/**
	 * 显示提示信息
	 * @param mContext
	 * @param string
	 */
	public static void showToast(Context mContext, String string) {
		if(!StringUtils.isEmpty(string)) {
			Toast.makeText(mContext, string, Toast.LENGTH_SHORT).show();
		}
	} 
}
