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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.joadtor.escam.R;
import com.joadtor.escam.component.Selector;


public class FileActivity extends Activity {
	
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
        setContentView(R.layout.activity_file);
        
        Bundle bundle = getIntent().getExtras();
        
        if(bundle.getBoolean("source")) {
        	// Camera code
        	
        	((TextView)findViewById(R.id.title_text)).setText("Camera");
        	
        	mImgUri = (Uri) bundle.getParcelable("URI");
        	
			loadBackground(new  File(mImgUri.getPath()));
        	
        	//finishActivity(CAMERA_REQUEST);
        }
        else { 
        	// File code
        	
        	((TextView)findViewById(R.id.title_text)).setText("File");
        	
        	mImgUri = (Uri) bundle.getParcelable("URI");
        	
        	String FilePath = getRealPathFromURI(mImgUri);
			
			loadBackground(new  File(FilePath));
        	
			//finishActivity(FILE_REQUEST);
        }        
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
				scale = (int)Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / 
						(double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
			}

			//Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			fis = new FileInputStream(f);
			b = BitmapFactory.decodeStream(fis, null, o2);
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
	        
	        
	        BitmapDrawable myBitmap = new BitmapDrawable(bitmap);

			if(myBitmap != null){
				selector = (Selector) findViewById(R.id.view_select);
				selector.setBackgroundDrawable(myBitmap);
				selector.setEllipseColor(getResources().getColor(R.color.EllipsesColor));
				selector.setLineColor(getResources().getColor(R.color.LinesColor));
			}
			
			// Hide edit tools
			selector.disableEdit();
			LinearLayout options_bar = (LinearLayout) findViewById(R.id.options_bar);
	        options_bar.setVisibility(View.GONE);
			
		} else if (id == R.id.turn_90) {
			//selector.rotateBackground(90);
			rotateImage(90);
			if(mTurned) mTurned=false;
			else mTurned = true;
		} else if (id == R.id.turn_180) {
			//selector.rotateBackground(180);
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
		
		Imgproc.warpPerspective(cvmat, img_result, perspective, img_size);
		
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


}
