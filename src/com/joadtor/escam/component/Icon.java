package com.joadtor.escam.component;

public class Icon {
	
	protected int id;
	protected int x;
	protected int y;
	protected int radio;
	
	public Icon(int id, int x, int y, int radio) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.radio = radio;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int getRadio() {
		return radio;
	}
	
	public void setRadio(int radio) {
		this.radio = radio;
	}
	
	// Returns true if it's on range
	public boolean isOnRange(int x, int y, int offset) {
		
		int distanceX = this.x - x;
		int distanceY = this.y - y;
		int effRadio = this.radio + offset;
		
		if(effRadio*effRadio >= distanceX*distanceX + distanceY*distanceY) {
			return true;
		}
		
		return false;
	}

}
