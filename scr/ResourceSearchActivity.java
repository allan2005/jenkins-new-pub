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

/*资管搜索与告警和性能专题搜索的区别是从服务器获取了关务业务列表，由于业务列表变化比较小
 * 就第一次进入程序时获取，然后存储起来第二次进入界面时直接去取出来显示
 * */
public class ResourceSearchActivity extends BaseSearchActivity implements View.OnClickListener {

	Spinner resource_search_type, resource_search_status;
	// 业务分类的一级分类和二级分类
	Button resource_search_service_first;

	EditText resource_search_name, resource_search_mark, resource_search_ip;

	Button resource_search_filter;

	private int type = 0, state = 0;
	private Object result;
	// 信息提示框
	private ProgressDialog notice;

	private final int GETDATACOMPLETED = 0;
	private final int LOADSERVICE = 1;
	private String relationId;
	private String chosedItem = "";
	private boolean isJump = true;// 当已经输入内容的输入框不符合格式时不跳转

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
		setContentView(R.layout.activity_resource_search);

		findView();
		setListener();

		if (Constant.SERVICELIST == null) {
			String url = Constant.SERVER_URL_ROOT
					+ "json/Common?operation=concern_list_v2&contactId=&verify=" + Constant.SIM_MD5;

			getDataFromServer(url, Constant.PARSESERVICE);
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

	private void setListener() {
		// TODO Auto-generated method stub
		resource_search_filter.setOnClickListener(this);
		resource_search_service_first.setOnClickListener(this);
		// 默认选择全部，传递的值为0到n，0代表全部
		resource_search_type.setOnItemSelectedListener(new OnItemSelectedListener() {

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
		// 默认选择全部，传递的值为0到n，0代表全部
		resource_search_status.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub
				state = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void findView() {
		resource_search_type = (Spinner) findViewById(R.id.resource_search_type);
		resource_search_status = (Spinner) findViewById(R.id.resource_search_status);
		resource_search_service_first = (Button) findViewById(R.id.resource_search_service_first);
		resource_search_name = (EditText) findViewById(R.id.resource_search_name);
		resource_search_mark = (EditText) findViewById(R.id.resource_search_mark);
		resource_search_ip = (EditText) findViewById(R.id.resource_search_ip);
		resource_search_filter = (Button) findViewById(R.id.resource_search_filter);

		// 设置资管设备类型spinner显示的数据
		String[] types = getResources().getStringArray(R.array.type);
		ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, types);
		typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		resource_search_type.setAdapter(typeAdapter);

		// 设置资管设备状态spinner显示的数据
		String[] statuses = getResources().getStringArray(R.array.resource_state);
		ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, statuses);
		stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		resource_search_status.setAdapter(stateAdapter);

	}

	// 封装筛查条件，然后传递给搜索界面
	public void onStartFilter() {
		ContentValues values = new ContentValues();
		values.put("type", type);// 资源类型
		values.put("state", state);// 管理状态

		String name = resource_search_name.getText().toString();
		if (!TextUtils.isEmpty(name))
			values.put("name", name);// 资源名称

		String ip = resource_search_ip.getText().toString();

		if (!TextUtils.isEmpty(ip)) {
			// 只有有数据初始化一下

			String str = "^(([1-9]|([1-9]\\d)|(1\\d\\d)|(2([0-4]\\d|5[0-5])))\\.)(([1-9]|([1-9]\\d)|(1\\d\\d)|(2([0-4]\\d|5[0-5])))\\.){2}([1-9]|([1-9]\\d)|(1\\d\\d)|(2([0-4]\\d|5[0-5])))$";

			Pattern pattern = Pattern.compile(str);
			Matcher matcher = pattern.matcher(ip);
			if (!matcher.matches()) {
				Toast.makeText(ResourceSearchActivity.this, "请输入正确的ip地址格式", Toast.LENGTH_LONG)
						.show();
				resource_search_ip.setText("");
				isJump = false;
			} else {
				isJump = true;
				values.put("ipAddress", ip);// ip地址
			}

		}

		String mark = resource_search_mark.getText().toString();
		if (!TextUtils.isEmpty(mark))
			values.put("mark", mark);// 资源标识

		if (!TextUtils.isEmpty(relationId))
			values.put("relationId", relationId);// 业务分类

		values.put("pageSize", Constant.PAGESIZE);// 每次加载的数据条数
		values.put("page", Constant.PAGE);// 当前页面
		if (isJump) {
			Constant.SEARCH_TYPE = 3;
			Intent intent = new Intent(this, ActivitySearch.class);
			intent.putExtra("values", values);// 把post查询的参数传到查询界面
			startActivity(intent);

			finish();
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.resource_search_filter) {
			// 这里的处理是把资管加入历史分类中，不同分类用逗号隔开，存放在sharedpreference中
			SharedPreferences sp = getSharedPreferences("config", Activity.MODE_PRIVATE);
			String history = sp.getString("history", "");
			if (TextUtils.isEmpty(history)) {
				history = "网管资管";

			} else {
				if (!history.equals("网管资管")) {
					if (history.contains("网管资管")) {
						int start = history.indexOf("网管资管");
						if (start > 0)
							history = history.substring(0, start - 1)
									+ history.substring(start + 4, history.length());
						else
							history = history.substring(0, start)
									+ history.substring(start + 5, history.length());
					}
					history += "," + "网管资管";
				}
			}
			Editor editor = sp.edit();
			editor.putString("history", history);
			editor.commit();
			// 封装筛查条件，然后传递给搜索界面
			onStartFilter();
		} else if (id == R.id.resource_search_service_first) {
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
					resource_search_service_first.setText("无");
				} else if (chosedItem.equals("all")) {
					resource_search_service_first.setText("全部");
				} else {
					resource_search_service_first.setText(chosedItem);
				}
			}
			break;

		}

	}

}
