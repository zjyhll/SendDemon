package com.beidouapp.et.av.activity;

import java.util.List;

import org.pjsip.pjsua2.VideoWindowHandle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.beidouapp.et.av.EtSDK;
import com.beidouapp.et.av.EtVideoPreviewHandler;
import com.beidouapp.et.av.crash.CrashHandler;
import com.beidouapp.et.avdemo.R;
import com.beidouapp.et.client.EtManager;
import com.beidouapp.et.client.api.IAV;
import com.beidouapp.et.client.callback.IAVCallback;
import com.beidouapp.et.client.callback.ICallback;
import com.beidouapp.et.client.callback.IReceiveListener;
import com.beidouapp.et.client.domain.EtMsg;

public class MainActivity extends Activity implements Handler.Callback,
		SurfaceHolder.Callback {
	public static final String TAG = MainActivity.class.getName();
	private Button btnLogin;
	private Button btnCalling;
	private Button btchat;
	private Button btnAgree;
	private EditText txtLoginUserId;
	private EditText txtCallerId;
	private TextView txtCallStatep2p;
	private SurfaceView surfacePreviewVideop2p;// my
	private SurfaceView surfaceIncomingVideop2p;// you
	private Button btnShowPreviewp2p;
	private Button btnHangupp2p;
	private final Handler handler = new Handler(this);

	private static EtVideoPreviewHandler previewHandler;

	private EtManager etManager;
	private EtSDK sdk = EtSDK.getInstance();
	private IAV av;

	private String friendUserId = "";
	private boolean isLogin = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		CrashHandler.getInstance().init(this);
		initComponent();
		displayButton(false);
	}

	private void initComponent() {
		txtLoginUserId = (EditText) findViewById(R.id.txtUserId);
		txtCallerId = (EditText) findViewById(R.id.txtCallerId);
		txtCallStatep2p = (TextView) findViewById(R.id.txtCallStatep2p);
		btnLogin = (Button) this.findViewById(R.id.btnLogin);
		btnCalling = (Button) this.findViewById(R.id.btnCalling);
		btchat = (Button) this.findViewById(R.id.btchat);
		btnAgree = (Button) this.findViewById(R.id.btnAgree);

		btnShowPreviewp2p = (Button) findViewById(R.id.btnShowPreviewp2p);
		btnHangupp2p = (Button) findViewById(R.id.btnHangupp2p);

		surfaceIncomingVideop2p = (SurfaceView) findViewById(R.id.surfaceIncomingVideop2p);
		surfaceIncomingVideop2p.getHolder().addCallback(this);
		surfaceIncomingVideop2p.setVisibility(View.GONE);

		surfacePreviewVideop2p = (SurfaceView) findViewById(R.id.surfacePreviewVideop2p);
		surfacePreviewVideop2p.setVisibility(View.GONE);
		btnLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doLogin();
			}
		});
		btnCalling.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doCalling();
			}
		});
		btchat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				if (isLogin) {
					Intent intent = new Intent();
					intent.setClass(MainActivity.this, IMActivity.class);
					startActivity(intent);
//				} else {
//					Toast.makeText(MainActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
//				}

			}
		});
		btnShowPreviewp2p.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				previewHandler.videoPreviewActive = !previewHandler.videoPreviewActive;
				surfacePreviewVideop2p
						.setVisibility(previewHandler.videoPreviewActive ? View.VISIBLE
								: View.GONE);
				btnShowPreviewp2p
						.setText(previewHandler.videoPreviewActive ? getString(R.string.hide_preview)
								: getString(R.string.show_preview));
				previewHandler.updateVideoPreview(surfacePreviewVideop2p
						.getHolder());
			}
		});
		btnHangupp2p.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doCallingEnd();
			}
		});
		btnAgree.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doCallAgree();
			}
		});
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (MsgConstant.MSG_MEDIA_STATE == msg.what) {
			txtCallStatep2p.setText("正在通话中...");
			surfaceIncomingVideop2p.setVisibility(View.VISIBLE);
			btnCalling.setEnabled(false);
			displayButton(true);
		} else if (MsgConstant.MSG_DISCONNECTING == msg.what) {
			displaySurface(false);
			txtCallStatep2p.setText("呼叫正在断开.");
		} else if (MsgConstant.MSG_DISCONNECTED == msg.what) {
			displaySurface(false);
			displayButton(false);
			btnAgree.setEnabled(false);
			btnCalling.setEnabled(true);
			txtCallStatep2p.setText("呼叫已挂断.");
		} else if (MsgConstant.MSG_EXCEPTION == msg.what) {
			displaySurface(false);
			Throwable ex = (Throwable) msg.obj;
			txtCallStatep2p.setText("断开呼叫出现异常." + ex.getMessage());
			txtCallStatep2p.setText("呼叫已断开.");
		} else {
			return false;
		}
		return true;
	}

	private void doCalling() {
		friendUserId = txtCallerId.getText().toString();// 被呼叫用户Id.
		av.startCall(friendUserId, "video", new ICallback<Void>() {
			@Override
			public void onSuccess(Void value) {
				setDisplayMessage("呼叫 " + friendUserId + " 成功!等待对方响应");
			}

			@Override
			public void onFailure(Void t, Throwable value) {
				setDisplayMessage("呼叫 " + friendUserId + " 失败!"
						+ value.getMessage());
			}
		});
	}

	private void doCallingEnd() {
		av.endCall();
	}

	private void doCallAgree() {
		av.agreeCall(friendUserId, new ICallback<Void>() {
			@Override
			public void onSuccess(Void value) {
				runOnUiThread(new Runnable() {
					public void run() {
						txtCallStatep2p.setText("已同意.正在建立连接...");
					}
				});
			}

			@Override
			public void onFailure(Void t, Throwable value) {
				displayMsgConfim("提示", value.getMessage());
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		sdk.unInit();
		sdk = null;
	}

	public void setDisplayMessage(final String msg) {
		runOnUiThread(new Runnable() {
			public void run() {
				txtCallStatep2p.setText(msg);
			}
		});
	}

	private ICallback<Void> connectCallback = new ICallback<Void>() {
		@Override
		public void onSuccess(Void value) {
			isLogin = true;
			av = sdk.getAV();
			av.setAVCallback(avCallback);
			Log.i(TAG, "登录成功!");
			setDisplayMessage(txtLoginUserId.getText().toString() + " 登录成功.");
			runOnUiThread(new Runnable() {
				public void run() {
					av.initConfig();
					previewHandler = new EtVideoPreviewHandler(av);
					surfacePreviewVideop2p.getHolder().addCallback(
							previewHandler);
					btnLogin.setText("登录");
					btnLogin.setEnabled(false);
				}
			});
		}

		@Override
		public void onFailure(Void v, final Throwable value) {
			isLogin = false;
			value.printStackTrace(System.err);
			setDisplayMessage(txtLoginUserId.getText().toString() + " 登录失败."
					+ value.getMessage());
			runOnUiThread(new Runnable() {
				public void run() {
					btnLogin.setEnabled(true);
					AlertDialog.Builder builder = new AlertDialog.Builder(
							MainActivity.this);
					builder.setMessage(value.getMessage());
					builder.setTitle("登录提示");
					builder.show();
				}
			});
		}
	};

	private void doLogin() {
		String loginUserId = txtLoginUserId.getText().toString();
		setDisplayMessage(loginUserId + " 正在登录...");
		btnLogin.setEnabled(false);
		sdk.init(loginUserId);
		new Thread(new Runnable() {
			@Override
			public void run() {
				etManager = sdk.getEtManager();
				etManager.setConnectCallback(connectCallback);
				etManager.setListener(new IReceiveListener() {
					public void onMessage(EtMsg msg) {
						Log.i(TAG, String.format("收到消息： %-10s", msg.getTopic())
								+ ", " + new String(msg.getPayload()));
					}

					@Override
					public void connectionLost(final Throwable cause) {
						// 丢失链接.
						cause.printStackTrace();
						Log.e(TAG, "connect lost!!!");
					}
				});
				etManager.connect();
			}
		}).start();
	}

	/*********************************** SDK监听. ************************************************/
	private IAVCallback avCallback = new IAVCallback() {

		@Override
		public void onByeEvent(String userId) {
			displayButton(false);
			runOnUiThread(new Runnable() {
				public void run() {
					txtCallStatep2p.setText("通信结束.");
				}
			});
		}

		@Override
		public void onCallDisconnected() {
			// 断开呼叫.
			Log.w(TAG, "呼叫被断开.");
			Message message = new Message();
			message.what = MsgConstant.MSG_DISCONNECTED;
			handler.sendMessage(message);
		}

		@Override
		public void onFailure(Throwable e) {
			final Throwable ex = e;
			ex.printStackTrace();
			setDisplayMessage("异常信息:" + e.getMessage());
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							MainActivity.this);
					builder.setMessage(ex.getMessage());
					builder.setTitle("错误");
					builder.show();
				}
			});
		}

		@Override
		public void onCallMediaState() {
			Message message = new Message();
			message.what = MsgConstant.MSG_MEDIA_STATE;
			handler.sendMessage(message);
		}

		@Override
		public void onInviteUserOfflineEvent(List<String> userList) {
			displayMsgConfim("错误", "不在线用户" + userList);
		}

		@Override
		public void onKickEvent(String kickUserId) {
			displayMsgConfim("错误", "您被踢下线!" + kickUserId);
		}

		@Override
		public void onUserOfflineEvent(String userId) {
			Log.w(TAG, "被呼叫用户 " + userId + " 不在线.");
			displayMsgConfim("提示", "被呼叫用户 " + userId + " 不在线.");
		}

		@Override
		public void onP2pUserAcceptEvent(final String userId) {
			Log.w(TAG, "被呼叫用户 " + userId + " 同意音视频.");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					av.regAccount();
				}
			});
		}

		@Override
		public void onP2pCallingIn(String userId) {
			friendUserId = userId;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					txtCallStatep2p.setText(friendUserId + " 呼入!");
					btnAgree.setEnabled(true);
				}
			});

		}
	};

	/**********************************
	 * video surface 实现.
	 **********************************/
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "--surface Created");
		this.setDisplayMessage("正在与" + friendUserId + "建立连接中...");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.i(TAG, "--surface Changed.......");
		updateVideoWindow(true);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "--surface Destroyed");
		updateVideoWindow(false);
	}

	private void updateVideoWindow(boolean show) {
		if (av.getCurrentEtCall() != null
				&& av.getCurrentEtCall().getVideoWindow() != null) {
			VideoWindowHandle vidWH = new VideoWindowHandle();
			if (show) {
				vidWH.getHandle().setWindow(
						surfaceIncomingVideop2p.getHolder().getSurface());
			} else {
				vidWH.getHandle().setWindow(null);
			}
			try {
				av.getCurrentEtCall().getVideoWindow().setWindow(vidWH);
			} catch (Throwable e) {
				System.out.println(e);
			}
		}
	}

	/**********************************
	 * 其他工具相关.
	 **********************************/

	private void displaySurface(boolean isDispaly) {
		surfacePreviewVideop2p.setVisibility(isDispaly ? View.VISIBLE
				: View.GONE);
		surfaceIncomingVideop2p.setVisibility(isDispaly ? View.VISIBLE
				: View.GONE);
	}

	private void displayButton(boolean isDispaly) {
		btnHangupp2p.setEnabled(isDispaly);
		btnShowPreviewp2p.setEnabled(isDispaly);
	}

	private void displayMsgConfim(final String title, final String content) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						MainActivity.this);
				builder.setMessage(content);
				builder.setTitle(title);
				builder.show();
			}
		});
	}
}