package com.joadtor.escam.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class Selector extends View {
	private Paint mPaint = new Paint();
	private Paint mPaintLine = new Paint();
	private ArrayList<Icon> mListIcon = null;
	private int mStrokeSize = 2;
	
	private int mActiveIcon = -1;
	private Boolean mEditable = true;

	// important: do not forget to add AttributeSet, it is necessary to have this
	// view called from an xml view file
	public Selector(Context context, AttributeSet attributeset) {
		
		super(context, attributeset);
		
		//mPaint.setColor(0xff336666);
		mPaint.setColor(Color.BLUE);
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(mStrokeSize);
		
		//mPaintLine.setColor(0xffcc6600);
		mPaintLine.setColor(Color.GREEN);
		mPaintLine.setAntiAlias(true);
		mPaintLine.setStyle(Paint.Style.STROKE);
		mPaintLine.setStrokeWidth(mStrokeSize);
		
	}
	
	public void rotateBackground(int degrees){
		
			Bitmap src = ((BitmapDrawable)getBackground()).getBitmap();
			Bitmap bitmap;

			Matrix matrix = new Matrix();
			matrix.postRotate(degrees);
			bitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
			
			
			BitmapDrawable myBitmap = new BitmapDrawable(bitmap);
			setBackgroundDrawable(myBitmap);
			
	}
	
	
	
	public int increaseStrokeSize(){
		++mStrokeSize;
		mPaint.setStrokeWidth(mStrokeSize);
		mPaintLine.setStrokeWidth(mStrokeSize);
		invalidate();
		return mStrokeSize;
	}
	
	public int decreaseStrokeSize(){
		--mStrokeSize;
		mPaint.setStrokeWidth(mStrokeSize);
		mPaintLine.setStrokeWidth(mStrokeSize);
		invalidate();
		return mStrokeSize;
	}
	
	public void setLineColor(int Color){
		
		mPaintLine.setColor(Color);
		invalidate();
	}
	
	public void setEllipseColor(int Color){
		
		mPaint.setColor(Color);
		invalidate();
	}

	@Override
	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		if(this.mEditable){
			int gap_x = this.getWidthC() /3;
			int gap_y = this.getHeigthC() /3;


			int id = 0;
			mListIcon = new ArrayList<Icon>();
			mListIcon.add(new Icon(id++,gap_x,gap_y,20));
			mListIcon.add(new Icon(id++,gap_x*2,gap_y,20));
			mListIcon.add(new Icon(id++,gap_x*2,gap_y*2,20));
			mListIcon.add(new Icon(id++,gap_x,gap_y*2,20));
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawColor(Color.TRANSPARENT);
		if(this.mEditable){
			for(Icon bullet : mListIcon) {
				if(mListIcon.size() > 1) 
					canvas.drawLine(bullet.getX(), bullet.getY(), mListIcon.get((bullet.id + 1) % mListIcon.size()).getX(),  mListIcon.get((bullet.id + 1) % mListIcon.size()).getY(), mPaintLine);
			}
			for(Icon bullet : mListIcon) {
				canvas.drawCircle(bullet.getX(), bullet.getY(), bullet.getRadio(), mPaint);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(this.mEditable){
			int action = event.getAction();

			int x = (int) event.getX();
			int y = (int) event.getY();

			switch (action) {

			case MotionEvent.ACTION_DOWN:

				for(Icon bullet : mListIcon) {
					if(bullet.isOnRange(x, y, 20)){

						mActiveIcon = bullet.getId();
					}
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if(mActiveIcon != -1) {

					mListIcon.get(mActiveIcon).setX(x);
					mListIcon.get(mActiveIcon).setY(y);
				}
				break;
			case MotionEvent.ACTION_UP:
			default:
				// Don't get out of bounds
				if(mActiveIcon != -1) {
					int width = this.getRight() - this.getLeft();
					int heigth = this.getBottom() - this.getTop();

					if(x < 0) x = 0;
					if(x > width) x = width;
					if(y < 0) y = 0;
					if(y > heigth) y = heigth;

					mListIcon.get(mActiveIcon).setX(x);
					mListIcon.get(mActiveIcon).setY(y);
				}
				mActiveIcon = -1;

				// Avoid line crossing
				Collections.sort((List<Icon>)mListIcon,new CompareIcon());

				ArrayList<Icon> listT = new ArrayList<Icon>();
				if(mListIcon.get(0).getX() < mListIcon.get(1).getX()) {

					listT.add(mListIcon.get(0));
					listT.add(mListIcon.get(1));
					if(mListIcon.get(2).getX() < mListIcon.get(3).getX()){
						listT.add(mListIcon.get(3));
						listT.add(mListIcon.get(2));
					} else {
						listT.add(mListIcon.get(2));
						listT.add(mListIcon.get(3));
					}
				} else {
					listT.add(mListIcon.get(1));
					listT.add(mListIcon.get(0));
					if(mListIcon.get(2).getX() < mListIcon.get(3).getX()){
						listT.add(mListIcon.get(3));
						listT.add(mListIcon.get(2));
					} else {
						listT.add(mListIcon.get(2));
						listT.add(mListIcon.get(3));
					}
				}

				if(listT.get(3).getX() < listT.get(2).getX() && listT.get(3).getY() < listT.get(2).getY()){
					if (listT.get(1).getX() < listT.get(2).getX() && listT.get(1).getX() < listT.get(3).getX()){
						float m1 = Math.abs((listT.get(1).getY() - listT.get(2).getY()) / (float)(listT.get(1).getX() - listT.get(2).getX()));
						float m2 = Math.abs((listT.get(1).getY() - listT.get(3).getY()) / (float)(listT.get(1).getX() - listT.get(3).getX()));

						if(m1 >= m2){
							Icon aux1, aux2;
							aux1 = listT.get(2);
							aux2 = listT.get(3);
							listT.remove(3);
							listT.remove(2);
							listT.add(aux2);
							listT.add(aux1);
						}
					}
				}
				
				

				if(listT.get(0).getX() > listT.get(2).getX() && listT.get(2).getX() > listT.get(3).getX() && listT.get(1).getX() > listT.get(0).getX() && listT.get(3).getY() > listT.get(2).getY()) {

					float m1 = Math.abs((listT.get(2).getY() - listT.get(0).getY()) / (float)(listT.get(2).getX() - listT.get(0).getX()));
					float m2 = Math.abs((listT.get(3).getY() - listT.get(0).getY()) / (float)(listT.get(3).getX() - listT.get(0).getX()));

					if(m1 <= m2){
						Icon aux1, aux2, aux3, aux4;

						aux1 = listT.get(2);
						aux2 = listT.get(0);
						aux3 = listT.get(1);
						aux4 = listT.get(3);

						listT = new ArrayList<Icon>();

						listT.add(aux1);
						listT.add(aux2);
						listT.add(aux3);
						listT.add(aux4);
					}	
				}

				
				
				if(listT.get(0).getY() < listT.get(3).getY() && listT.get(0).getY() > listT.get(1).getY() && listT.get(2).getX() < listT.get(1).getX()){

						float m1 = Math.abs((listT.get(0).getY() - listT.get(1).getY()) / (float)(listT.get(0).getX() - listT.get(1).getX()));
						float m2 = Math.abs((listT.get(2).getY() - listT.get(1).getY()) / (float)(listT.get(2).getX() - listT.get(1).getX()));

						if(m1 > m2){
							Icon aux1, aux2, aux3, aux4;

							aux1 = listT.get(3);
							aux2 = listT.get(1);
							aux3 = listT.get(0);
							aux4 = listT.get(2);

							listT = new ArrayList<Icon>();

							listT.add(aux1);
							listT.add(aux2);
							listT.add(aux3);
							listT.add(aux4);
						}
				}

				// Re-index icons
				for(int i = 0; i < listT.size(); i++)
					listT.get(i).setId(i);
				mListIcon = listT;

			}

			invalidate();
		}
		return true;
	}
	
	public int getWidthC() {
		
		return this.getRight() - this.getLeft();
	}
	
	public int getHeigthC() {
		
		return this.getBottom() - this.getTop();
	}
	
	public ArrayList<Icon> getIconList() {
		
		return mListIcon;
	}
	
	public int[] getPoints(){ // Returns an array with points {x1, y1, x2, y2, x3, y3, x4, y4}
		
		int[] list = new int[8];
			
		for(int i = 0; i < 4; i++){
			list[i*2] = mListIcon.get(i).x;
			list[i*2+1] = mListIcon.get(i).y;
		}
		
		return list;
	}
	
	public void disableEdit(){
		this.mEditable = false;
		invalidate();
	}
	
	public void enableEdit(){
		this.mEditable = true;
		invalidate();
	}
}
