package com.example.servicetest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	private Timer timer = new Timer();
	private TimerTask task;
	private Handler handler;
	private String title = "Signal";
	private XYSeries Xseries;
	private XYSeries Yseries;
	private XYSeries Zseries;
	private XYSeries Sseries;
	private XYMultipleSeriesDataset mDataset;
	private GraphicalView chart;
	private XYMultipleSeriesRenderer renderer;
	private Context context;
	private int addX = -1;

	private float X, Y, Z;
	private static final int length = 1026; // 缓存长度

	private ArrayList<Float> Xacc = new ArrayList<Float>();
	private ArrayList<Float> Yacc = new ArrayList<Float>();
	private ArrayList<Float> Zacc = new ArrayList<Float>();
	private ArrayList<Double> Sacc = new ArrayList<Double>();

	int[] xv = new int[length];// 这是X轴的点
	float[] Xyv = new float[length];// 这是对应X加速度的Y轴暂存点
	float[] Yyv = new float[length];// 这是对应Y加速度的Y轴暂存点
	float[] Zyv = new float[length];// 这是对应Z加速度的Y轴暂存点
	float[] Syv = new float[length];// 这是对应SVM的Y轴暂存点

	//private EditText editText = null;
	private MyReceiver receiver = null;
	private Button writebtn, readbtn;
	private Button btn_yes;
	private TextView temptxt;

	public final static String ACTION_BTN_YES = "com.example.Servicetest.btn_yes";
	public final static String ACTION_BTN_NO = "com.example.Servicetest.btn_no";
	public final static String INTENT_NAME = "btnid";

	public final static int INTENT_BTN_LOGIN = 1;

	// 通知栏事件
	NotificationManager mNotificationManager;
	// NotificationCompat.Builder mBuilder;
	int notifyId = 101;
	private static final int WINDOW_LENGTH = 80;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//editText = (EditText) findViewById(R.id.editText);
		writebtn = (Button) findViewById(R.id.writebtn);
		readbtn = (Button) findViewById(R.id.readbtn);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		temptxt = (TextView) findViewById(R.id.temptxt);
		// 启动服务
		startService(new Intent(MainActivity.this, CountService.class));
		// 注册广播接收器
		receiver = new MyReceiver();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.example.CountService");
		filter.addAction(ACTION_BTN_YES);
		filter.addAction(ACTION_BTN_NO);
		MainActivity.this.registerReceiver(receiver, filter);

		//InitFrame();
		Log.v("CountService", "初始化完毕");

		writebtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String str = "";
				String strX = "";
				String strY = "";
				String strZ = "";
				String strS = "";
				if (Xacc.size() != 0 && Yacc.size() != 0 && Zacc.size() != 0) {
					int lenX = Xacc.size();
					int lenY = Yacc.size();
					int lenZ = Zacc.size();

					if (lenX == lenY && lenY == lenZ) {
						for (int k = 0; k < lenX; k++) {
							if (k != lenX - 1) {
								strX += Xacc.get(k) + " ";
								strY += Yacc.get(k) + " ";
								strZ += Zacc.get(k) + " ";
								double acc = Sacc.get(k);
								strS += (float) acc + " ";
							} else {
								strX += Xacc.get(k);
								strY += Yacc.get(k);
								strZ += Zacc.get(k);
								double acc = Sacc.get(k);
								strS += (float) acc;
							}

						}
					}
				}
				str = strX + "\n" + strY + "\n" + strZ + "\n" + strS;
				writeSDcard(str);
			}
		});
		readbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					ArrayList<String> fileName = new ArrayList<String>();
					fileName = readSDcard();

					Intent intent = new Intent();
					intent.putExtra("opentype", 1);
					intent.putExtra("fileName", fileName);
					intent.setClass(MainActivity.this, TestActivity.class);
					startActivity(intent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 获取广播数据
	 * 
	 * @author jiqinlin
	 *
	 */
	public class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action != "com.example.CountService") {
				Log.v("CountService", action);
			}

			if (action.equals("com.example.CountService")) {
				int lastsize = Xacc.size();
				Bundle bundle = intent.getExtras();
				int count = bundle.getInt("count");

				Xacc = (ArrayList<Float>) bundle.getSerializable("Xacc");
				Yacc = (ArrayList<Float>) bundle.getSerializable("Yacc");
				Zacc = (ArrayList<Float>) bundle.getSerializable("Zacc");
				Sacc = (ArrayList<Double>) bundle.getSerializable("Sacc");
				if (Sacc.size() > WINDOW_LENGTH) {
					if (judgeFall()) {
						showNotify();
					}
				}
				//editText.setText(Sacc.get(0) + "");
				//updateChart();
			}
			if (action.equals(ACTION_BTN_YES)) {
				try {
					String[] XSerise;
					String[] YSerise;
					String[] ZSerise;
					String[] SSerise;
					ArrayList<Float> XSet = new ArrayList<>();
					ArrayList<Float> YSet = new ArrayList<>();
					ArrayList<Float> ZSet = new ArrayList<>();
					ArrayList<Float> SSet = new ArrayList<>();
					ArrayList<String> fileName = new ArrayList<String>();
					fileName = readSDcard();
					SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					Date date_now = new java.util.Date();
					long temp = 0;
					Date temp_date;
					String datestr = "";
					for (int i = 0; i < fileName.size(); i++) {
						String str = fileName.get(i);
						str = str.replace("_", ":");
						str = str.substring(0, str.length() - 4);
						Date date = sDateFormat.parse(str);

						if (i == 0) {
							temp = getIntervalDays(date, date_now);
						}
						if (getIntervalDays(date, date_now) < temp) {
							temp_date = date;
							temp = getIntervalDays(date, date_now);
							datestr = sDateFormat.format(temp_date) + ".txt";

						}
					}
					if (datestr != "") {

						String contant = "";
						contant = readFile(datestr);
						showMessage("进入打开文件" + datestr);
						temptxt.setText(contant);
						String[] tempStrArray = null;
						tempStrArray = contant.split("\n");
						XSerise = tempStrArray[0].split(" ");
						YSerise = tempStrArray[1].split(" ");
						ZSerise = tempStrArray[2].split(" ");
						SSerise = tempStrArray[3].split(" ");
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

					}
					Intent newintent = new Intent();
					newintent.putExtra("XSet", XSet);
					newintent.putExtra("YSet", YSet);
					newintent.putExtra("ZSet", ZSet);
					newintent.putExtra("SSet", SSet);
					newintent.putExtra("formNotification", false);
					newintent.setClass(MainActivity.this, AnalyseActivity.class);
					startActivity(newintent);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (action.equals(ACTION_BTN_NO)) {
				showMessage("取消");
				clearNotify(notifyId);
			}
		}
	}

	public void InitFrame() {
		Log.v("CountService", "进入初始化");
		context = getApplicationContext();
		// 这里获得main界面上的布局，下面会把图表画在这个布局里面
		LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout1);
		// 这个类用来放置曲线上的所有点，是一个点的集合，根据这些点画出曲线
		Xseries = new XYSeries(title);
		Yseries = new XYSeries(title);
		Zseries = new XYSeries(title);
		Sseries = new XYSeries(title);
		// 创建一个数据集的实例，这个数据集将被用来创建图表
		mDataset = new XYMultipleSeriesDataset();
		// 将点集添加到这个数据集中
		mDataset.addSeries(0, Xseries);
		mDataset.addSeries(1, Yseries);
		mDataset.addSeries(2, Zseries);
		mDataset.addSeries(3, Sseries);
		// 以下都是曲线的样式和属性等等的设置，renderer相当于一个用来给图表做渲染的句柄
		int color = Color.BLACK;
		PointStyle style = PointStyle.POINT;
		renderer = buildRenderer(color, style, true);
		renderer.setPanLimits(new double[] { 0, 10000, -30, 30 });
		renderer.setZoomEnabled(false, true);
		renderer.setZoomInLimitX(1);
		renderer.setZoomInLimitY(5);
		// 设置好图表的样式
		setChartSettings(renderer, "X", "Y", 0, 100, -10, 10, Color.WHITE, Color.WHITE);
		// 生成图表
		chart = ChartFactory.getCubeLineChartView(context, mDataset, renderer, 0);
		// 将图表添加到布局中去
		layout.addView(chart, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}

	protected XYMultipleSeriesRenderer buildRenderer(int color, PointStyle style, boolean fill) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		// 设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
		XYSeriesRenderer Xrenderer = new XYSeriesRenderer();
		Xrenderer.setColor(Color.GREEN);
		Xrenderer.setPointStyle(style);
		Xrenderer.setFillPoints(fill);
		Xrenderer.setLineWidth(1);
		XYSeriesRenderer Yrenderer = new XYSeriesRenderer();
		Yrenderer.setColor(Color.RED);
		Yrenderer.setPointStyle(style);
		Yrenderer.setFillPoints(fill);
		Yrenderer.setLineWidth(1);
		XYSeriesRenderer Zrenderer = new XYSeriesRenderer();
		Zrenderer.setColor(Color.BLUE);
		Zrenderer.setPointStyle(style);
		Zrenderer.setFillPoints(fill);
		Zrenderer.setLineWidth(1);
		XYSeriesRenderer Srenderer = new XYSeriesRenderer();
		Srenderer.setColor(Color.BLACK);
		Srenderer.setPointStyle(style);
		Srenderer.setFillPoints(fill);
		Srenderer.setLineWidth(1);
		renderer.addSeriesRenderer(Xrenderer);
		renderer.addSeriesRenderer(Yrenderer);
		renderer.addSeriesRenderer(Zrenderer);
		renderer.addSeriesRenderer(Srenderer);
		return renderer;
	}

	protected void setChartSettings(XYMultipleSeriesRenderer renderer, String xTitle, String yTitle, double xMin,
			double xMax, double yMin, double yMax, int axesColor, int labelsColor) {
		// 有关对图表的渲染可参看api文档
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
		renderer.setShowGrid(false);
		renderer.setXLabels(20);
		renderer.setYLabels(10);
		renderer.setXTitle("Time");
		renderer.setYTitle("num");
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setPointSize((float) 2);
		renderer.setShowLegend(false);
	}

	private void updateChart() {
		// 设置好下一个需要增加的节点
		addX = 0;
		// 移除数据集中旧的点集
		mDataset.removeSeries(Xseries);
		mDataset.removeSeries(Yseries);
		mDataset.removeSeries(Zseries);
		mDataset.removeSeries(Sseries);
		// 判断当前点集中到底有多少点，因为屏幕总共只能容纳100个，所以当点数超过100时，长度永远是100
		int len = Xseries.getItemCount();
		if (len > length) {
			len = length;
		}
		// 将旧的点集中x和y的数值取出来放入backup中，并且将x的值加1，造成曲线向右平移的效果
		for (int i = 0; i < len; i++) {
			xv[i] = (int) Xseries.getX(i) + 1;
			Xyv[i] = (float) Xseries.getY(i);
			Yyv[i] = (float) Yseries.getY(i);
			Zyv[i] = (float) Zseries.getY(i);
			Syv[i] = (float) Sseries.getY(i);
		}
		// 点集先清空，为了做成新的点集而准备
		Xseries.clear();
		Yseries.clear();
		Zseries.clear();
		Sseries.clear();
		/*
		 * 将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中 //
		 * 这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点 series.add(addX, addY); for
		 * (int k = 0; k < length; k++) { series.add(xv[k], yv[ k]); }
		 * mDataset.addSeries(series); 在数据集中添加新的点集
		 */
		// 将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
		// 这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
		if (Xacc.size() != 0) {
			Xseries.add(addX, (float) Xacc.get(0));
			for (int k = 0; k < len; k++) {
				// float a = (float) Xacc.get(k);
				Xseries.add(xv[k], Xyv[k]);
			}
			// 在数据集中添加新的点集
			mDataset.addSeries(0, Xseries);
		}
		if (Yacc.size() != 0) {
			Yseries.add(addX, (float) Yacc.get(0));
			for (int k = 0; k < len; k++) {
				// float a = (float) Yacc.get(k);
				Yseries.add(xv[k], Yyv[k]);
			}
			// 在数据集中添加新的点集
			mDataset.addSeries(1, Yseries);
		}

		if (Zacc.size() != 0) {
			Zseries.add(addX, (float) Zacc.get(0));
			for (int k = 0; k < len; k++) {
				// float a = (float) Zacc.get(k);
				Zseries.add(xv[k], Zyv[k]);
			}
			// 在数据集中添加新的点集
			mDataset.addSeries(2, Zseries);
		}
		if (Sacc.size() != 0) {
			Sseries.add(addX, Sacc.get(0));
			for (int k = 0; k < len; k++) {
				// float a = (float) Zacc.get(k);
				Sseries.add(xv[k], Syv[k]);
			}
			// 在数据集中添加新的点集
			mDataset.addSeries(3, Sseries);
		}
		// 视图更新，没有这一步，曲线不会呈现动态
		// 如果在非UI主线程中，需要调用postInvalidate()，具体参考api
		chart.invalidate();
		// checkfall
	}

	// 把数据写入SD卡
	private void writeSDcard(String str) {
		try {
			// 判断是否存在SD卡
			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			String date = sDateFormat.format(new java.util.Date());
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				// 获取SD卡的目录
				File file = Environment.getExternalStorageDirectory();
				String path = file.getCanonicalPath() + "/com.example.test";
				File filedir = new File(path);
				if (!filedir.exists())
					filedir.mkdir();
				System.out.println(file.getCanonicalPath() + "/com.example.test" + "/" + date + ".txt");
				FileOutputStream fileW = new FileOutputStream(
						file.getCanonicalPath() + "/com.example.test" + "/" + date + ".txt");
				fileW.write(str.getBytes());
				fileW.close();
			} else {
				showMessage("SD卡不存在！！");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 从SD卡中读取数据
	private ArrayList<String> readSDcard() throws IOException {

		ArrayList<String> fileName = new ArrayList<String>();
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File file = Environment.getExternalStorageDirectory();
			String path = file.getCanonicalPath() + "/com.example.test";

			File fpath = new File(path);// 获得SD卡路径
			File[] filesArray = fpath.listFiles();// 读取

			for (int i = 0; i < filesArray.length; i++) {
				File fp = filesArray[i];
				if (fp.exists()) {
					// 打开文件输入流
					fileName.add(fp.getName());
				} else {
					showMessage("该目录下文件不存在");
				}
			}
		} else {
			showMessage("SD卡不存在！！");
		}
		return fileName;
	}

	private String readFile(String name) throws IOException {
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

	public boolean judgeFall() {
		int tem = -1;
		double svm = 0;
		ArrayList<Float> XaccWindow = new ArrayList<Float>();
		ArrayList<Float> YaccWindow = new ArrayList<Float>();
		ArrayList<Float> ZaccWindow = new ArrayList<Float>();
		ArrayList<Double> SaccWindow = new ArrayList<Double>();
		int count = 0;
		for (int i = 0; i < Sacc.size(); i++) {

			if (Sacc.get(i) > 20 && Sacc.get(i) > svm) {
				tem = i;
				svm = Sacc.get(i);
				// showMessage("疑似跌倒发生");

			}
		}
		if (tem > 0) {
			if (tem - WINDOW_LENGTH / 2 > 0 && tem + WINDOW_LENGTH / 2 < Sacc.size()) {
				for (int i = 0; i < WINDOW_LENGTH; i++) {
					SaccWindow.add(Sacc.get(tem - WINDOW_LENGTH / 2 + i));
				}
				for (int i = 0; i < WINDOW_LENGTH; i++) {
					XaccWindow.add(Xacc.get(tem - WINDOW_LENGTH / 2 + i));
				}
				for (int i = 0; i < WINDOW_LENGTH; i++) {
					YaccWindow.add(Yacc.get(tem - WINDOW_LENGTH / 2 + i));
				}
				for (int i = 0; i < WINDOW_LENGTH; i++) {
					ZaccWindow.add(Zacc.get(tem - WINDOW_LENGTH / 2 + i));
				}
				double min = 100;
				for (int i = WINDOW_LENGTH / 2 - 10; i < WINDOW_LENGTH / 2 + 10; i++) {
					if (SaccWindow.get(i) > 20&&SaccWindow.get(i)>SaccWindow.get(i+1)&&SaccWindow.get(i)>SaccWindow.get(i-1)) {
						count++;
					}
					if (SaccWindow.get(i) < min) {
						min = SaccWindow.get(i);
					}
				}
				float avgXBefore = getAverage(WINDOW_LENGTH / 2 + 10, WINDOW_LENGTH / 2 + 20, XaccWindow);
				float avgYBefore = getAverage(WINDOW_LENGTH / 2 + 10, WINDOW_LENGTH / 2 + 20, YaccWindow);
				float avgZBefore = getAverage(WINDOW_LENGTH / 2 + 10, WINDOW_LENGTH / 2 + 20, ZaccWindow);
				float avgXAfter = getAverage(WINDOW_LENGTH / 2 - 20, WINDOW_LENGTH / 2 - 10, XaccWindow);
				float avgYAfter = getAverage(WINDOW_LENGTH / 2 - 20, WINDOW_LENGTH / 2 - 10, YaccWindow);
				float avgZAfter = getAverage(WINDOW_LENGTH / 2 - 20, WINDOW_LENGTH / 2 - 10, ZaccWindow);
				String statusBefore = showStatus(avgXBefore, avgYBefore, avgZBefore);
				String statusAfter = showStatus(avgXAfter, avgYAfter, avgZAfter);
				temptxt.setText("avgXBefore: " + avgXBefore + "\n" + "avgYBefore: " + avgYBefore + "\n" + "avgZBefore: "
						+ avgZBefore + "\n" + "avgXAfter: " + avgXAfter + "\n" + "avgYAfter: " + avgYAfter + "\n"
						+ "avgZAfter: " + avgZAfter + "\n" + "statusBefore: " + statusBefore + "\n" + "statusAfter: "
						+ statusAfter + "\n" + "min:" + min + "\n" + "max" + svm + "\n" + "count:" + count);
				if (statusBefore != statusAfter && tem != 0 && min < 8) {
					// showMessage("跌倒发生");
					if (count < 3) {
						return true;
					}
				}

				if (statusBefore == "screen up" && Sacc.get(tem) > 30 && min < 6) {
					if (count < 3) {
						return true;
					}
					// showMessage("疑似跌倒发生");
				}
			} else if (tem - WINDOW_LENGTH / 2 > 0) {
				

			} else if (tem + WINDOW_LENGTH / 2 < Sacc.size()) {
				

			} else {
				// showMessage("数据不规范");
			}
			
		} else {
			// showMessage("没有疑似跌倒状况");
		}
		return false;
	}

	private String showStatus(float X, float Y, float Z) {
		float tMax = 1.0f;
		float absx = Math.abs(X);
		float absy = Math.abs(Y);
		float absz = Math.abs(Z);

		if (absx > absy && absx > absz) {

			if (X > tMax) {
				// v.setText("turn left");
				return "turn left";
			} else if (X < -tMax) {
				// v.setText("turn right");
				return "turn right";
			}

		} else if (absy > absx && absy > absz) {

			if (Y > tMax) {
				// v.setText("turn up");
				return "turn up";
			} else if (Y < -tMax) {
				// v.setText("turn down");
				return "turn down";
			}
		}

		else if (absz > absx && absz > absy) {
			if (Z > 0) {
				// v.setText("screen up");
				return "screen up";
			} else {
				// v.setText("screen down");
				return "screen down";
			}
		} else {
			// v.setText("unknow action");
			return "unknow action";
		}
		return "error";
	}

	public void showNotify() {

		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
		remoteViews.setImageViewResource(R.id.btn_yes, R.drawable.icon_yes);
		remoteViews.setImageViewResource(R.id.btn_no, R.drawable.icon_no);
		remoteViews.setTextViewText(R.id.txt, "检测到疑似跌倒发生");
		Intent intent = new Intent(ACTION_BTN_YES);
		// intent.putExtra(INTENT_NAME, INTENT_BTN_LOGIN);
		PendingIntent intentpi = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.btn_yes, intentpi);

		Intent intent2 = new Intent(ACTION_BTN_NO);
		PendingIntent intentpi2 = PendingIntent.getBroadcast(this, 1, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.btn_no, intentpi2);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setOngoing(false);
		// builder.setAutoCancel(false);
		builder.setContent(remoteViews);
		builder.setTicker("跌倒检测");
		builder.setSmallIcon(R.drawable.ic_launcher);

		Notification notification = builder.build();
		notification.defaults = Notification.DEFAULT_SOUND;

		// notification.contentIntent = intentContent;

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(notifyId, notification);
	}

	/**
	 * 清除当前创建的通知栏
	 */
	public void clearNotify(int notifyId) {
		mNotificationManager.cancel(notifyId);// 删除一个特定的通知ID对应的通知
		// mNotification.cancel(getResources().getString(R.string.app_name));
	}

	public float getAverage(int start, int end, ArrayList<Float> array) {
		float sum = 0;
		for (int i = start; i < end; i++) {
			sum += array.get(i);
		}
		return sum / (end - start);
	}

	// 计算日期相差时间ms
	public long getIntervalDays(Date fDate, Date oDate) {
		if (null == fDate || null == oDate) {
			return -1;
		}
		long intervalMilli = oDate.getTime() - fDate.getTime();
		return intervalMilli;
	}

	// 显示信息
	public void showMessage(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onDestroy() {
		// 结束服务
		if (receiver != null) {
			MainActivity.this.unregisterReceiver(receiver);
			receiver = null;
		}
		mNotificationManager.cancel(notifyId);
		stopService(new Intent(MainActivity.this, CountService.class));
		super.onDestroy();
	}

}
