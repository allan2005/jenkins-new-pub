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
 * �澯ר������������ɸ��ҳ�棬���ɸ�������������������
 * 
 * @author cindy
 * 
 */
public class AlertSearchActivity extends BaseSearchActivity implements OnClickListener {

	Spinner alert_search_level, alert_search_status;
	Button alert_search_service_first;

	EditText alert_search_id;
	Button alert_search_filter;
	private RelativeLayout rl_starttime, rl_endtime;// ��ʼ�ͽ���ʱ��
	private TextView tv_starttime, tv_endtime;// ��ʾ��ʼ�ͽ���ʱ��
	private EditText processnum, content;// �����ź�����

	String[] levelStr;
	String[] statusStr;

	int alertLevel = 0;
	int alertState = 0;

	String keyCache = null;

	private String id;
	private Long startTime = null, endTime = null;// ��ʼ�ͽ���ʱ��ĺ���ֵ
	private String dateStr, timeStr;// ��¼���ں�ʱ����ֶ�
	private int isStart;// ����������ѡ��ʼ���ǽ���ʱ��

	private Object result;
	private List<ServiceModel> serviceList;

	// ��Ϣ��ʾ��
	private ProgressDialog notice;

	private final int GETDATACOMPLETED = 0;
	private final int LOADSERVICE = 1;
	private String relationId;
	private String chosedItem = "";

	Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case GETDATACOMPLETED:// �״λ�ȡҵ���б����
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

	// �������������ȡҵ�������
	private void getDataFromServer(String url, int parseMethodId) {
		showNotice();
		// TODO Auto-generated method stub
		if (NetUtil.hasNetwork(this)) {
			ThreadPoolManager.getInstance().addTask(new NetRunnable(url, parseMethodId));
		} else {
			Toast.makeText(this, R.string.net_error, Toast.LENGTH_LONG).show();
		}
	}

	// ���̨���н���
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

	// ��ʾ�����ʾ
	private void showNotice() {
		if (notice == null) {
			notice = new ProgressDialog(this);
			notice.setProgressStyle(ProgressDialog.STYLE_SPINNER);

			// ����ProgressDialog ����
			notice.setTitle(R.string.loading_resource_service_info);

			// ����ProgressDialog ��ʾ��Ϣd
			notice.setMessage(getResources().getString(R.string.loading));
			// ����ProgressDialog �Ľ������Ƿ���ȷ
			notice.setIndeterminate(false);
			// ����ProgressDialog �Ƿ���԰��˻ذ���ȡ��
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

		// ���ø澯����spinner��ʾ������
		levelStr = getResources().getStringArray(R.array.alert_type_list_3);
		ArrayAdapter<String> levelAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, levelStr);
		levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		alert_search_level.setAdapter(levelAdapter);

		// ���ø澯״̬spinner��ʾ������
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

		// Ĭ��ѡ��ȫ�������ݵ�ֵΪ0��n��0����ȫ��
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

		// Ĭ��ѡ��ȫ�������ݵ�ֵΪ0��n��0����ȫ��
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
			// ���ø澯�Ľ���ʱ��
			isStart = 1;
			choseTime();
		} else if (id == R.id.alert_search_starttime) {
			// ���ø澯����ʼʱ��
			isStart = 0;
			choseTime();
		} else if (id == R.id.alert_search_filter) {
			// ����Ĵ����ǰѸ澯������ʷ�����У���ͬ�����ö��Ÿ����������sharedpreference��
			SharedPreferences sp = getSharedPreferences("config", Activity.MODE_PRIVATE);
			String history = sp.getString("history", "");
			if (TextUtils.isEmpty(history)) {
				history = "���ܸ澯";

			} else {
				if (!history.equals("���ܸ澯")) {
					if (history.contains("���ܸ澯")) {
						int start = history.indexOf("���ܸ澯");
						if (start > 0)
							history = history.substring(0, start - 1)
									+ history.substring(start + 4, history.length());
						else
							history = history.substring(0, start)
									+ history.substring(start + 5, history.length());
					}
					history += "," + "���ܸ澯";
				}
			}
			Editor editor = sp.edit();
			editor.putString("history", history);
			editor.commit();

			// ��װɸ��������Ȼ�󴫵ݸ���������
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
					alert_search_service_first.setText("��");
				} else if (chosedItem.equals("all")) {
					alert_search_service_first.setText("ȫ��");
				} else {
					alert_search_service_first.setText(chosedItem);

				}
			}
			break;

		}

	}

	/**
	 * �Ѳ�ѯ�������ݸ���ѯ����
	 */
	public void onStartFilter() {

		id = alert_search_id.getText().toString();
		String pnum = processnum.getText().toString();
		String acontent = content.getText().toString();

		ContentValues values = new ContentValues();
		values.put("level", alertLevel);// �澯����
		values.put("state", alertState);// ȷ��״̬

		if (id != null && !id.equals("")) {
			values.put("alertId", id);// �澯���
		}
		if (!TextUtils.isEmpty(pnum)) {
			values.put("pnum", pnum);// �澯������
		}
		if (!TextUtils.isEmpty(acontent)) {
			values.put("acontent", acontent);// �澯����
		}

		if (startTime != null) {
			values.put("startTime", startTime);// �澯��ʼʱ��
		}
		if (endTime != null) {
			values.put("endTime", endTime);// �澯����ʱ��
		}

		if (!TextUtils.isEmpty(relationId))
			values.put("relationId", relationId);// ҵ�����

		values.put("pageSize", Constant.PAGESIZE);// ÿ���������������

		Constant.SEARCH_TYPE = 1;
		values.put("page", 1);
		Intent intent = new Intent(this, ActivitySearch.class);
		intent.putExtra("values", values);// ��post��ѯ�Ĳ���������ѯ����
		startActivity(intent);

		finish();
	}

	// �����Ի��򣬰������ں�ʱ��ѡ���
	private void choseTime() {
		// ����ʱ��ѡ���ʱ������Ϊ��ǰϵͳʱ�����ں�ʱ��
		Calendar c = Calendar.getInstance();

		dateStr = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-"
				+ c.get(Calendar.DAY_OF_MONTH);
		timeStr = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);

		AlertDialog.Builder builder = new Builder(this);
		builder.setView(showTimeView());

		builder.setTitle("ѡ��ʱ��");
		builder.setPositiveButton("����", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (0 == isStart) {
					startTime = DateUtil.getMillisecondsFromDate(dateStr + " " + timeStr,
							DateUtil.NOW_TIME2);
					tv_starttime.setText(dateStr + " " + timeStr);
					// �ж������ʱ��Ĺ�ϵ
					if (endTime != null) {
						if (endTime < startTime) {
							Toast.makeText(AlertSearchActivity.this, "��ʼʱ�䲻�ܴ��ڽ���ʱ��",
									Toast.LENGTH_LONG).show();
							tv_starttime.setText("");
						}
					}
				} else {
					endTime = DateUtil.getMillisecondsFromDate(dateStr + " " + timeStr,
							DateUtil.NOW_TIME2);
					tv_endtime.setText(dateStr + " " + timeStr);
					// �ж��뿪ʼʱ��Ĺ�ϵ
					if (startTime != null) {
						if (endTime < startTime) {
							Toast.makeText(AlertSearchActivity.this, "����ʱ�䲻�ܴ��ڿ�ʼʱ��",
									Toast.LENGTH_LONG).show();
							tv_endtime.setText("");

						}
					}
				}
			}
		});
		builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dateStr = null;
				timeStr = null;
			}
		});
		builder.create().show();
	}

	// ������Ĳ���
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
