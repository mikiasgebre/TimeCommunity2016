package com.example.myapplication;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Panel;

public class TimelineLayout extends AbsoluteLayout{
	
	protected LocalTime firstTime;
	protected LocalTime exclLastTime;
	
	public TimelineLayout(LocalTime ft, LocalTime exclLt){
		this.firstTime = ft;
		this.exclLastTime = exclLt;
	}

	protected void fillWithEmptyPanels(){
		TimelineLayout da = this;
		int vPos;
		Panel p;
		int hourHeight = minutesToHeight(60);
		for(LocalTime t = this.firstTime; t.isBefore(getInclLastMinuteOfDay());
				t = t.plusHours(1)){
			vPos = this.getVerticalPositionByTime(t);
			p = new Panel();
			p.setHeight(hourHeight + "px");
			p.setWidth(da.getWidth() + "px");
			da.addComponent(p, "top: " + vPos);
			//in case that t never exceeds lastTimeOfDay because lastTimeOfDay is the last possible
			//time of a day, check if t has gone past midnight
			if(t.plusHours(1).isBefore(t)) break;
		}
	}
	
	protected int getVerticalPositionByTime(LocalTime dt){
		int minutes = dt.getHour() * 60 + dt.getMinute();
		int firstTimeMinutes = firstTime.getHour() * 60 + firstTime.getMinute();
		int vPos = minutesToHeight(minutes - firstTimeMinutes);
		if(vPos < 0) p("Warning: getVerticalPositionByTime returns " + vPos);
		return vPos;
	}
	
	protected int minutesToHeight(int minutes){
		TimelineLayout da = this;
		assert firstTime.isAfter(exclLastTime) ||
				firstTime.equals(exclLastTime);
		//TOIMIIKO UUDELLA VIIMEISEN AJAN MÄÄRITTELYLLÄ?
		int totalMinutes = getMinutesDifference(this.firstTime, this.exclLastTime);
		//p("minutes difference is " + totalMinutes);
		double daHeight = (double) da.getHeight();
		double heightOfMinute = (1.0 / (double)totalMinutes) * daHeight;
		int height = (int) (heightOfMinute * (double)minutes);
		//p("saatiin " + minutes + "korkeus on " + height);
		return height;
	}
	
	protected int getMinutesDifference(LocalDateTime first, LocalDateTime last){
		assert last.isAfter(first) || last.equals(first);
		assert last.getDayOfYear() == first.getDayOfYear();
		return (last.getHour() * 60 + last.getMinute()) -
		(first.getHour() * 60 + first.getMinute());
	}
	
	protected int getMinutesDifference(LocalTime first, LocalTime last){
		assert last.isAfter(first) || last.equals(LocalTime.MIDNIGHT);
		int diff;
		if(last.equals(LocalTime.MIDNIGHT)){
			diff = 24 * 60;
		}else{
			diff = (last.getHour() * 60 + last.getMinute()) -
			(first.getHour() * 60 + first.getMinute());
		}
		//p("got " + first.toString() + " and " + last.toString() + ", gave away " + diff);
		return diff;
	}
	
	protected LocalTime getInclLastMinuteOfDay(){
		assert areFirstAndLastTimeOk();
		return this.exclLastTime.minusMinutes(1);
	}
	
	protected boolean areFirstAndLastTimeOk(){
		if(firstTime.getMinute() == 0 || exclLastTime.getMinute() == 0){
			return false;
		}
		boolean equal = !this.firstTime.equals(this.exclLastTime);
		boolean bothMidnight = this.firstTime.equals(LocalTime.MIDNIGHT) &&
				this.exclLastTime.equals(LocalTime.MIDNIGHT);
		boolean firstIsBefore = this.firstTime.isBefore(this.exclLastTime);
		return firstIsBefore || (equal && bothMidnight);
	}
	
	private void p(String s){
		System.out.println(s);
	}
}
