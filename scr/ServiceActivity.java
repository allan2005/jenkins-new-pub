package com.ultrapower.android.search.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ultrapower.android.search.R;
import com.ultrapower.android.search.adapter.ResourceAdapter;
import com.ultrapower.android.search.model.ServiceModel;
import com.ultrapower.android.search.util.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ������
 * @version ����ʱ�䣺2013-8-9 ����5:59:37
 */
public class ServiceActivity extends BaseSearchActivity {
	ListView service_lv;
	private List<String> groups;// ҵ��һ������
	private Map<String, List<ServiceModel>> childs;// ҵ���������

	private List<ServiceModel> spinnerList;// ҵ�����������ʾ������
	private String relationId = "";
	private boolean[] isChecked;// ��¼��������λ�ü�״̬
	private Builder builder;// �������൯����
	private ResourceAdapter adapter;
	private boolean[] selected;
	private String chosedItem = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_service);
		service_lv = (ListView) findViewById(R.id.service_lv);

		setData();
	}

	// ��ҵ���spinner��ʾ�����������һ��������spinnerѡ��--��ѡ�����һ������󵯳����������
	// ѡ��Ի���--��ѡ
	private void setData() {
		groups = new ArrayList<String>();
		childs = new HashMap<String, List<ServiceModel>>();

		for (ServiceModel myService : Constant.SERVICELIST) {
			String group = myService.getB();
			String child = myService.getD();
			// ��ȡһ������Ͷ�������
			if (groups.size() == 0) {
				groups.add(group);
				List<ServiceModel> childService = new ArrayList<ServiceModel>();
				childService.add(myService);
				childs.put(group, childService);
			} else if (!isContain(groups, group)) {
				// ���һ�����༯���в������÷��࣬�򴴽��µ�list���ϲ�����������༯����
				groups.add(group);
				List<ServiceModel> childService = new ArrayList<ServiceModel>();
				childService.add(myService);
				childs.put(group, childService);
			} else {
				// ���һ�����༯���а����÷��࣬��ȡ����list���ϲ�����������༯����
				List<ServiceModel> childService = childs.get(group);
				childService.add(myService);
				childs.put(group, childService);
			}
		}

		groups.add(0, "ȫ��");
		String[] groupStr = new String[groups.size()];
		for (int i = 0; i < groups.size(); i++) {
			groupStr[i] = groups.get(i);
		}

		ArrayAdapter<String> serviceAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, groupStr);

		service_lv.setAdapter(serviceAdapter);

		service_lv.setOnItemClickListener(new MyItemClickListener());

	}

	class MyItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			if (0 != position) {
				// ��һ��ҵ��ѡ�У�����ҵ��Ҳ������ʾ
				String group = groups.get(position);// һ������
				spinnerList = childs.get(group);// ��ȡ��һ�������Ӧ�Ķ�������

				String[] dataStr = new String[spinnerList.size()];
				// Ĭ��ȫ����ѡ��
				for (int i = 0; i < spinnerList.size(); i++) {
					dataStr[i] = spinnerList.get(i).getD();
				}
				showListDialog(dataStr);

			} else {
				// setResult(0);
				Intent data = new Intent();
				data.putExtra("relationId", relationId);
				data.putExtra("chosedItem", "all");
				setResult(0, data);
				finish();
			}

		}

	}

	public void showListDialog(final String[] dataStr) {
		selected = new boolean[dataStr.length];

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		DialogInterface.OnMultiChoiceClickListener choiceListener = new DialogInterface.OnMultiChoiceClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {

				selected[which] = isChecked;
			}
		};
		DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				StringBuffer sb = new StringBuffer("");
				String chosedItemtemp = "";
				for (int i = 0; i < selected.length; i++) {
					if (selected[i] == true) {
						sb.append(",").append(spinnerList.get(i).getA());
						chosedItemtemp += "," + dataStr[i];
						chosedItem = chosedItemtemp.substring(1);
					}
				}
				if (sb.length() > 0) {
					sb.deleteCharAt(0);
					relationId = sb.toString();
				} else {
					relationId = "";
				}
			}
		};
		builder.setMultiChoiceItems(dataStr, selected, choiceListener);
		builder.setPositiveButton(R.string.btn_ok, clickListener);
		builder.setNegativeButton(R.string.btn_cancel, null);
		builder.setCancelable(true);
		builder.create();
		builder.show();

	}

	// �жϷ������list���Ƿ��Ѿ������˵�ǰ�������
	private boolean isContain(List<String> groups, String group) {
		for (String myGroup : groups) {
			if (group.equals(myGroup))
				return true;

		}

		return false;
	}

	public void okButton(View v) {
		Intent data = new Intent();
		data.putExtra("relationId", relationId);
		data.putExtra("chosedItem", chosedItem);
		setResult(0, data);
		this.finish();
	}

	public void cancelButton(View v) {
		this.finish();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		this.finish();
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
