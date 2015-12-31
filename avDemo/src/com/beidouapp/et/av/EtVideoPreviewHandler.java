package com.beidouapp.et.av;

import org.pjsip.pjsua2.VideoPreviewOpParam;
import org.pjsip.pjsua2.VideoWindowHandle;

import com.beidouapp.et.client.api.IAV;

import android.util.Log;
import android.view.SurfaceHolder;

/**
 * 客户端前置相机预览处理器.
 * 
 * @author mhuang.
 *
 */
public class EtVideoPreviewHandler implements SurfaceHolder.Callback {
	private static final String TAG = EtVideoPreviewHandler.class.getName();

	/** 前置摄像头开启状态. */
	public boolean videoPreviewActive = false;

	private IAV av;

	public EtVideoPreviewHandler(IAV av) {
		this.av = av;
	}

	/**
	 * 更新摄像头窗口状态.
	 * 
	 * @param holder
	 */
	public void updateVideoPreview(SurfaceHolder holder) {
		try {
			if (av.getCurrentEtCall() != null && av.getCurrentEtCall().getVideoWindow() != null
					&& av.getCurrentEtCall().getVideoPreview() != null) {
				if (videoPreviewActive) {
					VideoWindowHandle vidWH = new VideoWindowHandle();
					vidWH.getHandle().setWindow(holder.getSurface());
					VideoPreviewOpParam vidPrevParam = new VideoPreviewOpParam();
					vidPrevParam.setWindow(vidWH);
					av.getCurrentEtCall().getVideoPreview().start(vidPrevParam);
				} else {
					av.getCurrentEtCall().getVideoPreview().stop();
				}
			}
		} catch (Throwable e) {
			System.out.println("Throwable:" + e);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Log.i(TAG, "video preview is surface changed.");
		updateVideoPreview(holder);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "video preview is surface created.");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		try {
			if (av.getCurrentEtCall() != null) {
				av.getCurrentEtCall().getVideoPreview().stop();
				Log.i(TAG, "video preview is surface destroy.");
			}
		} catch (Throwable e) {
			System.out.println(e);
		}
	}
}