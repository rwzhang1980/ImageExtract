package com.rwzhang.imageExtract;
import static com.rwzhang.imageExtract.constants.Constants.PI;
import static com.rwzhang.imageExtract.constants.Constants.TMP_FOLDER;
import static com.rwzhang.imageExtract.utils.Utils.output;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.rwzhang.imageExtract.entity.VertexAngle;
import com.rwzhang.imageExtract.utils.ImageUtil;

/**
 * 
 * @author rwzhang
 *
 */
public class Extract {
	
	public void extract(String input, String output){
		int i = 1;
		Mat src = Imgcodecs.imread(input);
		Mat dst = new Mat();
		//Imgproc.pyrMeanShiftFiltering(src, dst, 50, 10);//均值偏移
		//output(TMP_FOLDER + "/0_meanshift.jpg", dst);
		
		Mat kernel = new Mat(3, 3, CvType.CV_32F,new Scalar(-1)); 
		kernel.put(1, 1, 8.9);
		Imgproc.filter2D(src, dst, src.depth(),kernel);//锐化
		output(TMP_FOLDER + "/" + (i++) + "_sharpening.jpg", dst);
		
		Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGB2GRAY);//灰度化
		output(TMP_FOLDER + "/" + (i++) + "_gray.jpg", dst);
		
		//Imgproc.equalizeHist(dst, dst);//直方图均衡化
		//output(TMP_FOLDER + "/" + (i++) + "_equalizeHist.jpg", dst);
		
		ImageUtil.gammaCorrection(dst, dst, 0.8f);//gamma校正
		output(TMP_FOLDER + "/" + (i++) + "_gamma.jpg", dst);
		
		Imgproc.GaussianBlur(dst, dst, new Size(5, 5), 0, 0);//高斯滤波
		output(TMP_FOLDER + "/" + (i++) + "_gaussianBlur.jpg", dst);
		
		Imgproc.threshold(dst, dst, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);//二值化
		output(TMP_FOLDER + "/" + (i++) + "_thresholding.jpg", dst);

		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
		
		//Imgproc.dilate(dst, dst, element);//膨胀
		//output(TMP_FOLDER + "/" + (i++) + "_dilate.jpg", dst);
		
		Imgproc.morphologyEx(dst, dst, Imgproc.MORPH_CLOSE, element);//闭运算
		output(TMP_FOLDER + "/" + (i++) + "_morph_close.jpg", dst);
		
		Imgproc.erode(dst, dst, element);//腐蚀
		output(TMP_FOLDER + "/" + (i++) + "_erode.jpg", dst);
		
		Imgproc.Canny(dst, dst, 30, 120, 3);//边缘检测
		output(TMP_FOLDER + "/" + (i++) + "_canny.jpg", dst);
		
		//查找轮廓
		List<MatOfPoint> f_contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat(); 
		Imgproc.findContours(dst, f_contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		
		//加粗增强所有找到的轮廓
		Imgproc.drawContours(dst, f_contours, -1, new Scalar(255), 3);
		output(TMP_FOLDER + "/" + (i++) + "_strong.jpg", dst);
		
		//Imgproc.erode(dst, dst, element);//腐蚀
		//output(TMP_FOLDER + "/" + (i++) + "_erode.jpg", dst);
		
		//再次查找轮廓
		f_contours.clear();
		hierarchy = new Mat(); 
		Imgproc.findContours(dst, f_contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		MatOfPoint mpoint = getMaximum(f_contours);
		f_contours.clear();
		f_contours.add(mpoint);
		//画出唯一轮廓
		dst.setTo(new Scalar(0));//填充为黑色
		Imgproc.drawContours(dst, f_contours, -1, new Scalar(255, 255, 255), 3);
		output(TMP_FOLDER + "/" + (i++) + "_lastContours.jpg", dst);
		
		Mat lines = new Mat();
		Imgproc.HoughLinesP(dst, lines, 1, PI/180, 180, 30, 10);//使用霍夫变换查找线段
		//printLines(src, lines);
		VertexAngle vertexAngle = splitImg(dst, lines);
	    	//mp.push_back(new MatOfPoint2f(new Point(point.x, point.y)));
    	Imgproc.circle(dst, vertexAngle.getLTAngle(), 20, new Scalar(255), 5, Imgproc.LINE_AA);
    	Imgproc.circle(dst, vertexAngle.getRTAngle(), 20, new Scalar(255), 5, Imgproc.LINE_AA);
    	Imgproc.circle(dst, vertexAngle.getRBAngle(), 20, new Scalar(255), 5, Imgproc.LINE_AA);
    	Imgproc.circle(dst, vertexAngle.getLBAngle(), 20, new Scalar(255), 5, Imgproc.LINE_AA);
    	output(TMP_FOLDER + "/" + (i++) + "_angle.jpg", dst);
    	
		Mat mat = new Mat();
		mat.push_back(new MatOfPoint2f(new Point(vertexAngle.getLTAngle().x, vertexAngle.getLTAngle().y)));
		mat.push_back(new MatOfPoint2f(new Point(vertexAngle.getRTAngle().x, vertexAngle.getRTAngle().y)));
		mat.push_back(new MatOfPoint2f(new Point(vertexAngle.getRBAngle().x, vertexAngle.getRBAngle().y)));
		mat.push_back(new MatOfPoint2f(new Point(vertexAngle.getLBAngle().x, vertexAngle.getLBAngle().y)));
		
		Mat size = new Mat();
		size.push_back(new MatOfPoint2f(new Point(0, 0)));
	    size.push_back(new MatOfPoint2f(new Point(src.cols(), 0)));
	    size.push_back(new MatOfPoint2f(new Point(src.cols(), src.rows())));
	    size.push_back(new MatOfPoint2f(new Point(0, src.rows())));
	    Mat pt = Imgproc.getPerspectiveTransform(mat, size);
	    Imgproc.warpPerspective(src, src, pt, new Size(src.cols(), src.rows()));
	    output(TMP_FOLDER + "/" + (i++) + "_final.jpg", src);
	    
	    Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);//灰度化
	    output(TMP_FOLDER + "/" + (i++) + "_final.jpg", src);
	    ImageUtil.gammaCorrection(src, src, 1f/2.2f);//gamma校正
	    output(TMP_FOLDER + "/" + (i++) + "_final.jpg", src);
	    Imgproc.threshold(src, src, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);//二值化
	    output(TMP_FOLDER + "/" + (i++) + "_final.jpg", src);
	    
	}
	
	private Point getCenterPoint(Mat img){
		int row = img.rows();
		int col = img.cols();
		int centerX = col / 2 - 1;
		int centerY = row / 2 - 1;
		return new Point(centerX, centerY);
	}
	
	private void printLines(Mat src, Mat lines){
		for(int i = 0;i < lines.rows();i++){
			double[] l = lines.get(i, 0);
			Mat tmp = new Mat();
			src.copyTo(tmp);
			Imgproc.line(tmp, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(255), 5, Imgproc.LINE_AA);
			 output(TMP_FOLDER + "/a_" + i + ".jpg", tmp);
		}

	}
	
	private VertexAngle splitImg(Mat img, Mat lines){
		VertexAngle vertexAngle = new VertexAngle();
		Point centerPoint = getCenterPoint(img);
		vertexAngle.setCenterPoint(centerPoint);
		List<Point> points = getAllPoints(lines);
		for(int i = 0;i < points.size();i++){
			Point point = points.get(i);
			if(point.x <= centerPoint.x && point.y <= centerPoint.y){
				vertexAngle.getLeft_top().getPoints().add(point);
			}else if(point.x > centerPoint.x && point.y <= centerPoint.y){
				vertexAngle.getRight_top().getPoints().add(point);
			}else if(point.x <= centerPoint.x && point.y > centerPoint.y){
				vertexAngle.getLeft_bottom().getPoints().add(point);
			}else{
				vertexAngle.getRight_bottom().getPoints().add(point);
			}
		}
		return vertexAngle;
	}
	
	private List<Point> getAllPoints(Mat lines){
		List<Point> points = new ArrayList<Point>();
		for(int i = 0;i < lines.rows();i++){
			double[] line = lines.get(i, 0);
			points.add(new Point(line[0], line[1]));
			points.add(new Point(line[2], line[3]));
		}
		return points;
	}
	
	/**
	 * 获取轮廓最大的
	 * @param f_contours
	 * @return
	 */
	private MatOfPoint getMaximum(List<MatOfPoint> f_contours){
		MatOfPoint mpoint = null;
		double maxArea = 0;
		for (int i = 0; i < f_contours.size(); i++){
			MatOfPoint2f point2f = new MatOfPoint2f(f_contours.get(i).toArray());
			RotatedRect rect = Imgproc.minAreaRect(point2f);
			double currentArea = rect.size.height * rect.size.width;
	        //double currentArea = Imgproc.contourArea(f_contours.get(i));
	        if (currentArea > maxArea)
	        {
	        	mpoint = f_contours.get(i);
	        	maxArea = currentArea;
	        }
	    }
		return mpoint;
	}
	
	/**
	 * 获取两条线段的夹角
	 * @param o
	 * @param s
	 * @param e
	 * @return
	 */
	private double calAngle(Point o, Point s, Point e){
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
	
}
