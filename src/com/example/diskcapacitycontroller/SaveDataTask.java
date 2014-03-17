
package com.example.diskcapacitycontroller;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

public class SaveDataTask extends AsyncTask<Long, Integer, Boolean> {

    private MainActivity activity;
    private ProgressDialog dialog;
    private long availableByteSize;
    private long expectByteSize;

    private long KB = 1024;
    private long MB = KB * KB;

    public SaveDataTask(MainActivity activity, long availableByteSize, long expectByteSize) {
        this.activity = activity;
        this.expectByteSize = expectByteSize;
        this.availableByteSize = availableByteSize;

        dialog = new ProgressDialog(activity);
        dialog.setMessage("In processing...");
        dialog.setCancelable(false);
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    @Override
    protected void onPreExecute() {
        dialog.setMax((int) ((availableByteSize - expectByteSize) / KB));
        dialog.setProgress(0);
        dialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        dialog.setProgress(values[0]);
    }

    @Override
    protected Boolean doInBackground(Long... params) {
        long fileByteSize = this.availableByteSize - this.expectByteSize;

        Boolean result = false;
        if (fileByteSize <= 0) {
            return false;
        }
        long chunkSize = getChunkSize(fileByteSize);
        long divideCount = (long) Math.ceil(fileByteSize / (float) chunkSize);
        String str = generateStr(chunkSize);

        for (long i = 0; i < divideCount; i++) {
            long restByteSize = fileByteSize - (i + 1) * chunkSize;
            if (restByteSize <= 0) {
                fileByteSize = fileByteSize - i * chunkSize;
                chunkSize = getChunkSize(fileByteSize);
                divideCount = (long) Math.ceil(fileByteSize / (float) chunkSize);
                str = generateStr(chunkSize);
                i = 0;
            }

            result = saveFileToInternalStorage(fileByteSize, str);
            if (!result) {
                break;
            }
            onProgressUpdate((int) ((i * chunkSize) / KB));

        }
        return result;
    }

    private String generateStr(long chunkSize) {
        char[] chars = new char[(int) chunkSize];
        Arrays.fill(chars, 'x');
        String str = new String(chars);
        return str;
    }

    private long getChunkSize(long fileByteSize) {
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
            message = "Finished!";
        } else {
            message = "Error occured!";
        }
        Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_LONG).show();
        activity.refreshView();
    }

    public boolean saveFileToInternalStorage(long fileByteSize, String str) {
        try {
            FileOutputStream fos = activity.openFileOutput("dummy.txt", Activity.MODE_APPEND
                    | Activity.MODE_PRIVATE);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
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
