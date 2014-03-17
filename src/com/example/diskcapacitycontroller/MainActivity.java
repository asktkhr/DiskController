
package com.example.diskcapacitycontroller;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

    private static final int ITEM_KB = 0;
    private static final int ITEM_MB = 1;
    private static final int ITEM_GB = 2;
    private long KB = 1024;
    private long MB = KB * KB;
    private long GB = MB * KB;

    private TextView titleText;
    private EditText editText;
    private Spinner spinner;

    private long availableByteSize;
    private long totalByteSize;
    private long usageByteSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button generateButton = (Button) findViewById(R.id.generate_button);
        generateButton.setOnClickListener(this);

        Button deleteButton = (Button) findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(this);

        editText = (EditText) findViewById(R.id.expect_size_edit_text);
        titleText = (TextView) findViewById(R.id.title);

        setSpinner();
        getInternalStorageStatus();
        refreshView();
    }

    private void setSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.add("KB");
        adapter.add("MB");
        adapter.add("GB");
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void getInternalStorageStatus() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        availableByteSize = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
        totalByteSize = (long) stat.getBlockCount() * (long) stat.getBlockSize();
        usageByteSize = totalByteSize - availableByteSize;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.generate_button:
                getInternalStorageStatus();
                long expectSize = 0;
                String expectSizeString = editText.getText().toString();
                try {
                    expectSize = Long.valueOf(expectSizeString);

                } catch (NumberFormatException e) {

                }
                int position = spinner.getSelectedItemPosition();
                long expectByteSize = 0;

                switch (position) {
                    case ITEM_KB:
                        expectByteSize = expectSize * KB;
                        break;
                    case ITEM_MB:
                        expectByteSize = expectSize * MB;
                        break;
                    case ITEM_GB:
                        expectByteSize = expectSize * GB;
                        break;
                }

                new SaveDataTask(this, availableByteSize, expectByteSize).execute();
                break;
            case R.id.delete_button:
                if (deleteFile("dummy.txt")) {
                    Toast.makeText(this, "Success!", Toast.LENGTH_LONG).show();
                    refreshView();
                }
                break;
        }

    }

    public void refreshView() {
        getInternalStorageStatus();
        int position = spinner.getSelectedItemPosition();
        String availableCapacity = "";
        switch (position) {
            case ITEM_KB:
                availableCapacity = getString(R.string.available_capacity,
                        (long) Math.ceil(availableByteSize / (float) KB), "KB");
                break;
            case ITEM_MB:
                availableCapacity = getString(R.string.available_capacity,
                        (long) Math.ceil(availableByteSize / (float) MB), "MB");
                break;
            case ITEM_GB:
                availableCapacity = getString(R.string.available_capacity,
                        (long) Math.ceil(availableByteSize / (float) GB), "GB");
                break;
            default:
                availableCapacity = getString(R.string.available_capacity,
                        (long) Math.ceil(availableByteSize / (float) KB), "KB");
        }
        titleText.setText(availableCapacity);

    }

}
