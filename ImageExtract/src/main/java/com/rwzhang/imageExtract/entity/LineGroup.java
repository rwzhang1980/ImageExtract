package com.rwzhang.imageExtract.entity;

import static com.rwzhang.imageExtract.constants.Constants.TMP_FOLDER;
import static com.rwzhang.imageExtract.utils.Utils.output;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.rwzhang.imageExtract.utils.Utils;

public class LineGroup {

	private List<double[]> lines = new ArrayList<double[]>();//当前区域内的所有线段
	
	private int imgWidth;//原始图像宽度
	
	private int imgHeight;//原始图像高度
	
	private int type;
	
	public static final int LEFT_TOP = 1;
	
	public static final int RIGHT_TOP = 2;
	
	public static final int LEFT_BOTTOM = 3;
	
	public static final int RIGHT_BOTTOM = 4;
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public LineGroup(int imgWidth, int imgHeight, int type) {
		// TODO Auto-generated constructor stub
		this.imgWidth = imgWidth;
		this.imgHeight = imgHeight;
		this.type = type;
	}
	
	public List<double[]> getLines() {
		return lines;
	}

	/**
	 * 获取两条线段的交点
	 * @return
	 */
	public Point getCrossPoint(Mat src) throws Exception{
		List<double[]> group1 = new ArrayList<double[]>();
		List<double[]> group2 = new ArrayList<double[]>();
		//计算出每条线段水平方向的角度，按角度对线段进行分组
		List<Item> lineList = getLinesAngle();
		if(lineList.isEmpty()){
			throw new Exception(Utils.type2Label(type) + "没有线段");
		}
		double angle = lineList.get(0).getAngle();
		for(Item item : lineList){
			double _angle = item.getAngle();
			//角度相差30以内的认为是一组
			if(Math.abs(angle - _angle) <= 30 || Math.abs((180 - angle) - _angle) <= 30){
				group1.add(item.getLine());
			}else{
				group2.add(item.getLine());
			}
		}
		//将多条线段合并为一条线段，角的两边一共合并出2条线段
		double[] longLine1 = getLine(group1, src);
		double[] longLine2 = getLine(group2, src);
		
		//延长线段到图像两端
		double[] fullLine1 = getExtendedLine(longLine1);
		double[] fullLine2 = getExtendedLine(longLine2);
		
		//计算交点
		Point crossPoint = Utils.getCrossPoint(fullLine1, fullLine2);
		if(crossPoint == null){
			throw new Exception(Utils.type2Label(type) + "未找到交点");
		}
		return crossPoint;
	}
	
	/**
	 * 获取线段延长线
	 * @param line
	 * @return
	 */
	private double[] getExtendedLine(double[] line){
		//获取线段水平方向角度
		double angle = Utils.getAngle(line);
		if(angle == 0){//如果是水平横线
			return new double[]{0, line[1], imgWidth, line[3]};
		}else if(angle == 90){//如果是垂直竖线
			return new double[]{line[0], 0, line[2], imgHeight};
		}else{
			//将线段延伸到图像最两端
			Point startPoint = Utils.calExtendedLine(new Point(line[0], line[1]), new Point(line[2], line[3]), 0);
			Point endPoint = Utils.calExtendedLine(new Point(line[0], line[1]), new Point(line[2], line[3]), imgWidth);
			return new double[]{startPoint.x, startPoint.y, endPoint.x, endPoint.y};
		}
	}
	
	/**
	 * 将多条短线段合并为一条长线段
	 * @param group
	 * @return
	 */
	private double[] getLine(List<double[]> group, Mat src){
		int[] index = null; 
		//判断是否是竖线
		if(Utils.isVerticalLine(group)){
			index = new int[]{1, 3};
		}else{
			index = new int[]{0, 2};
		}
		//获取多个短线段中的起点与终点
		//printLines(group, src);
		Point startPoint = Utils.getMinimumPoint(group, index);
		Point endPoint = Utils.getMaximumPoint(group, index);
		return new double[]{startPoint.x, startPoint.y, endPoint.x, endPoint.y};
	}
	
	private void printLines(List<double[]> group, Mat src){
		for(int i = 0;i < group.size();i++){
			double[] l = group.get(i);
			Mat tmp = new Mat();
			src.copyTo(tmp);
			Imgproc.line(tmp, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(255), 5, Imgproc.LINE_AA);
			output(TMP_FOLDER + "/a_" + i + ".jpg", tmp);
		}
	}
	
	/**
	 * 获取每天线段水平方向的角度
	 * @return
	 */
	private List<Item> getLinesAngle(){
		List<Item> lineList = new ArrayList<Item>();
		for(double[] line : lines){
			double angle = Utils.getAngle(line);
			lineList.add(new Item(angle, line));
		}
		return lineList;
	}
	
	class Item{
		private double angle;
		private double[] line;
		public Item(double angle, double[] line) {
			// TODO Auto-generated constructor stub
			this.angle = angle;
			this.line = line;
		}
		public double getAngle() {
			return angle;
		}
		public void setAngle(double angle) {
			this.angle = angle;
		}
		public double[] getLine() {
			return line;
		}
		public void setLine(double[] line) {
			this.line = line;
		}
	}
	
}
