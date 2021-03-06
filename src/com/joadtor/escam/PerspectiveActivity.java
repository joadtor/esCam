package com.joadtor.escam;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.Toast;

import com.joadtor.escam.component.BetterPopupWindow;
import com.joadtor.escam.component.Selector;
import com.joadtor.escam.tools.ImageFilter;
import com.joadtor.escam.tools.Network;


public class PerspectiveActivity extends Activity {
	

	private static final int PROCESS_OK = 1313;
	
	private static final int FILE_OK = 95;
	private static final int FILE_KO = 59;
	
	private static final int NO_FILTER = 13001;
	private static final int BINARIZE = 13002;
	private static final int BIN_TOZERO = 13003;
	private static final int NIBLACK = 13004;
	private static final int SAUVOLA = 13005;
	private static final int BINARIZE_CUSTOM = 13006;
	private static final int NEURAL = 13007;
	
	private Mat mMat;
	private int mFilter;
	private SeekBar mSBar = null;
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
    		else if(b.getId() == R.id.niblack) {
    			setImageFilter(NIBLACK);
    			mFilter = NIBLACK;
    		}
    		else if(b.getId() == R.id.sauvola) {
    			setImageFilter(SAUVOLA);
    			mFilter = SAUVOLA;
    		}
    		else if(b.getId() == R.id.bicu) {
    			showDialog();
    			mFilter = BINARIZE_CUSTOM;
    		}
    		else if(b.getId() == R.id.neural) {
    			setImageFilter(NEURAL);
    			mFilter = NEURAL;
    		}
    		
    		this.dismiss();
    	}
    	
    }

	
    public void setImageFilter(int filterID, boolean first)
    {
    	Bitmap mBitmap = null;
    	
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
    		mMat = new Mat();
    		Utils.bitmapToMat(mBitmap, mMat);
    	
    		// Greyscale
    		Imgproc.cvtColor(mMat, mMat, Imgproc.COLOR_RGB2GRAY);

    		
    		// Binarizing
    		Imgproc.threshold(mMat, mMat, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
    		
    		// Creating a Bitmap from a Mat     
    		onTrimMemory(TRIM_MEMORY_COMPLETE);
    		mBitmap = Bitmap.createBitmap(mMat.cols(),  mMat.rows(), Bitmap.Config.RGB_565); 
    		Utils.matToBitmap(mMat, mBitmap);
    		
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
    		mMat = new Mat();
    		Utils.bitmapToMat(mBitmap, mMat); 		
    		
    		// Greyscale
    		Imgproc.cvtColor(mMat, mMat, Imgproc.COLOR_RGB2GRAY);
    		
    		// Binarizing
    		Imgproc.threshold(mMat, mMat, 0, 255, Imgproc.THRESH_TOZERO | Imgproc.THRESH_OTSU);
    		
    		// Creating a Bitmap from a Mat     
    		mBitmap = Bitmap.createBitmap(mMat.cols(),  mMat.rows(), Bitmap.Config.RGB_565); 
    		Utils.matToBitmap(mMat, mBitmap);
    		
    		BitmapDrawable myBitmap = new BitmapDrawable(mBitmap);
    		
    		gv.setFilter(mBitmap);
    		
    		Selector selector = (Selector) findViewById(R.id.view_select);
            
    		if(myBitmap != null){
    			selector = (Selector) findViewById(R.id.view_select);
    			selector.setBackgroundDrawable(myBitmap);
    		}
    	
    	}
    	if (filterID == NIBLACK){
    		//if(!first) Toast.makeText(getApplicationContext(), getResources().getString(R.string.tozero_filter), Toast.LENGTH_SHORT).show();
    		
    		// Get from SharedResources
            V_esCam gv = (V_esCam)getApplication();                        
            mBitmap = ImageFilter.setTreshholdNiblack(gv.getPerspective(), 10, 0.2f); 	
	   		
    		BitmapDrawable myBitmap = new BitmapDrawable(mBitmap);
    		
    		gv.setFilter(mBitmap);
    		
    		Selector selector = (Selector) findViewById(R.id.view_select);
            
    		if(myBitmap != null){
    			selector = (Selector) findViewById(R.id.view_select);
    			selector.setBackgroundDrawable(myBitmap);
    		}
    	
    	}
    	if (filterID == SAUVOLA){
    		//if(!first) Toast.makeText(getApplicationContext(), getResources().getString(R.string.tozero_filter), Toast.LENGTH_SHORT).show();
    		
    		// Get from SharedResources
            V_esCam gv = (V_esCam)getApplication();
            mBitmap = ImageFilter.setTreshholdSauvola(gv.getPerspective(), 10, 0.2f,128); 		
    		
    		BitmapDrawable myBitmap = new BitmapDrawable(mBitmap);
    		
    		gv.setFilter(mBitmap);
    		
    		Selector selector = (Selector) findViewById(R.id.view_select);
            
    		if(myBitmap != null){
    			selector = (Selector) findViewById(R.id.view_select);
    			selector.setBackgroundDrawable(myBitmap);
    		}
    	
    	}
    	if (filterID == BINARIZE_CUSTOM){
    		if(!first) Toast.makeText(getApplicationContext(), getResources().getString(R.string.binarize_custom_filter), Toast.LENGTH_SHORT).show();
    		
    		// Get from SharedResources
            V_esCam gv = (V_esCam)getApplication();
            mBitmap = gv.getPerspective();
    		
    		// Creating a Mat from a Bitmap
    		mMat = new Mat();
    		Utils.bitmapToMat(mBitmap, mMat);
    	
    		// Greyscale
    		Imgproc.cvtColor(mMat, mMat, Imgproc.COLOR_RGB2GRAY);
    		
    		// Check Threshold
    		
    		double threshold = mSBar.getProgress();
    		
    		// Binarizing
    		Imgproc.threshold(mMat, mMat, threshold, 255, Imgproc.THRESH_BINARY);
    		
    		// Creating a Bitmap from a Mat     
    		onTrimMemory(TRIM_MEMORY_COMPLETE);
    		mBitmap = Bitmap.createBitmap(mMat.cols(),  mMat.rows(), Bitmap.Config.RGB_565); 
    		Utils.matToBitmap(mMat, mBitmap);
    		
    		BitmapDrawable myBitmap = new BitmapDrawable(mBitmap);
    		
    		gv.setFilter(mBitmap);
    		
    		Selector selector = (Selector) findViewById(R.id.view_select);
            
    		if(myBitmap != null){
    			selector = (Selector) findViewById(R.id.view_select);
    			selector.setBackgroundDrawable(myBitmap);
    		}
    	
    	}
    	if (filterID == NEURAL){
    		//if(!first) Toast.makeText(getApplicationContext(), getResources().getString(R.string.binarize_custom_filter), Toast.LENGTH_SHORT).show();
    		
    		// Get from SharedResources
            V_esCam gv = (V_esCam)getApplication();
            mBitmap = setNeuralNetwork(gv.getPerspective()); 		
    		
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
    
    public void showDialog(){
		final AlertDialog.Builder alert = new AlertDialog.Builder(this); 

		alert.setTitle(getString(R.string.dialog_title)); 
		alert.setMessage(getString(R.string.dialog_msg)); 

		LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_perspective, (ViewGroup)findViewById(R.id.dialog_layout));

		

		alert.setView(layout);
		if (mSBar != null){
			int bar_progress = mSBar.getProgress();
			mSBar = (SeekBar)layout.findViewById(R.id.dialog_seekbar);
			mSBar.setProgress(bar_progress);
		}
		else mSBar = (SeekBar)layout.findViewById(R.id.dialog_seekbar);

		alert.setPositiveButton(getString(R.string.dialog_ok),new DialogInterface.OnClickListener() 
		{ 
	        public void onClick(DialogInterface dialog,int id)  
	        { 
	            setImageFilter(BINARIZE_CUSTOM);
	        } 
	    }); 
		
		alert.setNegativeButton(getString(R.string.dialog_cancel),new DialogInterface.OnClickListener() 
		{ 
	        public void onClick(DialogInterface dialog,int id)  
	        { 
	        } 
	    }); 

	    alert.show(); 
	}
    
    
    
    public Bitmap setNeuralNetwork(Bitmap src){
    	
    	Log.d("Neural Network", "Intialize Neural");
    	
    	int N = 2;
    	
    	InputStream ins = getResources().openRawResource(
    		getResources().getIdentifier("raw/v2", "raw", getPackageName()));
    	
    	BufferedReader br = null;
    	
    	try {
			br = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	Bitmap rslt = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Config.ARGB_4444);
    	
    	Network myNet = new Network(br);
		double[] v = new double[(2*N+1)*(2*N+1)];
		long startTime = System.currentTimeMillis();
		float[][] m = new float[src.getHeight()][src.getWidth()];
		for(int y = 0; y < src.getHeight(); y++) {
			for(int x = 0; x < src.getWidth(); x++) {
				m[y][x] = 1.0f-((float)ImageFilter.getGreyscaleFromColor(src.getPixel(x, y))/255);
			}
			Log.i("NN", String.format("Row %d/%d", y, src.getHeight()));
		}
    	
		
    	for(int y = 0; y < src.getHeight(); y++) {
			for(int x = 0; x < src.getWidth(); x++) {
				int index = 0;
				
				for (int dx = -N; dx <= N; ++dx ) {
					for (int dy = -N; dy <= N; ++dy) {
						int ix = x + dx;
						int iy = y + dy;
						if (ix >= 0 && ix < src.getWidth()
							&& iy >= 0 && iy < src.getHeight())
						 v[index] = m[iy][ix];
						else
							v[index] = 0.0;
						
						index++;
					}
					
				}
				// Computing the NN output
				double []out = myNet.computeNetwork(v);
				int value = (int)((1.0 - out[0])*255);
				/*if(value < 128 ) {
					rslt.setPixel(x, y, Color.BLACK);
					
				}
				else {
					rslt.setPixel(x, y, Color.WHITE);
					
				}*/
				rslt.setPixel(x, y, Color.rgb(value, value,value));
				//int alpha = Color.alpha(value);
				
				
				
			}
			Log.i("NN", String.format("Row 2 %d/%d", y, src.getHeight()));
			
    	}
    	long stopTime = System.currentTimeMillis();
    	long elapsedTime = stopTime - startTime;
    	Log.i("NN", String.format("%d", elapsedTime)); 
		
    	// String str = readTextFile(ins);
    	return rslt;
    }
}


