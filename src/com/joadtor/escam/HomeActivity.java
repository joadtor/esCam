package com.joadtor.escam;

import java.io.File;

import com.joadtor.escam.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class HomeActivity extends Activity {
	
	private static final int FILE_REQUEST = 1;
	private static final int CAMERA_REQUEST = 1337;
	private static final int PROCESS_OK = 1313;
	
	private static final int FILE_OK = 95;
	private Uri mImgUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        String folderName = getString(R.string.app_name);

		File path = new File(Environment.getExternalStorageDirectory(), folderName);

		if (!path.exists() && !path.mkdirs())
			return;
        
		mImgUri = Uri.fromFile(new File(path, "pic13.tmp"));
		
		// Set default preferences (Only first time)
		
		PreferenceManager.setDefaultValues(this, R.layout.activity_preferences, false); 
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.menu_about) {
			startActivity (new Intent(getApplicationContext(), AboutActivity.class));
			return true;
		} if (itemId == R.id.menu_settings) {
			startActivity (new Intent(getApplicationContext(), PreferencesActivity.class));
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
    }
    
    public void onClickMainMenu (View v)
    {
    	int id = v.getId ();

    	Intent intent;
    	if (id == R.id.cameraButton) {
			intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			Uri backup = mImgUri;
			intent.putExtra( MediaStore.EXTRA_OUTPUT, backup );
			startActivityForResult(intent, CAMERA_REQUEST);
		} else if (id == R.id.fileButton) {
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			startActivityForResult(intent,FILE_REQUEST);
		} else {
		}
    }
    
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	if(resultCode == RESULT_OK && requestCode == CAMERA_REQUEST){ 	// If "source" is true set camera on
    		
    		Intent i = new Intent(getApplicationContext(), FileActivity.class);
    		i.putExtra("source", true);
    		i.putExtra("URI",mImgUri);
    		startActivityForResult(i, PROCESS_OK);				
    	}
    	
    	if(resultCode == RESULT_OK && requestCode == FILE_REQUEST){ 	// else

    		Uri selectedImage = data.getData();
    		
    		Intent i = new Intent(getApplicationContext(), FileActivity.class);
    		i.putExtra("source", false);
    		i.putExtra("URI", selectedImage);
    		startActivityForResult(i, PROCESS_OK);				
    	}
    	if(resultCode == FILE_OK && requestCode == PROCESS_OK){ 		
    		Toast.makeText(getApplicationContext(), getResources().getString(R.string.image_saved), Toast.LENGTH_LONG).show();
    	}
    }
}
