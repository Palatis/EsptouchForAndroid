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
	 * @param successed   whether the esptouch task is executed suc
	 * @param bssid       the device's bssid
	 * @param inetAddress the device's ip address
	 */
	public EsptouchResult(boolean successed, String bssid, InetAddress inetAddress) {
		mSuccess = successed;
		mBssid = bssid;
		mInetAddress = inetAddress;
		mCancelled = new AtomicBoolean(false);
	}

	public boolean isSuccess() {
		return mSuccess;
	}

	public String getBssid() {
		return mBssid;
	}

	public boolean isCancelled() {
		return mCancelled.get();
	}

	public void setCancelled(boolean cancelled) {
		mCancelled.set(cancelled);
	}

	public InetAddress getInetAddress() {
		return mInetAddress;
	}

	@Override
	public String toString() {
		return super.toString() +
				"{ success = " + mSuccess +
				", cancelled = " + mCancelled.get() +
				", bssid = " + mBssid +
				", ip = " + mInetAddress.getHostAddress() +
				"}";
	}
}
