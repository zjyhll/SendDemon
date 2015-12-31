package com.beidouapp.et.av.activity.view;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by yz1309 on 2015/5/1.
 */
public class AudioManager {
    private MediaRecorder mMediaRecorder;
    private String mDir;
    private String mCurrentFilePath;
    private Boolean isPrepare = false;

    private static AudioManager mInstace;

    private AudioManager(String dir) {
        mDir = dir;
    }

    public String getCurrentFilePath() {
        return mCurrentFilePath;
    }

    public interface AudioStateListener {
        void wellPrepared();
    }

    public AudioStateListener mLinster;

    public void setOnAudioStateListener(AudioStateListener listener) {
        mLinster = listener;
    }

    public static AudioManager getmInstace(String dir) {
        if (mInstace == null) {
            synchronized (AudioManager.class) {
                if (mInstace == null)
                    mInstace = new AudioManager(dir);
            }
        }

        return mInstace;

    }

    public void prepareAudio() {

        isPrepare = false;
        File dir = new File(mDir);
        if (!dir.exists())
            dir.mkdirs();
        String fileName = generateFileName();
        File file = new File(dir, fileName);
        mCurrentFilePath = file.getAbsolutePath();
        mMediaRecorder = new MediaRecorder();
        /*设置输出文件*/
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        /*设置 音频源为麦克风*/
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        /*设置 音频格式*/
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
        /*设置 音频编码*/
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mMediaRecorder.prepare();

            mMediaRecorder.start();
            /*准备结束*/
            isPrepare = true;

            if (mLinster != null) {
                mLinster.wellPrepared();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 随机生成文件名称
     *
     * @return
     */
    private String generateFileName() {
        return UUID.randomUUID().toString() + ".amr";

    }

    public int getVoiceLevel(int maxLevel) {
        if (isPrepare) {
            /*getMaxAmplitude 范围 1-32767*/
            try {
                return maxLevel * mMediaRecorder.getMaxAmplitude() / 32768 + 1;
            } catch (Exception ex) {

            }
        }
        return 1;
    }

    public void release() {
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    public void cancel() {

        release();
        if (mCurrentFilePath != null) {
            File file = new File(mCurrentFilePath);

            file.delete();
            mCurrentFilePath = null;

        }
    }
}
