package com.example.myapplication;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

public class ScheduleEventBox extends CustomComponent{
	public static final String CLASSNAME = "schedule-event-box";
	private final Panel outermost;
	private final Label label;
	//private final CourseSession courseSession;
	//private int layoutHeight;
	
	public ScheduleEventBox(String text){
		setPrimaryStyleName(CLASSNAME);
		outermost = new Panel();
		label = new Label(text);
		outermost.setContent(label);
		outermost.setSizeFull();
		label.setSizeFull();
		setCompositionRoot(outermost);
		//this.courseSession = courseSession;
	}
	
//	private int determineHeight(){
//		return 0;
//	}
//	
//	private int determineVerticalPosition(){
//		return 0;
//	}
}
