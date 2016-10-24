package com.example.myapplication;

import java.time.LocalTime;

import com.vaadin.ui.Label;

public class HoursColumn extends TimelineLayout{

	public HoursColumn(LocalTime ft, LocalTime exclLt){
		super(ft, exclLt);
	}
	
	public void update(LocalTime newFirstTime, LocalTime newExclLastTime){
		this.firstTime = newFirstTime;
		this.exclLastTime = newExclLastTime;
		this.removeAllComponents();
		int pos;
		Label hourLabel;
		for(LocalTime t = firstTime; t.isBefore(getInclLastMinuteOfDay()); t = t.plusHours(1)){
			pos = getVerticalPositionByTime(t);
			hourLabel = new Label(t.getHour() + " - " + t.plusHours(1));
			this.addComponent(hourLabel, "top: " + pos + "px");
			//in case that t never exceeds lastTimeOfDay because lastTimeOfDay is the last possible
			//time of a day, check if t has gone past midnight //THIS IS BS THOUGH
			if(t.plusHours(1).isBefore(t)) break;
		}
	}
}
