package com.ultrapower.android.search.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ultrapower.android.search.R;
import com.ultrapower.android.search.adapter.CategoryAdapter;
import com.ultrapower.android.search.util.Constant;

/**
 * @author 黄丽慧
 * @version 创建时间：2013-7-23 下午4:21:52
 * 专题搜索分类界面，为各个专题搜索的入口,listview呈现，点击条目进入对应的专题搜索进行条件筛查
 */
public class ActivityCategory extends BaseSearchActivity {
	private ListView category_listview;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Constant.activityList.add(this);
		setContentView(R.layout.activity_category);
		
		findViews();
		setListener();
		
	}

	private void setListener() {
		
		category_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = null;
				switch (position) {
				case 0://跳转到告警专题搜索页面
					intent = new Intent(ActivityCategory.this,AlertSearchActivity.class);
					startActivity(intent);
					break;
				case 1://跳转到性能专题搜索页面
					intent = new Intent(ActivityCategory.this,DeviceSearchActivity.class);
					startActivity(intent);
					break;
				case 2://跳转到资管专题搜索页面
					intent = new Intent(ActivityCategory.this,ResourceSearchActivity.class);
					startActivity(intent);
					break;

				}
			}
		});
	}

	private void findViews() {
		// TODO Auto-generated method stub
		category_listview = (ListView) findViewById(R.id.category_listview);
		
		CategoryAdapter adapter = new CategoryAdapter(this);
		category_listview.setAdapter(adapter);
		
	}
}
