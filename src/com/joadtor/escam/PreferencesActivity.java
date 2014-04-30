package com.joadtor.escam;

import java.io.File;


import com.joadtor.escam.FileActivity.PopupWindow;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.Button;

public class PreferencesActivity extends PreferenceActivity{
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.activity_preferences);
       
    }

}
