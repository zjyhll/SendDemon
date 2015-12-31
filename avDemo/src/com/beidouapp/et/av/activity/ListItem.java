package com.beidouapp.et.av.activity;

public class ListItem {
	private String picUrl = null;
	private String contentStr = null;
	private String fileStr = null;
	private String msgId = null;
	// 语音
	private float time = 0;
	private String filePath = null;

	public ListItem(float time, String filePath) {
		this.time = time;
		this.filePath = filePath;
	}
	public ListItem(){
		
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

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public String getContentStr() {
		return contentStr;
	}

	public void setContentStr(String contentStr) {
		this.contentStr = contentStr;
	}

	public String getFileStr() {
		return fileStr;
	}

	public void setFileStr(String fileStr) {
		this.fileStr = fileStr;
	}
}
