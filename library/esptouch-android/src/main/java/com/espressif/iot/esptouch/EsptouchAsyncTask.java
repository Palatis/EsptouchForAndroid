package com.espressif.iot.esptouch;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * <p>An easy-to-use wrapper class to make your life easier.</p>
 * <p><b>DO NOT CALL {@link #cancel(boolean)} DIRECTLY.</b> It will still wait until the
 * underlying {@link EsptouchTask} to retrieve enough results (or timeout). Call
 * {@link #interrupt()} instead if you really want to cancel the task.</p>
 * <p>Created by Palatis on 2016/4/3.</p>
 */
@SuppressWarnings("unused")
public abstract class EsptouchAsyncTask
		extends AsyncTask<Void, EsptouchResult, Void>
		implements EsptouchTask.OnEsptouchResultListener, EsptouchTask.NetworkHelperCallback, EsptouchTask.Logger {
	private static final String TAG = EsptouchAsyncTask.class.getSimpleName();

	private WifiManager mWifiManager;
	private final int mResultsWanted;
	private EsptouchTask mEsptouchTask;
	private WifiManager.MulticastLock mMulticastLock;
	private InetAddress mLocalInetAddress;

	/**
	 * Construct an EsptouchAsyncTask instance
	 *
	 * @param context       the context, an {@link android.app.Application} will suffice.
	 * @param ssid          the SSID of the AP you want the esp8266 to connect to. you must be
	 *                      connected to this AP first.
	 * @param bssid         the BSSID of the AP you want the esp8266 to connect to (if there are
	 *                      multiple APs with same SSID, ex. in a WDS config)
	 * @param passphrase    the password for WEP, or PSK (pres-shared-key) for WPA/WPA2. pass
	 *                      {@code ""} or {@code null} for open wifi.
	 * @param hidden        is the SSID of the AP hidden
	 * @param resultsWanted how many esp8266 do you want to connect
	 */
	public EsptouchAsyncTask(@NonNull Context context, @NonNull String ssid, @Nullable String bssid, @Nullable String passphrase, boolean hidden, int resultsWanted) {
		super();

		mResultsWanted = resultsWanted;



		if (passphrase == null)
			passphrase = "";

		mEsptouchTask = new EsptouchTask(ssid, bssid, passphrase, hidden);
		mEsptouchTask.setEsptouchListener(this);
		mEsptouchTask.setGetLocalInetAddressCallback(this);
		mEsptouchTask.setLogger(this);

		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}

	/**
	 * <p>This method will try {@link WifiManager.MulticastLock#acquire()} the
	 * {@link WifiManager.MulticastLock}, so call super as late as possible. Which means,</p>
	 * <pre>{@code
	 * @Override
	 * protected void onPreExecute() {
	 *     // do your work here
	 *
	 *     super.onPreExecute(); // and call me when you're done
	 * }
	 * }</pre>
	 * {@inheritDoc}
	 */
	@CallSuper
	@Override
	protected void onPreExecute() {
		final WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		final int ip = wifiInfo.getIpAddress();
		if (ip == 0) {
			interrupt();
			throw new RuntimeException("No IP address, not connected to wifi?");
		}

		try {
			mLocalInetAddress = InetAddress.getByAddress(new byte[]{(byte) (ip & 0xff), (byte) (ip >> 8 & 0xff), (byte) (ip >> 16 & 0xff), (byte) (ip >> 24 & 0xff)});
		} catch (UnknownHostException ex) {
			interrupt();
			throw new RuntimeException(ex);
		}

		mMulticastLock = mWifiManager.createMulticastLock("test wifi");
		mWifiManager = null; // release resources
		mMulticastLock.acquire();
	}

	/**
	 * <p>This function calls {@link WifiManager.MulticastLock#release()}, so call super as early as possible. Which means,</p>
	 * <pre>{@code
	 * @Override
	 * protected void onPostExecute(Void ignored) {
	 *     super.onPostExecute(ignored); // call super as soon as possible
	 *
	 *     // do your work here
	 * }
	 * }</pre>
	 * {@inheritDoc}
	 *
	 * @param ignored ignored
	 */
	@CallSuper
	@Override
	protected void onPostExecute(Void ignored) {
		cleanUp();
	}

	/**
	 * <p>This function calls {@link WifiManager.MulticastLock#release()}, so call super as early as possible. Which means,</p>
	 * <pre>{@code
	 * @Override
	 * protected void onCancelled(Void ignored) {
	 *     super.onCancelled(ignored); // call super as soon as possible
	 *
	 *     // do your work here
	 * }
	 * }</pre>
	 * {@inheritDoc}
	 *
	 * @param ignored ignored
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@CallSuper
	@Override
	protected void onCancelled(Void ignored) {
		cleanUp();
	}

	/**
	 * <p>This function calls {@link WifiManager.MulticastLock#release()}, so call super as early as possible. Which means,</p>
	 * <pre>{@code
	 * @Override
	 * protected void onCancelled() {
	 *     super.onCancelled(); // call super as soon as possible
	 *
	 *     // do your work here
	 * }
	 * }</pre>
	 * {@inheritDoc}
	 */
	@CallSuper
	@Override
	protected void onCancelled() {
		cleanUp();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param ignored ignored
	 */
	@WorkerThread
	@Override
	protected Void doInBackground(Void... ignored) {
		if (isCancelled())
			return null;
		try {
			mEsptouchTask.executeForResults(mResultsWanted);
		} finally {
			mEsptouchTask = null;
		}
		return null;
	}

	/**
	 * interrupt the underlying EsptouchTask
	 */
	@CallSuper
	public void interrupt() {
		cancel(true);
		if (mEsptouchTask != null) {
			mEsptouchTask.interrupt();
			mEsptouchTask = null;
		}
	}

	@Override
	public void info(String msg) {
		Log.i(TAG, msg);
	}

	@Override
	public void debug(String msg) {
		Log.d(TAG, msg);
	}

	@Override
	public void warning(String msg) {
		Log.w(TAG, msg);
	}

	@Override
	public InetAddress getLocalInetAddress() {
		return mLocalInetAddress;
	}

	@Override
	public InetAddress parseInetAddress(byte[] inputBytes, int offset, int length) {
		final byte[] ipBytes = new byte[4];
		System.arraycopy(inputBytes, offset, ipBytes, 0, length);
		try {
			return InetAddress.getByAddress(ipBytes);
		} catch (UnknownHostException ex) {
			throw new RuntimeException(ex);
		} finally {
			cleanUp();
		}
	}

	private void cleanUp() {
		try {
			mMulticastLock.release();
			mMulticastLock = null;
		} catch (RuntimeException ex) {
			Log.d(TAG, "cannot release multicast lock", ex);
			mMulticastLock = null;
		}
	}

	@Override
	public final void onResult(EsptouchResult result) {
		publishProgress(result);
	}
}
