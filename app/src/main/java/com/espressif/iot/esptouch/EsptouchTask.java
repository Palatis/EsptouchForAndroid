package com.espressif.iot.esptouch;

import android.content.Context;

import com.espressif.iot.esptouch.task.EsptouchTaskParameter;
import com.espressif.iot.esptouch.task.__EsptouchTask;

import java.util.List;

public class EsptouchTask {
	public interface IEsptouchListener {
		/**
		 * when new esptouch result is added, the listener will call
		 * onEsptouchResultAdded callback
		 *
		 * @param result
		 *            the Esptouch result
		 */
		void onEsptouchResultAdded(EsptouchResult result);
	}

	public __EsptouchTask _mEsptouchTask;
	private EsptouchTaskParameter _mParameter;

	/**
	 * Constructor of EsptouchTask
	 *
	 * @param apSsid
	 *            the Ap's ssid
	 * @param apBssid
	 *            the Ap's bssid
	 * @param apPassword
	 *            the Ap's password
	 * @param isSsidHidden
	 *            whether the Ap's ssid is hidden
	 * @param context
	 *            the Context of the Application
	 */
	public EsptouchTask(String apSsid, String apBssid, String apPassword,
			boolean isSsidHidden, Context context) {
		_mParameter = new EsptouchTaskParameter();
		_mEsptouchTask = new __EsptouchTask(apSsid, apBssid, apPassword,
				context, _mParameter, isSsidHidden);
	}

	/**
	 * Constructor of EsptouchTask
	 *
	 * @param apSsid
	 *            the Ap's ssid
	 * @param apBssid
	 *            the Ap's bssid
	 * @param apPassword
	 *            the Ap's password
	 * @param isSsidHidden
	 *            whether the Ap's ssid is hidden
	 * @param timeoutMillisecond
	 *            (it should be >= 15000+6000) millisecond of total timeout
	 * @param context
	 *            the Context of the Application
	 */
	public EsptouchTask(String apSsid, String apBssid, String apPassword,
			boolean isSsidHidden, int timeoutMillisecond, Context context) {
		_mParameter = new EsptouchTaskParameter();
		_mParameter.setWaitUdpTotalMillisecond(timeoutMillisecond);
		_mEsptouchTask = new __EsptouchTask(apSsid, apBssid, apPassword,
				context, _mParameter, isSsidHidden);
	}

	public void interrupt() {
		_mEsptouchTask.interrupt();
	}

	public EsptouchResult executeForResult() throws RuntimeException {
		return _mEsptouchTask.executeForResult();
	}

	public boolean isCancelled() {
		return _mEsptouchTask.isCancelled();
	}

	public List<EsptouchResult> executeForResults(int expectTaskResultCount)
			throws RuntimeException {
		if (expectTaskResultCount <= 0) {
			expectTaskResultCount = Integer.MAX_VALUE;
		}
		return _mEsptouchTask.executeForResults(expectTaskResultCount);
	}

	public void setEsptouchListener(IEsptouchListener esptouchListener) {
		_mEsptouchTask.setEsptouchListener(esptouchListener);
	}
}
