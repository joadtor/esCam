package com.joadtor.escam;

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
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.joadtor.escam.component.BetterPopupWindow;
import com.joadtor.escam.component.Selector;


public class PerspectiveActivity extends Activity {
	
	private static final int IMAGE_MAX_SIZE = 1920;
	private static final int PROCESS_OK = 1313;
	
	private static final int FILE_OK = 95;
	private static final int FILE_KO = 59;
	
	private static final int NO_FILTER = 13001;
	private static final int BINARIZE = 13002;
	private static final int BIN_TOZERO = 13003;
	
	private Uri mImgUri;
	private Bitmap mBitmap;
	private Boolean mTurned = false;
	private int mFilter;
	
	static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perspective);
        
        
        // Get filter
    	
    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    	mFilter = Integer.valueOf(sharedPrefs.getString("prefFilter", "1")) + 13000;
    	
    	setImageFilter(mFilter,true);
    	

		// Hide selection tools
    	
    	Selector selector = (Selector) findViewById(R.id.view_select);
		selector.disableEdit();
        
        // Options
        Button options = (Button) this.findViewById(R.id.options);
        options.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PopupWindow mPW = new PopupWindow(v);
				mPW.setLayoutResource(R.layout.popup_perspective);
				mPW.showLikeQuickAction(0,10);
			}
		});
    
    }



	@SuppressLint("ResourceAsColor")
	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_file, menu);
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
    	
    	if(resultCode == FILE_OK && requestCode == PROCESS_OK){ 		
    		Intent i = new Intent();
    	    setResult(FILE_OK, i);
    	    finish();		
    	}
    	else if(resultCode == FILE_KO && requestCode == PROCESS_OK){ 		
    		Intent i = new Intent();
    	    setResult(FILE_KO, i);
    	    finish();			
    	}

    }
    
    @SuppressLint("CutPasteId")
	public void onClickMainMenu (View v)
    {
    	int id = v.getId ();

    	
    	if (id == R.id.ok_button) {
			
			Intent i = new Intent(getApplicationContext(), SaveActivity.class);
    		startActivityForResult(i, PROCESS_OK);
			
		} 
    }

    public class PopupWindow extends BetterPopupWindow implements OnClickListener {


    	public PopupWindow(View anchor) {
    		super(anchor);
    	}

    	public void setLayoutResource(int resource){
    		// inflate layout
    		LayoutInflater inflater =
    				(LayoutInflater) this.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    		
    		ViewGroup root = (ViewGroup) inflater.inflate(resource, null);

    		// setup button events
    		for(int i = 0, icount = root.getChildCount() ; i < icount ; i++) {
    			View v = root.getChildAt(i);

    			if(v instanceof TableRow) {
    				TableRow row = (TableRow) v;

    				for(int j = 0, jcount = row.getChildCount() ; j < jcount ; j++) {
    					View item = row.getChildAt(j);
    					if(item instanceof Button) {
    						Button b = (Button) item;
    						b.setOnClickListener(this);
    					}
    				}
    			}
    		}

    		// set the inflated view as what we want to display
    		this.setContentView(root);
    	}
    	@Override
    	public void onClick(View v) {
    		// we'll just display a simple toast on a button click
    		Button b = (Button) v;
    		if(b.getId() == R.id.wout) {
    			setImageFilter(NO_FILTER);
    			mFilter = NO_FILTER;
    		}
    		else if(b.getId() == R.id.bina) {
    			setImageFilter(BINARIZE);
    			mFilter = BINARIZE;
    		}
    		else if(b.getId() == R.id.bima) {
    			setImageFilter(BIN_TOZERO);
    			mFilter = BIN_TOZERO;
    		}
    		
    		this.dismiss();
    	}
    	
    }

	
    public void setImageFilter(int filterID, boolean first)
    {
    	if (filterID == NO_FILTER){
    		if(!first) Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_filter), Toast.LENGTH_SHORT).show();
        	    		
    		// Get from SharedResources
            V_esCam gv = (V_esCam)getApplication();
            mBitmap = gv.getPerspective();
            BitmapDrawable myBitmap = new BitmapDrawable(mBitmap);
            
    		gv.setFilter(mBitmap);
       
            Selector selector = (Selector) findViewById(R.id.view_select);
            
    		if(myBitmap != null){
    			selector = (Selector) findViewById(R.id.view_select);
    			selector.setBackgroundDrawable(myBitmap);
    		}
    	}
    	if (filterID == BINARIZE){
    		if(!first) Toast.makeText(getApplicationContext(), getResources().getString(R.string.binarize_filter), Toast.LENGTH_SHORT).show();
    		
    		// Get from SharedResources
            V_esCam gv = (V_esCam)getApplication();
            mBitmap = gv.getPerspective();
    		
    		// Creating a Mat from a Bitmap
    		Mat img_src = new Mat();
    		Utils.bitmapToMat(mBitmap, img_src);
    		
    		// Greyscale
    		Imgproc.cvtColor(img_src, img_src, Imgproc.COLOR_RGB2GRAY);

    		
    		// Binarizing
    		Imgproc.threshold(img_src, img_src, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
    		
    		// Creating a Bitmap from a Mat     
    		mBitmap = Bitmap.createBitmap(img_src.cols(),  img_src.rows(), Bitmap.Config.ARGB_8888); 
    		Utils.matToBitmap(img_src, mBitmap);
    		
    		BitmapDrawable myBitmap = new BitmapDrawable(mBitmap);
    		
    		gv.setFilter(mBitmap);
    		
    		Selector selector = (Selector) findViewById(R.id.view_select);
            
    		if(myBitmap != null){
    			selector = (Selector) findViewById(R.id.view_select);
    			selector.setBackgroundDrawable(myBitmap);
    		}
    		
    		
    	}
    	if (filterID == BIN_TOZERO){
    		if(!first) Toast.makeText(getApplicationContext(), getResources().getString(R.string.tozero_filter), Toast.LENGTH_SHORT).show();
    		
    		// Get from SharedResources
            V_esCam gv = (V_esCam)getApplication();
            mBitmap = gv.getPerspective();
    		
    		// Creating a Mat from a Bitmap
    		Mat img_src = new Mat();
    		Utils.bitmapToMat(mBitmap, img_src);
    		    		
    		// Greyscale
    		Imgproc.cvtColor(img_src, img_src, Imgproc.COLOR_RGB2GRAY);
    		
    		// Binarizing
    		Imgproc.threshold(img_src, img_src, 0, 255, Imgproc.THRESH_TOZERO | Imgproc.THRESH_OTSU);
    		
    		// Creating a Bitmap from a Mat     
    		mBitmap = Bitmap.createBitmap(img_src.cols(),  img_src.rows(), Bitmap.Config.ARGB_8888); 
    		Utils.matToBitmap(img_src, mBitmap);
    		
    		BitmapDrawable myBitmap = new BitmapDrawable(mBitmap);
    		
    		gv.setFilter(mBitmap);
    		
    		Selector selector = (Selector) findViewById(R.id.view_select);
            
    		if(myBitmap != null){
    			selector = (Selector) findViewById(R.id.view_select);
    			selector.setBackgroundDrawable(myBitmap);
    		}
    		
    		
    	}

    	
    }

    public void setImageFilter(int filterID) {
    	 setImageFilter(filterID, false);
    }
}


