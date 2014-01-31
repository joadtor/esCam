package com.joadtor.escam;

import org.opencv.android.OpenCVLoader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
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
	
	private Uri mImgUri;
	private Bitmap mBitmap;
	private Boolean mTurned = false;
	
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
        
        
        // Get from SharedResources
        V_esCam gv = (V_esCam)getApplication();
        mBitmap = gv.getPerspective();
        BitmapDrawable myBitmap = new BitmapDrawable(mBitmap);

        
        Selector selector = (Selector) findViewById(R.id.view_select);
        
		if(myBitmap != null){
			selector = (Selector) findViewById(R.id.view_select);
			selector.setBackgroundDrawable(myBitmap);
			selector.setEllipseColor(getResources().getColor(R.color.EllipsesColor));
			selector.setLineColor(getResources().getColor(R.color.LinesColor));
		}
         
    	

		selector.disableEdit();
		LinearLayout options_bar = (LinearLayout) findViewById(R.id.options_bar);
        options_bar.setVisibility(View.GONE);
        
        
        // Options
        Button options = (Button) this.findViewById(R.id.options);
        options.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PopupWindow dw = new PopupWindow(v);
				dw.setLayoutResource(R.layout.popup_perspective);
				dw.showLikeQuickAction(0,20);
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

    }
    
    @SuppressLint("CutPasteId")
	public void onClickMainMenu (View v)
    {
    	int id = v.getId ();
    	int level;
    	
    	Selector selector = (Selector) findViewById(R.id.view_select);
    	
    	Button button_i;
    	Button button_d;
    	
    	if (id == R.id.inc_size) {
			level = selector.increaseStrokeSize();
			button_i = (Button) findViewById(R.id.inc_size);
			button_d = (Button) findViewById(R.id.dec_size);
			if(level == 7)
    			button_i.setEnabled(false);
    		else button_d.setEnabled(true);
		} else if (id == R.id.dec_size) {
			level = selector.decreaseStrokeSize();
			button_i = (Button) findViewById(R.id.inc_size);
			button_d = (Button) findViewById(R.id.dec_size);
			if(level == 1)
    			button_d.setEnabled(false);
    		else button_i.setEnabled(true);
		} else if (id == R.id.ok_button) {
			
	        
			
		} else if (id == R.id.turn_90) {
			//selector.rotateBackground(90);
			//rotateImage(90);
			if(mTurned) mTurned=false;
			else mTurned = true;
		} else if (id == R.id.turn_180) {
			//selector.rotateBackground(180);
			//rotateImage(180);
		} else {
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
    		if(b.getId() == R.id.one) 
    			setImageFilter("primer");
    		else if(b.getId() == R.id.two) 
    			setImageFilter("segundo");
    		else if(b.getId() == R.id.three) 
    			setImageFilter("tercero");
    		else if(b.getId() == R.id.four) 
    			setImageFilter("cuarto");
    		else if(b.getId() == R.id.five) 
    			setImageFilter("quinto");
    		
    		this.dismiss();
    	}
    }

	
    public void setImageFilter(String text)
    {
    	String text_out = "Se ha pulsado el " + text + " botón.";
    	Toast.makeText(getApplicationContext(), text_out, Toast.LENGTH_SHORT).show();
    }

}


