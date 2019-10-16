package com.dkm.hjdl;

import android.Manifest;
import android.app.Activity;

public class SDKPlgPermission {
	static private String[] _perms = { Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO };

	static public void reqPermission(Activity cur) {
		SDKPlgDKM.initPermissions(cur, _perms);
	}
}
