package com.ultrapower.android.search.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.ultrapower.android.search.R;
import com.ultrapower.android.search.model.ServiceModel;
import com.ultrapower.android.search.util.Constant;
import com.ultrapower.android.search.util.NetUtil;
import com.ultrapower.android.search.util.ThreadPoolManager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeviceSearchActivity extends BaseSearchActivity implements View.OnClickListener {

	Spinner device_search_type;
	Button device_search_service_first;
	// ip的格式要用正则表达式进行限制
	EditText device_search_name, device_search_version, device_search_ip;
	// cpu的区间开始必须必结束小
	EditText device_search_memoryend, device_search_memorystart;
	// 内存的区间开始必须必结束小
	EditText device_search_cpustart, device_search_cpuend;
	Button device_search_filter;

	private int type = 0;
	private String cpuStart, cpuEnd, memoryStart, memoryEnd;
	private final int GETDATACOMPLETED = 0;
	private final int LOADSERVICE = 1;
	private Object result;

	private String relationId;
	private boolean isJump = true;// 用来判断是否能够跳转到新的界面
	// 信息提示框
	private ProgressDialog notice;
	private String chosedItem = "";

	Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case GETDATACOMPLETED:// 首次获取业务列表完成
				if (result != null) {
					List<ServiceModel> serviceList = (List<ServiceModel>) result;
					Constant.SERVICELIST = serviceList;
				}
				if (notice != null)
					notice.dismiss();
				break;

			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Constant.activityList.add(this);
		setContentView(R.layout.activity_device_search);

		findView();
		setListener();

		if (Constant.SERVICELIST == null) {
			String url = Constant.SERVER_URL_ROOT
					+ "json/Common?operation=concern_list_v2&contactId=&verify=" + Constant.SIM_MD5;

			getDataFromServer(url, Constant.PARSESERVICE);
		} else {
			handler.sendEmptyMessage(LOADSERVICE);
		}
	}

	// 与服务器交互获取业务的数据
	private void getDataFromServer(String url, int parseMethodId) {
		showNotice();
		// TODO Auto-generated method stub
		if (NetUtil.hasNetwork(this)) {
			ThreadPoolManager.getInstance().addTask(new NetRunnable(url, parseMethodId));
		} else {
			Toast.makeText(this, R.string.net_error, Toast.LENGTH_LONG).show();
		}
	}

	// 与后台进行交互
	class NetRunnable implements Runnable {
		private String url;
		private int parseMethodId;

		public NetRunnable(String url, int parseMethodId) {
			this.url = url;
			this.parseMethodId = parseMethodId;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			result = NetUtil.get(null, url, parseMethodId);
			handler.sendEmptyMessage(GETDATACOMPLETED);
		}

	}

	// 提示框的显示
	private void showNotice() {
		if (notice == null) {
			notice = new ProgressDialog(this);
			notice.setProgressStyle(ProgressDialog.STYLE_SPINNER);

			// 设置ProgressDialog 标题
			notice.setTitle(R.string.loading_resource_service_info);

			// 设置ProgressDialog 提示信息d
			notice.setMessage(getResources().getString(R.string.loading));
			// 设置ProgressDialog 的进度条是否不明确
			notice.setIndeterminate(false);
			// 设置ProgressDialog 是否可以按退回按键取消
			notice.setCancelable(true);
			notice.show();
		} else {
			notice.show();
		}
	}

	private void findView() {
		device_search_type = (Spinner) findViewById(R.id.device_search_type);
		device_search_service_first = (Button) findViewById(R.id.device_search_service_first);
		device_search_name = (EditText) findViewById(R.id.device_search_name);
		device_search_version = (EditText) findViewById(R.id.device_search_version);
		device_search_ip = (EditText) findViewById(R.id.device_search_ip);
		device_search_memorystart = (EditText) findViewById(R.id.device_search_memorystart);
		device_search_memoryend = (EditText) findViewById(R.id.device_search_memoryend);
		device_search_cpuend = (EditText) findViewById(R.id.device_search_cpuend);
		device_search_cpustart = (EditText) findViewById(R.id.device_search_cpustart);
		device_search_filter = (Button) findViewById(R.id.device_search_filter);

	}

	private void setListener() {
		device_search_filter.setOnClickListener(this);
		device_search_service_first.setOnClickListener(this);

		// 设置性能类型spinner显示的数据
		String[] types = getResources().getStringArray(R.array.type);
		ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, types);
		typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		device_search_type.setAdapter(typeAdapter);
		// 默认选择全部，传递的值为0到n，0代表全部
		device_search_type.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub
				type = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

	}

	// 封装筛查条件，然后传递给搜索界面
	public void onStartFilter() {
		ContentValues values = new ContentValues();
		values.put("type", type);// 设备类别
		String mark = device_search_version.getText().toString();
		if (!TextUtils.isEmpty(mark))
			values.put("mark", mark);// 设备型号

		String name = device_search_name.getText().toString();
		if (!TextUtils.isEmpty(name))
			values.put("name", name);// 设备名称

		String ip = device_search_ip.getText().toString();
		if (!TextUtils.isEmpty(ip)) {
			// 只有有数据初始化一下

			String str = "^(([1-9]|([1-9]\\d)|(1\\d\\d)|(2([0-4]\\d|5[0-5])))\\.)(([1-9]|([1-9]\\d)|(1\\d\\d)|(2([0-4]\\d|5[0-5])))\\.){2}([1-9]|([1-9]\\d)|(1\\d\\d)|(2([0-4]\\d|5[0-5])))$";

			Pattern pattern = Pattern.compile(str);
			Matcher matcher = pattern.matcher(ip);
			if (!matcher.matches()) {
				Toast.makeText(DeviceSearchActivity.this, "请输入正确的ip地址格式", Toast.LENGTH_LONG).show();
				device_search_ip.setText("");
				isJump = false;
			} else {
				isJump = true;
				values.put("ipAddress", ip);// 设备ip地址
			}

		}

		// 内存和CPU的开始结束值都不能大于100，且开始值不能大于结束值
		cpuStart = device_search_cpustart.getText().toString();
		cpuEnd = device_search_cpuend.getText().toString();
		memoryStart = device_search_memorystart.getText().toString();
		memoryEnd = device_search_memoryend.getText().toString();

		if (!TextUtils.isEmpty(cpuStart) && TextUtils.isEmpty(cpuEnd)
				|| TextUtils.isEmpty(cpuStart) && !TextUtils.isEmpty(cpuEnd)) {
			Toast.makeText(this, "请重新输入cpu值", Toast.LENGTH_SHORT).show();
			return;
		}
		if (!TextUtils.isEmpty(memoryStart) && TextUtils.isEmpty(memoryEnd)
				|| TextUtils.isEmpty(memoryStart) && !TextUtils.isEmpty(memoryEnd)) {
			Toast.makeText(this, "请重新输入内存值", Toast.LENGTH_SHORT).show();
			return;
		}

		if (!TextUtils.isEmpty(cpuStart) && !TextUtils.isEmpty(cpuEnd)) {

			if ((Double.parseDouble(cpuStart) >= Double.parseDouble(cpuEnd))) {
				Toast.makeText(this, "cpu开始值不能大于结束值", Toast.LENGTH_SHORT).show();
				cpuEnd = null;
				device_search_cpuend.setText("");
				return;
			}

			if (Integer.parseInt(cpuStart) > 100 || Integer.parseInt(cpuEnd) > 100) {
				Toast.makeText(this, "cpu值不能大于100", Toast.LENGTH_SHORT).show();
				isJump = false;
			} else {
				isJump = true;
				values.put("cpuStart", cpuStart);// cpu开始区间
				values.put("cpuEnd", cpuEnd);// cpu结束区间
			}
		}

		if (!TextUtils.isEmpty(memoryStart) && !TextUtils.isEmpty(memoryEnd)) {

			if ((Double.parseDouble(memoryStart) >= Double.parseDouble(memoryEnd))) {
				Toast.makeText(this, "内存开始值不能大于结束值", Toast.LENGTH_SHORT).show();
				memoryEnd = null;
				device_search_memoryend.setText("");
				return;
			}

			if (Integer.parseInt(memoryStart) > 100 || Integer.parseInt(memoryEnd) > 100) {
				Toast.makeText(this, "内存值不能大于100", Toast.LENGTH_SHORT).show();
				isJump = false;
			} else {
				isJump = true;
				values.put("memoryStart", memoryStart);// 内存开始区间
				values.put("memoryEnd", memoryEnd);// 内存结束区间
			}
		}

		if (!TextUtils.isEmpty(relationId))
			values.put("relationId", relationId);// 业务分类

		values.put("pageSize", Constant.PAGESIZE);// 每次请求的数据条数
		values.put("page", Constant.PAGE);// 当前页面

		if (isJump) {
			Constant.SEARCH_TYPE = 2;
			Intent intent = new Intent(this, ActivitySearch.class);
			intent.putExtra("values", values);// 把post查询的参数传到查询界面
			startActivity(intent);
			finish();
		}

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.device_search_filter) {

			// 这里的处理是把性能加入历史分类中，不同分类用逗号隔开，存放在sharedpreference中
			SharedPreferences sp = getSharedPreferences("config", Activity.MODE_PRIVATE);
			String history = sp.getString("history", "");
			if (TextUtils.isEmpty(history)) {
				history = "网管性能";

			} else {
				if (!history.equals("网管性能")) {
					if (history.contains("网管性能")) {
						int start = history.indexOf("网管性能");
						if (start > 0)
							history = history.substring(0, start - 1)
									+ history.substring(start + 4, history.length());
						else
							history = history.substring(0, start)
									+ history.substring(start + 5, history.length());
					}
					history += "," + "网管性能";
				}
			}
			Editor editor = sp.edit();
			editor.putString("history", history);
			editor.commit();

			// 封装筛查条件，然后传递给搜索界面
			onStartFilter();
		} else if (id == R.id.device_search_service_first) {
			if (Constant.SERVICELIST != null) {
				Intent intent = new Intent(this, ServiceActivity.class);
				startActivityForResult(intent, 0);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case 0:
			if (data != null) {
				relationId = data.getStringExtra("relationId");
				chosedItem = data.getStringExtra("chosedItem");
				if (chosedItem.equals("")) {
					device_search_service_first.setText("无");
				} else if (chosedItem.equals("all")) {
					device_search_service_first.setText("全部");
				} else {
					device_search_service_first.setText(chosedItem);
				}
			}
			break;

		}

	}

}
