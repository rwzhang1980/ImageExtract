package com.rwzhang.imageExtract.entity;

import org.opencv.core.Point;

public class VertexAngle {

	private Area left_top = new Area();
	
	private Area right_top = new Area();
	
	private Area left_bottom = new Area();
	
	private Area right_bottom = new Area();
	
	private Point centerPoint;
	
	public Area getLeft_top() {
		return left_top;
	}

	public Area getRight_top() {
		return right_top;
	}

	public Area getLeft_bottom() {
		return left_bottom;
	}

	public Area getRight_bottom() {
		return right_bottom;
	}

	public Point getCenterPoint() {
		return centerPoint;
	}

	public void setCenterPoint(Point centerPoint) {
		this.centerPoint = centerPoint;
	}

	public Point getLTAngle(){
		return left_top.getFarthest(centerPoint);
	}
	
	public Point getRTAngle(){
		return right_top.getFarthest(centerPoint);
	}
	
	public Point getLBAngle(){
		return left_bottom.getFarthest(centerPoint);
	}
	
	public Point getRBAngle(){
		return right_bottom.getFarthest(centerPoint);
	}
}
