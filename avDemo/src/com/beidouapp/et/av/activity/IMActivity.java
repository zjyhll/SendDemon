package com.beidouapp.et.av.activity;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beidouapp.et.av.EtSDK;
import com.beidouapp.et.av.activity.view.AudioRecorderButton;
import com.beidouapp.et.av.activity.view.MediaManager;
import com.beidouapp.et.avdemo.R;
import com.beidouapp.et.client.EtManager;
import com.beidouapp.et.client.api.IFile;
import com.beidouapp.et.client.api.IM;
import com.beidouapp.et.client.callback.FileCallBack;
import com.beidouapp.et.client.callback.ICallback;
import com.beidouapp.et.client.callback.IFileReceiveListener;
import com.beidouapp.et.client.callback.IReceiveListener;
import com.beidouapp.et.client.domain.DocumentInfo;
import com.beidouapp.et.client.domain.EtMsg;

public class IMActivity extends Activity {
	public static final String TAG = IMActivity.class.getName();
	private IM im = null;
	private IFile iFile = null;
	private EtSDK sdk = EtSDK.getInstance();
	// msgId
	private String chattomsgId = "10001";
//	private String OtheruserId = "2190";
//	private String myuserId = "2188";
	 private String OtheruserId = "2188";
	 private String myuserId = "2190";
	private EtManager etManager;
	//
	// 自己发的
	final int TYPE_SELF = 0;
	// 服务器发的
	final int TYPE_OTHER = 1;
	//
	private EditText tv_inputcontent = null;
	private ListView lv_home = null;
	private TextView tv_input_send = null;
	private TextView tv_input_pic = null;//
	private TextView tv_input_file = null;//
	private AudioRecorderButton mAudioRecorderButton = null;//
	private View id_recorder_anim_self;
	private View id_recorder_anim_other;
	private MyListAdpter listadapter = null;
	private String inputcontentStr = null;// 输入的内容
	//
	private int downloader = 0;
	private final int FILE_SELECT_CODE = 1;
	private final int FILE_SELECT_CODE1 = 2;
	//
	private ArrayList<ListItem> listItem = null;
	private ListItem item = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_im);
		doLogin();
		listItem = new ArrayList<ListItem>();
		item = new ListItem();
		//
		tv_inputcontent = (EditText) findViewById(R.id.tv_inputcontent);
		lv_home = (ListView) findViewById(R.id.lv_home);
		tv_input_send = (TextView) findViewById(R.id.tv_input_send);
		tv_input_pic = (TextView) findViewById(R.id.tv_input_pic);
		tv_input_file = (TextView) findViewById(R.id.tv_input_file);
		mAudioRecorderButton = (AudioRecorderButton) findViewById(R.id.id_recorder_btn);
		listadapter = new MyListAdpter(IMActivity.this);
		lv_home.setAdapter(listadapter);
		// 发图片
		tv_input_pic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				try {
					downloader = 1;
					startActivityForResult(
							Intent.createChooser(intent, "选择打开方式..."),
							FILE_SELECT_CODE);
				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(IMActivity.this, "请先安装一个文件管理器",
							Toast.LENGTH_SHORT).show();
				}

			}
		});
		// 发文件
		tv_input_file.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent1 = new Intent();
				intent1.setAction(android.content.Intent.ACTION_GET_CONTENT);
				intent1.setType("application/msword;application/vnd.ms-excel;application/vnd.ms-powerpoint;text/plain;image/*");
				intent1.addCategory(Intent.CATEGORY_OPENABLE);
				try {
					downloader = 2;
					startActivityForResult(
							Intent.createChooser(intent1, "选择打开方式..."),
							FILE_SELECT_CODE1);
				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(IMActivity.this, "请先安装一个文件管理器",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		// 发文字
		tv_input_send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//

				inputcontentStr = tv_inputcontent.getText().toString();
				tv_inputcontent.setText("");
				//
				item = new ListItem();
				item.setMsgId(chattomsgId);
				item.setContentStr(inputcontentStr);
				listItem.add(item);
				listadapter.notifyDataSetChanged();
				lv_home.smoothScrollToPosition(listadapter.getCount() - 1);
				/**
				 * @userId ：接收用户ID。必填。
				 * @content ：发布的主题内容。此内容支持String及byte[]两种类型，必填。。必填。
				 * @msgId ：消息Id,用户设置，不做唯一性校验.用于标识消息是否处理成功的状态。选填。
				 * @callback：同上。必填。
				 * @qos：消息质量控制等级。选填时取默认值。建议用户默认。。
				 */
				im.chatTo(OtheruserId, inputcontentStr, chattomsgId,
						connectCallback2);
			}
		});
		// 发语音
		initEvent();
	}

	/**
	 * 语音相关
	 */
	private void initEvent() {
		mAudioRecorderButton
				.setAudioFinishRecorderListener(new AudioRecorderButton.AudioFinishRecorderListener() {
					@Override
					public void onFinish(float seconds, String filePath) {
						item = new ListItem(seconds, filePath);
						item.setMsgId(chattomsgId);
						listItem.add(item);
						//
						iFile.fileTo(OtheruserId, filePath, fileCallBack);
						handler.sendEmptyMessage(pic);
					}
				});
		lv_home.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					final int position, long id) {
				int type = listadapter.getItemViewType(position);
				final String soundStr = listItem.get(position).getFilePath();
				switch (type) {
				case TYPE_SELF:// extractBytesFromBytes
					if (!TextUtils.isEmpty(soundStr)) {
						/* 播放动画 播放音频 */
						if (id_recorder_anim_self != null) {
							id_recorder_anim_self
									.setBackgroundResource(R.drawable.adj);
							id_recorder_anim_self = null;
						}
						id_recorder_anim_self = view
								.findViewById(R.id.id_recorder_anim_self);
						id_recorder_anim_self
								.setBackgroundResource(R.drawable.play_anim);
						AnimationDrawable anim = (AnimationDrawable) id_recorder_anim_self
								.getBackground();
						anim.start();

						MediaManager.playSound(soundStr,
								new MediaPlayer.OnCompletionListener() {
									@Override
									public void onCompletion(MediaPlayer mp) {
										id_recorder_anim_self
												.setBackgroundResource(R.drawable.adj);
									}
								});
					}
					break;
				case TYPE_OTHER:// picUrl
					if (!TextUtils.isEmpty(soundStr)) {
						/* 播放动画 播放音频 */
						if (id_recorder_anim_other != null) {
							id_recorder_anim_other
									.setBackgroundResource(R.drawable.adj);
							id_recorder_anim_other = null;
						}
						id_recorder_anim_other = view
								.findViewById(R.id.id_recorder_anim_other);
						id_recorder_anim_other
								.setBackgroundResource(R.drawable.play_anim);
						AnimationDrawable anim = (AnimationDrawable) id_recorder_anim_other
								.getBackground();
						anim.start();

						MediaManager.playSound(soundStr,
								new MediaPlayer.OnCompletionListener() {
									@Override
									public void onCompletion(MediaPlayer mp) {
										id_recorder_anim_other
												.setBackgroundResource(R.drawable.adj);
									}
								});

					}
					break;
				}
			}
		});
	}

	public class Recorder {
		public Recorder(float time, String filePath) {
			this.time = time;
			this.filePath = filePath;
		}

		public float getTime() {
			return time;
		}

		public void setTime(float time) {
			this.time = time;
		}

		public String getFilePath() {
			return filePath;
		}

		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}

		float time;
		String filePath;
	}

	// 接收的数据
	String topicstr = null;
	byte[] tempbyte = null;

	private void doLogin() {
		sdk.init(myuserId);
		new Thread(new Runnable() {
			@Override
			public void run() {
				etManager = sdk.getEtManager();
				etManager.setConnectCallback(connectCallback);
				etManager.setListener(new IReceiveListener() {
					public void onMessage(EtMsg msg) {// 别人发过来的消息
						Log.i(TAG, String.format("收到消息： %-10s", msg.getTopic())
								+ ", " + new String(msg.getPayload()));
						tempbyte = msg.getPayload();
						handler.sendEmptyMessage(SuccessOther);
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

	private ICallback<Void> connectCallback = new ICallback<Void>() {
		@Override
		public void onSuccess(Void value) {
			im = sdk.getIM();
			// 文件传输
			iFile = etManager.getFile();
			runOnUiThread(new Runnable() {
				public void run() {
					etManager.setFileListener(buildFileReceiveListener());
					im.connect();
					System.out.println("onSuccess");
				}
			});
		}

		@Override
		public void onFailure(Void v, final Throwable value) {
			System.out.println("onFailure");
		}
	};
	private static final int SuccessMY = 1;
	private static final int SuccessOther = 2;
	private static final int fiald = 3;
	private static final int pic = 4;
	private static final int file = 5;
	Handler handler = new Handler() {// 这里都是返回的数据

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SuccessMY:
				Toast.makeText(IMActivity.this, "发送成功", Toast.LENGTH_SHORT)
						.show();
				break;
			case SuccessOther:
				item = new ListItem();
				try {
					item.setContentStr(new String(tempbyte, "utf-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				listItem.add(item);

				listadapter.notifyDataSetChanged();
				lv_home.smoothScrollToPosition(listadapter.getCount() - 1);
				break;
			case fiald:
				Toast.makeText(IMActivity.this, "发送失败", Toast.LENGTH_SHORT)
						.show();
				break;
			case pic:// 传图片
				// tempbyte
				listadapter.notifyDataSetChanged();
				lv_home.smoothScrollToPosition(listadapter.getCount() - 1);
				break;
			case file:// 传文件
				listadapter.notifyDataSetChanged();
				lv_home.smoothScrollToPosition(listadapter.getCount() - 1);
				break;
			default:
				break;
			}
		}
	};

	public Bitmap Bytes2Bimap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	// 别人或自己发送的值
	String EtMsgstr = null;
	private ICallback<EtMsg> connectCallback2 = new ICallback<EtMsg>() {

		@Override
		public void onSuccess(EtMsg paramT) {
			try {
				EtMsgstr = new String(paramT.getPayload(), "utf-8");
				System.out.println("byte:" + paramT.getPayload() + "\nstr:"
						+ EtMsgstr);

				if (TextUtils.equals(chattomsgId, paramT.getMsgId())) {// 返回true为自己的
					handler.sendEmptyMessage(SuccessMY);
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		@Override
		public void onFailure(EtMsg paramT, Throwable paramThrowable) {
			handler.sendEmptyMessage(fiald);
		}

	};

	private FileCallBack fileCallBack = new FileCallBack() {

		@Override
		public void onSuccess(DocumentInfo paramDocumentInfo, String paramString) {
			// TODO Auto-generated method stub
			System.out.println("下载完毕 onSuccess");
		}

		@Override
		public void onProcess(DocumentInfo paramDocumentInfo,
				String paramString, long paramLong1, long paramLong2) {
			// TODO Auto-generated method stub
			System.out.println("onProcess");
		}

		@Override
		public void onFailure(String paramString, Throwable paramThrowable) {
			// TODO Auto-generated method stub
			System.out.println("onFailure");
		}
	};

	public class MyListAdpter extends BaseAdapter {
		private Context context = null;
		private LayoutInflater inflater = null;

		public MyListAdpter(Context context) {
			this.context = context;

		}

		@Override
		public int getCount() {
			return listItem != null ? listItem.size() : 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		// 每个convert view都会调用此方法，获得当前所需要的view样式
		@Override
		public int getItemViewType(int position) {
			if (!TextUtils.isEmpty((String) listItem.get(position).getMsgId())) {// 根据需要修改这里
				return TYPE_SELF;
			} else {
				return TYPE_OTHER;
			}
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolderSelf holderSelf = null;
			ViewHolderOther holderOther = null;
			int type = getItemViewType(position);
			if (convertView == null) {
				inflater = LayoutInflater.from(context);
				holderSelf = new ViewHolderSelf();
				holderOther = new ViewHolderOther();
				switch (type) {
				case TYPE_SELF:
					convertView = inflater.inflate(
							R.layout.item_communication_self_lv, parent, false);
					holderSelf.content_self = (TextView) convertView
							.findViewById(R.id.content_self);
					holderSelf.iv_self = (ImageView) convertView
							.findViewById(R.id.iv_self);
					holderSelf.ll_sound_self = (LinearLayout) convertView
							.findViewById(R.id.ll_sound_self);
					holderSelf.id_recorder_time_self = (TextView) convertView
							.findViewById(R.id.id_recorder_time_self);
					convertView.setTag(holderSelf);
					break;
				case TYPE_OTHER:
					convertView = inflater
							.inflate(R.layout.item_communication_other_lv,
									parent, false);
					holderOther.content_other = (TextView) convertView
							.findViewById(R.id.content_other);
					holderOther.iv_other = (ImageView) convertView
							.findViewById(R.id.iv_other);
					holderOther.ll_sound_other = (LinearLayout) convertView
							.findViewById(R.id.ll_sound_other);
					holderOther.id_recorder_time_other = (TextView) convertView
							.findViewById(R.id.id_recorder_time_other);

					convertView.setTag(holderOther);
					break;
				default:
					break;
				}
			} else {
				switch (type) {
				case TYPE_SELF:
					holderSelf = (ViewHolderSelf) convertView.getTag();
					break;
				case TYPE_OTHER:
					holderOther = (ViewHolderOther) convertView.getTag();
					break;
				}

			}
			// 图
			String picUrl = listItem.get(position).getPicUrl();
			// 文字
			String contentStr = listItem.get(position).getContentStr();
			// 文件
			String fileStr = listItem.get(position).getFileStr();
			// 语音 播放使用
			String soundStr = listItem.get(position).getFilePath();
			float soundtimeStr = listItem.get(position).getTime();
			// 设置资源
			switch (type) {
			case TYPE_SELF:// extractBytesFromBytes

				if (!TextUtils.isEmpty(picUrl)) {// 比较标识位判断是否是图片
					holderSelf.content_self.setVisibility(View.GONE);
					holderSelf.ll_sound_self.setVisibility(View.GONE);
					holderSelf.iv_self.setVisibility(View.VISIBLE);// 图片控件可见
					try {
						holderSelf.iv_self.setImageBitmap(getBitmaptz(picUrl));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (!TextUtils.isEmpty(contentStr)) {// 文字不需要截取
					holderSelf.iv_self.setVisibility(View.GONE);
					holderSelf.ll_sound_self.setVisibility(View.GONE);
					holderSelf.content_self.setVisibility(View.VISIBLE);// 文字控件可见
					holderSelf.content_self.setText(contentStr);
				} else if (!TextUtils.isEmpty(fileStr)) {
					holderSelf.iv_self.setVisibility(View.GONE);
					holderSelf.ll_sound_self.setVisibility(View.GONE);
					holderSelf.content_self.setVisibility(View.VISIBLE);// 文字控件可见
					holderSelf.content_self.setText(fileStr + "  已发送");
				} else if (!TextUtils.isEmpty(soundStr)) {
					holderSelf.iv_self.setVisibility(View.GONE);
					holderSelf.content_self.setVisibility(View.GONE);
					holderSelf.ll_sound_self.setVisibility(View.VISIBLE);// 声音控件可见
					//
					holderSelf.id_recorder_time_self.setText(soundtimeStr + "");
				}

				break;
			case TYPE_OTHER:// picUrl
				if (!TextUtils.isEmpty(picUrl)) {// 比较标识位判断是否是图片
					holderOther.iv_other.setVisibility(View.VISIBLE);
					holderOther.content_other.setVisibility(View.GONE);
					try {
						holderOther.iv_other
								.setImageBitmap(getBitmaptz(picUrl));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (!TextUtils.isEmpty(contentStr)) {// 文字不需要截取
					holderOther.iv_other.setVisibility(View.GONE);
					holderOther.ll_sound_other.setVisibility(View.GONE);
					holderOther.content_other.setVisibility(View.VISIBLE);
					holderOther.content_other.setText(contentStr);
				} else if (!TextUtils.isEmpty(fileStr)) {
					holderOther.iv_other.setVisibility(View.GONE);
					holderOther.ll_sound_other.setVisibility(View.GONE);
					holderOther.content_other.setVisibility(View.VISIBLE);
					holderOther.content_other.setText(fileStr + "  已接收");
				} else if (!TextUtils.isEmpty(soundStr)) {
					holderOther.iv_other.setVisibility(View.GONE);
					holderOther.content_other.setVisibility(View.GONE);
					holderOther.ll_sound_other.setVisibility(View.VISIBLE);
					//
					holderOther.id_recorder_time_other.setText(soundtimeStr
							+ "");

				}
				break;
			}

			return convertView;
		}

		private class ViewHolderSelf {
			private TextView content_self;
			private ImageView iv_self;
			private LinearLayout ll_sound_self;
			private TextView id_recorder_time_self;

		}

		private class ViewHolderOther {
			private TextView content_other;
			private ImageView iv_other;
			private LinearLayout ll_sound_other;
			private TextView id_recorder_time_other;

		}
	}

	/**
	 * @param srcPath
	 * @return
	 * @throws Exception
	 *             本地图片操作
	 */
	public static Bitmap getBitmaptz(String srcPath) throws Exception {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// newOpts.inSampleSize = 2;// 设置缩放比例
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return bitmap;// 压缩好比例大小后再进行质量压缩
	}

	public String Path;
	public String Path1;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FILE_SELECT_CODE:
			if (resultCode == RESULT_OK) {
				// Get the Uri of the selected file
				Uri uri = data.getData();
				ContentResolver resolver = getContentResolver();
				String filetype = resolver.getType(uri);
				if (filetype.startsWith("image")) {
					Path = FileUtils.getPath(IMActivity.this, uri);
					// 自己这边也该显示图片，自己的需要存chattomsgId
					item = new ListItem();
					item.setPicUrl(Path);
					item.setMsgId(chattomsgId);
					listItem.add(item);
					//
					iFile.fileTo(OtheruserId, Path, fileCallBack);
					handler.sendEmptyMessage(pic);

				}

			}
			break;
		case FILE_SELECT_CODE1:
			if (resultCode == RESULT_OK) {
				// Get the Uri of the selected file
				Uri uri = data.getData();
				ContentResolver resolver = getContentResolver();
				String filetype = resolver.getType(uri);
				Path1 = FileUtils.getPath(IMActivity.this, uri);
				//
				item = new ListItem();
				item.setFileStr(Path1);
				item.setMsgId(chattomsgId);
				listItem.add(item);
				//
				iFile.fileTo(OtheruserId, Path1, fileCallBack);
				handler.sendEmptyMessage(file);
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// 文件传输发送端------------------------------------------------------------------------
	private void fileTo(IFile file) {
		file.fileTo("414", "c:\\p2p.png", new FileCallBack() {
			@Override
			public void onSuccess(DocumentInfo documentInfo, String fileFullPath) {
				System.out.println("fileTo onSuccess\r\n" + documentInfo
						+ "\r\n" + fileFullPath);
				System.out.println("\r\n");
			}

			@Override
			public void onProcess(DocumentInfo documentInfo,
					String fileFullPath, long currentIndex, long total) {
				System.out.println("fileTo onProcess\r\n" + documentInfo
						+ "\r\n" + fileFullPath);
				System.out.println(total + "：" + currentIndex + "\r\n");
			}

			@Override
			public void onFailure(String fileFullPath, Throwable throwable) {
				System.out.println("fileTo onFailure+\r\n" + fileFullPath);
				throwable.printStackTrace(System.err);
			}
		});
	}

	private void fileFrom(IFile file) {
		file.fileFrom("414", "report.png", new ICallback<Void>() {
			@Override
			public void onSuccess(Void value) {
				System.out.println("fileFrom onSuccess");
			}

			@Override
			public void onFailure(Void t, Throwable e) {
				System.out.println("fileFrom onFailure");
				e.printStackTrace(System.err);
			}
		});
	}

	public String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		}
		return sdDir.toString();

	}

	// 文件接收端--------------------------------------------------------------

	private IFileReceiveListener buildFileReceiveListener() {
		return new IFileReceiveListener() {
			@Override
			public void onReceived(String senderId, DocumentInfo documentInfo) {

				File appDir = null;
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					appDir = new File(
							Environment.getExternalStorageDirectory(),
							"downfile1");
				} else {
					appDir = new File(IMActivity.this.getFilesDir(),
							"downfile1");
				}
				if (!appDir.exists()) {
					appDir.mkdir();
				}

				// 用户自行确定是否下载消息,此处模拟直接下载文件.
				System.out.println("接收文件.发送文件用户【" + senderId + "】, 接收到文件信息："
						+ documentInfo);
				int isok = etManager.getFile().downloadFile(documentInfo,
						appDir + File.separator + documentInfo.getFileName());
				System.out.println("文件下载完成....");
				// 根据isok判断是否接收成功
				String type = documentInfo.getType();
				if (documentInfo.getFileName().endsWith("jpg")) {// 判断是图片或文字
					item = new ListItem();
					item.setPicUrl(appDir + File.separator
							+ documentInfo.getFileName());
					listItem.add(item);
				}else if(documentInfo.getFileName().endsWith("amr")){
					item = new ListItem();
					item.setFilePath(appDir + File.separator
							+ documentInfo.getFileName());
					listItem.add(item);
				}else {
					item = new ListItem();
					item.setFileStr(appDir + File.separator
							+ documentInfo.getFileName());
					listItem.add(item);
				}
				handler.sendEmptyMessage(file);
			}

			@Override
			public void onCheckingFile(String senderId,
					DocumentInfo documentInfo) {
				System.out.println("检查本地文件是否存在! 用户ID = 【" + senderId
						+ "】, 接收到文件描述消息：" + documentInfo);
				String fileName = documentInfo.getFileName();
				// 模拟客户端检查文件
				if (fileName.indexOf("png") == -1) {
					// 文件不存在.
					documentInfo.setDescn(fileName + " 文件不存在.");
					// documentInfo.setType(FileTransferTypeEnum.EXCEPTION
					// .getCode());
					etManager.getFile().sendMsg(senderId, documentInfo, null);
					return;
				}
				String fileFullName = "d:\\" + fileName;
				etManager.getFile().fileTo(senderId, fileFullName,
						new FileCallBack() {
							@Override
							public void onSuccess(DocumentInfo documentInfo,
									String fileFullPath) {
								System.out.println("fileTo onSuccess");
								System.out.println(documentInfo);
								System.out.println(fileFullPath);
							}

							@Override
							public void onProcess(DocumentInfo documentInfo,
									String fileFullPath, long currentIndex,
									long total) {
								System.out.println("fileTo onProcess");
								System.out.println(documentInfo);
								System.out.println(fileFullPath);
							}

							@Override
							public void onFailure(String fileFullPath,
									Throwable throwable) {
								System.out.println("fileTo onFailure");
								System.out.println(fileFullPath);
								throwable.printStackTrace();
							}
						});
			}
		};
	}
}
