package com.example.servicetest;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.content.Intent;
import android.os.IBinder;

public class CountService extends Service {
	
	private SensorManager sm;
	private float X, Y, Z;
	private static final int length = 200; // 缓存长度
	
	private ArrayList<Float> Xacc = new ArrayList<Float>();
	private ArrayList<Float> Yacc = new ArrayList<Float>();
	private ArrayList<Float> Zacc = new ArrayList<Float>();
	private ArrayList<Double> Sacc = new ArrayList<Double>();
	
	private int count = 0;
	//private boolean threadDisable = false;

	
	WakeLock mWakeLock;

	@Override
	public void onCreate() {
		super.onCreate();
		acquireWakeLock();
		sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		/*
		 * 最常用的一个方法 注册事件 参数1 ：SensorEventListener监听器 参数2 ：Sensor
		 * 一个服务可能有多个Sensor实现，此处调用getDefaultSensor获取默认的Sensor 参数3 ：模式 可选数据变化的刷新频率
		 */
		sm.registerListener(MySensorEventListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				20000);
		
		
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		releaseWakeLock();
		sm.unregisterListener(MySensorEventListener);
		super.onDestroy();
		//count = 0;
		//threadDisable = true;
		
		Log.v("CountService", "on destroy");
	}
	private SensorEventListener MySensorEventListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			int sensorType = event.sensor.getType();
			// 获取三轴信息
			switch (sensorType) {
			case Sensor.TYPE_ACCELEROMETER:
				X = event.values[0];
				Y = event.values[1];
				Z = event.values[2];
				

				int listlengthX = Xacc.size();
				if (listlengthX == 0) {
					Xacc.add(X);
					//Log.v("CountService","X is" + X);
				} else {
					if (listlengthX < length) {
						Xacc.add(0, X);
						//Log.v("CountService","X is" + X);
					} else {
						Xacc.remove(listlengthX - 1);
						Xacc.add(0, X);
					}
				}

				int listlengthY = Yacc.size();
				if (listlengthY == 0) {
					Yacc.add(Y);
				} else {
					if (listlengthY < length) {
						Yacc.add(0, Y);
					} else {
						Yacc.remove(listlengthX - 1);
						Yacc.add(0, Y);
					}
				}
				int listlengthZ = Zacc.size();
				if (listlengthZ == 0) {
					Zacc.add(Z);
				} else {
					if (listlengthZ < length) {
						Zacc.add(0, Z);
					} else {
						Zacc.remove(listlengthX - 1);
						Zacc.add(0, Z);
					}
				}
				double S = Math.sqrt(X * X + Y * Y + Z * Z);
				int listlengthS = Sacc.size();
				if (listlengthS == 0) {
					Sacc.add(S);
				} else {
					if (listlengthS < length) {
						Sacc.add(0, S);
					} else {
						Sacc.remove(listlengthX - 1);
						Sacc.add(0, S);
					}
				}
				
				Intent intent = new Intent();
				intent.putExtra("count", count);
				intent.putExtra("Xacc", Xacc);
				intent.putExtra("Yacc", Yacc);
				intent.putExtra("Zacc", Zacc);
				intent.putExtra("Sacc", Sacc);
				intent.setAction("com.example.CountService");
				sendBroadcast(intent);
				break;
			default:
				break;
			}

		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	};
	//申请设备电源锁  
    private void acquireWakeLock()  
    {  
        if (null == mWakeLock)  
        {  
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);  
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE,"");  
            if (null != mWakeLock)  
            {  
                mWakeLock.acquire();  
            }  
        }  
    }  
  
    //释放设备电源锁  
    private void releaseWakeLock()  
    {  
        if (null != mWakeLock)  
        {  
            mWakeLock.release();  
            mWakeLock = null;  
        }  
    }  
	
}
