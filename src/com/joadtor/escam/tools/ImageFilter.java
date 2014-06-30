package com.joadtor.escam.tools;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;

// https://github.com/pakozm/april-ann/blob/master/packages/imaging/binarization_filter/c_src/binarization.cc
public class ImageFilter {
	public static Bitmap setTreshholdNiblackComplex(Bitmap src, int windowRadius, float k, float minThreshold, float maxThreshold) {
		
		Bitmap rslt = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Config.ARGB_4444);
		
		float[][] S = new float[src.getWidth()][src.getHeight()];
		float[][] S2 = new float[src.getWidth()][src.getHeight()];

		// each pixel in the "sum" image contains the sum of the pixels to the left
		// and above it, same for sumOfSquares

		// init to the upper-left corner pixel
		S[0][0] = (float) getGreyscaleFromColor(src.getPixel(0, 0));
		S2[0][0] = S[0][0]*S[0][0];
		

		// First row
		for (int x = 1; x < src.getWidth(); x++) {
			float current_pixel = (float) getGreyscaleFromColor(src.getPixel(x, 0));
			S[x][0] = S[x-1][0] + current_pixel;
			S2[x][0] = S[x-1][0] + current_pixel * current_pixel;
		}

		// Rest of rows
		for (int y = 1; y < src.getHeight(); y++)
		    for (int x = 0; x < src.getWidth(); x++) {
		      float s = (float) S[x][y-1];
		      float s2 = (float) S2[x][y-1];
		      if ( x > 0 ) {
		        s += S[x-1][y] - S[x-1][y-1];
		        s2 += S2[x-1][y] - S2[x-1][y-1];;
		      }
		      
		      float current_pixel = (float) getGreyscaleFromColor(src.getPixel(x, y));
		      S[x][y] = s + current_pixel;
		      S2[x][y] = s2 + current_pixel * current_pixel; // In original code is s, supposed an error
		    }

		// Apply Niblack filter using sum and sumOfSquares for fast mean/std.dev. computation
		int windowSize = 2*windowRadius+1;
		int totalWindowPixels = windowSize*windowSize;
		for (int y = 0; y < src.getHeight(); y++)
		    for (int x = 0; x < src.getWidth(); x++) {
		      float val = (float) getGreyscaleFromColor(src.getPixel(x, y));

		      if ( val < minThreshold ) {
		    	  rslt.setPixel(x, y, Color.BLACK);
		      }
		      else if (val > maxThreshold) {
		    	  rslt.setPixel(x, y, Color.WHITE);
		      }
		      else {
		        int windowUpper, windowLower, windowLeft, windowRight;
		        
		        windowUpper = ( y - windowRadius < 0 ? 0 : y - windowRadius);
		        windowLeft = ( x - windowRadius < 0 ? 0 : x - windowRadius);
		        windowLower = ( y + windowRadius > src.getHeight() - 1 ? src.getHeight()-1 : y + windowRadius);
		        windowRight = ( x + windowRadius > src.getWidth() - 1 ? src.getWidth() -1 : x + windowRadius);

		        // assume pixels outside the image are white (value = 1)
		        int windowPixels = (windowRight-windowLeft+1) * (windowLower-windowUpper+1);
		        float s = totalWindowPixels - windowPixels;
		        float s2 = s; // 1 squared is 1, too

		        s += S[windowRight][windowLower];
		        s2 += S2[windowRight][windowLower];

		        if (windowLeft > 0) {
		          s -= S[windowLeft-1][windowLower];
		          s2 -= S2[windowLeft-1][windowLower];
		        }

		        if (windowUpper > 0) {
		          s -= S[windowRight][windowUpper-1];
		          s2 -= S2[windowRight][windowUpper-1];
		        }
		        
		        if (windowLeft > 0 && windowUpper > 0) {
		          s += S[windowLeft-1][windowUpper-1];
		          s2 += S2[windowLeft-1][windowUpper-1];
		        }

		        float mean = s/totalWindowPixels;
		        float std_dev = (float) Math.sqrt(s2/totalWindowPixels - mean*mean);
		        float threshold = mean + k*std_dev;

		        if( val < threshold ) rslt.setPixel(x, y, Color.BLACK);
	            else rslt.setPixel(x, y, Color.WHITE);
		      }
		    }
		
		return rslt;
	}
	public static Bitmap setTreshholdNiblack(Bitmap src, int windowRadius, float k) 
	{
		
		Bitmap rslt = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Config.ARGB_4444);
		
		// Image Integral Matrix
		float[][] M = new float[src.getWidth()][src.getHeight()];
		
		// Square Image Integral
		float[][] M2 = new float[src.getWidth()][src.getHeight()];
			
		int env = windowRadius;
		for(int y = 0; y < src.getHeight(); y++)
			for(int x = 0; x < src.getWidth(); x++) {
				M[x][y] = (float) getGreyscaleFromColor(src.getPixel(x, y));
				M2[x][y] = (float) M[x][y]*M[x][y];
				
				if(x>0 && y>0){
					M[x][y] += M[x-1][y] + M[x][y-1] - M[x-1][y-1];
					M2[x][y] += M2[x-1][y]+ M2[x][y-1] - M2[x-1][y-1];
				}
				else if(x>0 && !(y>0)){
					M[x][y] += M[x-1][y];
					M2[x][y] += M2[x-1][y];
				}
				else if(!(x>0) && y>0){
					M[x][y]+=M[x][y-1];
					M2[x][y]+=M2[x][y-1];
				}
			}
		
		for(int y = 0; y < src.getHeight(); y++)
			for(int x = 0; x < src.getWidth(); x++) {
				int limInf, limSup, limRight, limLeft;
				int area = 1;
				
				// We take the limits of the environment
				if(x-env < 0) {
					limLeft = 0;
				}
				else limLeft = x-env;
				
				if(x+env >= src.getWidth()) {
					limRight = src.getWidth()-1;
				}
				else limRight = x+env;
				
				if(y-env < 0) {
					limSup = 0;
				}
				else limSup = y-env;
				
				if(y+env >= src.getHeight()) {
					limInf = src.getHeight()-1;
				}
				else limInf = y+env;
				
				area = (limInf-limSup+1)*(limRight-limLeft+1);
				
				// Calculate the mean
				float mean = (float) (M[limLeft][limSup]+ M[limRight][limInf] - M[limLeft][limInf] -M[limRight][limSup])/area;
				float mean2 = (float) (M2[limLeft][limSup]+ M2[limRight][limInf] - M2[limLeft][limInf] -M2[limRight][limSup])/area;
				
				//Compute the Standar Deviacion square(Mean^2-mean2)
	            float sd = (float) Math.sqrt(mean2-mean*mean);
				
	            //Apply the Threshold T=mean-0.2sd
	            float T = (float) (mean - k*sd);
	            if( getGreyscaleFromColor(src.getPixel(x, y)) < T ) rslt.setPixel(x, y, Color.BLACK);
	            else rslt.setPixel(x, y, Color.WHITE);
			}
		
		
		return rslt;
	}
	public static Bitmap setTreshholdSauvola(Bitmap src, int windowRadius, float k, float r){
		
		
		Bitmap rslt = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Config.ARGB_4444);
		
		// Image Integral Matrix
		float[][] M = new float[src.getWidth()][src.getHeight()];
		
		// Square Image Integral
		float[][] M2 = new float[src.getWidth()][src.getHeight()];
	    

	    int env=windowRadius;
	    for(int y = 0; y < src.getHeight(); y++)
	        for (int x = 0; x < src.getWidth(); x++){
	            M[x][y]= (float) getGreyscaleFromColor(src.getPixel(x, y));
	            M2[x][y]= (float) M[x][y]*M[x][y];
	            if(x>0 && y>0){
	                M[x][y]+=M[x-1][y]+M[x][y-1]-M[x-1][y-1];
	                M2[x][y]+=M2[x-1][y]+M2[x][y-1]-M2[x-1][y-1];
	            }
	            else if(x>0 && !(y>0)){
	                M[x][y]+=M[x-1][y];
	                M2[x][y]+=M2[x-1][y];
	            }
	            else if(!(x>0) && y>0){
	                M[x][y]+=M[x][y-1];
	                M2[x][y]+=M2[x][y-1];
	            }

	        }

	    for(int y = 0; y < src.getHeight(); y++){
	        for (int x = 0; x < src.getWidth(); x++){
	            int limInf , limSup , limRight,limLeft = 0;
	            int area = 1;

	            // We take the limits of the enviroment
	            if(x-env < 0){
	                limLeft = 0;
	            }
	            else limLeft=x-env;

	            if(x+env >= src.getWidth()){
	                limRight = src.getWidth()-1;
	            }
	            else limRight=x+env;

	            if(y-env < 0){
	                limSup = 0;
	            }
	            else limSup=y-env;

	            if(y+env >= src.getHeight()){
	                limInf = src.getHeight()-1;
	            }
	            else limInf=y+env;

	            area = (limInf-limSup+1)*(limRight-limLeft+1);

	            //Calculate the mean
	            double mean = (float)(M[limLeft][limSup]+ M[limRight][limInf] - M[limLeft][limInf] -M[limRight][limSup])/area;
	            double mean2 = (float)(M2[limLeft][limSup]+ M2[limRight][limInf] - M2[limLeft][limInf] -M2[limRight][limSup])/area;
	            //Compute the Standar Deviacion square(Mean^2-mean2)
	            double sd = (float) Math.sqrt(mean2-mean*mean);

	            //Apply the Threshold T=mean-0.2sd
	            float T = (float)(mean *(1+k*(sd/(r-1))));
	            if( getGreyscaleFromColor(src.getPixel(x, y)) < T ) rslt.setPixel(x, y, Color.BLACK);
	            else rslt.setPixel(x, y, Color.WHITE);
	        }
	    }		
		
		
		return rslt;
	}
	
	
	public static int getGreyscaleFromColor(int color) {
		// http://en.wikipedia.org/wiki/Grayscale	
		return (int) (0.2126*Color.red(color) + 0.7152*Color.green(color) + 0.0722*Color.blue(color));
	}
	
}
