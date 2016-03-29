package com.espressif.iot.esptouch.demo_activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.espressif.iot.esptouch.EsptouchResult;
import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot_esptouch_demo.BuildConfig;
import com.espressif.iot_esptouch_demo.R;

import java.net.InetAddress;
import java.util.List;

public class EsptouchDemoActivity extends Activity implements OnClickListener {

	private static final String TAG = "EsptouchDemoActivity";

	private TextView mTvApSsid;

	private EditText mEdtApPassword;

	private Button mBtnConfirm;

	private Switch mSwitchIsSsidHidden;

	private EspWifiAdminSimple mWifiAdmin;

	private Spinner mSpinnerTaskCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.esptouch_demo_activity);

		mWifiAdmin = new EspWifiAdminSimple(this);
		mTvApSsid = (TextView) findViewById(R.id.tvApSssidConnected);
		mEdtApPassword = (EditText) findViewById(R.id.edtApPassword);
		mBtnConfirm = (Button) findViewById(R.id.btnConfirm);
		mSwitchIsSsidHidden = (Switch) findViewById(R.id.switchIsSsidHidden);
		mBtnConfirm.setOnClickListener(this);
		initSpinner();
	}

	private void initSpinner() {
		mSpinnerTaskCount = (Spinner) findViewById(R.id.spinnerTaskResultCount);
		int[] spinnerItemsInt = getResources().getIntArray(R.array.taskResultCount);
		int length = spinnerItemsInt.length;
		Integer[] spinnerItemsInteger = new Integer[length];
		for (int i = 0; i < length; i++) {
			spinnerItemsInteger[i] = spinnerItemsInt[i];
		}
		ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, spinnerItemsInteger);
		mSpinnerTaskCount.setAdapter(adapter);
		mSpinnerTaskCount.setSelection(1);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// display the connected ap's ssid
		String apSsid = mWifiAdmin.getWifiConnectedSsid();
		if (apSsid != null) {
			mTvApSsid.setText(apSsid);
		} else {
			mTvApSsid.setText("");
		}
		// check whether the wifi is connected
		boolean isApSsidEmpty = TextUtils.isEmpty(apSsid);
		mBtnConfirm.setEnabled(!isApSsidEmpty);
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnConfirm) {
			String apSsid = mTvApSsid.getText().toString();
			String apPassword = mEdtApPassword.getText().toString();
			String apBssid = mWifiAdmin.getWifiConnectedBssid();
			Boolean isSsidHidden = mSwitchIsSsidHidden.isChecked();
			String isSsidHiddenStr = "NO";
			String taskResultCountStr = Integer.toString(mSpinnerTaskCount.getSelectedItemPosition());
			if (isSsidHidden) {
				isSsidHiddenStr = "YES";
			}
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "mBtnConfirm is clicked, mEdtApSsid = " + apSsid + ", " + " mEdtApPassword = " + apPassword);
			}
			new EsptouchAsyncTask().execute(apSsid, apBssid, apPassword, isSsidHiddenStr, taskResultCountStr);
		}
	}

	private class EsptouchAsyncTask extends AsyncTask<String, EsptouchResult, List<EsptouchResult>>
			implements EsptouchTask.OnEsptouchResultListener, EsptouchTask.NetworkHelperCallback, EsptouchTask.Logger {
		private ProgressDialog mProgressDialog;
		private EsptouchTask mEsptouchTask;
		// without the lock, if the user tap confirm and cancel quickly enough,
		// the bug will arise. the reason is follows:
		// 0. task is starting created, but not finished
		// 1. the task is cancel for the task hasn't been created, it do nothing
		// 2. task is created
		// 3. Oops, the task should be cancelled, but it is running
		private final Object mLock = new Object();
		private WifiManager.MulticastLock mMulticastLock;

		@Override
		protected void onPreExecute() {
			mProgressDialog = new ProgressDialog(EsptouchDemoActivity.this);
			mProgressDialog.setMessage("Esptouch is configuring, please wait for a moment...");
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					synchronized (mLock) {
						if (BuildConfig.DEBUG) {
							Log.i(TAG, "progress dialog is canceled");
						}
						if (mEsptouchTask != null) {
							mEsptouchTask.interrupt();
						}
					}
				}
			});
			mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Waiting...", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			mProgressDialog.show();
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

			WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			mMulticastLock = manager.createMulticastLock("test wifi");
			mMulticastLock.acquire();
		}

		@Override
		protected List<EsptouchResult> doInBackground(String... params) {
			int taskResultCount = -1;
			synchronized (mLock) {
				String apSsid = params[0];
				String apBssid = params[1];
				String apPassword = params[2];
				String isSsidHiddenStr = params[3];
				String taskResultCountStr = params[4];
				boolean isSsidHidden = isSsidHiddenStr.equals("YES");
				taskResultCount = Integer.parseInt(taskResultCountStr);
				mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, isSsidHidden);
				mEsptouchTask.setEsptouchListener(this);
				mEsptouchTask.setGetLocalInetAddressCallback(this);
				mEsptouchTask.setLogger(this);
			}
			return mEsptouchTask.executeForResults(taskResultCount);
		}

		@Override
		protected void onPostExecute(List<EsptouchResult> result) {
			mMulticastLock.release();
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("Confirm");
			EsptouchResult firstResult = result.get(0);
			// check whether the task is cancelled and no results received
			if (!firstResult.isCancelled()) {
				int count = 0;
				// max results to be displayed, if it is more than maxDisplayCount,
				// just show the count of redundant ones
				final int maxDisplayCount = 5;
				// the task received some results including cancelled while
				// executing before receiving enough results
				if (firstResult.isSuccess()) {
					StringBuilder sb = new StringBuilder();
					for (EsptouchResult resultInList : result) {
						sb.append("Esptouch success, bssid = ")
								.append(resultInList.getBssid())
								.append(",InetAddress = ")
								.append(resultInList.getInetAddress().getHostAddress())
								.append("\n");
						count++;
						if (count >= maxDisplayCount) {
							break;
						}
					}
					if (count < result.size()) {
						sb.append("\nthere's ")
								.append(result.size() - count)
								.append(" more result(s) without showing\n");
					}
					mProgressDialog.setMessage(sb.toString());
				} else {
					mProgressDialog.setMessage("Esptouch fail");
				}
			}
		}

		@Override
		protected void onProgressUpdate(EsptouchResult... values) {
			final EsptouchResult result = values[0];
			Toast.makeText(getApplicationContext(), result.getBssid() + " is connected to the wifi", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onResult(EsptouchResult result) {
			publishProgress(result);
		}

		@Override
		public InetAddress getLocalInetAddress() {
			return EspNetUtil.getLocalInetAddress(getApplicationContext());
		}

		@Override
		public InetAddress parseInetAddress(byte[] bytes, int offset, int length) {
			return EspNetUtil.parseInetAddress(bytes, offset, length);
		}

		@Override
		public void info(String msg) {
			Log.i("EsptouchTask", msg);
		}

		@Override
		public void debug(String msg) {
			Log.d("EsptouchTask", msg);
		}

		@Override
		public void warning(String msg) {
			Log.w("EsptouchTask", msg);
		}
	}
}
