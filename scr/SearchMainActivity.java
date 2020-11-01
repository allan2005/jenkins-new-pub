package com.ultrapower.android.search.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ultrapower.android.search.R;
import com.ultrapower.android.search.adapter.MainAdapter;
import com.ultrapower.android.search.model.HotCategory;
import com.ultrapower.android.search.util.Constant;
import com.ultrapower.android.search.util.NetUtil;
import com.ultrapower.android.search.util.ThreadPoolManager;
import com.ultrapower.android.search.util.UltraEncryptionUtil;
import com.ultrapower.android.search.util.UltraPhoneStateUtil;

import java.util.ArrayList;
import java.util.List;

public class SearchMainActivity extends BaseSearchActivity implements OnClickListener{

	private RelativeLayout main_ll_search;
	private Button main_bt_category;
	private EditText main_et_search;
	private ExpandableListView main_listview;
	private Object result;
	private final int GETDATACOMPLETED = 0;
	private final int LOADHISTORY = 2;
	private String[] hotCategory ;
	private List<String> historyItem;
	private String[] titles = {"历史分类","热门分类"};
	private SharedPreferences sp;
	
	private ProgressDialog notice;
	private MainAdapter adapter;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case GETDATACOMPLETED:
				if(result!=null){
					List<HotCategory> categoryList = (List<HotCategory>) result;
					
					//这里没有对热门分类进行排序
					hotCategory = new String[categoryList.size()];
					
					for(int i=0;i<categoryList.size();i++){
						hotCategory[i] = categoryList.get(i).getOPTNAME();
					}
					Constant.HOTCATEGORY = hotCategory;
				}
				
				if(!TextUtils.isEmpty(Constant.EXCEPTION))
					Toast.makeText(SearchMainActivity.this, Constant.EXCEPTION, Toast.LENGTH_LONG).show();
				
				loadHistory();
				if(notice!=null&&notice.isShowing())
					notice.dismiss();
				break;

			case 1:
				//删除键被点击变化数据
				int position = (Integer) msg.obj;
				historyItem.remove(position);
				if(adapter!=null){
					adapter.notifyDataSetChanged();
				}
				break;
			case LOADHISTORY:
				loadHistory();
				break;
			}
			
		};
	};
	
	private void loadHistory(){
		String history = sp.getString("history", "");
		if(!TextUtils.isEmpty(history)){
			historyItem = new ArrayList<String>();
			String[] his = history.split(",");
			for(String hist:his){
				historyItem.add(hist);
			}
		}
		
		hotCategory = Constant.HOTCATEGORY;
		adapter = new MainAdapter(handler,SearchMainActivity.this, titles, hotCategory, historyItem);
		main_listview.setAdapter(adapter);
		//如果有数据，那么就把子项展开
		if(adapter!=null){
		int count = adapter.getGroupCount();
		if(count>0){
			for(int i=0;i<count;i++){
				main_listview.expandGroup(i);
			}
			
			}
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Constant.activityList.add(this);
		sp = getSharedPreferences("config", Activity.MODE_PRIVATE);
		
		//获取当前手机的加密成md5的的sim号
		Constant.SIM_MD5 = UltraEncryptionUtil.toMD5(UltraPhoneStateUtil.getIMSI(this));
		
		
		//与后台进行交互
		if(Constant.HOTCATEGORY==null){
			getDataFromServer(Constant.URL_HOT_CATEGORY,Constant.PARSEHOTCATEGORY);
		}else{
			handler.sendEmptyMessage(LOADHISTORY);
		}
		
		setContentView(R.layout.activity_main);
		//获取控件
		findview();
		setListener();
		
		
	}
	
	//加载数据是的提示
	private void showDialog(){
		if(notice==null){
			notice = new ProgressDialog(this);
			notice.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	          // 设置ProgressDialog 标题
	          notice.setTitle(R.string.loading_hotcategory_info);
	          // 设置ProgressDialog 提示信息d
	          notice.setMessage(getResources().getString(R.string.loading));
	          // 设置ProgressDialog 的进度条是否不明确
	          notice.setIndeterminate(false);
	          // 设置ProgressDialog 是否可以按退回按键取消
	          notice.setCancelable(true);
	          notice.show();
	          
		}else{
			notice.show();
		}
	}
	private void setListener() {
		// TODO Auto-generated method stub
		main_bt_category.setOnClickListener(this);
		main_et_search.setOnClickListener(this);
		
		main_listview.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				
				TextView history_item_title = (TextView) v.findViewById(R.id.history_item_title);
				
				String text = history_item_title.getText().toString().trim();
					Intent intent = null;
					if(text.equals("网管告警")){
						intent = new Intent(SearchMainActivity.this,AlertSearchActivity.class);
						startActivity(intent);
					}
					if(text.equals("网管性能")){
						intent = new Intent(SearchMainActivity.this,DeviceSearchActivity.class);
						startActivity(intent);
					}
					if(text.equals("网管资管")){
						intent = new Intent(SearchMainActivity.this,ResourceSearchActivity.class);
						startActivity(intent);
					}
					
				return false;
			}
		});
	}

	private void findview(){
		main_ll_search = (RelativeLayout) findViewById(R.id.main_ll_search);
		main_bt_category = (Button) findViewById(R.id.main_bt_category);
		main_et_search = (EditText) findViewById(R.id.main_et_search);
		main_listview = (ExpandableListView) findViewById(R.id.main_listview);
		
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.main_bt_category) {
			Intent intent = new Intent(this,ActivityCategory.class);
			startActivity(intent);
		}else if(id == R.id.main_et_search){
			Constant.SEARCH_TYPE = 0;
			Intent intent = new Intent(SearchMainActivity.this,ActivitySearch.class);
			startActivity(intent);
		}
	}

	//当values为空时，搜索全部
		private void getDataFromServer(String url,int parseMethodId) {
			// TODO Auto-generated method stub
			showDialog();
			if(NetUtil.hasNetwork(this)){
				ThreadPoolManager.getInstance().addTask(new NetRunnable(url,parseMethodId));
				
			}else{
				Toast.makeText(this, R.string.net_error, Toast.LENGTH_LONG).show();
			}
		}
		
		//与后台进行交互
			class NetRunnable implements Runnable{
				private String url;
				private int parseMethodId;
				public NetRunnable(String url,int parseMethodId){
					this.url = url;
					this.parseMethodId = parseMethodId;
				}
				@Override
				public void run() {
					// TODO Auto-generated method stub
					result = NetUtil.get(null,url,parseMethodId);
					handler.sendEmptyMessage(GETDATACOMPLETED);
				}
				
			}
	
		@Override
		protected void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
			//使界面显示时，输入框都是失去焦点
			main_ll_search.setFocusable(true);
			main_ll_search.setFocusableInTouchMode(true);
			main_et_search.clearFocus();
		}
		
		//对返回键进行处理，使得按返回键，返回的是掌控数宝的首页
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if(keyCode==KeyEvent.KEYCODE_BACK){
				
				for(Activity activity:Constant.activityList){
					activity.finish();
				}
			}
			
			return super.onKeyDown(keyCode, event);
			
		}
}
