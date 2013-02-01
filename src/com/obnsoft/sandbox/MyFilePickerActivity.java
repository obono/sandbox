/*
 * Copyright (C) 2012 OBN-soft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.obnsoft.sandbox;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.obnsoft.app.FilePickerActivity;

public class MyFilePickerActivity extends FilePickerActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.file_picker);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCurrentDirectoryChanged(String path) {
        super.onCurrentDirectoryChanged(path);
        TextView tv = (TextView) findViewById(R.id.text_current_directory);
        tv.setText(getTrimmedCurrentDirectory(path));
        Button btn = (Button) findViewById(R.id.button_back_directory);
        btn.setEnabled(getLastDirectory() != null);
        btn = (Button) findViewById(R.id.button_upper_directory);
        btn.setEnabled(getUpperDirectory() != null);
    }

    public void onBackDirectory(View v) {
        onBackPressed();
    }

    public void onUpperDirectory(View v) {
        goToUpperDirectory();
    }
}
