package com.espressif.iot.esptouch.task;

public class EsptouchTaskParameter {
	int mIntervalGuideCodeMillisecond;
	int mIntervalDataCodeMillisecond;
	int mTimeoutGuideCodeMillisecond;
	int mTimeoutDataCodeMillisecond;
	int mTotalRepeatTime;
	int mEsptouchResultOneLen;
	int mEsptouchResultMacLen;
	int mEsptouchResultIpLen;
	int mEsptouchResultTotalLen;
	int mPortListening;
	int mTargetPort;
	int mWaitUdpReceivingMillisecond;
	int mWaitUdpSendingMillisecond;
	int mThresholdSucBroadcastCount;
	int mExpectTaskResultCount;
	private static int _datagramCount = 0;

	public EsptouchTaskParameter() {
		mIntervalGuideCodeMillisecond = 10;
		mIntervalDataCodeMillisecond = 10;
		mTimeoutGuideCodeMillisecond = 2000;
		mTimeoutDataCodeMillisecond = 4000;
		mTotalRepeatTime = 1;
		mEsptouchResultOneLen = 1;
		mEsptouchResultMacLen = 6;
		mEsptouchResultIpLen = 4;
		mEsptouchResultTotalLen = 1 + 6 + 4;
		mPortListening = 18266;
		mTargetPort = 7001;
		mWaitUdpReceivingMillisecond = 15000;
		mWaitUdpSendingMillisecond = 45000;
		mThresholdSucBroadcastCount = 1;
		mExpectTaskResultCount = 1;
	}

	// the range of the result should be 1-100
	private static synchronized int __getNextDatagramCount() {
		return 1 + (_datagramCount++) % 100;
	}

	public int getTimeoutTotalCodeMillisecond() {
		return mTimeoutGuideCodeMillisecond + mTimeoutDataCodeMillisecond;
	}

	// target hostname is : 234.1.1.1, 234.2.2.2, 234.3.3.3 to 234.100.100.100
	public String getTargetHostname() {
		int count = __getNextDatagramCount();
		return "234." + count + "." + count + "." + count;
	}

	public int getWaitUdpTotalMillisecond() {
		return mWaitUdpReceivingMillisecond + mWaitUdpSendingMillisecond;
	}

	public void setWaitUdpTotalMillisecond(int waitUdpTotalMillisecond) {
		if (waitUdpTotalMillisecond < mWaitUdpReceivingMillisecond + getTimeoutTotalCodeMillisecond())
			waitUdpTotalMillisecond = mWaitUdpReceivingMillisecond + getTimeoutTotalCodeMillisecond();
		mWaitUdpSendingMillisecond = waitUdpTotalMillisecond - mWaitUdpReceivingMillisecond;
	}
}
