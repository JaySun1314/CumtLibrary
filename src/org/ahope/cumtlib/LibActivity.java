package org.ahope.cumtlib;

import java.util.List;

import org.ahope.cumtlib.Entity.Book;
import org.ahope.cumtlib.Utilities.ScanBook;
import org.ahope.cumtlib.Utilities.SysApplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.baidu.mobstat.StatService;

public class LibActivity extends Activity {
	private EditText ScanET;
	private EditText Stu_idET;
	private EditText PwdET;
	private ImageButton ScanButton;
	private ImageButton LoginButton;
	//������Ŀ������ǣ���Ϊ��������������Ϊ�����߼���
	boolean scanflag = true;
	RadioGroup group;
	private String userName;
	private String password;
	String LoginResult = null;

	List<Book> list = null;

	/** ��¼loading��ʾ�� */
	private ProgressDialog proDialog;

	/** ��¼��̨֪ͨ����UI�߳�,��Ҫ���ڵ�¼ʧ��,֪ͨUI�̸߳��½��� */
	Handler loginHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				if (proDialog != null) {
					proDialog.dismiss();
				}
				Toast.makeText(LibActivity.this, "��½ʧ�ܡ�", Toast.LENGTH_SHORT)
				.show();
				break;
			case 0:
				if (proDialog != null) {
					proDialog.dismiss();
				}
				Toast.makeText(LibActivity.this, "������ѧ�Ż�����", Toast.LENGTH_LONG).show();
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SysApplication.getInstance().addActivity(this);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_lib);
		findviews();
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				//��ȡ������ѡ�����ID
				int radioButtonId = group.getCheckedRadioButtonId();
				//����ID��ȡRadioButton��ʵ��
				RadioButton rb = (RadioButton)LibActivity.this.findViewById(radioButtonId);
				if(rb.getText().equals("������"))
					scanflag = false;
				else
					scanflag = true;
			}
		});
		ScanButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if(ScanBook.isConnectInternet(LibActivity.this))
				{String bookname = ScanET.getText().toString();
				if(bookname.equals("")||bookname==null)
					Toast.makeText(LibActivity.this, "������������ݡ�", Toast.LENGTH_SHORT)
					.show();
				else
				{
				Intent i = new Intent(LibActivity.this, ScanResult.class);
				Bundle bundle = new Bundle();
				bundle.putString("bookname", bookname);
				bundle.putBoolean("scanflag", scanflag);
				i.putExtras(bundle);
				startActivity(i);}}
				else 
					Toast.makeText(LibActivity.this, "����û��������", Toast.LENGTH_LONG).show();
			}
		});
		LoginButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(ScanBook.isConnectInternet(LibActivity.this))
				{proDialog = ProgressDialog.show(LibActivity.this, "������..",
						"������..���Ժ�....", true, true);
				// ��һ���߳̽��е�¼��֤,��Ҫ������ʧ��,�ɹ�����ֱ��ͨ��startAcitivity(Intent)ת��
				Thread loginThread = new Thread(new LoginFailureHandler());
				loginThread.start();}
				else 
					Toast.makeText(LibActivity.this, "����û��������", Toast.LENGTH_LONG).show();
			}
		});
	}

	void findviews() {
		ScanET = (EditText) findViewById(R.id.scan_book);
		Stu_idET = (EditText) findViewById(R.id.num);
		PwdET = (EditText) findViewById(R.id.pwd);
		ScanButton = (ImageButton) findViewById(R.id.scan_button);
		LoginButton = (ImageButton) findViewById(R.id.login_button);
		group = (RadioGroup) findViewById(R.id.rgroup);
	}

	private boolean validateLocalLogin(String userName, String password) {
		// ���ڱ�ǵ�½״̬
		boolean loginState = false;
		LoginResult = ScanBook.mybook(userName, password);
		if (LoginResult.equals("1") || LoginResult.equals("0"))
			loginState = false;
		else
			loginState = true;

		return loginState;
	}

	class LoginFailureHandler implements Runnable {

		public void run() {
			userName = Stu_idET.getText().toString();
			password = PwdET.getText().toString();
			if(check(userName,password))
			{boolean loginState = validateLocalLogin(userName, password);
			// ��½�ɹ�
			if (loginState) {
				// ��Ҫ�������ݵ���½��Ľ���,
				Intent intent = new Intent();
				intent.setClass(LibActivity.this, MyBook.class);
				Bundle bundle = new Bundle();
				bundle.putString("mbook", LoginResult);
				bundle.putString("user",userName);
				bundle.putString("passwd", password);
				intent.putExtras(bundle);
				//�����û���Ϣ
				SharedPreferences pre = getSharedPreferences("user_msg",MODE_PRIVATE);
				SharedPreferences.Editor editor = pre.edit();
				editor.putString("name", userName);
				editor.putString("password", password);
				editor.commit();
				// ת���½���ҳ��
				startActivity(intent);
				finish();
				proDialog.dismiss();
			} else {
				// ͨ������handler��֪ͨUI���̸߳���UI,
				loginHandler.sendEmptyMessage(1);
			}}
			else 
				loginHandler.sendEmptyMessage(0);
		}
	}
	boolean check(String user,String passwd)
	{
		if(user.equals("")||passwd.equals("")){
			return false;
		}
		else
			return true;
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		StatService.onResume(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		StatService.onPause(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Dialog alertDialog = new AlertDialog.Builder(this)
					.setTitle("ȷ���˳���")
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									SysApplication.getInstance().exit();
								}
							})
					.setNegativeButton("ȡ��",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
								}
							}).create();
			alertDialog.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
