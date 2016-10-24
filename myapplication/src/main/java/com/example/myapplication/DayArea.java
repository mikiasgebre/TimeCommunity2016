package com.example.myapplication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Panel;

public class DayArea extends TimelineLayout{
	
	//private final AbsoluteLayout absLo;
	
	public DayArea(LocalTime ft, LocalTime exclLt){
		super(ft, exclLt);
	}
	
	/*
	 * Pre-condition: all courseSessions are in the time of day which is displayed.
	 */
	public void addCourseSessions(CourseSession[] courseSessions){
		this.removeAllComponents();
		this.fillWithEmptyPanels();
		ScheduleEventBox seb;
		DayArea da;
		
		int sebBeginningPos;
		int sebHeight;
		for(CourseSession cs : courseSessions){
			da = this;
			sebBeginningPos = getVerticalPositionByTime(cs.getTime().toLocalTime());
			sebHeight = minutesToHeight(cs.getDurationMinutes());
			seb = new ScheduleEventBox(cs.getCourse().getName());
			seb.setHeight(sebHeight + "px");
			seb.setWidth(da.getWidth() + "px");
			da.addComponent(seb, "top: " + sebBeginningPos + "px; left: 0px");
			//p("adding ScheduleEventComponent to top position " + sebBeginningPos + "to da");
		}
	}
	
}
