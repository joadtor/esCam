package com.joadtor.escam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.joadtor.escam.component.BetterPopupWindow;
import com.joadtor.escam.component.Selector;


public class SaveActivity extends Activity {
	
	private static final int FILE_OK = 95;
	private static final int FILE_KO = 59;
	
	private File mPath;
	private String mFileName;
	private int mFormat;
	
	static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_save);

    	V_esCam gv = (V_esCam)getApplication();
    	
    	((ImageView)findViewById(R.id.preview)).setImageBitmap(gv.getFilter());
    	
    	// Get format
    	
    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    	mFormat = Integer.valueOf(sharedPrefs.getString("prefFormat", "0"));

    	// Preparing the file path
    	
    	String folderName = getString(R.string.app_name) + "/images";

    	mPath = new File(Environment.getExternalStorageDirectory(), folderName);

    	if (!mPath.exists() && !mPath.mkdirs())
    		return;
    	
    	Calendar now = Calendar.getInstance();
    	
    	if(mFormat == 1){ // if mFormat == 1 PNG
    		mFileName = String.format("%d%02d%02d_%02d%02d%02d.png", now.get(Calendar.YEAR), now.get(Calendar.MONTH)+1, now.get(Calendar.DATE), now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
    	}
    	else { // else jpg
    		mFileName = String.format("%d%02d%02d_%02d%02d%02d.jpg", now.get(Calendar.YEAR), now.get(Calendar.MONTH)+1, now.get(Calendar.DATE), now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
    	}

    	((TextView)findViewById(R.id.path_text)).setText(mPath.getPath() + "/" + mFileName);

    }


	@SuppressLint("ResourceAsColor")
	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_save, menu);
        return true;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
    
    // from http://stackoverflow.com/questions/3401579/get-filename-and-path-from-uri-from-mediastore
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
    
    @SuppressLint("CutPasteId")
	public void onClickMainMenu (View v)
    {
    	int id = v.getId ();
    	
    	if (id == R.id.save_button) {
    		V_esCam gv = (V_esCam)getApplication();
    		saveBitmap(gv.getFilter());
    		
    		Intent i = new Intent();
    	    setResult(FILE_OK, i);
    	    finish();
    	}
    }

    public void saveBitmap(Bitmap image) {


    	FileOutputStream saveFile;
    	try {
    		saveFile = new FileOutputStream(new File(mPath, mFileName));

    		if(mFormat == 1){
    			image.compress(Bitmap.CompressFormat.PNG,100, saveFile);
    		}
    		else {
    			image.compress(Bitmap.CompressFormat.JPEG,100, saveFile);
    		}

    		saveFile.flush();
    		saveFile.close();
    	} catch (Exception e) {
    		Intent i = new Intent();
    	    setResult(FILE_KO, i);
    	    finish();
    	}


    	// MediaStore.Images.Media.insertImage( ... )
    }
}


