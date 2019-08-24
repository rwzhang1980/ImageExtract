package com.rwzhang.imageExtract.utils;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import com.rwzhang.imageExtract.constants.Constants;
import com.rwzhang.imageExtract.entity.LineGroup;

public class Utils {

	public static void output(String path, Mat img){
		if(Constants.DEBUG){
			Imgcodecs.imwrite(path, img);
		}
	}
	
	/**
	 * 获取两条线段的夹角
	 * @param o 夹角point
	 * @param s
	 * @param e
	 * @return
	 */
	public static double calAngle(Point o, Point s, Point e){
		double cosfi = 0, fi = 0, norm = 0;
		double dsx = s.x - o.x;
		double dsy = s.y - o.y;
		double dex = e.x - o.x;
		double dey = e.y - o.y;

		cosfi = dsx * dex + dsy * dey;
		norm = (dsx * dsx + dsy * dsy) * (dex * dex + dey * dey);
		cosfi /= Math.sqrt(norm);

		if (cosfi >= 1.0) return 0;
		if (cosfi <= -1.0) return Math.PI;
		fi = Math.acos(cosfi);
		if (180 * fi / Math.PI < 180){
			return 180 * fi / Math.PI;
		}else{
			return 360 - 180 * fi / Math.PI;
		}
	}
	
	/**
	 * 判断线段组成的直线是竖线还是横线
	 * @param lines
	 * @return
	 */
	public static boolean isVerticalLine(List<double[]> lines){
		double spacingX = 0;
		double spacingY = 0;
		for(double[] line : lines){
			double x1 = line[0];
			double y1 = line[1];
			double x2 = line[2];
			double y2 = line[3];
			spacingX += Math.abs(x1 - x2);
			spacingY += Math.abs(y1 - y2);
		}
		//y值增量比x值增量大认为是更倾向于竖线
		if(spacingY / lines.size() > spacingX / lines.size()){
			return true;
		}
		return false;
	}
	
	/**
	 * 获取线段中最大的点
	 * @param lines
	 * @param indexs
	 * @return
	 */
	public static Point getMaximumPoint(List<double[]> lines, int... indexs){
		double max = 0;
		double[] point = new double[2];
		for(double[] line : lines){
			for(int index : indexs){
				if(line[index] > max){
					max = line[index];
					if(index == 0 || index == 2){
						point[0] = max;
						point[1] = line[index + 1];
					}else{
						point[0] = line[index - 1];
						point[1] = max;
					}
				}
			}
		}
		return new Point(point[0], point[1]);
	}
	
	/**
	 * 获取线段中最小的点
	 * @param lines
	 * @param indexs
	 * @return
	 */
	public static Point getMinimumPoint(List<double[]> lines, int[] indexs){
		double min = Double.MAX_VALUE;
		double[] point = new double[2];
		for(double[] line : lines){
			for(int index : indexs){
				if(line[index] < min){
					min = line[index];
					if(index == 0 || index == 2){
						point[0] = min;
						point[1] = line[index + 1];
					}else{
						point[0] = line[index - 1];
						point[1] = min;
					}
				}
			}
		}
		return new Point(point[0], point[1]);
	}
	
	/**
	 * 获取线段角度
	 * @param line
	 * @return
	 */
	public static double getAngle(double[] line){
		Point p1 = new Point(line[0], line[1]);
		Point p2 = new  Point(line[2], line[3]);
		Point p3 = null;
		if(p2.y < p1.y){
			p3 = p2;
			p2 = p1;
			p1 = p3;
		}
		p3 = new Point(0, p2.y);
		return Utils.calAngle(p2, p1, p3);
	}
	
	/**
	 * 计算延长线坐标
	 * @param a
	 * @param b
	 * @param x
	 * @return
	 */
	public static Point calExtendedLine(Point a, Point b, int x){
		double k0 = (b.y - a.y) / (b.x - a.x);
		double e = (b.y - k0 * b.x);
		double y = k0 * x + e;
		return new Point(x, y);
	}
	
	/**
	 * 计算交点
	 * @param lsegA
	 * @param lsegB
	 * @return
	 */
	public static Point getCrossPoint(double[] lsegA, double[] lsegB){
		double x;
		double y;
		double x1 = lsegA[0];
		double y1 = lsegA[1];
		double x2 = lsegA[2];
		double y2 = lsegA[3];
		double x3 = lsegB[0];
		double y3 = lsegB[1];
		double x4 = lsegB[2];
		double y4 = lsegB[3];
		double k1 = Double.MAX_VALUE;
		double k2 = Double.MAX_VALUE;
		boolean flag1 = false;
		boolean flag2 = false;

		if ((x1 - x2) == 0)
			flag1 = true;
		if ((x3 - x4) == 0)
			flag2 = true;

		if (!flag1)
			k1 = (y1 - y2) / (x1 - x2);
		if (!flag2)
			k2 = (y3 - y4) / (x3 - x4);

		if (k1 == k2)
			return null;

		if (flag1) {
			if (flag2)
				return null;
			x = x1;
			if (k2 == 0) {
				y = y3;
			} else {
				y = k2 * (x - x4) + y4;
			}
		} else if (flag2) {
			x = x3;
			if (k1 == 0) {
				y = y1;
			} else {
				y = k1 * (x - x2) + y2;
			}
		} else {
			if (k1 == 0) {
				y = y1;
				x = (y - y4) / k2 + x4;
			} else if (k2 == 0) {
				y = y3;
				x = (y - y2) / k1 + x2;
			} else {
				x = (k1 * x2 - k2 * x4 + y4 - y2) / (k1 - k2);
				y = k1 * (x - x2) + y2;
			}
		}
		if (between(x1, x2, x) && between(y1, y2, y) && between(x3, x4, x) && between(y3, y4, y)) {
			Point point = new Point(x, y);
			if (point.equals(new Point(lsegA[0], lsegA[1])) || point.equals(new Point(lsegA[2], lsegA[3])))
				return null;
			return point;
		} else {
			return null;
		}             
    }
    
	public static boolean between(double a, double b, double target) {
		if (target >= a - 0.01 && target <= b + 0.01 || target <= a + 0.01 && target >= b - 0.01)
			return true;
		else
			return false;
	}
	
	public static String type2Label(int type){
		String label = "";
		switch(type){
		case LineGroup.LEFT_TOP:
			label = "左上区域";
			break;
		case LineGroup.LEFT_BOTTOM:
			label = "左下区域";
			break;
		case LineGroup.RIGHT_TOP:
			label = "右上区域";
			break;
		case LineGroup.RIGHT_BOTTOM:
			label = "右下区域";
			break;
		default:
			label = "区域类型错误";
		}
		return label;
	}
	
	/**
	 * 检查是否是梯形
	 * @param ltp
	 * @param rtp
	 * @param lbp
	 * @param rbp
	 * @return
	 */
	public static boolean checkTrapezium(Point ltp, Point rtp, Point lbp, Point rbp){
		double lta = calAngle(ltp, lbp, rtp);
		double rta = calAngle(rtp, ltp, rbp);
		double rba = calAngle(rbp, rtp, lbp);
		double lba = calAngle(lbp, ltp, rbp);
		if(checkAngle(lta, rta) && checkAngle(rba, lba)){
			return true;
		}
		if(checkAngle(rta, rba) && checkAngle(lba, lta)){
			return true;
		}
		return false;
	}
	
	private static boolean checkAngle(double angle1, double angle2){
		if((angle1 <= 85 || angle1 >= 95) && (angle2 <= (angle1 + 4) && angle2 >= (angle1 - 4))){
			return true;
		}
		return false;
	}
}
