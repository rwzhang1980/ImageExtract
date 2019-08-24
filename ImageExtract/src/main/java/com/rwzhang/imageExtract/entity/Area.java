package com.rwzhang.imageExtract.entity;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Point;

public class Area {
	
	private List<Point> points = new ArrayList<Point>();
	
	private int type;
	
	public Area(){
	}
	
	public Area(int type){
		this.type = type;
	}
	
	public List<Point> getPoints() {
		return points;
	}

	public void setPoints(List<Point> points) {
		this.points = points;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public Point getFarthest(Point point){
		Point farthestPoint = null;
		double distance = 0;
		for(int i = 0;i < points.size();i++){
			Point _point = points.get(i);
			double _distance = distance(point, _point);
			if(_distance > distance){
				distance = _distance;
				farthestPoint = _point;
			}
		}
		return farthestPoint;
	}
	
	private double distance(Point p1, Point p2){
		return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
	}
	
}
