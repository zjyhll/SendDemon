package com.beidouapp.et.av;

import com.beidouapp.et.client.EtFactory;
import com.beidouapp.et.client.EtManager;
import com.beidouapp.et.client.ModuleType;
import com.beidouapp.et.client.api.IAV;
import com.beidouapp.et.client.api.IContext;
import com.beidouapp.et.client.api.IM;
import com.beidouapp.et.client.api.IWeb;

/**
 * ET SDK 单例类.提供SDK核心通信功能.
 * 
 * @author mhuang.
 *
 */
public class EtSDK {
	public static final String TAG = EtSDK.class.getName();
	public static final String APP_KEY = "4cdad356-85b4-008627";
	public static final String SECURE_KEY = "c7147f6fea0daf6cbd36ed65b037b4c9";
	public static final int LB_PORT = 8085;
	public static final String LB_IP = "115.28.55.154";// "115.28.55.154";
														// 220.169.30.125
	private EtFactory factory;
	private IContext etContext;
	private EtManager etManager;

	private static EtSDK sdk = new EtSDK();

	private EtSDK() {
	}

	public static EtSDK getInstance() {
		return sdk;
	}

	public void init(String userId) {
		factory = new EtFactory();
		etContext = factory.getEtContext();
		etContext.setAppKey(APP_KEY).setSecretKey(SECURE_KEY).setFirstReconnectCount(0).setReconnectCount(-1).setDefaultQos(1).setServerDomain(LB_IP)
				.setServerPort(LB_PORT + "").setClientId(userId);
	}

	public void unInit() {
		factory = null;
		etContext = null;
		etManager = null;
	}

	public EtManager getEtManager() {
		etManager = factory.create(etContext, new ModuleType[]{ModuleType.WEB, ModuleType.AV, ModuleType.FILE});
		return etManager;
	}

	/**
	 * 获得IM消息接口.
	 * 
	 * @return
	 */
	public IM getIM() {
		return etManager.getIm();
	}

	/**
	 * 获得用户、群管理接口.
	 * 
	 * @return
	 */
	public IWeb getWeb() {
		return etManager.getWeb();
	}

	/**
	 * 获得音视频接口.
	 * 
	 * @return
	 */
	public IAV getAV() {
		return etManager.getAV();
	}
}