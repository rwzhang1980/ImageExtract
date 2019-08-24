package com.rwzhang.imageExtract;

import org.opencv.core.Core;

public class Main {

	public static void main(String[] args) {
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			Extract2 e = new Extract2();
			e.extract("D:\\Works\\JavaWorks\\SVM\\ImageExtract\\res\\18.jpg", "");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
