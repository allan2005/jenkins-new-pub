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
 * @author 黄丽慧
 * @version 创建时间：2013-8-9 下午5:59:37
 */
public class ServiceActivity extends BaseSearchActivity {
	ListView service_lv;
	private List<String> groups;// 业务一级分类
	private Map<String, List<ServiceModel>> childs;// 业务二级分类

	private List<ServiceModel> spinnerList;// 业务二级分类显示的数据
	private String relationId = "";
	private boolean[] isChecked;// 记录二级分类位置及状态
	private Builder builder;// 二级分类弹出框
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

	// 把业务的spinner显示出来，这里的一级分类用spinner选中--单选，点击一级分类后弹出二级分类的
	// 选择对话框--多选
	private void setData() {
		groups = new ArrayList<String>();
		childs = new HashMap<String, List<ServiceModel>>();

		for (ServiceModel myService : Constant.SERVICELIST) {
			String group = myService.getB();
			String child = myService.getD();
			// 获取一级分类和二级分类
			if (groups.size() == 0) {
				groups.add(group);
				List<ServiceModel> childService = new ArrayList<ServiceModel>();
				childService.add(myService);
				childs.put(group, childService);
			} else if (!isContain(groups, group)) {
				// 如果一级分类集合中不包含该分类，则创建新的list集合并存入二级分类集合中
				groups.add(group);
				List<ServiceModel> childService = new ArrayList<ServiceModel>();
				childService.add(myService);
				childs.put(group, childService);
			} else {
				// 如果一级分类集合中包含该分类，则取出的list集合并存入二级分类集合中
				List<ServiceModel> childService = childs.get(group);
				childService.add(myService);
				childs.put(group, childService);
			}
		}

		groups.add(0, "全部");
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
				// 当一级业务被选中，二级业务也跟着显示
				String group = groups.get(position);// 一级分类
				spinnerList = childs.get(group);// 获取到一级分类对应的二级分类

				String[] dataStr = new String[spinnerList.size()];
				// 默认全部被选中
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

	// 判断分组标题list中是否已经包含了当前分组标题
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
