package com.ultrapower.android.search.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ultrapower.android.search.R;
import com.ultrapower.android.search.adapter.AllAdapter;
import com.ultrapower.android.search.adapter.CategoryResultAdapter;
import com.ultrapower.android.search.model.SearchResult;
import com.ultrapower.android.search.util.Constant;
import com.ultrapower.android.search.util.NetUtil;
import com.ultrapower.android.search.util.ThreadPoolManager;

import java.util.List;
import java.util.Map;

/**
 * 搜索页面
 * 
 * @author 黄丽慧 该类为整个万能搜索的核心，是真正执行搜索功能的类，可以实现专题搜索和全局搜索， 全局搜索可以进行历史关键字提示
 *         专题搜索的结果用listview显示，全局搜索结果用expandablelistview显示
 *         该页面中跳转的详细页面调的是UltraNmsMobile_Plus原来各自模块的界面
 */
public class ActivitySearch extends BaseSearchActivity implements OnClickListener {
	private Button search_bt_filter;// 筛查按钮，如果是搜索全部则隐藏
	private AutoCompleteTextView search_et_search;// 搜索框，可以进行历史关键字提示
	private ImageView search_ib_search;// 搜索按钮
	private ImageView search_ib_delete;// 搜索框里的清除按钮
	private ListView search_listview;// 用来显示分类筛查的数据
	private ExpandableListView search_listview_all;// 用来显示全部的数据;
	private ContentValues values;// post请求时的参数

	private Object result;// 搜索返回的结果

	// 用来接收全局搜索的显示数据，加载更多的数据也是要放到这个集合中，每次新加载都要清空该数据
	private Map<String, List<SearchResult>> allMap;
	// 用来接收专题搜索的显示数据，加载更多的数据也是要放到这个集合中，每次新加载都要清空该数据
	private List<SearchResult> categoryList;

	private AllAdapter allAdapter;
	private CategoryResultAdapter categoryAdapter;

	private SharedPreferences sp;
	// 信息提示框
	private ProgressDialog notice;

	private ArrayAdapter adapter;// 自动填充控件的适配器
	// 用来判断用户当前动作是否为加载更多,0为新加载，1为加载全局搜索的更多数据，2加载专题搜索的更多数据
	private int isMore = 0;
	private int mPageSize = 1;

	private final int GETDATACOMPLETED = 0;// 刷新数据完成的标记
	private final int CATEGORYMORECOMPLETED = 1;// 专题搜索结果的加载更多时的标记
	private final int ALLMORECOMPLETED = 2;// 全局搜索时加载更多的标记

	private InputMethodManager im;// 用来控制软键盘的显示和隐藏
	private Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case GETDATACOMPLETED:
				// 与后台交互完成
				if (result != null) {
					// 根据返回的类型来判定是执行的全局搜索（map）还是专题搜索（list）
					if (result instanceof Map) {
						// 当为全局搜索时，就让分类搜索结果显示框不可见
						search_listview.setVisibility(View.GONE);

						allMap = null;
						allMap = (Map<String, List<SearchResult>>) result;

						search_listview_all.setVisibility(View.VISIBLE);
						String content = search_et_search.getText().toString();
						if (allMap.size() > 0) {
							allAdapter = new AllAdapter(allMap, ActivitySearch.this, content);
							search_listview_all.setAdapter(allAdapter);
							search_listview_all.expandGroup(0);
						} else {
							Toast.makeText(ActivitySearch.this, R.string.all_no_data,
									Toast.LENGTH_LONG).show();

						}

					}

					if (result instanceof List) {
						// 当为全局搜索时，就让分类搜索结果显示框不可见

						search_listview_all.setVisibility(View.GONE);
						categoryList = null;
						categoryList = (List<SearchResult>) result;

						categoryAdapter = null;
						// 传入要显示的数据
						search_listview.setVisibility(View.VISIBLE);
						categoryAdapter = new CategoryResultAdapter(categoryList,
								ActivitySearch.this);
						search_listview.setAdapter(categoryAdapter);
					}
				}
				if (result == null) {
					if (!TextUtils.isEmpty(Constant.EXCEPTION)) {
						Toast.makeText(ActivitySearch.this, Constant.EXCEPTION, Toast.LENGTH_SHORT)
								.show();
					} else {
						Toast.makeText(ActivitySearch.this, R.string.load_no_data,
								Toast.LENGTH_SHORT).show();
					}
				}

				if (notice != null && notice.isShowing())
					notice.dismiss();
				break;
			case CATEGORYMORECOMPLETED:
				// 分类加载更多完毕，把数据添加到原来数据后面,然后更新视图
				if (result != null) {
					for (int i = 0; i < ((List<SearchResult>) result).size(); i++) {
						categoryList.add(((List<SearchResult>) result).get(i));
					}
					categoryAdapter.notifyDataSetChanged();
				}

				if (notice != null && notice.isShowing())
					notice.dismiss();
				break;

			case ALLMORECOMPLETED:
				// 全部加载更多完毕，把数据添加到原来数据后面,然后更新视图
				Map<String, List<SearchResult>> allMore = (Map<String, List<SearchResult>>) result;
				if (allMore != null) {
					for (Map.Entry<String, List<SearchResult>> entry : allMore.entrySet()) {
						String key = entry.getKey();
						List<SearchResult> moreList = entry.getValue();
						List<SearchResult> oldList = allMap.get(key);
						for (int j = 0; j < moreList.size(); j++) {
							oldList.add(moreList.get(j));
						}

						allMap.put(key, oldList);
					}

					allAdapter.notifyDataSetChanged();
				}

				if (notice != null && notice.isShowing())
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

		sp = getSharedPreferences("config", Activity.MODE_PRIVATE);
		im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		setContentView(R.layout.activity_search);

		// 专题搜索界面传递过来的数据，url的请求参数
		values = getIntent().getParcelableExtra("values");

		// 当values不为空时访问服务器,对页数和告警id进行初始化
		if (values != null) {

			// 加载新数据
			isMore = 0;
			String url = null;
			switch (Constant.SEARCH_TYPE) {
			case 1:
				url = Constant.URL_SEARCH_ALERT;
				break;
			case 2:
				url = Constant.URL_SEARCH_DEVICE;
				break;
			case 3:
				url = Constant.URL_SEARCH_RESOURCE;
				break;

			}

			getDataFromServer(url, Constant.PARSECATEGORYSEARCH, values);
		}

		findview();
		setListener();

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void findview() {

		search_bt_filter = (Button) findViewById(R.id.search_bt_filter);
		search_et_search = (AutoCompleteTextView) findViewById(R.id.search_et_search);
		search_ib_search = (ImageView) findViewById(R.id.search_ib_search);
		search_ib_delete = (ImageView) findViewById(R.id.search_ib_delete);
		search_listview = (ListView) findViewById(R.id.search_listview);
		search_listview_all = (ExpandableListView) findViewById(R.id.search_listview_all);

		// 当搜索全部分类时，隐藏筛选按钮
		if (0 == Constant.SEARCH_TYPE) {
			search_bt_filter.setText("专题");
		}

		// 当为专题搜索时，不弹出输入框

		if (Constant.SEARCH_TYPE > 0) {
			RelativeLayout search_rl_title = (RelativeLayout) findViewById(R.id.search_rl_title);
			search_rl_title.setFocusable(true);
			search_rl_title.setFocusableInTouchMode(true);
			search_et_search.clearFocus();
		}

		setAutoData();

	}

	// 为自动填充控件设置适配器，对搜索过的关键字进行提示，这里对关键字用逗号拼接后存储在sharedpeferences中
	private void setAutoData() {
		// 对取出来的字符串进行处理，让最后一次查询的放在最上面
		String keyWords = sp.getString("keywords", "");

		if (!TextUtils.isEmpty(keyWords)) {
			String[] keywords = keyWords.split(",");
			String[] reverseKeywords = new String[keywords.length];
			int j = keywords.length - 1;
			for (int i = 0; i < keywords.length; i++) {
				reverseKeywords[i] = keywords[j];
				j--;
			}

			adapter = null;
			adapter = new ArrayAdapter(this, R.layout.search_textview, reverseKeywords);
			search_et_search.setAdapter(adapter);

		}
	}

	private void setListener() {
		// TODO Auto-generated method stub
		search_bt_filter.setOnClickListener(this);
		search_ib_search.setOnClickListener(this);
		search_ib_delete.setOnClickListener(this);

		search_et_search.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// 当搜索框获取焦点是就能自动提示
				if (hasFocus) {
					if (!im.isActive())
						im.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
					setAutoData();
				} else {
					im.hideSoftInputFromWindow(v.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
		});
		// 点击更多时，向服务器请求数据
		search_listview_all.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition,
					long id) {
				// 点击最后一个group的时候就是加载更多
				if (groupPosition == 3) {

					List<SearchResult> alertResult = allMap.get("alert");
					if (alertResult != null)
						Constant.ALERTID = alertResult.get(alertResult.size() - 1).getID();
					// 加载更多时需要传递最后一条告警的id，便于分页查询，初始值为0
					values.put("alertId", Constant.ALERTID);

					mPageSize += 1;
					// 不知道相同关键字会不会被覆盖
					values.put("page", mPageSize);

					// 加载更多数据
					isMore = 1;
					getDataFromServer(Constant.URL_SEARCH_ALL, Constant.PARSEALL, values);

				}
				return false;
			}
		});

		// 点击不同的分类条目跳转到不同的详细页面中去
		search_listview_all.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
					int childPosition, long id) {
				// TODO Auto-generated method stub
				if (groupPosition < allMap.size()) {
					List<SearchResult> resultList = null;
					switch (groupPosition) {
					case 0:
						Constant.SEARCH_TYPE = 1;
						resultList = allMap.get("alert");
						break;
					case 1:
						Constant.SEARCH_TYPE = 2;
						resultList = allMap.get("device");
						break;
					case 2:
						Constant.SEARCH_TYPE = 3;
						resultList = allMap.get("resource");
						break;

					}

					// 获取每条数据的id值
					int itemId = 0;
					if (resultList != null) {
						itemId = resultList.get(childPosition).getID();
					}

					// 跳转到详细界面
					toDetailActivity(itemId);
				}
				return false;
			}
		});

		search_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				/*
				 * 点击最后一个是加载更多，其他是跳转到详细页面， 详细页面要根据分类搜索的不同，跳转到不同的界面
				 */
				if (categoryList.size() > 0 && position >= categoryList.size()) {
					// 加载更多
					if (1 == Constant.SEARCH_TYPE) {
						// 设置告警id
						Constant.ALERTID = categoryList.get(position - 1).getID();
						values.put("alertId", Constant.ALERTID);
					}
					// 设置页数
					values.put("page", ++mPageSize);

					// 加载更多数据
					isMore = 2;
					String url = null;
					switch (Constant.SEARCH_TYPE) {
					case 1:
						url = Constant.URL_SEARCH_ALERT;
						break;
					case 2:
						url = Constant.URL_SEARCH_DEVICE;
						break;
					case 3:
						url = Constant.URL_SEARCH_RESOURCE;
						break;

					}
					getDataFromServer(url, Constant.PARSECATEGORYSEARCH, values);

				} else {
					// 跳转到详细页面
					int id = categoryList.get(position).getID();
					toDetailActivity(id);
				}
			}
		});

		// 当选框里面有关键字时，显示删除按钮,没有关键字隐藏按钮
		search_et_search.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				String text = search_et_search.getText().toString();
				if (TextUtils.isEmpty(text)) {
					search_ib_delete.setVisibility(View.GONE);
				} else {
					search_ib_delete.setVisibility(View.VISIBLE);

				}
			}
		});
	}

	// 跳转到详细的页面
	private void toDetailActivity(int id) {
		Intent intent = new Intent();
		switch (Constant.SEARCH_TYPE) {
		case 1:
			// 掌控数宝的告警详细页面
			intent.setAction(Constant.ACTION_ALERT);
			intent.putExtra("EXTRA_ALERT_EVENTID", id + "");
			break;
		case 2:
			// 掌控数宝的性能详细页面
			intent.setAction(Constant.ACTION_DEVICE);
			intent.putExtra("EXTRA_DEVICE_RESID", id + "");
			intent.putExtra("EXTRA_DEVICE_SELECTTAB", 0);
			break;
		case 3:
			// 掌控数宝的资管详细页面
			intent.setAction(Constant.ACTION_RESOURCE);
			intent.putExtra("EXTRA_RESOURCE_RESID", id + "");

			break;

		}

		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		RelativeLayout search_rl_title = (RelativeLayout) findViewById(R.id.search_rl_title);
		search_rl_title.setFocusable(true);
		search_rl_title.setFocusableInTouchMode(true);
		search_et_search.clearFocus();

		if (id == R.id.search_bt_filter) {
			// 如果是分类搜索该按钮跳转到专题搜索的界面中，如果全局搜索则跳转到专题分类中去

			search_et_search.clearFocus();
			Intent intent = null;
			switch (Constant.SEARCH_TYPE) {
			case 0:
				intent = new Intent(this, ActivityCategory.class);
				break;
			case 1:
				intent = new Intent(this, AlertSearchActivity.class);
				break;
			case 2:
				intent = new Intent(this, DeviceSearchActivity.class);
				break;
			case 3:
				intent = new Intent(this, ResourceSearchActivity.class);
				break;

			}
			startActivity(intent);
			finish();
		} else if (id == R.id.search_ib_search) {
			// 根据条件开始从服务器获取数据
			// 取出输入框的内容,
			String keyword = search_et_search.getText().toString();
			if (!TextUtils.isEmpty(keyword)) {

				saveKeyWords(keyword);

				values = null;
				values = new ContentValues();

				values.put("searchKeywords", keyword);
				values.put("pageSize", Constant.PAGESIZE);
				values.put("page", Constant.PAGE);
				values.put("alertId", Constant.ALERTID);

				// 加载新数据,全局搜索
				isMore = 0;
				getDataFromServer(Constant.URL_SEARCH_ALL, Constant.PARSEALL, values);
			} else {
				Toast.makeText(this, "输入框内容不能为空！", Toast.LENGTH_LONG).show();

			}
		} else if (id == R.id.search_ib_delete) {
			// 点击清除把搜索框清空
			search_et_search.setText("");
		}
	}

	// 把搜索过的关键字保持下来进行提示
	private void saveKeyWords(String keyword) {

		String keywords = sp.getString("keywords", "");

		if (TextUtils.isEmpty(keywords)) {
			keywords = keyword;

		} else {
			if (!keywords.equals(keyword)) {
				if (keywords.contains(keyword)) {
					int start = keywords.indexOf(keyword);
					// 这里的处理有问题需要调试，另外自动填充有问题，应该让其失去焦点
					if (start > 0)
						keywords = keywords.substring(0, start - 1)
								+ keywords.substring(start + keyword.length(), keywords.length());
					else
						keywords = keywords.substring(0, start)
								+ keywords.substring(start + keyword.length() + 1,
										keywords.length());
				}

				keywords += "," + keyword;
			}
		}

		Editor editor = sp.edit();
		editor.putString("keywords", keywords);
		editor.commit();
	}

	// 创建线程与服务器交互
	private void getDataFromServer(String url, int parseMethodId, ContentValues values) {
		showNotice();
		// TODO Auto-generated method stub
		if (NetUtil.hasNetwork(this)) {
			ThreadPoolManager.getInstance().addTask(new NetRunnable(url, parseMethodId, values));
		} else {
			Toast.makeText(this, R.string.net_error, Toast.LENGTH_LONG).show();
		}
	}

	// 请求数据时的提示框的显示
	private void showNotice() {
		if (notice == null) {
			notice = new ProgressDialog(this);
			notice.setProgressStyle(ProgressDialog.STYLE_SPINNER);

			switch (Constant.SEARCH_TYPE) {
			case 0:
				// 设置ProgressDialog 标题
				notice.setTitle(R.string.loading_all_info);

				break;
			case 1:
				// 设置ProgressDialog 标题
				notice.setTitle(R.string.loading_alert_list);
				break;
			case 2:
				// 设置ProgressDialog 标题
				notice.setTitle(R.string.loading_device_list);
				break;
			case 3:
				// 设置ProgressDialog 标题
				notice.setTitle(R.string.loading_resource_list);
				break;

			}

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

	// 与后台进行交互
	class NetRunnable implements Runnable {
		private String url;
		private int parseMethodId;
		private ContentValues values;

		public NetRunnable(String url, int parseMethodId, ContentValues values) {
			this.url = url;
			this.parseMethodId = parseMethodId;
			this.values = values;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			result = NetUtil.get(values, url, parseMethodId);
			switch (isMore) {
			case 0:
				handler.sendEmptyMessage(GETDATACOMPLETED);
				break;
			case 1:
				handler.sendEmptyMessage(ALLMORECOMPLETED);
				break;
			case 2:
				handler.sendEmptyMessage(CATEGORYMORECOMPLETED);
				break;

			}

		}

	}

	// 对返回键进行处理，使得按返回键，返回的是搜索首页
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			Intent intent = new Intent(this, SearchMainActivity.class);
			startActivity(intent);
			finish();
		}

		return super.onKeyDown(keyCode, event);

	}
}
