package com.example.diskcontroller;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public class SaveDataTask extends AsyncTask<Long, Void, Boolean> {

	private MainActivity activity;
	private ProgressDialog dialog;
	private long availableByteSize;

	private long KB = 1024;
	private long MB = KB * KB;
	private long GB = MB * KB;

	public SaveDataTask(MainActivity activity, long availableByteSize) {
		this.activity = activity;
		dialog = new ProgressDialog(activity);
		dialog.setMessage("generate dummyfile");
		dialog.setCancelable(false);

		this.availableByteSize = availableByteSize;
	}

	@Override
	protected void onPreExecute() {
		dialog.show();
	}

	@Override
	protected Boolean doInBackground(Long... params) {
		long fileByteSize = availableByteSize - params[0];
		
		Boolean result = false;
		// 保存可能領域が足りない
		if (fileByteSize <= 0) {
			return false;
		}
		long chunkSize = getChunkSize(fileByteSize);
		long divideCount = (long)Math.ceil(fileByteSize / (float)chunkSize);
		String str = generateStr(chunkSize);
		
		for (long i = 0; i < divideCount; i++) {
			long restByteSize = fileByteSize - (i + 1) * chunkSize;
			if(restByteSize <= 0){
				fileByteSize = fileByteSize - i * chunkSize;
				chunkSize = getChunkSize(fileByteSize);
				divideCount = (long)Math.ceil(fileByteSize / (float)chunkSize);
				str = generateStr(chunkSize);
				i = 0;
			}

			result = saveFileToInternalStorage(fileByteSize, str);
			if(!result){
				break;
			}

		}
		return result;
	}

	private String generateStr(long chunkSize){
		char[] chars = new char[(int) chunkSize];
		Arrays.fill(chars, 'x');
		String str = new String(chars);
		return str;
	}
	private long getChunkSize(long fileByteSize){
		long chunkSize = 1 * KB;
		if (fileByteSize < 100 * KB) {
		} else if (100 * KB <= fileByteSize && fileByteSize <= 1 * MB) {
			chunkSize = 100 * KB;
		} else if (1 * MB < fileByteSize) {
			chunkSize = MB;
		}
		return chunkSize;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		dialog.dismiss();
		String message = "";
		if (result) {
			message = "generate success";
		} else {
			message = "generate failed";
		}
		activity.refreshView();
	}

	public boolean saveFileToInternalStorage(long fileByteSize, String str) {
		// ファイル保存開始
		try {
			FileOutputStream fos = activity.openFileOutput("dummy.txt", Activity.MODE_APPEND | Activity.MODE_PRIVATE);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			// 文字列の書き込み
			bw.append(str);
			bw.flush();
			bw.close();
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
