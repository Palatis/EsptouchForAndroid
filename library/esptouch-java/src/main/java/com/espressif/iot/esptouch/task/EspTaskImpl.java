package com.espressif.iot.esptouch.task;

import com.espressif.iot.esptouch.EsptouchResult;
import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.protocol.EsptouchGenerator;
import com.espressif.iot.esptouch.udp.UDPSocketClient;
import com.espressif.iot.esptouch.udp.UDPSocketServer;
import com.espressif.iot.esptouch.util.ByteUtil;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class EspTaskImpl {
	/**
	 * one indivisible data contain 3 9bits info
	 */
	private static final int ONE_DATA_LEN = 3;

	private volatile List<EsptouchResult> mEsptouchResultList;
	private volatile boolean mSuccessed = false;
	private volatile boolean mInterrupted = false;
	private volatile boolean mExecuted = false;
	private AtomicBoolean mCancelled;
	private final UDPSocketClient mSocketClient;
	private final UDPSocketServer mSocketServer;
	private final String mApSsid;
	private final String mApBssid;
	private final boolean mIsSsidHidden;
	private final String mApPassword;
	private EsptouchTaskParameter mParameter;
	private volatile Map<String, Integer> mBssidTaskSucCountMap;

	private EsptouchTask.OnEsptouchResultListener mOnEsptouchResultListener;
	private EsptouchTask.NetworkHelperCallback mNetworkHelperCallback;
	private EsptouchTask.Logger mLogger;

	public EspTaskImpl(String apSsid, String apBssid, String apPassword, EsptouchTaskParameter parameter, boolean isSsidHidden) {
		if (apSsid == null || apSsid.isEmpty())
			throw new IllegalArgumentException("AP SSID should not be null or empty");

		mApSsid = apSsid;
		mApBssid = apBssid;
		mApPassword = apPassword == null ? "" : apPassword;
		mCancelled = new AtomicBoolean(false);
		mParameter = parameter;
		mSocketClient = new UDPSocketClient();
		mSocketServer = new UDPSocketServer(mParameter.getPortListening(), mParameter.getWaitUdpTotalMillisecond());
		mIsSsidHidden = isSsidHidden;
		mEsptouchResultList = new ArrayList<>();
		mBssidTaskSucCountMap = new HashMap<>();
	}

	private void __putEsptouchResult(boolean isSuc, String bssid, InetAddress inetAddress) {
		synchronized (mEsptouchResultList) {
			// check whether the result receive enough UDP response
			boolean isTaskSucCountEnough = false;
			Integer count = mBssidTaskSucCountMap.get(bssid);
			if (count == null) {
				count = 0;
			}
			++count;
			mBssidTaskSucCountMap.put(bssid, count);
			isTaskSucCountEnough = count >= mParameter
					.getThresholdSucBroadcastCount();
			if (!isTaskSucCountEnough) {
				return;
			}
			// check whether the result is in the mEsptouchResultList already
			boolean isExist = false;
			for (EsptouchResult esptouchResultInList : mEsptouchResultList) {
				if (esptouchResultInList.getBssid().equals(bssid)) {
					isExist = true;
					break;
				}
			}
			// only add the result who isn't in the mEsptouchResultList
			if (!isExist) {
				final EsptouchResult esptouchResult = new EsptouchResult(isSuc, bssid, inetAddress);
				mEsptouchResultList.add(esptouchResult);
				if (mOnEsptouchResultListener != null) {
					mOnEsptouchResultListener.onResult(esptouchResult);
				}
			}
		}
	}

	private synchronized void __interrupt() {
		if (!mInterrupted) {
			mInterrupted = true;
			mSocketClient.interrupt();
			mSocketServer.interrupt();
			// interrupt the current Thread which is used to wait for udp response
			Thread.currentThread().interrupt();
		}
	}

	public void interrupt() {
		mLogger.debug("interrupt()");

		mCancelled.set(true);
		__interrupt();
	}

	private void __listenAsyn(final int expectDataLen) {
		new Thread() {
			public void run() {
				mLogger.debug("__listenAsyn() start");

				long startTimestamp = System.currentTimeMillis();
				byte[] apSsidAndPassword = ByteUtil.getBytesByString(mApSsid + mApPassword);
				byte expectOneByte = (byte) (apSsidAndPassword.length + 9);
				mLogger.debug("expectOneByte: " + expectOneByte);

				byte receiveOneByte = -1;
				byte[] receiveBytes = null;
				while (mEsptouchResultList.size() < mParameter.getExpectTaskResultCount() && !mInterrupted) {
					receiveBytes = mSocketServer.receiveSpecLenBytes(expectDataLen);
					if (receiveBytes != null) {
						receiveOneByte = receiveBytes[0];
					} else {
						receiveOneByte = -1;
					}
					if (receiveOneByte == expectOneByte) {
						mLogger.debug("receive correct broadcast");

						// change the socket's timeout
						long consume = System.currentTimeMillis() - startTimestamp;
						int timeout = (int) (mParameter.getWaitUdpTotalMillisecond() - consume);
						if (timeout < 0) {
							break;
						} else {
							mSocketServer.setSoTimeout(timeout);
							mLogger.debug("mSocketServer's new timeout is " + timeout + " milliseconds");
							mLogger.debug("receive correct broadcast");

							if (receiveBytes != null) {
								String bssid = ByteUtil.parseBssid(
										receiveBytes,
										mParameter.getEsptouchResultOneLen(),
										mParameter.getEsptouchResultMacLen());
								InetAddress inetAddress = parseInetAddress(
										receiveBytes,
										mParameter.getEsptouchResultOneLen() + mParameter.getEsptouchResultMacLen(),
										mParameter.getEsptouchResultIpLen());
								__putEsptouchResult(true, bssid, inetAddress);
							}
						}
					}
				}
				mSuccessed = mEsptouchResultList.size() >= mParameter.getExpectTaskResultCount();
				__interrupt();
			}
		}.start();
	}

	private static InetAddress parseInetAddress(byte[] inputBytes, int offset, int length) {
		try {
			return InetAddress.getByAddress(new byte[]{inputBytes[offset], inputBytes[offset + 1], inputBytes[offset + 2], inputBytes[offset + 3]});
		} catch (UnknownHostException ex) {
			throw new RuntimeException(ex);
		}
	}

	private boolean __execute(EsptouchGenerator generator) {
		long startTime = System.currentTimeMillis();
		long currentTime = startTime;
		long lastTime = currentTime - mParameter.getTimeoutTotalCodeMillisecond();

		byte[][] gcBytes2 = generator.getGCBytes2();
		byte[][] dcBytes2 = generator.getDCBytes2();

		int index = 0;
		while (!mInterrupted) {
			if (currentTime - lastTime >= mParameter.getTimeoutTotalCodeMillisecond()) {
				// send guide code
				while (!mInterrupted
						&& System.currentTimeMillis() - currentTime < mParameter
						.getTimeoutGuideCodeMillisecond()) {
					mSocketClient.sendData(gcBytes2,
							mParameter.getTargetHostname(),
							mParameter.getTargetPort(),
							mParameter.getIntervalGuideCodeMillisecond());
					// check whether the udp is send enough time
					if (System.currentTimeMillis() - startTime > mParameter.getWaitUdpSendingMillisecond()) {
						break;
					}
				}
				lastTime = currentTime;
			} else {
				mSocketClient.sendData(dcBytes2, index, ONE_DATA_LEN,
						mParameter.getTargetHostname(),
						mParameter.getTargetPort(),
						mParameter.getIntervalDataCodeMillisecond());
				index = (index + ONE_DATA_LEN) % dcBytes2.length;
			}
			currentTime = System.currentTimeMillis();
			// check whether the udp is send enough time
			if (currentTime - startTime > mParameter.getWaitUdpSendingMillisecond()) {
				break;
			}
		}

		return mSuccessed;
	}

	private void __checkTaskValid() {
		if (mNetworkHelperCallback == null)
			throw new IllegalStateException("should have GetLocalInetAddressCallback set.");
		if (mExecuted)
			throw new IllegalStateException("the Esptouch task could be executed only once");
		this.mExecuted = true;
	}

	public boolean isCancelled() {
		return this.mCancelled.get();
	}

	public void executeForResults(int expectTaskResultCount) throws RuntimeException {
		__checkTaskValid();

		mParameter.setExpectTaskResultCount(expectTaskResultCount);

		InetAddress localInetAddress = mNetworkHelperCallback.getLocalInetAddress();
		mLogger.debug("localInetAddress: " + localInetAddress);

		// generator the esptouch byte[][] to be transformed, which will cost
		// some time(maybe a bit much)
		EsptouchGenerator generator = new EsptouchGenerator(mApSsid, mApBssid, mApPassword, localInetAddress, mIsSsidHidden);
		// listen the esptouch result asyn
		__listenAsyn(mParameter.getEsptouchResultTotalLen());
		boolean isSuc = false;
		for (int i = 0; i < mParameter.getTotalRepeatTime(); i++) {
			isSuc = __execute(generator);
			if (isSuc) {
				return;
			}
		}

		if (!mInterrupted) {
			// wait the udp response without sending udp broadcast
			try {
				Thread.sleep(mParameter.getWaitUdpReceivingMillisecond());
			} catch (InterruptedException e) {
				// receive the udp broadcast or the user interrupt the task
				if (this.mSuccessed) {
					return;
				} else {
					this.__interrupt();
					return;
				}
			}
			this.__interrupt();
		}

		return;
	}

	public void setEsptouchListener(EsptouchTask.OnEsptouchResultListener listener) {
		mOnEsptouchResultListener = listener;
	}

	public void setGetLocalInetAddressCallback(EsptouchTask.NetworkHelperCallback callback) {
		mNetworkHelperCallback = callback;
	}

	public void setLogger(EsptouchTask.Logger logger) {
		mLogger = logger;
	}
}
