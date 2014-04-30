package com.joadtor.escam;

import java.io.File;
import java.io.FileInputStream;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
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
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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
import android.widget.TableRow;
import android.widget.Toast;

import com.joadtor.escam.component.BetterPopupWindow;
import com.joadtor.escam.component.Selector;


public class FileActivity extends Activity {
	
	private static final int IMAGE_MAX_SIZE = 2500;
	private static final int PROCESS_OK = 1313;
	
	private static final int FILE_OK = 95;
	private static final int FILE_KO = 59;
	
	private Uri mImgUri;
	private Bitmap mBitmap;
	private int mInterAlg;
	
	static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        
        Bundle bundle = getIntent().getExtras();
        
        if(bundle.getBoolean("source")) {
        	// Camera code
        	     	
        	mImgUri = (Uri) bundle.getParcelable("URI");
        	
			loadBackground(new  File(mImgUri.getPath()));
        	
        }
        else { 
        	// File code
        	        	
        	mImgUri = (Uri) bundle.getParcelable("URI");
        	
        	String FilePath = getRealPathFromURI(mImgUri);
			
			loadBackground(new  File(FilePath));
        	
        }      
        // Get interpolation algorithm
        
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	
        switch(Integer.valueOf(sharedPrefs.getString("prefInterAlg", "5"))){

        case 1:
        	mInterAlg = Imgproc.INTER_AREA;
        	break;
        case 2:
        	mInterAlg = Imgproc.INTER_BITS2;
        	break;
        case 3:
        	mInterAlg = Imgproc.INTER_CUBIC;
        	break;
        case 4:
        	mInterAlg = Imgproc.INTER_LANCZOS4;
        	break;
        case 5:
        	mInterAlg = Imgproc.INTER_LINEAR;
        	break;
        case 6:
        	mInterAlg = Imgproc.INTER_MAX;
        	break;
        case 7:
        	mInterAlg = Imgproc.INTER_NEAREST;
        	break;
        default:
        	mInterAlg = Imgproc.INTER_LINEAR;
        	break;

        }

        // Options
        Button options = (Button) this.findViewById(R.id.options);
        options.setOnClickListener(new View.OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		PopupWindow dw = new PopupWindow(v);
        		dw.setLayoutResource(R.layout.popup_file);
        		dw.showLikeQuickAction(0,10);
        	}
        });
    }


	@SuppressLint("ResourceAsColor")
	private void loadBackground(File imgFile) {
		
		if(imgFile.exists()){

			//Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			mBitmap = decodeFile(imgFile);
			
			if(mBitmap.getHeight() <	mBitmap.getWidth()){
				
				Matrix matrix = new Matrix();
				matrix.postRotate(90);
				
				mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
				
				
			} 

			BitmapDrawable myBitmap = new BitmapDrawable(mBitmap);
			

			
			if(myBitmap != null){
				Selector selector = (Selector) findViewById(R.id.view_select);
				selector.setBackgroundDrawable(myBitmap);
				selector.setEllipseColor(getResources().getColor(R.color.EllipsesColor));
				selector.setLineColor(getResources().getColor(R.color.LinesColor));
			}

		}
		else {
			finish();
		}
	}
    
    // http://stackoverflow.com/questions/477572/strange-out-of-memory-issue-while-loading-an-image-to-a-bitmap-object/823966#823966
	private Bitmap decodeFile(File f){
		Bitmap b = null;

		//Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;

		FileInputStream fis;
		try {
			
			fis = new FileInputStream(f);

			BitmapFactory.decodeStream(fis, null, o);
			fis.close();


			int scale = 1;
			if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
			        if (o.outWidth > o.outHeight) {
			        scale = Math.round((float) o.outHeight / (float) IMAGE_MAX_SIZE);
			        } else {
			        scale = Math.round((float) o.outWidth / (float) IMAGE_MAX_SIZE);
			        }
			}

			//Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			fis = new FileInputStream(f);
			b = BitmapFactory.decodeStream(fis, null, o2);
			//b = BitmapFactory.decodeStream(fis);
			fis.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return b;
	}
	
	
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
			
	        Bitmap bitmap;
	        
	        bitmap = homographyCorrection(mBitmap, calculatePoints());
	        
	        // Load from SharedResources
	        V_esCam gv = (V_esCam)getApplication();
	        gv.setPerspective(bitmap);
	        
	        Intent i = new Intent(getApplicationContext(), PerspectiveActivity.class);
    		startActivityForResult(i, PROCESS_OK);	
	        
	     
			
		} else if (id == R.id.turn_90) {

			rotateImage(90);
		} else if (id == R.id.turn_180) {

			rotateImage(180);
		} else {
		}
    }


	private Bitmap homographyCorrection(Bitmap bmp, int[] points) {
		// Creating a Mat from a Bitmap
		Mat cvmat = new Mat();
		Utils.bitmapToMat(bmp, cvmat);
		
		
		// Perspective correction
		
		double i_height = (double) cvmat.height();
		double i_width = (double) cvmat.width();
		
		Mat p_src = new Mat(4,1,CvType.CV_32FC2);
		Mat p_dest = new Mat(4,1,CvType.CV_32FC2);
		
		double[] src_points = new double[8];
		double[] dest_points = new double[8];
		
		for(int i=0;i < 8;i++)
			src_points[i] = (double) points[i];
		
		dest_points[0] = 0.0;
		dest_points[1] = 0.0;
		dest_points[2] = i_width;
		dest_points[3] = 0.0;
		dest_points[4] = i_width;
		dest_points[5] = i_height;
		dest_points[6] = 0.0;
		dest_points[7] = i_height;

		
		p_src.put(0, 0, src_points);
		p_dest.put(0, 0, dest_points);

		Mat perspective = Imgproc.getPerspectiveTransform(p_src,p_dest);
		
		Size img_size = new Size((double)cvmat.width(),(double)cvmat.height());
		Mat img_result = new Mat(img_size,cvmat.type());
		
		Imgproc.warpPerspective(cvmat, img_result, perspective, img_size, mInterAlg);
		
		// Creating a Bitmap from a Mat     
		Bitmap bdst = Bitmap.createBitmap(img_result.cols(),  img_result.rows(), Bitmap.Config.ARGB_8888); 
		Utils.matToBitmap(img_result, bdst);

		return bdst;
	}
	
	
	public void rotateImage(int degrees){

		Selector selector = (Selector) findViewById(R.id.view_select);

		Matrix matrix = new Matrix();
		matrix.postRotate(degrees);
		mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);

		BitmapDrawable myBitmap = new BitmapDrawable(mBitmap);
		selector.setBackgroundDrawable(myBitmap);

	}
	
	public int[] calculatePoints(){
		Selector selector = (Selector) findViewById(R.id.view_select);
		int[] list = selector.getPoints();
		
		
		float width = (float) mBitmap.getWidth() / (float)selector.getWidth();
		float height = (float) mBitmap.getHeight() / (float)selector.getHeight();
		
		for(int i = 0; i < 4; i++){
			list[i*2] = (int) (list[i*2] * width);
			list[i*2+1] = (int) (list[i*2+1] *height);
		}
		
		return list;
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
    		if(b.getId() == R.id.area) {
    			Toast.makeText(getApplicationContext(), getResources().getString(R.string.inter_area), Toast.LENGTH_SHORT).show();
    			mInterAlg = Imgproc.INTER_AREA;
    		}
    		else if(b.getId() == R.id.bits2) {
    			Toast.makeText(getApplicationContext(), getResources().getString(R.string.inter_bits2), Toast.LENGTH_SHORT).show();
    			mInterAlg = Imgproc.INTER_BITS2;
    		}
    		else if(b.getId() == R.id.cubic) {
    			Toast.makeText(getApplicationContext(), getResources().getString(R.string.inter_cubic), Toast.LENGTH_SHORT).show();
    			mInterAlg = Imgproc.INTER_CUBIC;
    		}
    		else if(b.getId() == R.id.lanczos) {
    			Toast.makeText(getApplicationContext(), getResources().getString(R.string.inter_lanczos4), Toast.LENGTH_SHORT).show();
    			mInterAlg = Imgproc.INTER_LANCZOS4;
    		}
    		else if(b.getId() == R.id.linear) {
    			Toast.makeText(getApplicationContext(), getResources().getString(R.string.inter_linear), Toast.LENGTH_SHORT).show();
    			mInterAlg = Imgproc.INTER_LINEAR;
    		}
    		else if(b.getId() == R.id.max) {
    			Toast.makeText(getApplicationContext(), getResources().getString(R.string.inter_max), Toast.LENGTH_SHORT).show();
    			mInterAlg = Imgproc.INTER_MAX;
    		}
    		else if(b.getId() == R.id.nearestn) {
    			Toast.makeText(getApplicationContext(), getResources().getString(R.string.inter_nearest), Toast.LENGTH_SHORT).show();
    			mInterAlg = Imgproc.INTER_NEAREST;
    		}
    		
    		
    		this.dismiss();
    	}
    }



}
