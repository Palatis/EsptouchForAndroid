package com.espressif.iot.esptouch;

import com.espressif.iot.esptouch.task.EspTaskImpl;
import com.espressif.iot.esptouch.task.EsptouchTaskParameter;

import java.net.InetAddress;

public class EsptouchTask {
	public interface OnEsptouchResultListener {
		void onResult(EsptouchResult result);
	}

	public interface NetworkHelperCallback {
		InetAddress getLocalInetAddress();
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

	public EspTaskImpl _mEsptouchTask;
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
		_mEsptouchTask = new EspTaskImpl(apSsid, apBssid, apPassword, _mParameter, isSsidHidden);
		_mEsptouchTask.setLogger(DUMMY_LOGGER);
	}

	/**
	 * Constructor of EsptouchTask
	 *
	 * @param apSsid             the Ap's ssid
	 * @param apBssid            the Ap's bssid
	 * @param apPassword         the Ap's password
	 * @param isSsidHidden       whether the Ap's ssid is hidden
	 * @param timeoutMillisecond timeout in milliseconds
	 */
	public EsptouchTask(String apSsid, String apBssid, String apPassword, boolean isSsidHidden, int timeoutMillisecond) {
		this(apSsid, apBssid, apPassword, isSsidHidden);
		_mParameter.setWaitUdpTotalMillisecond(timeoutMillisecond);
	}

	public void interrupt() {
		_mEsptouchTask.interrupt();
	}

	public void executeForResults(int expectTaskResultCount) throws RuntimeException {
		if (expectTaskResultCount <= 0) {
			expectTaskResultCount = Integer.MAX_VALUE;
		}
		_mEsptouchTask.executeForResults(expectTaskResultCount);
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
