package com.espressif.iot.esptouch.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.espressif.iot.esptouch.EsptouchAsyncTask;
import com.espressif.iot.esptouch.EsptouchResult;

import java.util.ArrayList;

class EsptouchDemoAsyncTask extends EsptouchAsyncTask {
	private Context mContext;

	/**
	 * {@inheritDoc}
	 */
	public EsptouchDemoAsyncTask(@NonNull Context context, @NonNull String ssid, @Nullable String bssid, @Nullable String passphrase, boolean hidden, int resultsWanted) {
		super(context, ssid, bssid, passphrase, hidden, resultsWanted);

		mContext = context;
	}

	private ViewSwitcher mSwitcher;
	private AlertDialog mDialog;
	private final ArrayList<String> mMessages = new ArrayList<>();
	private final BaseAdapter mAdapter = new BaseAdapter() {
		@Override
		public int getCount() {
			return mMessages.size();
		}

		@Override
		public String getItem(int position) {
			return mMessages.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// it's just a demo...
			@SuppressLint("ViewHolder")
			final View root = LayoutInflater.from(parent.getContext())
					.inflate(android.R.layout.simple_list_item_1, parent, false);
			final TextView text = (TextView) root.findViewById(android.R.id.text1);
			text.setText(getItem(position));
			return root;
		}
	};

	protected void onPreExecute() {
		final ContextThemeWrapper context = new ContextThemeWrapper(mContext, android.support.v7.appcompat.R.style.Theme_AppCompat_Dialog_Alert);
		@SuppressLint("InflateParams")
		final View content = LayoutInflater.from(context)
				.inflate(R.layout.dialog_progress, null, false);
		mSwitcher = (ViewSwitcher) content.findViewById(R.id.switcher);
		final TextView title = (TextView) content.findViewById(android.R.id.title);
		title.setText(R.string.smartconfig);
		final ListView messages = (ListView) content.findViewById(R.id.messages);
		mMessages.add(mContext.getString(R.string.starting));
		messages.setAdapter(mAdapter);
		mDialog = new AlertDialog.Builder(context)
				.setCancelable(false)
				.setView(content)
				.setPositiveButton(R.string.waiting, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						interrupt();
					}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						interrupt();
					}
				})
				.create();
		mDialog.show();
		mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Void ignored) {
		super.onPostExecute(ignored);

		mMessages.add(mContext.getString(R.string.completed));
		onTaskComplete();
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();

		mMessages.add(mContext.getString(R.string.user_canceled));
		onTaskComplete();
	}

	private void onTaskComplete() {
		mAdapter.notifyDataSetChanged();
		final Button button = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
		button.setEnabled(true);
		button.setText(android.R.string.ok);
		mDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
		mSwitcher.showNext();
	}

	@Override
	protected void onProgressUpdate(EsptouchResult... results) {
		for (EsptouchResult result : results) {
			mMessages.add(result.toString());
		}
		if (results.length > 0)
			mAdapter.notifyDataSetChanged();
	}
}
