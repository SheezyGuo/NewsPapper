package com.uestc.spider.www;

import java.lang.Thread.UncaughtExceptionHandler;

public class ThreadUncaughtExceptionHandler implements UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		// TODO Auto-generated method stub
		System.out.println("Exception occurred in Thread:" + t.getName()
				+ "\nMessage:" + e.getMessage());
		e.printStackTrace();
	}

}