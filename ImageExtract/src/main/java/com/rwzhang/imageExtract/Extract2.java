package com.rwzhang.imageExtract;
import static com.rwzhang.imageExtract.constants.Constants.PI;
import static com.rwzhang.imageExtract.constants.Constants.TMP_FOLDER;
import static com.rwzhang.imageExtract.utils.Utils.output;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.rwzhang.imageExtract.entity.AreaLines;
import com.rwzhang.imageExtract.utils.ImageUtil;
import com.rwzhang.imageExtract.utils.Utils;

public class Extract2 {

	public void extract(String input, String output) throws Exception{
		int i = 1;
		Mat src = Imgcodecs.imread(input);
		Mat dst = new Mat();
		Imgproc.pyrMeanShiftFiltering(src, dst, 50, 10);//均值偏移
		output(TMP_FOLDER + "/0_meanshift.jpg", dst);
		

		//Imgproc.medianBlur(src, dst, 7);
		//output(TMP_FOLDER + "/" + (i++) + "_medianBlur.jpg", dst);
		
		//Imgproc.GaussianBlur(src, dst, new Size(3, 3), 0, 0);//高斯滤波
		//output(TMP_FOLDER + "/" + (i++) + "_gaussianBlur.jpg", dst);

		Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGB2GRAY);//灰度化
		output(TMP_FOLDER + "/" + (i++) + "_gray.jpg", dst);
		
		ImageUtil.gammaCorrection(dst, dst, 3f/2.2f);//gamma校正
		output(TMP_FOLDER + "/" + (i++) + "_gamma.jpg", dst);
		
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
		Imgproc.morphologyEx(dst, dst, Imgproc.MORPH_CLOSE, element);//闭运算
		output(TMP_FOLDER + "/" + (i++) + "_morph_close.jpg", dst);
		
		//Imgproc.threshold(dst, dst, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);//二值化
		//output(TMP_FOLDER + "/" + (i++) + "_thresholding.jpg", dst);
		
		Mat grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_x = new Mat();
        Mat abs_grad_y = new Mat();

        Imgproc.Sobel(dst, grad_x, CvType.CV_16S, 1, 0, 3, 1, 1, Core.BORDER_DEFAULT);
        Core.convertScaleAbs(grad_x, abs_grad_x);
        output(TMP_FOLDER + "/" + (i++) + "_sobelX.jpg", grad_x);
        
        Imgproc.Sobel(dst, grad_y, CvType.CV_16S, 0, 1, 3, 1, 1, Core.BORDER_DEFAULT);
        Core.convertScaleAbs(grad_y, abs_grad_y);
        output(TMP_FOLDER + "/" + (i++) + "_sobelY.jpg", grad_y);
        
        // Total Gradient (approximate)
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 1, 1, dst);
        output(TMP_FOLDER + "/" + (i++) + "_sobel.jpg", dst);
        

		
		//Imgproc.erode(dst, dst, element);//腐蚀
		//output(TMP_FOLDER + "/" + (i++) + "_erode.jpg", dst);
		//Imgproc.dilate(dst, dst, element);//膨胀
		//output(TMP_FOLDER + "/" + (i++) + "_dilate.jpg", dst);
		
		
		//Imgproc.erode(dst, dst, element);//腐蚀
				//output(TMP_FOLDER + "/" + (i++) + "_erode.jpg", dst);
		
		
		
		//Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGB2GRAY);//灰度化
		//output(TMP_FOLDER + "/" + (i++) + "_gray.jpg", dst);
		
		//Imgproc.Canny(dst, dst, 30, 120, 3);//边缘检测
		//output(TMP_FOLDER + "/" + (i++) + "_canny.jpg", dst);
		
		//查找轮廓
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat(); 
		Imgproc.findContours(dst, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		
		//加粗增强所有找到的轮廓
		//Imgproc.drawContours(dst, contours, -1, new Scalar(255), 3);
		//output(TMP_FOLDER + "/" + (i++) + "_strong.jpg", dst);
		
		
		//再次查找轮廓
		contours.clear();
		hierarchy = new Mat(); 
		Imgproc.findContours(dst, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		//printContours(src, contours);
		MatOfPoint mpoint = getMaximum(contours);
		contours.clear();
		contours.add(mpoint);
		//画出唯一轮廓
		dst.setTo(new Scalar(0));//填充为黑色
		Imgproc.drawContours(dst, contours, -1, new Scalar(255, 255, 255), 3);
		output(TMP_FOLDER + "/" + (i++) + "_lastContours.jpg", dst);
		
		Mat lines = new Mat();
		Imgproc.HoughLinesP(dst, lines, 1, PI/180, 180, 30, 10);//使用霍夫变换查找线段
		//printLines(src, lines);
		
		AreaLines areaLines = putLines(getCenterPoint(dst), lines, src.cols(), src.rows());
    	
		//获取每个区域的交点坐标
		Point ltp = areaLines.getLeft_top_area().getCrossPoint(src);
		Point rtp = areaLines.getRight_top_area().getCrossPoint(src);
		Point rbp = areaLines.getRight_bottom_area().getCrossPoint(src);
		Point lbp = areaLines.getLeft_bottom_area().getCrossPoint(src);
		boolean flag = Utils.checkTrapezium(ltp, rtp, lbp, rbp);
		System.out.println(flag);
		
		Imgproc.circle(dst, ltp, 20, new Scalar(255), 5, Imgproc.LINE_AA);
    	Imgproc.circle(dst, rtp, 20, new Scalar(255), 5, Imgproc.LINE_AA);
    	Imgproc.circle(dst, rbp, 20, new Scalar(255), 5, Imgproc.LINE_AA);
    	Imgproc.circle(dst, lbp, 20, new Scalar(255), 5, Imgproc.LINE_AA);
    	output(TMP_FOLDER + "/" + (i++) + "_angle.jpg", dst);
    	
    	//开始做透视变换
		Mat mat = new Mat();
		mat.push_back(new MatOfPoint2f(ltp));
		mat.push_back(new MatOfPoint2f(rtp));
		mat.push_back(new MatOfPoint2f(rbp));
		mat.push_back(new MatOfPoint2f(lbp));
		
		Size outputSize = getOutputSize(ltp, rtp, rbp, lbp);
		
		Mat size = new Mat();
		size.push_back(new MatOfPoint2f(new Point(0, 0)));
	    size.push_back(new MatOfPoint2f(new Point(outputSize.width, 0)));
	    size.push_back(new MatOfPoint2f(new Point(outputSize.width, outputSize.height)));
	    size.push_back(new MatOfPoint2f(new Point(0, outputSize.height)));
	    Mat pt = Imgproc.getPerspectiveTransform(mat, size);
	    Imgproc.warpPerspective(src, src, pt, new Size(outputSize.width, outputSize.height));
	    output(TMP_FOLDER + "/" + (i++) + "_final.jpg", src);
	    
	    //Imgproc.cvtColor(src, src, Imgproc.COLOR_RGB2GRAY);//灰度化
	    //output(TMP_FOLDER + "/" + (i++) + "_final.jpg", src);
	    
	    //ImageUtil.gammaCorrection(src, src, 1f/2.2f);//gamma校正
	    //output(TMP_FOLDER + "/" + (i++) + "_final.jpg", src);
	    
	    Mat kernel = new Mat(3, 3, CvType.CV_32F,new Scalar(-1)); 
	    kernel.put(1, 1, 10.5);
		Imgproc.filter2D(src, src, src.depth(), kernel);//锐化
		output(TMP_FOLDER + "/" + (i++) + "_sharpening.jpg", src);
		
		//Imgproc.equalizeHist(src, src);//直方图均衡化
		//output(TMP_FOLDER + "/" + (i++) + "_equalizeHist.jpg", src);
//	    Imgproc.threshold(src, src, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);//二值化
//	    output(TMP_FOLDER + "/" + (i++) + "_final.jpg", src);
	    
	}
	
//	private void drawHull(List<MatOfPoint> contours, Mat src){
//		for(MatOfPoint contour : contours){
//			MatOfInt hull = new MatOfInt();
//			Imgproc.convexHull(contour, hull);
//			List<MatOfPoint> p2 = new ArrayList<MatOfPoint>();
//			for(int i = 0;i < hull.rows();i++){
//				p2.add(contours.get(hull.get(i, 0)))
//			}
//			Imgproc.polylines(src, i, true, new Scalar(255, 255, 255), 3); 
//		}
//	}
	
	/**
	 * 获取图像的中心点
	 * @param img
	 * @return
	 */
	private Point getCenterPoint(Mat img){
		int row = img.rows();
		int col = img.cols();
		int centerX = col / 2 - 1;
		int centerY = row / 2 - 1;
		return new Point(centerX, centerY);
	}
	
	private void printContours(Mat src, List<MatOfPoint> contours){
		for(int i = 0;i < contours.size();i++){
			MatOfPoint contour = contours.get(i);
			Mat mat = new Mat();
			src.copyTo(mat);
			List<MatOfPoint> ps = new ArrayList<MatOfPoint>();
			ps.add(contour);
			mat.setTo(new Scalar(0));
			Imgproc.drawContours(mat, ps, -1, new Scalar(255, 255, 255), 3);
			 output(TMP_FOLDER + "/" + (i++) + "_contours.jpg", mat);
		}
	}
	
	/**
	 * 输出线段在原始图中的位置，测试时用
	 * @param src
	 * @param lines
	 */
	private void printLines(Mat src, Mat lines){
		for(int i = 0;i < lines.rows();i++){
			double[] l = lines.get(i, 0);
			Mat tmp = new Mat();
			src.copyTo(tmp);
			Imgproc.line(tmp, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(255), 5, Imgproc.LINE_AA);
			output(TMP_FOLDER + "/a_" + i + ".jpg", tmp);
		}
	}
	
	/**
	 * 将线段放置到对应区域的对象中
	 * @param centerPoint
	 * @param point
	 * @param areaLines 存放区域线段集合的对象
	 * @param line
	 */
	private void putLines(Point centerPoint, Point point, AreaLines areaLines, double[] line){
		if(point.x <= centerPoint.x && point.y <= centerPoint.y){//左上区域
			if(!areaLines.getLeft_top_area().getLines().contains(line)){
				areaLines.getLeft_top_area().getLines().add(line);
			}
		}else if(point.x > centerPoint.x && point.y <= centerPoint.y){//右上区域
			if(!areaLines.getRight_top_area().getLines().contains(line)){
				areaLines.getRight_top_area().getLines().add(line);
			}
		}else if(point.x <= centerPoint.x && point.y > centerPoint.y){//左下区域
			if(!areaLines.getLeft_bottom_area().getLines().contains(line)){
				areaLines.getLeft_bottom_area().getLines().add(line);
			}
		}else{
			if(!areaLines.getRight_bottom_area().getLines().contains(line)){//右下区域
				areaLines.getRight_bottom_area().getLines().add(line);
			}
		}
	}
	
	/**
	 * 将线段放置到对应区域的对象中
	 * @param centerPoint
	 * @param point
	 * @param areaLines 存放区域线段集合的对象
	 * @param line
	 */
	private AreaLines putLines(Point centerPoint, Mat lines, int imgWidth, int imgHeight){
		AreaLines areaLines = new AreaLines(imgWidth, imgHeight);
		for(int i = 0;i < lines.rows();i++){
			double[] line = lines.get(i, 0);
			Point p1 = new Point(line[0], line[1]);
			Point p2 = new Point(line[2], line[3]);

			putLines(centerPoint, p1, areaLines, line);
			putLines(centerPoint, p2, areaLines, line);
		}
		return areaLines;
	}
	
	/**
	 * 获取轮廓最大的
	 * @param f_contours
	 * @return
	 */
	private MatOfPoint getMaximum(List<MatOfPoint> contours){
		MatOfPoint mpoint = null;
		double maxArea = 0;
		for (int i = 0; i < contours.size(); i++){
			MatOfPoint2f point2f = new MatOfPoint2f(contours.get(i).toArray());
			RotatedRect rect = Imgproc.minAreaRect(point2f);
			double currentArea = rect.size.height * rect.size.width;
	        //double currentArea = Imgproc.contourArea(f_contours.get(i));
	        if (currentArea > maxArea)
	        {
	        	mpoint = contours.get(i);
	        	maxArea = currentArea;
	        }
	    }
		return mpoint;
	}
	
	public static Size getOutputSize(Point ltp, Point rtp, Point rbp, Point lbp){
		 double h1 = Math.sqrt(Math.pow(ltp.x - lbp.x, 2) + Math.pow(ltp.y - lbp.y, 2));
		 double h2 = Math.sqrt(Math.pow(rtp.x - rbp.x, 2) + Math.pow(rtp.y - rbp.y, 2));
		 
		 double w1 = Math.sqrt(Math.pow(ltp.x - rtp.x, 2) + Math.pow(ltp.y - rtp.y, 2));
		 double w2 = Math.sqrt(Math.pow(lbp.x - rbp.x, 2) + Math.pow(lbp.y - rbp.y, 2));
		 return new Size(Math.max(w1, w2), Math.max(h1, h2));
	}
	
}
