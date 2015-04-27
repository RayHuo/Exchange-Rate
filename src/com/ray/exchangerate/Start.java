package com.ray.exchangerate;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

public class Start extends Activity {
	private static final String[] money_kind = {"人民币", "港元", "美元", "欧元", "英镑", "卢布", "日元"};
	private ArrayAdapter<String> spinner_adapter = null;
	
	private EditText account = null;
	private EditText exchangeAccount = null;
	private Spinner originType = null;
	private Spinner exchangeType = null;
	private Button submit = null;
	private final String baiduURL = "http://www.baidu.com/s?wd=";
	
	private Handler m_handler = null;	// 把handler定义在这里，它才属于UI主线程，这样才能重写它的handlermessage以修改UI
	private Thread m_thread = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 为了能在UI主线程执行网络访问并获取数据的功能，需要加入以下的strictMode的设置代码。
		// android 2.3之后的版本都不允许在UI主线程执行网络访问操作，这其实会给UI主线程带来压力，并造成“假死”状态。
		// 另开一个子线程执行网络数据获取操作的话，可以不加这一段。
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
	        .detectDiskReads()
	        .detectDiskWrites()
	        .detectNetwork()   // or .detectAll() for all detectable problems
	        .penaltyLog()
	        .build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
	        .detectLeakedSqlLiteObjects()
	        .detectLeakedClosableObjects()
	        .penaltyLog()
	        .penaltyDeath()
	        .build());
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		
		account = (EditText)findViewById(R.id.account);
		exchangeAccount = (EditText)findViewById(R.id.exchangeAccount);
		originType = (Spinner)findViewById(R.id.originType);
		exchangeType = (Spinner)findViewById(R.id.exchangeType);
		submit = (Button)findViewById(R.id.submit);
		
		
		spinner_adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_item, money_kind);
		spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		originType.setAdapter(spinner_adapter);
		exchangeType.setAdapter(spinner_adapter);
		
		originType.setSelection(0, true);	// 设置spinner显示的默认值，此处为第一个:“人民币”
		exchangeType.setSelection(0, true);
		
		originType.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				arg0.setVisibility(View.VISIBLE);	// 在spinner中显示选中的item
//				Toast.makeText(Start.this, money_kind[arg2], Toast.LENGTH_LONG).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		exchangeType.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				arg0.setVisibility(View.VISIBLE);	// 在spinner中显示选中的item
//				Toast.makeText(Start.this, money_kind[arg2], Toast.LENGTH_LONG).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
//		System.out.println("waht");
		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// 调用以下两个函数中的任何一个均可达到效果。
//				getExchangeAccount();
				String originSelectedType = originType.getSelectedItem().toString();
				String exchangeSelectedType = exchangeType.getSelectedItem().toString();
				String accountValue = account.getText().toString();
				if(originSelectedType.equals(exchangeSelectedType)) {
					exchangeAccount.setText(accountValue);
				}
				else {
					getExchangeAccountWithThread();
				}
			}
			
		});	
		
	}

	// 获取汇率的函数，这是直接在主线程（UI线程）中执行网络获取数据，会对主线程造成压力，可以考虑使用开个新线程
	private void getExchangeAccount() {
		/*
		 * 接着进行汇率转换
		 */
		// 获取输入的数额
		// 获取spinner选中的item内容
		String originSelectedType = originType.getSelectedItem().toString();
		String exchangeSelectedType = exchangeType.getSelectedItem().toString();
		String accountValue = account.getText().toString();
		
		
		try {
			String urlParameters = URLEncoder.encode(accountValue + " " + originSelectedType 
					+ " " + exchangeSelectedType ,"UTF-8");
			String realURL = baiduURL + urlParameters;
//			System.out.println(realURL);
//			Toast.makeText(Start.this, realURL, Toast.LENGTH_SHORT).show();
			// 通过URL对象的openStream获取得到整个网页信息，然后查找相应的汇率数值。
			URL m_url=new URL(realURL);
			InputStreamReader isr = new InputStreamReader(m_url.openStream(), "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			

			/*
			 * 创建文件，把获取得到的html文件写到SD卡中。
			 * */
			BufferedWriter bw = null;
			String sdpath = null;	// 存放SD卡根目录
			// 判断是否存在SD卡
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
				File file = new File(Environment.getExternalStorageDirectory(), "OutputFiles.txt");
				if(!file.exists()) {
					file.createNewFile();
				}
				bw = new BufferedWriter(new FileWriter(file));
				
			}

			
//			String hit1 = accountValue + originSelectedType + "="; // "<em>5569人民币</em>元=";
			String hit1 = "<em>" + accountValue + originSelectedType;	// "</em>元=";
//			Toast.makeText(Start.this, "hit1 = " + hit1, Toast.LENGTH_SHORT).show();
			String hit2 = "<em>"; // "日元";
//			Toast.makeText(Start.this, "hit2 = " + hit2, Toast.LENGTH_SHORT).show();
			String result = "";		// 汇率结果
			int startSite = -1;
			int endSite = -1;
			String str = null;
			while((str=br.readLine()) != null) {
				bw.write(str);
				bw.flush(); 
//				Toast.makeText(Start.this, str, Toast.LENGTH_SHORT).show();
				
				if((startSite = str.indexOf(hit1)) >= 0) {
//					Toast.makeText(Start.this, "startSite=" + startSite, Toast.LENGTH_SHORT).show();
					int startSite2 = str.indexOf("=", startSite+1);
					endSite = str.indexOf(hit2, startSite2+1);
//					Toast.makeText(Start.this, "endSite=" + endSite, Toast.LENGTH_SHORT).show();
					result += str.substring(startSite2+1, endSite);
					
				}
			}
			bw.close();
			br.close();
			isr.close();
			
			// 把结果赋值给exchangeAccount
			exchangeAccount.setText(result);
//			Toast.makeText(Start.this, result, Toast.LENGTH_SHORT).show();
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// 通过加入子线程来执行网络数据获取，以减轻
	private void getExchangeAccountWithThread() {
		// 创建handler，这是与UI主线程绑定的handler。
		m_handler = new Handler() {
			@Override
			// 重写这个方法，即在这里执行网络访问，并获取数据，以更新UI。
			public void handleMessage(Message msg) { 
				super.handleMessage(msg);
				// 在这里获取msg中的信息，并依此修改UI，这里的msg就是后面的m_thread子线程所sendMessage发过来的。
				switch (msg.what) {
				case 0:
					exchangeAccount.setText(msg.obj.toString());
					break;
				case 1:
					// 把结果赋值给exchangeAccount
					exchangeAccount.setText("0");
//					Toast.makeText(Start.this, "Cannot Find Exchange", Toast.LENGTH_LONG).show();
					break;
				default:
					break;
				}
				
			}
		};
		
		// 新建一个thread，这里开个子线程，执行访问网络数据的行为，然后把数据给到一个message对象，并通过handler的sendMessage把
		// 信息传递给handler绑定的UI主线程中的messageQueue，然后使用这个信息，并修改UI。
		m_thread = new Thread(new Runnable() {
			public void run() {
				// 获取输入的数额
				// 获取spinner选中的item内容
				String originSelectedType = originType.getSelectedItem().toString();
				String exchangeSelectedType = exchangeType.getSelectedItem().toString();
				String accountValue = account.getText().toString();
							
				try {
					String urlParameters = URLEncoder.encode(accountValue + " " + originSelectedType 
							+ " " + exchangeSelectedType ,"UTF-8");
					String realURL = baiduURL + urlParameters;
					// 通过URL对象的openStream获取得到整个网页信息，然后查找相应的汇率数值。
					URL m_url=new URL(realURL);
					InputStreamReader isr = new InputStreamReader(m_url.openStream(), "UTF-8");
					BufferedReader br = new BufferedReader(isr);
					

					/*
					 * 创建文件，把获取得到的html文件写到SD卡中。
					 * */
//					BufferedWriter bw = null;
//					String sdpath = null;	// 存放SD卡根目录
//					// 判断是否存在SD卡
//					if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//						sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
//						File file = new File(Environment.getExternalStorageDirectory(), "OutputFiles2.txt");
//						if(!file.exists()) {
//							file.createNewFile();
//						}
//						bw = new BufferedWriter(new FileWriter(file));
//						
//					}

					String hit1 = "<em>" + accountValue + originSelectedType;	// "<em>5569人民币";
					String hit2 = "<em>"; // "日元";
					String result = "";		// 汇率结果
					int startSite = -1;
					int endSite = -1;
					int startSite2 = -1;
					String str = null;
					while((str=br.readLine()) != null) {
//						bw.write(str);
//						bw.flush(); 					
						if((startSite = str.indexOf(hit1)) >= 0) {
							startSite2 = str.indexOf("=", startSite);	// 这里判断两次是因为存在样式问题						
							endSite = str.indexOf(hit2, startSite2);
							result += str.substring(startSite2+1, endSite);
						}
					}
//					bw.close();
					br.close();
					isr.close();
					
					// 把结果赋值给exchangeAccount
					Message msg = new Message();
					msg.obj = result;
					msg.what = 0;
					// m_handler是默认跟主线程绑定的，所以其msg会传到主线程的messageQueue中，
					// 并通过Looper获取到该msg并在handlerMessage中处理。
					m_handler.sendMessage(msg);	
					
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		m_thread.start();	// 启动线程
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start, menu);
		return true;
	}

}
