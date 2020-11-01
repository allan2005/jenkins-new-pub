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

/*�ʹ�������澯������ר�������������Ǵӷ�������ȡ�˹���ҵ���б�����ҵ���б�仯�Ƚ�С
 * �͵�һ�ν������ʱ��ȡ��Ȼ��洢�����ڶ��ν������ʱֱ��ȥȡ������ʾ
 * */
public class ResourceSearchActivity extends BaseSearchActivity implements View.OnClickListener {

	Spinner resource_search_type, resource_search_status;
	// ҵ������һ������Ͷ�������
	Button resource_search_service_first;

	EditText resource_search_name, resource_search_mark, resource_search_ip;

	Button resource_search_filter;

	private int type = 0, state = 0;
	private Object result;
	// ��Ϣ��ʾ��
	private ProgressDialog notice;

	private final int GETDATACOMPLETED = 0;
	private final int LOADSERVICE = 1;
	private String relationId;
	private String chosedItem = "";
	private boolean isJump = true;// ���Ѿ��������ݵ�����򲻷��ϸ�ʽʱ����ת

	Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case GETDATACOMPLETED:// �״λ�ȡҵ���б����
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

	private void setListener() {
		// TODO Auto-generated method stub
		resource_search_filter.setOnClickListener(this);
		resource_search_service_first.setOnClickListener(this);
		// Ĭ��ѡ��ȫ�������ݵ�ֵΪ0��n��0����ȫ��
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
		// Ĭ��ѡ��ȫ�������ݵ�ֵΪ0��n��0����ȫ��
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

		// �����ʹ��豸����spinner��ʾ������
		String[] types = getResources().getStringArray(R.array.type);
		ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, types);
		typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		resource_search_type.setAdapter(typeAdapter);

		// �����ʹ��豸״̬spinner��ʾ������
		String[] statuses = getResources().getStringArray(R.array.resource_state);
		ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, statuses);
		stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		resource_search_status.setAdapter(stateAdapter);

	}

	// ��װɸ��������Ȼ�󴫵ݸ���������
	public void onStartFilter() {
		ContentValues values = new ContentValues();
		values.put("type", type);// ��Դ����
		values.put("state", state);// ����״̬

		String name = resource_search_name.getText().toString();
		if (!TextUtils.isEmpty(name))
			values.put("name", name);// ��Դ����

		String ip = resource_search_ip.getText().toString();

		if (!TextUtils.isEmpty(ip)) {
			// ֻ�������ݳ�ʼ��һ��

			String str = "^(([1-9]|([1-9]\\d)|(1\\d\\d)|(2([0-4]\\d|5[0-5])))\\.)(([1-9]|([1-9]\\d)|(1\\d\\d)|(2([0-4]\\d|5[0-5])))\\.){2}([1-9]|([1-9]\\d)|(1\\d\\d)|(2([0-4]\\d|5[0-5])))$";

			Pattern pattern = Pattern.compile(str);
			Matcher matcher = pattern.matcher(ip);
			if (!matcher.matches()) {
				Toast.makeText(ResourceSearchActivity.this, "��������ȷ��ip��ַ��ʽ", Toast.LENGTH_LONG)
						.show();
				resource_search_ip.setText("");
				isJump = false;
			} else {
				isJump = true;
				values.put("ipAddress", ip);// ip��ַ
			}

		}

		String mark = resource_search_mark.getText().toString();
		if (!TextUtils.isEmpty(mark))
			values.put("mark", mark);// ��Դ��ʶ

		if (!TextUtils.isEmpty(relationId))
			values.put("relationId", relationId);// ҵ�����

		values.put("pageSize", Constant.PAGESIZE);// ÿ�μ��ص���������
		values.put("page", Constant.PAGE);// ��ǰҳ��
		if (isJump) {
			Constant.SEARCH_TYPE = 3;
			Intent intent = new Intent(this, ActivitySearch.class);
			intent.putExtra("values", values);// ��post��ѯ�Ĳ���������ѯ����
			startActivity(intent);

			finish();
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.resource_search_filter) {
			// ����Ĵ����ǰ��ʹܼ�����ʷ�����У���ͬ�����ö��Ÿ����������sharedpreference��
			SharedPreferences sp = getSharedPreferences("config", Activity.MODE_PRIVATE);
			String history = sp.getString("history", "");
			if (TextUtils.isEmpty(history)) {
				history = "�����ʹ�";

			} else {
				if (!history.equals("�����ʹ�")) {
					if (history.contains("�����ʹ�")) {
						int start = history.indexOf("�����ʹ�");
						if (start > 0)
							history = history.substring(0, start - 1)
									+ history.substring(start + 4, history.length());
						else
							history = history.substring(0, start)
									+ history.substring(start + 5, history.length());
					}
					history += "," + "�����ʹ�";
				}
			}
			Editor editor = sp.edit();
			editor.putString("history", history);
			editor.commit();
			// ��װɸ��������Ȼ�󴫵ݸ���������
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
					resource_search_service_first.setText("��");
				} else if (chosedItem.equals("all")) {
					resource_search_service_first.setText("ȫ��");
				} else {
					resource_search_service_first.setText(chosedItem);
				}
			}
			break;

		}

	}

}
