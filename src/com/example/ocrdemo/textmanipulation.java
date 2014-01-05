package com.example.ocrdemo;

import android.content.Intent;
import android.net.Uri;

public class textmanipulation {
	String manipulation(String text){
		
		
		if(text.contains("Error") || text.contains("ERROR") ){
		    //Do something. 
			return "Error is detected";
		}
		
		
		
		
		
		return text;
	}
}
