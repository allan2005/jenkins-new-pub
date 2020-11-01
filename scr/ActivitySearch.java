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
 * ����ҳ��
 * 
 * @author ������ ����Ϊ�������������ĺ��ģ�������ִ���������ܵ��࣬����ʵ��ר��������ȫ�������� ȫ���������Խ�����ʷ�ؼ�����ʾ
 *         ר�������Ľ����listview��ʾ��ȫ�����������expandablelistview��ʾ
 *         ��ҳ������ת����ϸҳ�������UltraNmsMobile_Plusԭ������ģ��Ľ���
 */
public class ActivitySearch extends BaseSearchActivity implements OnClickListener {
	private Button search_bt_filter;// ɸ�鰴ť�����������ȫ��������
	private AutoCompleteTextView search_et_search;// �����򣬿��Խ�����ʷ�ؼ�����ʾ
	private ImageView search_ib_search;// ������ť
	private ImageView search_ib_delete;// ��������������ť
	private ListView search_listview;// ������ʾ����ɸ�������
	private ExpandableListView search_listview_all;// ������ʾȫ��������;
	private ContentValues values;// post����ʱ�Ĳ���

	private Object result;// �������صĽ��

	// ��������ȫ����������ʾ���ݣ����ظ��������Ҳ��Ҫ�ŵ���������У�ÿ���¼��ض�Ҫ��ո�����
	private Map<String, List<SearchResult>> allMap;
	// ��������ר����������ʾ���ݣ����ظ��������Ҳ��Ҫ�ŵ���������У�ÿ���¼��ض�Ҫ��ո�����
	private List<SearchResult> categoryList;

	private AllAdapter allAdapter;
	private CategoryResultAdapter categoryAdapter;

	private SharedPreferences sp;
	// ��Ϣ��ʾ��
	private ProgressDialog notice;

	private ArrayAdapter adapter;// �Զ����ؼ���������
	// �����ж��û���ǰ�����Ƿ�Ϊ���ظ���,0Ϊ�¼��أ�1Ϊ����ȫ�������ĸ������ݣ�2����ר�������ĸ�������
	private int isMore = 0;
	private int mPageSize = 1;

	private final int GETDATACOMPLETED = 0;// ˢ��������ɵı��
	private final int CATEGORYMORECOMPLETED = 1;// ר����������ļ��ظ���ʱ�ı��
	private final int ALLMORECOMPLETED = 2;// ȫ������ʱ���ظ���ı��

	private InputMethodManager im;// ������������̵���ʾ������
	private Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case GETDATACOMPLETED:
				// ���̨�������
				if (result != null) {
					// ���ݷ��ص��������ж���ִ�е�ȫ��������map������ר��������list��
					if (result instanceof Map) {
						// ��Ϊȫ������ʱ�����÷������������ʾ�򲻿ɼ�
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
						// ��Ϊȫ������ʱ�����÷������������ʾ�򲻿ɼ�

						search_listview_all.setVisibility(View.GONE);
						categoryList = null;
						categoryList = (List<SearchResult>) result;

						categoryAdapter = null;
						// ����Ҫ��ʾ������
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
				// ������ظ�����ϣ���������ӵ�ԭ�����ݺ���,Ȼ�������ͼ
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
				// ȫ�����ظ�����ϣ���������ӵ�ԭ�����ݺ���,Ȼ�������ͼ
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

		// ר���������洫�ݹ��������ݣ�url���������
		values = getIntent().getParcelableExtra("values");

		// ��values��Ϊ��ʱ���ʷ�����,��ҳ���͸澯id���г�ʼ��
		if (values != null) {

			// ����������
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

		// ������ȫ������ʱ������ɸѡ��ť
		if (0 == Constant.SEARCH_TYPE) {
			search_bt_filter.setText("ר��");
		}

		// ��Ϊר������ʱ�������������

		if (Constant.SEARCH_TYPE > 0) {
			RelativeLayout search_rl_title = (RelativeLayout) findViewById(R.id.search_rl_title);
			search_rl_title.setFocusable(true);
			search_rl_title.setFocusableInTouchMode(true);
			search_et_search.clearFocus();
		}

		setAutoData();

	}

	// Ϊ�Զ����ؼ����������������������Ĺؼ��ֽ�����ʾ������Թؼ����ö���ƴ�Ӻ�洢��sharedpeferences��
	private void setAutoData() {
		// ��ȡ�������ַ������д��������һ�β�ѯ�ķ���������
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
				// ���������ȡ�����Ǿ����Զ���ʾ
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
		// �������ʱ�����������������
		search_listview_all.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition,
					long id) {
				// ������һ��group��ʱ����Ǽ��ظ���
				if (groupPosition == 3) {

					List<SearchResult> alertResult = allMap.get("alert");
					if (alertResult != null)
						Constant.ALERTID = alertResult.get(alertResult.size() - 1).getID();
					// ���ظ���ʱ��Ҫ�������һ���澯��id�����ڷ�ҳ��ѯ����ʼֵΪ0
					values.put("alertId", Constant.ALERTID);

					mPageSize += 1;
					// ��֪����ͬ�ؼ��ֻ᲻�ᱻ����
					values.put("page", mPageSize);

					// ���ظ�������
					isMore = 1;
					getDataFromServer(Constant.URL_SEARCH_ALL, Constant.PARSEALL, values);

				}
				return false;
			}
		});

		// �����ͬ�ķ�����Ŀ��ת����ͬ����ϸҳ����ȥ
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

					// ��ȡÿ�����ݵ�idֵ
					int itemId = 0;
					if (resultList != null) {
						itemId = resultList.get(childPosition).getID();
					}

					// ��ת����ϸ����
					toDetailActivity(itemId);
				}
				return false;
			}
		});

		search_listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				/*
				 * ������һ���Ǽ��ظ��࣬��������ת����ϸҳ�棬 ��ϸҳ��Ҫ���ݷ��������Ĳ�ͬ����ת����ͬ�Ľ���
				 */
				if (categoryList.size() > 0 && position >= categoryList.size()) {
					// ���ظ���
					if (1 == Constant.SEARCH_TYPE) {
						// ���ø澯id
						Constant.ALERTID = categoryList.get(position - 1).getID();
						values.put("alertId", Constant.ALERTID);
					}
					// ����ҳ��
					values.put("page", ++mPageSize);

					// ���ظ�������
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
					// ��ת����ϸҳ��
					int id = categoryList.get(position).getID();
					toDetailActivity(id);
				}
			}
		});

		// ��ѡ�������йؼ���ʱ����ʾɾ����ť,û�йؼ������ذ�ť
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

	// ��ת����ϸ��ҳ��
	private void toDetailActivity(int id) {
		Intent intent = new Intent();
		switch (Constant.SEARCH_TYPE) {
		case 1:
			// �ƿ������ĸ澯��ϸҳ��
			intent.setAction(Constant.ACTION_ALERT);
			intent.putExtra("EXTRA_ALERT_EVENTID", id + "");
			break;
		case 2:
			// �ƿ�������������ϸҳ��
			intent.setAction(Constant.ACTION_DEVICE);
			intent.putExtra("EXTRA_DEVICE_RESID", id + "");
			intent.putExtra("EXTRA_DEVICE_SELECTTAB", 0);
			break;
		case 3:
			// �ƿ��������ʹ���ϸҳ��
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
			// ����Ƿ��������ð�ť��ת��ר�������Ľ����У����ȫ����������ת��ר�������ȥ

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
			// ����������ʼ�ӷ�������ȡ����
			// ȡ������������,
			String keyword = search_et_search.getText().toString();
			if (!TextUtils.isEmpty(keyword)) {

				saveKeyWords(keyword);

				values = null;
				values = new ContentValues();

				values.put("searchKeywords", keyword);
				values.put("pageSize", Constant.PAGESIZE);
				values.put("page", Constant.PAGE);
				values.put("alertId", Constant.ALERTID);

				// ����������,ȫ������
				isMore = 0;
				getDataFromServer(Constant.URL_SEARCH_ALL, Constant.PARSEALL, values);
			} else {
				Toast.makeText(this, "��������ݲ���Ϊ�գ�", Toast.LENGTH_LONG).show();

			}
		} else if (id == R.id.search_ib_delete) {
			// �����������������
			search_et_search.setText("");
		}
	}

	// ���������Ĺؼ��ֱ�������������ʾ
	private void saveKeyWords(String keyword) {

		String keywords = sp.getString("keywords", "");

		if (TextUtils.isEmpty(keywords)) {
			keywords = keyword;

		} else {
			if (!keywords.equals(keyword)) {
				if (keywords.contains(keyword)) {
					int start = keywords.indexOf(keyword);
					// ����Ĵ�����������Ҫ���ԣ������Զ���������⣬Ӧ������ʧȥ����
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

	// �����߳������������
	private void getDataFromServer(String url, int parseMethodId, ContentValues values) {
		showNotice();
		// TODO Auto-generated method stub
		if (NetUtil.hasNetwork(this)) {
			ThreadPoolManager.getInstance().addTask(new NetRunnable(url, parseMethodId, values));
		} else {
			Toast.makeText(this, R.string.net_error, Toast.LENGTH_LONG).show();
		}
	}

	// ��������ʱ����ʾ�����ʾ
	private void showNotice() {
		if (notice == null) {
			notice = new ProgressDialog(this);
			notice.setProgressStyle(ProgressDialog.STYLE_SPINNER);

			switch (Constant.SEARCH_TYPE) {
			case 0:
				// ����ProgressDialog ����
				notice.setTitle(R.string.loading_all_info);

				break;
			case 1:
				// ����ProgressDialog ����
				notice.setTitle(R.string.loading_alert_list);
				break;
			case 2:
				// ����ProgressDialog ����
				notice.setTitle(R.string.loading_device_list);
				break;
			case 3:
				// ����ProgressDialog ����
				notice.setTitle(R.string.loading_resource_list);
				break;

			}

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

	// ���̨���н���
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

	// �Է��ؼ����д���ʹ�ð����ؼ������ص���������ҳ
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
