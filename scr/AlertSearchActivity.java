package com.ultrapower.android.search.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;

import com.ultrapower.android.search.R;
import com.ultrapower.android.search.model.ServiceModel;
import com.ultrapower.android.search.util.Constant;
import com.ultrapower.android.search.util.DateUtil;
import com.ultrapower.android.search.util.NetUtil;
import com.ultrapower.android.search.util.ThreadPoolManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 告警专题搜索的条件筛查页面，点击筛查把条件传给搜索界面
 * 
 * @author cindy
 * 
 */
public class AlertSearchActivity extends BaseSearchActivity implements OnClickListener {

	Spinner alert_search_level, alert_search_status;
	Button alert_search_service_first;

	EditText alert_search_id;
	Button alert_search_filter;
	private RelativeLayout rl_starttime, rl_endtime;// 起始和结束时间
	private TextView tv_starttime, tv_endtime;// 显示起始和结束时间
	private EditText processnum, content;// 工单号和内容

	String[] levelStr;
	String[] statusStr;

	int alertLevel = 0;
	int alertState = 0;

	String keyCache = null;

	private String id;
	private Long startTime = null, endTime = null;// 开始和结束时间的毫秒值
	private String dateStr, timeStr;// 记录日期和时间的字段
	private int isStart;// 用来区分是选择开始还是结束时间

	private Object result;
	private List<ServiceModel> serviceList;

	// 信息提示框
	private ProgressDialog notice;

	private final int GETDATACOMPLETED = 0;
	private final int LOADSERVICE = 1;
	private String relationId;
	private String chosedItem = "";

	Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case GETDATACOMPLETED:// 首次获取业务列表完成
				if (result != null) {
					serviceList = (List<ServiceModel>) result;
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
		setContentView(R.layout.activity_alert_search);

		serviceList = new ArrayList<ServiceModel>();
		findview();
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

	private void findview() {
		alert_search_level = (Spinner) findViewById(R.id.alert_search_level);
		alert_search_status = (Spinner) findViewById(R.id.alert_search_status);
		alert_search_service_first = (Button) findViewById(R.id.alert_search_service_first);
		alert_search_id = (EditText) findViewById(R.id.alert_search_id);
		alert_search_filter = (Button) findViewById(R.id.alert_search_filter);
		rl_starttime = (RelativeLayout) findViewById(R.id.alert_search_starttime);
		rl_endtime = (RelativeLayout) findViewById(R.id.alert_search_endtime);
		tv_starttime = (TextView) findViewById(R.id.alert_search_starttv);
		tv_endtime = (TextView) findViewById(R.id.alert_search_endtv);
		processnum = (EditText) findViewById(R.id.alert_search_pnum);
		content = (EditText) findViewById(R.id.alert_search_content);

		// 设置告警级别spinner显示的数据
		levelStr = getResources().getStringArray(R.array.alert_type_list_3);
		ArrayAdapter<String> levelAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, levelStr);
		levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		alert_search_level.setAdapter(levelAdapter);

		// 设置告警状态spinner显示的数据
		statusStr = getResources().getStringArray(R.array.alert_state_list_4);
		ArrayAdapter<String> statusAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, statusStr);
		statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		alert_search_status.setAdapter(statusAdapter);

	}

	private void setListener() {
		rl_starttime.setOnClickListener(this);
		rl_endtime.setOnClickListener(this);
		alert_search_filter.setOnClickListener(this);
		alert_search_service_first.setOnClickListener(this);

		// 默认选择全部，传递的值为0到n，0代表全部
		alert_search_level.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub
				alertLevel = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});

		// 默认选择全部，传递的值为0到n，0代表全部
		alert_search_status.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// TODO Auto-generated method stub
				alertState = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.alert_search_endtime) {
			// 设置告警的结束时间
			isStart = 1;
			choseTime();
		} else if (id == R.id.alert_search_starttime) {
			// 设置告警的起始时间
			isStart = 0;
			choseTime();
		} else if (id == R.id.alert_search_filter) {
			// 这里的处理是把告警加入历史分类中，不同分类用逗号隔开，存放在sharedpreference中
			SharedPreferences sp = getSharedPreferences("config", Activity.MODE_PRIVATE);
			String history = sp.getString("history", "");
			if (TextUtils.isEmpty(history)) {
				history = "网管告警";

			} else {
				if (!history.equals("网管告警")) {
					if (history.contains("网管告警")) {
						int start = history.indexOf("网管告警");
						if (start > 0)
							history = history.substring(0, start - 1)
									+ history.substring(start + 4, history.length());
						else
							history = history.substring(0, start)
									+ history.substring(start + 5, history.length());
					}
					history += "," + "网管告警";
				}
			}
			Editor editor = sp.edit();
			editor.putString("history", history);
			editor.commit();

			// 封装筛查条件，然后传递给搜索界面
			onStartFilter();
		} else if (id == R.id.alert_search_service_first) {
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
					alert_search_service_first.setText("无");
				} else if (chosedItem.equals("all")) {
					alert_search_service_first.setText("全部");
				} else {
					alert_search_service_first.setText(chosedItem);

				}
			}
			break;

		}

	}

	/**
	 * 把查询参数传递给查询界面
	 */
	public void onStartFilter() {

		id = alert_search_id.getText().toString();
		String pnum = processnum.getText().toString();
		String acontent = content.getText().toString();

		ContentValues values = new ContentValues();
		values.put("level", alertLevel);// 告警级别
		values.put("state", alertState);// 确认状态

		if (id != null && !id.equals("")) {
			values.put("alertId", id);// 告警编号
		}
		if (!TextUtils.isEmpty(pnum)) {
			values.put("pnum", pnum);// 告警工单号
		}
		if (!TextUtils.isEmpty(acontent)) {
			values.put("acontent", acontent);// 告警内容
		}

		if (startTime != null) {
			values.put("startTime", startTime);// 告警开始时间
		}
		if (endTime != null) {
			values.put("endTime", endTime);// 告警结束时间
		}

		if (!TextUtils.isEmpty(relationId))
			values.put("relationId", relationId);// 业务分类

		values.put("pageSize", Constant.PAGESIZE);// 每次请求的数据条数

		Constant.SEARCH_TYPE = 1;
		values.put("page", 1);
		Intent intent = new Intent(this, ActivitySearch.class);
		intent.putExtra("values", values);// 把post查询的参数传到查询界面
		startActivity(intent);

		finish();
	}

	// 弹出对话框，包含日期和时间选择框
	private void choseTime() {
		// 弹出时间选择框时就设置为当前系统时间日期和时间
		Calendar c = Calendar.getInstance();

		dateStr = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-"
				+ c.get(Calendar.DAY_OF_MONTH);
		timeStr = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);

		AlertDialog.Builder builder = new Builder(this);
		builder.setView(showTimeView());

		builder.setTitle("选择时间");
		builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (0 == isStart) {
					startTime = DateUtil.getMillisecondsFromDate(dateStr + " " + timeStr,
							DateUtil.NOW_TIME2);
					tv_starttime.setText(dateStr + " " + timeStr);
					// 判断与结束时间的关系
					if (endTime != null) {
						if (endTime < startTime) {
							Toast.makeText(AlertSearchActivity.this, "开始时间不能大于结束时间",
									Toast.LENGTH_LONG).show();
							tv_starttime.setText("");
						}
					}
				} else {
					endTime = DateUtil.getMillisecondsFromDate(dateStr + " " + timeStr,
							DateUtil.NOW_TIME2);
					tv_endtime.setText(dateStr + " " + timeStr);
					// 判断与开始时间的关系
					if (startTime != null) {
						if (endTime < startTime) {
							Toast.makeText(AlertSearchActivity.this, "结束时间不能大于开始时间",
									Toast.LENGTH_LONG).show();
							tv_endtime.setText("");

						}
					}
				}
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dateStr = null;
				timeStr = null;
			}
		});
		builder.create().show();
	}

	// 弹出框的布局
	private View showTimeView() {
		View timeView = View.inflate(this, R.layout.timepopup_alert_search, null);

		DatePicker dPicker = (DatePicker) timeView.findViewById(R.id.timepopup_dpicker);
		TimePicker tPicker = (TimePicker) timeView.findViewById(R.id.timepopup_tpicker);

		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		dPicker.init(year, month, day, new OnDateChangedListener() {

			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				// TODO Auto-generated method stub
				dateStr = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
			}
		});

		tPicker.setOnTimeChangedListener(new OnTimeChangedListener() {

			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				// TODO Auto-generated method stub
				timeStr = hourOfDay + ":" + minute;
			}
		});

		return timeView;
	}
}
