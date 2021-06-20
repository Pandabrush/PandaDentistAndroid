package com.pandadentist.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.pandadentist.bleconnection.utils.Logger;

/**
 * Author: 张维亚
 * File Name: ScreenUtil.java
 * 创建时间：2014年5月30日 下午3:27:47
 * 修改时间：2014年5月30日 下午3:27:47
 * Module Name: 具体模块见相应注释
 * Description: 关于屏幕相关的util
 **/
@SuppressWarnings("unused")
public class ZScreen {

	private static DisplayMetrics getDisplayMetrics(Context ctx) {
		try{
			DisplayMetrics outMetrics = new DisplayMetrics();
			WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
			wm.getDefaultDisplay().getMetrics(outMetrics);
			return outMetrics;
		}catch(NullPointerException e){
			Logger.e("getDisplayMetrics", e.getMessage());
			return new NullDisplayMetrics();
		}
	}

	/**
	 * 获得设备的dpi
	 */
	public static float getScreenDpi(Context ctx) {
		return getDisplayMetrics(ctx).densityDpi;
	}

	/**
	 * 获得设备屏幕密度
	 */
	public static float getScreenDensity(Context ctx) {
		DisplayMetrics dm = getDisplayMetrics(ctx);
		return dm.density;
	}

	public static float getScreenScaledDensity(Context ctx) {
		DisplayMetrics dm = getDisplayMetrics(ctx);
		return dm.scaledDensity;
	}
	
	/**
	 * 获得设备屏幕宽度
	 */
	public static int getScreenWidth(Context ctx) {
		DisplayMetrics dm = getDisplayMetrics(ctx);
		return dm.widthPixels;
	}

	/**
	 * 获得设备屏幕高度
	 * According to phone resolution height
	 */
	public static int getScreenHeight(Context ctx) {
		DisplayMetrics dm = getDisplayMetrics(ctx);
		return dm.heightPixels;
	}

	/**
	 * According to the resolution of the phone from the dp unit will become a px (pixels)
	 */
	public static int dip2px(Context ctx, int dip) {
		float density = getScreenDensity(ctx);
		return (int) (dip * density + 0.5f);
	}
	/**
	 * According to the resolution of the phone from the dp unit will become a px (pixels)
	 */
	public static float dip2px(Context ctx, float dip) {
		float density = getScreenDensity(ctx);
		return  (dip * density + 0.5f);
	}

	/**
	 * Turn from the units of px (pixels) become dp according to phone resolution
	 */
	public static int px2dip(Context ctx, float px) {
		float density = getScreenDensity(ctx);
		return (int) (px / density + 0.5f);
	}
	


	public static int px2sp(Context ctx, float px) {
		float scale = getScreenScaledDensity(ctx);
		return (int) (px / scale + 0.5f);
	}


	public static int sp2px(Context ctx, int sp){
		float scale = getScreenScaledDensity(ctx);
		return (int) (sp * scale + 0.5f);
	}
	
	/**
	 * @ClassName: NullDisplayMetrics 
	 * @Description: 防止获取DisplayMetrics对象失败而导致的nullpointer异常  
	 * @date 2015年10月12日 下午3:04:17
	 */
	static class NullDisplayMetrics extends DisplayMetrics{
		NullDisplayMetrics(){
	        widthPixels = 0;
	        heightPixels = 0;
	        density = 0.0f;
	        densityDpi = 120;
	        scaledDensity = 0.0f;
	        xdpi = 0.0f;
	        ydpi = 0.0f;
		}
	}
}
