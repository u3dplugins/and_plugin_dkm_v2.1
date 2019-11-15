package com.dkm.hjdl;

import android.content.Context;
import android.view.SurfaceView;
import android.view.View;

/**
 * 类名 : Android Fragment 显示 Bug <br/>
 * 作者 : canyon / 龚阳辉 <br/>
 * 时间 : 2018-09-29 11：30 <br/>
 * 功能 : Unity 5.6上用手点击可以响应事件，但就是显示不出来
 */
public class CUnityPlayer extends com.unity3d.player.UnityPlayer {
	public CUnityPlayer(Context context) {
		super(context);
	}

	@Override
	public void addView(View child) {
		if (child instanceof SurfaceView) {
            ((SurfaceView)child).setZOrderOnTop(false);
        }
        super.addView(child);
	}
}
