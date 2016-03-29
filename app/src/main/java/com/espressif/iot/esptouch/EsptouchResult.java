package com.espressif.iot.esptouch;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class EsptouchResult {

	private final boolean mSuccess;
	private final String mBssid;
	private final InetAddress mInetAddress;
	private AtomicBoolean mCancelled;

	/**
	 * Constructor of EsptouchResult
	 *
	 * @param isSuc whether the esptouch task is executed suc
	 * @param bssid the device's bssid
	 * @param inetAddress the device's ip address
	 */
	public EsptouchResult(boolean isSuc, String bssid,InetAddress inetAddress) {
		this.mSuccess = isSuc;
		this.mBssid = bssid;
		this.mInetAddress = inetAddress;
		this.mCancelled = new AtomicBoolean(false);
	}

	public boolean isSuc() {
		return this.mSuccess;
	}

	public String getBssid() {
		return this.mBssid;
	}
	public boolean isCancelled() {
		return mCancelled.get();
	}

	public void setCancelled(boolean isCancelled){
		this.mCancelled.set(isCancelled);
	}

	public InetAddress getInetAddress() {
		return this.mInetAddress;
	}

}
