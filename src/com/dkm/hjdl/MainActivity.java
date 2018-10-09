package com.dkm.hjdl;

import com.dcproxy.framework.callback.IShowLogoCallBack;
import com.dcproxy.openapi.JyslSDK;
import com.tencent.bugly.crashreport.CrashReport;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

/**
 * 类名 : Android 入口对象 <br/>
 * 作者 : canyon / 龚阳辉 <br/>
 * 时间 : 2018-01-02 17：01 <br/>
 * 功能 : 实现生命周期
 */
public class MainActivity extends com.unity3d.player.UnityPlayerActivity {
	TelephonyManager m_mgrTM = null;
	@Override
	public void onConfigurationChanged(Configuration arg0) {
		super.onConfigurationChanged(arg0);
		JyslSDK.getInstance().onConfigurationChanged(arg0);
	}

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		_bug4U56Fragment();
		
		this.m_mgrTM = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		
		//横屏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		// 初始化bugly的异常捕获 b336483791 61e3febef5 JyslSDK里有监听崩溃日志
		CrashReport.initCrashReport(getApplicationContext(), "61e3febef5",false);
		
		// 数据统计
		new HJDLStatistics().Init(1, 0).DoStatistices();

		initMHandler();
		
		// 初始化，必须调用
		JyslSDK.getInstance().setResultCallback(SDKPlgDKM.getInstance());
		new HJDLStatistics().Init(2, 0).DoStatistices();
		JyslSDK.getInstance().init(MainActivity.this);
		JyslSDK.getInstance().onCreate(bundle);
		//_showSplash(3000);

		// 更新信息收集接口和点击进入游戏按钮事件收集（可选接入）
		// AkRoleParam param = new AkRoleParam();
		// AkRoleParam 如果有角色信息则填入AkRoleParam 没有则不用操作
		// JyslSDK.getInstance().CheckUpdateStart(param); // 游戏检查更新开始信息收集
		// JyslSDK.getInstance().CheckUpdateFinish(param); // 游戏检查更新完成信息收集
		// JyslSDK.getInstance().ClickEnterGameButton(param); // 点击游戏开始按钮信息收集		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		JyslSDK.getInstance().onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		JyslSDK.getInstance().onPause();
		SDKPlgDKM.reListenerState(this.m_mgrTM,true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		JyslSDK.getInstance().onResume();
		SDKPlgDKM.reListenerState(this.m_mgrTM,false);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		JyslSDK.getInstance().onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		JyslSDK.getInstance().onNewIntent(intent);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		JyslSDK.getInstance().onRestart();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		JyslSDK.getInstance().onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		JyslSDK.getInstance().onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
		JyslSDK.getInstance().onStop();
	}
	
	void _bug4U56Fragment(boolean _isFlags) {
		getWindow().setFormat(-3);
		this.mUnityPlayer = new CUnityPlayer(this);
	    if (_isFlags && this.mUnityPlayer.getSettings().getBoolean("hide_status_bar", true)) {
	      getWindow().setFlags(1024, 1024);
	    }
	    setContentView(this.mUnityPlayer);
	    this.mUnityPlayer.requestFocus();
	}
	
	void _bug4U56Fragment() {
		_bug4U56Fragment(true);
	}

	boolean _KeyCodeEvent(int keyCode, KeyEvent event) {
		//拦截返回键
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK){
            //判断触摸UP事件才会进行返回事件处理 KeyEvent.ACTION_UP
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                onBackPressed();
                //只要是返回事件，直接返回true，表示消费掉
                return true;
            }
        }
        return false;
	}
	
//	@Override
//	public boolean dispatchKeyEvent(KeyEvent event) {
//		if(_KeyCodeEvent(event.getKeyCode(), event)){
//			return true;
//		}
//		return super.dispatchKeyEvent(event);
//	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(_KeyCodeEvent(keyCode, event)){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onBackPressed() {
		// 游戏方负责关闭游戏,通过JyslResultCallback.EXITGAME 回调，没有这需求可以不用管
		// true 为游戏方关闭， false 为SDK 自己关闭游戏, 默认false是SDK 关闭游戏
		JyslSDK.getInstance().setIsCpExitGame(false);
		boolean isSelfExit = !JyslSDK.getInstance().onBackPressed();
		if (isSelfExit) {
			showTip4Exit();
		}
	}

	void showTip4Exit() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
				.setTitle("退出游戏").setMessage("您确定要退出游戏么?")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						exitGame();
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		alertDialog.show();
	}

	void exitGame() {
		finish();
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	void initMHandler() {
		if (mHandler == null) {
			mHandler = new Handler(Looper.getMainLooper()) {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
					case 0:
						exitGame();
						break;
					default:
						break;
					}
				}

			};
		}
	}
	
	void _showSplash(int ms){
		// 会读取 assets 目录下的名为 splash 的 png 格式的图片
		JyslSDK.getInstance().showLogo(this,"splash.png",ms,new IShowLogoCallBack() {
            @Override
            public void onFinished(String msg, int code) {
                   //显示闪屏结束
            }
        });
	}

	static Handler mHandler = null;

	static public void sendMsg(int state) {
		if (mHandler == null)
			return;

		Message msg = new Message();
		msg.what = state;
		mHandler.sendMessage(msg);
	}
}
