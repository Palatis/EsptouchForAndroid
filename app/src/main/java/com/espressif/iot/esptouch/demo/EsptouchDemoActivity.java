package com.espressif.iot.esptouch.demo;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class EsptouchDemoActivity extends AppCompatActivity {
	private static final String TAG = "EsptouchDemoActivity";

	private TextView mApSSID;
	private TextInputLayout mApPassphrase;
	private Button mConfirm;
	private SwitchCompat mSwitchIsSsidHidden;
	private EspWifiAdminSimple mWifiAdmin;
	private Spinner mTaskCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.esptouch_demo_activity);
		setSupportActionBar((android.support.v7.widget.Toolbar) findViewById(R.id.toolbar));

		mWifiAdmin = new EspWifiAdminSimple(this);
		mApSSID = (TextView) findViewById(R.id.ap_ssid);
		mApPassphrase = (TextInputLayout) findViewById(R.id.ap_passphrase);
		mSwitchIsSsidHidden = (SwitchCompat) findViewById(R.id.ap_ssid_hidden);
		mTaskCount = (Spinner) findViewById(R.id.spinnerTaskResultCount);
		mConfirm = (Button) findViewById(R.id.confirm);
		mConfirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String apSsid = mWifiAdmin.getWifiConnectedSsid();
				if (apSsid == null)
					apSsid = "";
				String apPassword = mApPassphrase.getEditText().getText().toString();
				String apBssid = mWifiAdmin.getWifiConnectedBssid();
				Boolean isSsidHidden = mSwitchIsSsidHidden.isChecked();

				// FIXME: context leak during screen reorientation
				new EsptouchDemoAsyncTask(EsptouchDemoActivity.this, apSsid, apBssid, apPassword, isSsidHidden, mTaskCount.getSelectedItemPosition())
						.execute();
			}
		});

		int[] spinnerItemsInt = getResources().getIntArray(R.array.taskResultCount);
		int length = spinnerItemsInt.length;
		Integer[] spinnerItemsInteger = new Integer[length];
		for (int i = 0; i < length; i++) {
			spinnerItemsInteger[i] = spinnerItemsInt[i];
		}
		ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerItemsInteger);
		mTaskCount.setAdapter(adapter);
		mTaskCount.setSelection(1);

		final TextView appVersion = (TextView) findViewById(R.id.app_version);
		appVersion.setText(BuildConfig.VERSION_NAME);
	}

	@Override
	protected void onResume() {
		super.onResume();
		final String apSsid = mWifiAdmin.getWifiConnectedSsid();
		mApSSID.setText(getResources().getString(R.string.ssid, (apSsid == null) ? "" : apSsid));
		mConfirm.setEnabled(!TextUtils.isEmpty(apSsid));
	}
}
