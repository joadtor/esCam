package com.joadtor.escam;

import android.app.Application;
import android.graphics.Bitmap;

public class V_esCam extends Application{
	private Bitmap mPerspective;

	public Bitmap getPerspective() {
		return mPerspective;
	}

	public void setPerspective(Bitmap mPerspective) {
		this.mPerspective = mPerspective;
	}
}
