package com.example.servicetest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TestActivity extends Activity {

	private ListView listview;
	private TextView txt;
	ArrayList<String> fileName = new ArrayList<>();
	String[] XSerise;
	String[] YSerise;
	String[] ZSerise;
	String[] SSerise;
	ArrayList<Float> XSet = new ArrayList<>();
	ArrayList<Float> YSet = new ArrayList<>();
	ArrayList<Float> ZSet = new ArrayList<>();
	ArrayList<Float> SSet = new ArrayList<>();
	String contant = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		listview = (ListView) findViewById(R.id.lv);
		txt = (TextView) findViewById(R.id.txt);
		Intent intent = getIntent();
		if ((intent.getIntExtra("opentype", -1) == 1)) {
			fileName = (ArrayList<String>) intent.getSerializableExtra("fileName");
		} else if (intent.getIntExtra("opentype", -1) == 2) {
			try {
				Log.v("TestActivity", "进入初始化");
				String name = intent.getStringExtra("onefilename");
				contant = readSDcard(name);
				String[] temp = null;
				temp = contant.split("\n");
				XSerise = temp[0].split(" ");
				YSerise = temp[1].split(" ");
				ZSerise = temp[2].split(" ");
				SSerise = temp[3].split(" ");
				if (SSet.size() != 0 || XSet.size() != 0 || YSet.size() != 0 || ZSet.size() != 0) {
					SSet.clear();
					XSet.clear();
					YSet.clear();
					ZSet.clear();
				}
				for (int i = 0; i < XSerise.length; i++) {
					SSet.add(Float.parseFloat(SSerise[i]));
					XSet.add(Float.parseFloat(XSerise[i]));
					YSet.add(Float.parseFloat(YSerise[i]));
					ZSet.add(Float.parseFloat(ZSerise[i]));
				}
				Intent newintent = new Intent();
				newintent.putExtra("XSet", XSet);
				newintent.putExtra("YSet", YSet);
				newintent.putExtra("ZSet", ZSet);
				newintent.putExtra("SSet", SSet);
				newintent.putExtra("formNotification", false);
				newintent.setClass(TestActivity.this, AnalyseActivity.class);
				startActivity(intent);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			finish();
		}

		listview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, getData()));
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				try {
					String name = fileName.get(arg2);
					contant = readSDcard(name);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String[] temp = null;
				temp = contant.split("\n");
				XSerise = temp[0].split(" ");
				YSerise = temp[1].split(" ");
				ZSerise = temp[2].split(" ");
				SSerise = temp[3].split(" ");
				if (SSet.size() != 0 || XSet.size() != 0 || YSet.size() != 0 || ZSet.size() != 0) {
					SSet.clear();
					XSet.clear();
					YSet.clear();
					ZSet.clear();
				}
				for (int i = 0; i < XSerise.length; i++) {
					SSet.add(Float.parseFloat(SSerise[i]));
					XSet.add(Float.parseFloat(XSerise[i]));
					YSet.add(Float.parseFloat(YSerise[i]));
					ZSet.add(Float.parseFloat(ZSerise[i]));
				}
				Intent intent = new Intent();
				intent.putExtra("XSet", XSet);
				intent.putExtra("YSet", YSet);
				intent.putExtra("ZSet", ZSet);
				intent.putExtra("SSet", SSet);
				intent.putExtra("formNotification", false);
				intent.setClass(TestActivity.this, AnalyseActivity.class);
				startActivity(intent);

				// TODO 这里可以以后写处理数据的方法
			}
		});

	}

	private String readSDcard(String name) throws IOException {
		StringBuffer str = new StringBuffer();
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File file = Environment.getExternalStorageDirectory();
			String path = file.getCanonicalPath() + "/com.example.test" + "/" + name;

			File fpath = new File(path);// 获得SD卡路径
			if (fpath.exists()) {
				FileInputStream fileR = new FileInputStream(fpath);
				BufferedReader reads = new BufferedReader(new InputStreamReader(fileR));
				String st = null;
				while ((st = reads.readLine()) != null) {
					str.append(st + "\n");
				}
			}

		}
		return str.toString();
	}

	private List<String> getData() {
		return fileName;
	}

	void OnPause() {
		// super.onStop();
		finish();
	}
}
