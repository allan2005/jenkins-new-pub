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
 * @author ������
 * @version ����ʱ�䣺2013-7-23 ����4:21:52
 * ר������������棬Ϊ����ר�����������,listview���֣������Ŀ�����Ӧ��ר��������������ɸ��
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
				case 0://��ת���澯ר������ҳ��
					intent = new Intent(ActivityCategory.this,AlertSearchActivity.class);
					startActivity(intent);
					break;
				case 1://��ת������ר������ҳ��
					intent = new Intent(ActivityCategory.this,DeviceSearchActivity.class);
					startActivity(intent);
					break;
				case 2://��ת���ʹ�ר������ҳ��
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
