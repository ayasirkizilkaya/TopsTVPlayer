package com.yuantops.tvplayer;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.yuantops.tvplayer.api.HttpClientAPI;
import com.yuantops.tvplayer.util.StringUtils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

/** 
 * 全局上下文：保存调用全局配置，调用网络访问api
 *
 * @author yuan (yuan.tops@gmail.com)
 * @created Dec 28, 2014 2:10:53 PM
 */
public class AppContext extends Application{
	private static final String TAG = AppContext.class.getSimpleName();

	public static final int NETTYPE_WIFI = 0x01;
	public static final int NETTYPE_CMWAP = 0x02;
	public static final int NETTYPE_CMNET = 0x03; 
	
	public static final String ENCRYPT_KEY = "toBeOrNotToBe";
	
	private boolean isLogin = false;
	private String loginAccount = null;
	private String loginRecordId = null;
	
	private String webServerIP = null;
	private String socketServerIP = null;
	
	private String IPAddress = null;
	private String IPAddress_hex = null;
	private String deviceType = null;
	
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		// Create global configuration and initialize ImageLoader with this config
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
		ImageLoader.getInstance().init(config);
	}
		
	/**
	 * 检查是否已经接入网络
	 */
	public boolean isNetworkConnected(){
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}
	
	/**
	 * 获取当前网络类型
	 * @return 0：没有网络   1：WIFI网络   2：WAP网络    3：NET网络
	 */
	public int getNetworkType() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}		
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = networkInfo.getExtraInfo();
			if(!StringUtils.isEmpty(extraInfo)){
				if (extraInfo.toLowerCase().equals("cmnet")) {
					netType = NETTYPE_CMNET;
				} else {
					netType = NETTYPE_CMWAP;
				}
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = NETTYPE_WIFI;
		}
		return netType;
	}
	
	
	/**
	 * 设置web服务器,socket服务器IP地址
	 * @param webServerIP
	 * @param socketServerIP
	 */
	public void setServerIP(String webServerIP, String socketServerIP) {
		this.webServerIP = webServerIP;
		this.socketServerIP = socketServerIP;
	}
	
	/**
	 * 初始化本机IP地址
	 */
	public void initClientIP(){
		WifiManager wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInf = wifiMan.getConnectionInfo();
		int ipAddress = wifiInf.getIpAddress();
		String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
		
		if(!StringUtils.isEmpty(ip)){
			this.IPAddress = ip;
			this.IPAddress_hex = get_Hex_subnet(ip);
		}else{
			this.IPAddress = "0.0.0.0";
			this.IPAddress_hex = "00000000";
		}
	}
	
	private String get_Hex_subnet(String ip) {
		StringBuffer subnet = new StringBuffer();
		try {
			for (String s : ip.split("\\.")) {
				subnet.append(former(Integer.toHexString(Integer.parseInt(s))));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return subnet.toString();
	}
	
	public String former(String hex) {
		if (hex.length() == 1) {
			return "0" + hex;
		} else {
			return hex;
		}
	}
		
	
	/**
	 * 初始化App的一系列变量: IP、八进制形式的IP、 设备型号
	 *
	 */
	public void initAppEnv() {
		//设备IP、
		initClientIP();
		deviceType = Build.MODEL;
	}
	
	
	/**
	 * 获取本机IP地址
	 * @return IP
	 */
	public String getClientIP(){
		return this.IPAddress;
	}
	
	/**
	 * 获取本机IP地址_hex格式
	 * @return
	 */
	public String getClientIP_hex() {
		return this.IPAddress_hex;
	}
	
	/**
	 * 获取机器型号
	 * @return device type
	 */
	public String getDeviceType() {
		return this.deviceType;
	}
	
	/**
	 * 获取登录标志
	 */
	public boolean isLogin(){
		return this.isLogin;
	}
	
	/**
	 * 获得登录用户名
	 */
	public String getLoginAccount(){
		return this.loginAccount;
	}
	
	/**
	 * 获得登录在数据库中的记录号
	 */
	public String getloginRecordId(){
		return this.loginRecordId;
	}
			
	/**
	 * 获取web服务器IP地址
	 * @return
	 */
	public String getWebServerIP() {
		return this.webServerIP;
	}
	
	/**
	 * 获取socket服务器IP地址
	 * @return
	 */
	public String getSocketServerIP() {
		return this.socketServerIP;
	}
	
	/**
	 * 注销此次登录
	 * 在服务器上注销登录；还原登录标记
	 */
	public void logout(){
		if(StringUtils.isEmpty(this.loginAccount) || StringUtils.isEmpty(this.loginAccount)){
			return;
		}
		//TODO 在服务器上注销此次登录
		HttpClientAPI.logout(this.webServerIP, this.loginRecordId);
		
		this.isLogin = false;
		this.loginAccount = null;
		this.loginRecordId = null;
	}
		
	/**
	 * 用户名和密码认证
	 * @param name 用户名
	 * @param pwd 密码（加密后的）
	 * @return 1）认证失败：“0” 2）认证成功：数据库中登录记录号
	 */
	/*public String loginAuthenticate(String account, String pwd){
		//TODO 
		return HttpClientAPI.loginAuth(account, pwd);
	}*/
	
	/**
	 * 更新登录标志、登录帐号、登录ID
	 * @param account 用户名
	 * @param recordId 数据库中登录记录号
	 */
	public void setLogged(String account, String recordId){
		this.isLogin = true;
		this.loginAccount = account;
		this.loginRecordId = recordId;
	}
	
	/**
	 * 根据isRememberMe：
	 * true：保存记忆标志、用户名、密码(加密后)、服务器IP到磁盘
	 * false: 将磁盘中上述信息置空
	 * @param isRememberMe 
	 * @param account 用户名
	 * @param pwd 密码（加密后）
	 */
	public void saveLoginParams(boolean isRememberMe, String account, String pwd, String serverIP){
		if(isRememberMe){
			SharedPreferences sharedPreferences = this.getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString("account", account);
			editor.putString("password", pwd);
			editor.putString("severip", serverIP);
			editor.putString("isRememberMe", "true");
			editor.commit();			
		}else{
			clearLoginInfo();
		}		
	}
	
	/**
	 * 保存单个登录参数到磁盘
	 * @param key
	 * @param value
	 */
	public void saveLoginParams(String key, String value){
		SharedPreferences sharedPreferences = this.getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	/**
	 * 获取保存的登录参数
	 * @param key 
	 * @return value
	 */
	public String getLoginParams(String key){
		SharedPreferences sharedPreferences = this.getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
		return sharedPreferences.getString(key, "");
	}
	
	/**
	 * 清除保存在本地的登录参数:
	 */
	public void clearLoginInfo(){
		SharedPreferences sharedPreferences = this.getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("account", "");
		editor.putString("password", "");
		editor.putString("isRememberMe", "");
		editor.putString("severip", "");
		editor.commit();
	}
	
}
