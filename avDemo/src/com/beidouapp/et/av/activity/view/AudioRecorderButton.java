package com.beidouapp.et.av.activity.view;

import com.beidouapp.et.avdemo.R;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

/**
 * Created by yz1309 on 2015/5/1.
 */
public class AudioRecorderButton extends Button
        implements AudioManager.AudioStateListener {
    /*y轴取消的距离*/
    private static final int Distance_Cancel_Y = 50;
    private static final int State_Normal = 1;
    private static final int State_Recording = 2;
    private static final int State_Want_To_Cancel = 3;

    private int mCurState = State_Normal;
    /*已经开始录音*/
    private Boolean isRecording = false;

    private DialogManager mDialogManager;

    private AudioManager mAudioManager;
    private float mTime;

    /*是否触发long click*/
    private boolean mReady;


    public AudioRecorderButton(Context context) {
        super(context, null);
    }

    public AudioRecorderButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        mDialogManager = new DialogManager(getContext());

        String dir = Environment.getExternalStorageDirectory() +
                "/slantech_recorder";
        mAudioManager = AudioManager.getmInstace(dir);
        mAudioManager.setOnAudioStateListener(this);

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mReady = true;
                mAudioManager.prepareAudio();
                return false;
            }
        });
    }

    /*录音完成后的回调*/
    public interface AudioFinishRecorderListener {
        void onFinish(float seconds, String filePath);
    }

    private AudioFinishRecorderListener mListener;

    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listner) {
        mListener = listner;
    }

    /*实现AudioManager中的接口*/
    @Override
    public void wellPrepared() {
        mHandler.sendEmptyMessage(Msg_Audio_Prepared);
    }


    /*获取音量大小的Runnable*/
    private Runnable mGetVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
            while (isRecording) {
                try {
                    Thread.sleep(100);
                    mTime += 0.1f;
                    mHandler.sendEmptyMessage(Msg_Voice_Changed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private static final int Msg_Audio_Prepared = 0X110;
    private static final int Msg_Voice_Changed = 0X111;
    private static final int Msg_Dialog_Dimiss = 0X112;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Msg_Audio_Prepared:
                    mDialogManager.showRecordingDialog();
                    isRecording = true;
                    new Thread(mGetVoiceLevelRunnable).start();
                    break;
                case Msg_Voice_Changed:
                    mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
                    break;
                case Msg_Dialog_Dimiss:
                    mDialogManager.dimissDialog();
                    break;
            }
        }

        ;
    };


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int aciton = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (aciton) {
            case MotionEvent.ACTION_DOWN:

                changeState(State_Recording);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isRecording) {
                /*根据xy的坐标判断是否想要取消*/
                    if (wantToCancel(x, y)) {
                        changeState(State_Want_To_Cancel);
                    } else {
                        changeState(State_Recording);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!mReady) {
                    reset();
                    return super.onTouchEvent(event);
                }
                if (!isRecording || mTime < 0.6f) {
                    mDialogManager.tooShort();
                    mAudioManager.cancel();
                    /*延迟1.3秒关闭*/
                    mHandler.sendEmptyMessageDelayed(Msg_Dialog_Dimiss, 1300);
                } else if (mCurState == State_Recording) {
                    /*正常录制结束*/
                    //release callback to activity
                    mDialogManager.dimissDialog();
                    if (mListener != null) {
                        mListener.onFinish(mTime, mAudioManager.getCurrentFilePath());
                    }
                    mAudioManager.release();


                } else if (mCurState == State_Want_To_Cancel) {
                    //cancel
                    mDialogManager.dimissDialog();
                    mAudioManager.cancel();
                }
                reset();
                break;
        }

        return super.onTouchEvent(event);
    }

    /*恢复状态及标志位*/
    private void reset() {
        isRecording = false;
        changeState(State_Normal);
        mReady = false;
        mTime = 0;


    }

    private boolean wantToCancel(int x, int y) {
        if (x < 0 || x > getWidth()) {
            /*按钮范围外 按钮右部*/
            return true;
        }
        if (y < -Distance_Cancel_Y || y > getHeight() + Distance_Cancel_Y) {
            /*按钮上部 下部*/
            return true;
        }
        return false;
    }

    /*
    * 显示文本 背景色
    * */
    private void changeState(int state_recording) {
        if (mCurState != state_recording) {
            mCurState = state_recording;
            switch (state_recording) {
                case State_Normal:
                    setBackgroundResource(R.drawable.btn_recorder_normal);
                    setText(R.string.str_reorder_normal);
                    break;

                case State_Recording:
                    setBackgroundResource(R.drawable.btn_recordering);
                    setText(R.string.str_reorder_recording);
                    if (isRecording) {
                        //TODO dialog.recording();
                        mDialogManager.recording();
                    }
                    break;
                case State_Want_To_Cancel:
                    setBackgroundResource(R.drawable.btn_recordering);
                    setText(R.string.str_reorder_want_to_cancel);
                    //TODO dialog.wantCancel();
                    mDialogManager.wantToCancel();
                    break;

            }
        }
    }


}
