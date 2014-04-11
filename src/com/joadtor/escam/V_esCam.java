package com.joadtor.escam;

import android.app.Application;
import android.graphics.Bitmap;

public class V_esCam extends Application{
	private Bitmap mPerspective;
	private Bitmap mFilter;

	public Bitmap getPerspective() {
		return mPerspective;
	}

	public void setPerspective(Bitmap mPerspective) {
		this.mPerspective = mPerspective;
	}

	public Bitmap getFilter() {
		return mFilter;
	}

	public void setFilter(Bitmap mFilter) {
		this.mFilter = mFilter;
	}
}
