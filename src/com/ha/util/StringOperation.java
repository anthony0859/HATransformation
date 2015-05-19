package com.ha.util;

// Operations on some complicated string results.
public class StringOperation {
	//Get subString before dot from target string like "xxx" from "xxx.yyy".
	public String getStrBeforeDot(String str) {
		if (str==null || str.length()==0)
			return null;
		StringBuilder beforeStr = new StringBuilder();
		for (int i=0; i<str.length(); i++) {
			if (str.charAt(i) != '.') 
				beforeStr.append(str.charAt(i));
			 else 
				break;
		}
		return beforeStr.toString();
	}
	
	//Get subString after dot from target string like "yyy" from "xxx.yyy".
	public String getStrAfterDot(String str) {
		if (str==null || str.length()==0)
			return null;
		StringBuilder afterStr = new StringBuilder();	
		int k=0;	
		for (int i=0; i<str.length(); i++) {	
			if (str.charAt(i)=='.') {	
				k=i;	
				break;	
			}	
		} 
		for (int i=k+1; i<str.length(); i++) {
			afterStr.append(str.charAt(i));
		}
		return afterStr.toString();
	}
	
	public String rmLastNum(String str) {
		if (str==null || str.length()==0)
			return null;
		if (str.charAt(str.length()-1) >= '0' && str.charAt(str.length()-1) <= '9') {
			StringBuilder newStr = new StringBuilder();
			for (int i=0; i<str.length()-1; i++)
				newStr.append(str.charAt(i));
			return newStr.toString();
		} else {
			return str;
		} 
	}
}

