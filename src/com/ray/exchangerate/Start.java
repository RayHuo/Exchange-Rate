package com.ray.exchangerate;

import android.os.Bundle;
import android.os.Environment;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
				/*
				 * ���Ž��л���ת��
				 */
				// ��ȡ���������
				// ��ȡspinnerѡ�е�item����
				String originSelectedType = originType.getSelectedItem().toString();
				String exchangeSelectedType = exchangeType.getSelectedItem().toString();
				String accountValue = account.getText().toString();
				
				String baiduURL = "http://www.baidu.com/s?wd=";
				try {
					String urlParameters = URLEncoder.encode(accountValue + " " + originSelectedType 
							+ " " + exchangeSelectedType ,"UTF-8");
					String realURL = baiduURL + urlParameters;
//					System.out.println(realURL);
//					Toast.makeText(Start.this, realURL, Toast.LENGTH_SHORT).show();
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

					
//					String hit1 = accountValue + originSelectedType + "="; // "<em>5569�����</em>Ԫ=";
					String hit1 = "<em>" + accountValue + originSelectedType;	// "</em>Ԫ=";
//					Toast.makeText(Start.this, "hit1 = " + hit1, Toast.LENGTH_SHORT).show();
					String hit2 = "<em>"; // "��Ԫ";
//					Toast.makeText(Start.this, "hit2 = " + hit2, Toast.LENGTH_SHORT).show();
					String result = "";		// ���ʽ��
					int startSite = -1;
					int endSite = -1;
					String str = null;
					while((str=br.readLine()) != null) {
						bw.write(str);
						bw.flush(); 
//						Toast.makeText(Start.this, str, Toast.LENGTH_SHORT).show();
						
						if((startSite = str.indexOf(hit1)) >= 0) {
//							Toast.makeText(Start.this, "startSite=" + startSite, Toast.LENGTH_SHORT).show();
							int startSite2 = str.indexOf("=", startSite+1);
							endSite = str.indexOf(hit2, startSite2+1);
//							Toast.makeText(Start.this, "endSite=" + endSite, Toast.LENGTH_SHORT).show();
							result += str.substring(startSite2+1, endSite);
							
						}
//						numb++;
					}
					bw.close();
					br.close();
					isr.close();
					
					// �ѽ����ֵ��exchangeAccount
					exchangeAccount.setText(result);
//					Toast.makeText(Start.this, result, Toast.LENGTH_SHORT).show();
					
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
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start, menu);
		return true;
	}

}
