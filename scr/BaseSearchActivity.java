package com.ultrapower.android.search.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ultrapower.android.search.util.Constant;
import com.ultrapower.android.search.util.NetUtil;
import com.ultrapower.android.search.util.ThreadPoolManager;
import com.ultrapower.android.search.util.UltraEncryptionUtil;
import com.ultrapower.android.search.util.UltraPhoneStateUtil;

public class BaseSearchActivity extends Activity {
	private static final String TAG = "BaseSearchActivity";

	private Context context;

	private boolean isOnPause = false;

	private Handler verifyHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case Constant.WHAT_VERIFY:
				String url = Constant.SERVER_URL_ROOT + Constant.SERVER_URL_VERIFY_LOGIN
						+ "&verify="
						+ UltraEncryptionUtil.toMD5(UltraPhoneStateUtil.getIMSI(context));
				ThreadPoolManager.getInstance().addTask(new NetRunnable(url, Constant.PARSELOGIN));
				break;

			default:
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.i(TAG, this.getClass().getSimpleName() + " onCreate");
		super.onCreate(savedInstanceState);

		context = BaseSearchActivity.this;
		isOnPause = false;
	}

	@Override
	protected void onStop() {
		super.onStop();

		Log.d(TAG, this.getClass().getSimpleName() + " onStop");
		isOnPause = true;
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		Log.i(TAG, this.getClass().getSimpleName() + " onRestart");
		/* 根据Activity的onstop和onRestart重新验证登录信息 */
/*		if (isOnPause) {
			verifyHandler.sendEmptyMessage(Constant.WHAT_VERIFY);
		}*/
	}

	/**
	 * 根据用户是否从易运维下线来处理掌控数宝是否要下线。
	 * 
	 * @param isOnLine
	 */
	private void skipTo(boolean isOnLine) {
		if (isOnLine) {
			Log.d(TAG, this.getClass().getSimpleName() + " is onLine!");
		} else {
			Log.d(TAG, this.getClass().getSimpleName() + " is not onLine!");
			super.finish();

		}
	}

	/**
	 * 网络请求,数据解析
	 * 
	 * @author Administrator
	 * 
	 */
	class NetRunnable implements Runnable {

		String url = "";
		int parseMethodId = 0;

		public NetRunnable(String url, int id) {
			this.url = url;
			this.parseMethodId = id;
		}

		@Override
		public void run() {
			Object result = NetUtil.get(null, url, parseMethodId);
			if (result != null) {
				if (result.equals(Constant.STATUS_SUCCESS)) {
					skipTo(true);
				} else if (result.equals(Constant.STATUS_ERROR)) {
					skipTo(false);
				}
			}
		}

	}
}
