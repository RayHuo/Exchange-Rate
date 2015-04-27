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
	private static final String[] money_kind = {"�����", "��Ԫ", "��Ԫ", "ŷԪ", "Ӣ��", "¬��", "��Ԫ"};
	private ArrayAdapter<String> spinner_adapter = null;
	
	private EditText account = null;
	private EditText exchangeAccount = null;
	private Spinner originType = null;
	private Spinner exchangeType = null;
	private Button submit = null;
	private final String baiduURL = "http://www.baidu.com/s?wd=";
	
	private Handler m_handler = null;	// ��handler�����������������UI���̣߳�����������д����handlermessage���޸�UI
	private Thread m_thread = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Ϊ������UI���߳�ִ��������ʲ���ȡ���ݵĹ��ܣ���Ҫ�������µ�strictMode�����ô��롣
		// android 2.3֮��İ汾����������UI���߳�ִ��������ʲ���������ʵ���UI���̴߳���ѹ��������ɡ�������״̬��
		// ��һ�����߳�ִ���������ݻ�ȡ�����Ļ������Բ�����һ�Ρ�
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
		
		originType.setSelection(0, true);	// ����spinner��ʾ��Ĭ��ֵ���˴�Ϊ��һ��:������ҡ�
		exchangeType.setSelection(0, true);
		
		originType.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				arg0.setVisibility(View.VISIBLE);	// ��spinner����ʾѡ�е�item
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
				arg0.setVisibility(View.VISIBLE);	// ��spinner����ʾѡ�е�item
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
				// �����������������е��κ�һ�����ɴﵽЧ����
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

	// ��ȡ���ʵĺ���������ֱ�������̣߳�UI�̣߳���ִ�������ȡ���ݣ�������߳����ѹ�������Կ���ʹ�ÿ������߳�
	private void getExchangeAccount() {
		/*
		 * ���Ž��л���ת��
		 */
		// ��ȡ���������
		// ��ȡspinnerѡ�е�item����
		String originSelectedType = originType.getSelectedItem().toString();
		String exchangeSelectedType = exchangeType.getSelectedItem().toString();
		String accountValue = account.getText().toString();
		
		
		try {
			String urlParameters = URLEncoder.encode(accountValue + " " + originSelectedType 
					+ " " + exchangeSelectedType ,"UTF-8");
			String realURL = baiduURL + urlParameters;
//			System.out.println(realURL);
//			Toast.makeText(Start.this, realURL, Toast.LENGTH_SHORT).show();
			// ͨ��URL�����openStream��ȡ�õ�������ҳ��Ϣ��Ȼ�������Ӧ�Ļ�����ֵ��
			URL m_url=new URL(realURL);
			InputStreamReader isr = new InputStreamReader(m_url.openStream(), "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			

			/*
			 * �����ļ����ѻ�ȡ�õ���html�ļ�д��SD���С�
			 * */
			BufferedWriter bw = null;
			String sdpath = null;	// ���SD����Ŀ¼
			// �ж��Ƿ����SD��
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
				File file = new File(Environment.getExternalStorageDirectory(), "OutputFiles.txt");
				if(!file.exists()) {
					file.createNewFile();
				}
				bw = new BufferedWriter(new FileWriter(file));
				
			}

			
//			String hit1 = accountValue + originSelectedType + "="; // "<em>5569�����</em>Ԫ=";
			String hit1 = "<em>" + accountValue + originSelectedType;	// "</em>Ԫ=";
//			Toast.makeText(Start.this, "hit1 = " + hit1, Toast.LENGTH_SHORT).show();
			String hit2 = "<em>"; // "��Ԫ";
//			Toast.makeText(Start.this, "hit2 = " + hit2, Toast.LENGTH_SHORT).show();
			String result = "";		// ���ʽ��
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
			
			// �ѽ����ֵ��exchangeAccount
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
	
	// ͨ���������߳���ִ���������ݻ�ȡ���Լ���
	private void getExchangeAccountWithThread() {
		// ����handler��������UI���̰߳󶨵�handler��
		m_handler = new Handler() {
			@Override
			// ��д�����������������ִ��������ʣ�����ȡ���ݣ��Ը���UI��
			public void handleMessage(Message msg) { 
				super.handleMessage(msg);
				// �������ȡmsg�е���Ϣ���������޸�UI�������msg���Ǻ����m_thread���߳���sendMessage�������ġ�
				switch (msg.what) {
				case 0:
					exchangeAccount.setText(msg.obj.toString());
					break;
				case 1:
					// �ѽ����ֵ��exchangeAccount
					exchangeAccount.setText("0");
//					Toast.makeText(Start.this, "Cannot Find Exchange", Toast.LENGTH_LONG).show();
					break;
				default:
					break;
				}
				
			}
		};
		
		// �½�һ��thread�����￪�����̣߳�ִ�з����������ݵ���Ϊ��Ȼ������ݸ���һ��message���󣬲�ͨ��handler��sendMessage��
		// ��Ϣ���ݸ�handler�󶨵�UI���߳��е�messageQueue��Ȼ��ʹ�������Ϣ�����޸�UI��
		m_thread = new Thread(new Runnable() {
			public void run() {
				// ��ȡ���������
				// ��ȡspinnerѡ�е�item����
				String originSelectedType = originType.getSelectedItem().toString();
				String exchangeSelectedType = exchangeType.getSelectedItem().toString();
				String accountValue = account.getText().toString();
							
				try {
					String urlParameters = URLEncoder.encode(accountValue + " " + originSelectedType 
							+ " " + exchangeSelectedType ,"UTF-8");
					String realURL = baiduURL + urlParameters;
					// ͨ��URL�����openStream��ȡ�õ�������ҳ��Ϣ��Ȼ�������Ӧ�Ļ�����ֵ��
					URL m_url=new URL(realURL);
					InputStreamReader isr = new InputStreamReader(m_url.openStream(), "UTF-8");
					BufferedReader br = new BufferedReader(isr);
					

					/*
					 * �����ļ����ѻ�ȡ�õ���html�ļ�д��SD���С�
					 * */
//					BufferedWriter bw = null;
//					String sdpath = null;	// ���SD����Ŀ¼
//					// �ж��Ƿ����SD��
//					if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//						sdpath = Environment.getExternalStorageDirectory().getAbsolutePath();
//						File file = new File(Environment.getExternalStorageDirectory(), "OutputFiles2.txt");
//						if(!file.exists()) {
//							file.createNewFile();
//						}
//						bw = new BufferedWriter(new FileWriter(file));
//						
//					}

					String hit1 = "<em>" + accountValue + originSelectedType;	// "<em>5569�����";
					String hit2 = "<em>"; // "��Ԫ";
					String result = "";		// ���ʽ��
					int startSite = -1;
					int endSite = -1;
					int startSite2 = -1;
					String str = null;
					while((str=br.readLine()) != null) {
//						bw.write(str);
//						bw.flush(); 					
						if((startSite = str.indexOf(hit1)) >= 0) {
							startSite2 = str.indexOf("=", startSite);	// �����ж���������Ϊ������ʽ����						
							endSite = str.indexOf(hit2, startSite2);
							result += str.substring(startSite2+1, endSite);
						}
					}
//					bw.close();
					br.close();
					isr.close();
					
					// �ѽ����ֵ��exchangeAccount
					Message msg = new Message();
					msg.obj = result;
					msg.what = 0;
					// m_handler��Ĭ�ϸ����̰߳󶨵ģ�������msg�ᴫ�����̵߳�messageQueue�У�
					// ��ͨ��Looper��ȡ����msg����handlerMessage�д���
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
		m_thread.start();	// �����߳�
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start, menu);
		return true;
	}

}
