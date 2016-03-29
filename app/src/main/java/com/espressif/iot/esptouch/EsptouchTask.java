package com.espressif.iot.esptouch;

import com.espressif.iot.esptouch.task.EsptouchTaskParameter;
import com.espressif.iot.esptouch.task.__EsptouchTask;

import java.net.InetAddress;
import java.util.List;

public class EsptouchTask {
	public interface OnEsptouchResultListener {
		void onResult(EsptouchResult result);
	}

	public interface NetworkHelperCallback {
		InetAddress getLocalInetAddress();

		InetAddress parseInetAddress(byte[] bytes, int offset, int length);
	}

	public interface Logger {
		void info(String msg);

		void debug(String msg);

		void warning(String msg);
	}

	private static final Logger DUMMY_LOGGER = new Logger() {
		@Override
		public void info(String msg) {
		}

		@Override
		public void debug(String msg) {
		}

		@Override
		public void warning(String msg) {
		}
	};

	public __EsptouchTask _mEsptouchTask;
	private EsptouchTaskParameter _mParameter;

	/**
	 * Constructor of EsptouchTask
	 *
	 * @param apSsid       the Ap's ssid
	 * @param apBssid      the Ap's bssid
	 * @param apPassword   the Ap's password
	 * @param isSsidHidden whether the Ap's ssid is hidden
	 */
	public EsptouchTask(String apSsid, String apBssid, String apPassword, boolean isSsidHidden) {
		_mParameter = new EsptouchTaskParameter();
		_mEsptouchTask = new __EsptouchTask(apSsid, apBssid, apPassword, _mParameter, isSsidHidden);
		_mEsptouchTask.setLogger(DUMMY_LOGGER);
	}

	/**
	 * Constructor of EsptouchTask
	 *
	 * @param apSsid       the Ap's ssid
	 * @param apBssid      the Ap's bssid
	 * @param apPassword   the Ap's password
	 * @param isSsidHidden whether the Ap's ssid is hidden
	 */
	public EsptouchTask(String apSsid, String apBssid, String apPassword, boolean isSsidHidden, int timeoutMillisecond) {
		this(apSsid, apBssid, apPassword, isSsidHidden);
		_mParameter.setWaitUdpTotalMillisecond(timeoutMillisecond);
	}

	public void interrupt() {
		_mEsptouchTask.interrupt();
	}

	public boolean isCancelled() {
		return _mEsptouchTask.isCancelled();
	}

	public List<EsptouchResult> executeForResults(int expectTaskResultCount) throws RuntimeException {
		if (expectTaskResultCount <= 0) {
			expectTaskResultCount = Integer.MAX_VALUE;
		}
		return _mEsptouchTask.executeForResults(expectTaskResultCount);
	}

	public void setEsptouchListener(OnEsptouchResultListener listener) {
		_mEsptouchTask.setEsptouchListener(listener);
	}

	public void setGetLocalInetAddressCallback(NetworkHelperCallback callback) {
		_mEsptouchTask.setGetLocalInetAddressCallback(callback);
	}

	public void setLogger(Logger logger) {
		_mEsptouchTask.setLogger(logger == null ? DUMMY_LOGGER : logger);
	}
}
