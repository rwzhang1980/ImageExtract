package com.rwzhang.imageExtract.entity;

public class AreaLines {

	private LineGroup left_top_area;
	
	private LineGroup right_top_area;
	
	private LineGroup left_bottom_area;
	
	private LineGroup right_bottom_area;

	public AreaLines(int imgWidth, int imgHeight) {
		// TODO Auto-generated constructor stub
		left_top_area = new LineGroup(imgWidth, imgHeight, LineGroup.LEFT_TOP);
		right_top_area = new LineGroup(imgWidth, imgHeight, LineGroup.RIGHT_TOP);
		left_bottom_area = new LineGroup(imgWidth, imgHeight, LineGroup.LEFT_BOTTOM);
		right_bottom_area = new LineGroup(imgWidth, imgHeight, LineGroup.RIGHT_BOTTOM);
	}
	
	public LineGroup getLeft_top_area() {
		return left_top_area;
	}

	public void setLeft_top_area(LineGroup left_top_area) {
		this.left_top_area = left_top_area;
	}

	public LineGroup getRight_top_area() {
		return right_top_area;
	}

	public void setRight_top_area(LineGroup right_top_area) {
		this.right_top_area = right_top_area;
	}

	public LineGroup getLeft_bottom_area() {
		return left_bottom_area;
	}

	public void setLeft_bottom_area(LineGroup left_bottom_area) {
		this.left_bottom_area = left_bottom_area;
	}

	public LineGroup getRight_bottom_area() {
		return right_bottom_area;
	}

	public void setRight_bottom_area(LineGroup right_bottom_area) {
		this.right_bottom_area = right_bottom_area;
	}
	
}
