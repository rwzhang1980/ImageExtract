package com.rwzhang.imageExtract.utils;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * 图像处理
 * @author rwzhang
 *
 */
public class ImageUtil {

	
	/**
	 * gamma校正
	 * @param sourceMat
	 * @param gamma
	 * @return
	 */
	public static void gammaCorrection(Mat src, Mat dst, float gamma){
		int width = src.cols();
		int height = src.rows();
		byte[] data = new byte[width * height];
		src.get(0, 0, data);
		int index = 0;
		float i = 0f;
		for(int row = 0;row < height;row++){
			for(int col = 0;col < width;col++){
				index = row * width + col;
				i = data[index] & 0xff;
				i = (i + 0.5f) / 256;
				i = (float)Math.pow(i, gamma);
				i = i * 256 -0.5f;
				data[index] = (byte)i;
			}
		}
		src.copyTo(dst);
		dst.put(0, 0, data);
	}
	
	/**
	 * 图像裁剪
	 * @param mat
	 * @param pointX
	 * @param pointY
	 * @param width
	 * @param height
	 * @return
	 */
	public static Mat cutImage(Mat sourceImg, int pointX, int pointY, int width, int height){
		int imgWidth = sourceImg.cols();
		int imgHeight = sourceImg.rows();
		if(pointX + width > imgWidth || pointY + height > imgHeight){
			return null;
		}
		Rect rect = new Rect(pointX, pointY, width, height);
		Mat newImg = new Mat(sourceImg, rect);
		Mat retImg = new Mat();
		newImg.copyTo(retImg);
		return retImg;
	}
	
	/**
	 * 图片缩放
	 * @param sourceImg
	 * @param width
	 * @param height
	 * @return
	 */
	public static Mat resize(Mat sourceImg, int width, int height){
		Mat retImg = new Mat();
		Imgproc.resize(sourceImg, retImg, new Size(width, height));
		return retImg;
	}
	
	/**
	 * 翻转图像
	 * @param sourceImg
	 * @param flipCode 0代表垂直方向旋转180度； 1代表水平方向旋转180度；-1代表垂直和水平方向同时旋转
	 * @return
	 */
	public static Mat filp(Mat sourceImg, int flipCode){
		Mat retImg = new Mat();
		Core.flip(sourceImg, retImg, flipCode);
		return retImg;
	}
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		Mat mat = Imgcodecs.imread("D:\\Works\\JavaWorks\\SVM\\ImageClassify\\pos\\crop_000606a.png");
		ImageUtil.gammaCorrection(mat, mat, 1f/2.2f);
	}
	
	public static void removeHighLights(IplImage src, IplImage dst, double re){
		int height = src.height();
		int width = src.width();
		int step = src.widthStep();
		int i = 0;
		int j = 0;
		char[] srcData = new char[height * width];
		for(int x = 0;x < height;x++){
		}
	}
}
