package com.example.myapplication;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

//KORJATTAVA TÄMÄN LUOKAN ONGELMAT
//DUMMY-SIVUT LUOTAVA JA NAVIGAATIO NIILLE TÄSTÄ SIVUSTA
//PITÄISIKÖ VARSINAINEN KALENTERIBOKSI EROTTAA OMAKSI KOMPONENTIKSEEN?

//Class invariant: all dayAreas are of same height. &&
//!firstTimeOfDay.equals(exclLastTimeOfDay) unless both are midnight. &&
//firstTimeOfDay.minutes == lastTimeOfDay.minutes == 0

public class ScheduleComponent extends CustomComponent{
	
	private Course[] courses;
	private LocalDate displayedWeek;
	
	private final int firstDataColumn = 1;
	private final int firstDataRow = 1;
	private LocalTime firstTimeOfDay = LocalTime.of(8, 0);
	//private LocalTime lastHourOfDay = LocalTime.of(23, 0);
	private LocalTime exclLastTimeOfDay = LocalTime.of(0, 0); //Exclusive
	private final GridLayout eventsFrame;
	//private final AbsoluteLayout[] dayAreas;
	private final DayArea[] dayAreas;
	//private final AbsoluteLayout hoursLayout;
	private final HoursColumn hoursColumn;
	private final WeekNavigationComponent weekNavigationComponent;
	private final VerticalLayout mainLayout;
	private final int weekDaysCount = 7;

	public ScheduleComponent(Course[] courses){
		this.courses = courses;
		this.mainLayout = new VerticalLayout();
        this.eventsFrame = new GridLayout(8, 2); //(8, 16);
        this.displayedWeek = getFirstDateOfWeek(LocalDate.now()); //LocalDateTime.now();
        this.weekNavigationComponent =
        		new WeekNavigationComponent(displayedWeek.atStartOfDay(), this);
        eventsFrame.setSizeFull();
        mainLayout.setSizeFull();
        weekNavigationComponent.setSizeFull();
        //setSizeFull();
        eventsFrame.setWidth("800px");
        eventsFrame.setHeight("750px");
        this.dayAreas = createDayAreas();
        addDayAreas();
        //this.hoursLayout = createHoursColumn();
        this.hoursColumn = createHoursColumn();
		eventsFrame.addComponent(hoursColumn, 0, firstDataRow);
        //weekNavigationComponent.setWidth("750px"); EI TOIMI
        //weekNavigationComponent.setHeight("80px"); EI TOIMI
		updateEventsFrame(displayedWeek, this.courses);
		mainLayout.addComponent(weekNavigationComponent);
		mainLayout.addComponent(eventsFrame);
		setCompositionRoot(mainLayout);
    }
	
	public void onPreviousWeekButtonClick(Button.ClickEvent e){
		LocalDate previousWeek = this.displayedWeek.plusWeeks(-1);
		updateTime(previousWeek);
	}
	
	public void onNextWeekButtonClick(Button.ClickEvent e){
		LocalDate nextWeek = this.displayedWeek.plusWeeks(1);
		updateTime(nextWeek);
	}
	
	private DayArea[] createDayAreas(){
		DayArea[] layouts = new DayArea[weekDaysCount];
		DayArea cl;
		double width;
		for(int i = 0; i < layouts.length; i++){
			cl = new DayArea(this.firstTimeOfDay, this.exclLastTimeOfDay);
			cl.setHeight((int) eventsFrame.getHeight() + "px");
			width = (double) eventsFrame.getWidth() / (double) layouts.length;
			cl.setWidth((int) width + "px");
			layouts[i] = cl;
		}
		return layouts;
	}
	
	private void addDayAreas(){
		int daIndex = 0;
		for(int i = firstDataColumn; i < eventsFrame.getColumns(); i++){
			eventsFrame.addComponent(dayAreas[daIndex], i, firstDataRow);
			daIndex++;
		}
	}
	
	private void updateTime(LocalDate weekStartDate){
		this.displayedWeek = weekStartDate;
		updateEventsFrame(weekStartDate, courses);
		weekNavigationComponent.update(weekStartDate.atStartOfDay());
	}
	
	/*
	 * MISTÄ TÄTÄ KUTSUTAAN?
	 */
	private void updateCourses(Course[] courses){
		//TODO
	}
	
	/*
	 * OVERLAPPAAMISTA EI OTETA VIELÄ HUOMIOON
	 * ENTÄ JOS ALKAA ENNEN ENSIMMÄISTÄ KELLONAIKAA?
	 * ENTÄ JOS ENSIMMÄINEN KELLONAIKA MUUTTUU?
	 * VOIKO MIKÄ TAHANSA PÄIVÄ OLLA VIIKON ENSIMMÄINEN? PITÄISIKÖ VOIDA?
	 * Pre-condition: none of the CourseSessions in courses must span from one day to another.
	 */
	private void updateEventsFrame(LocalDate weekStartDate, Course[] courses){
		GridLayout ef = eventsFrame;
		//Remove some existing components
		for(AbsoluteLayout dayArea : this.dayAreas){
			dayArea.removeAllComponents();
		}
		for(int c = firstDataColumn; c < eventsFrame.getColumns(); c++){
			eventsFrame.removeComponent(c, 0);
		}
		//fill with Panels to show borders
		fillWithEmptyPanels();
		//ef.removeAllComponents();
		LocalDate[] weekDays = new LocalDate[weekDaysCount];
		for(int i = 0; i < weekDays.length; i++){
			weekDays[i] = weekStartDate.plusDays(i);
		}
		//Add column titles
		int weekDayIndex = 0;
		for(int c = firstDataColumn; c < ef.getColumns(); c++){
			ef.addComponent(new Label(weekDays[weekDayIndex].toString()), c, 0);
			weekDayIndex++;
		}
		//Add row titles
		updateHoursColumn();
		//Add course sessions
		LocalDateTime firstDtOfWeek = getFirstDtOfWeek();
		LocalDateTime lastDtOfWeek = getExclLastDtOfWeek();
		addCourseSessions(weekStartDate, courses, firstDtOfWeek, lastDtOfWeek);
	}
	
	private HoursColumn createHoursColumn(){
		//AbsoluteLayout hoursLayout = new AbsoluteLayout();
		HoursColumn hc = new HoursColumn(this.firstTimeOfDay, this.exclLastTimeOfDay);
		hc.setHeight((int) eventsFrame.getHeight() + "px");
		hc.setWidth("100px");
		return hc;
	}
	
	private void updateHoursColumn(){
		this.hoursColumn.update(this.firstTimeOfDay, this.exclLastTimeOfDay);
	}
	
	/*
	 * Adds a panel for each displayed hour.
	 */
	private void fillWithEmptyPanels(){
		for(DayArea da : dayAreas){
			da.fillWithEmptyPanels();
		}
	}
	
	/*
	 * Creates and adds ScheduleEventBoxes to the day areas.
	 */
	private void addCourseSessions(LocalDate weekStartDate, Course[] courses,
			LocalDateTime firstDtOfWeek, LocalDateTime lastDtOfWeek){
		CourseSession[] courseSessions = getLecturesInWeek(weekStartDate, courses);
		DayArea da;
		LocalDateTime csStart;
		HashMap<DayArea, ArrayList<CourseSession>> das = new HashMap<>();
		
		for(CourseSession cs : courseSessions){
			csStart = cs.getTime();
			if(!shouldBeInDisplayedArea(csStart)){
				continue;
			}
			da = getDayAreaByDateTime(csStart, firstDtOfWeek, lastDtOfWeek);
			if(!das.containsKey(da)){
				das.put(da, new ArrayList<CourseSession>());
			}
			das.get(da).add(cs);
			//p("adding ScheduleEventComponent to top position " + sebBeginningPos + "to da");
		}
		for(Map.Entry<DayArea, ArrayList<CourseSession>> ent : das.entrySet()){
			DayArea tempDa = ent.getKey();
			tempDa.addCourseSessions(das.get(tempDa).toArray(new CourseSession[0]));
		}
	}
	
	private boolean shouldBeInDisplayedArea(LocalDateTime target){
		LocalDateTime firstDateTime = getFirstDtOfWeek();
		LocalDateTime lastDateTime = getExclLastDtOfWeek();
		boolean lateEnough = target.isAfter(firstDateTime) ||
				target.isEqual(firstDateTime);
		boolean earlyEnough = target.isBefore(lastDateTime);
		return lateEnough && earlyEnough;
	}
	
	private LocalDateTime getExclLastDtOfWeek(){
		LocalDateTime lastDateTime;
		if(!this.exclLastTimeOfDay.equals(LocalTime.MIDNIGHT)){
			lastDateTime = this.displayedWeek.plusDays(6).atTime(this.exclLastTimeOfDay);
		}else{
			lastDateTime = this.displayedWeek.plusDays(7).atTime(this.exclLastTimeOfDay);
		}
		return lastDateTime;
	}
	
	private LocalDateTime getFirstDtOfWeek(){
		LocalDateTime firstDateTime = this.displayedWeek.atTime(this.firstTimeOfDay);
		return firstDateTime;
	}
	
	private DayArea getDayAreaByDateTime(LocalDateTime dateTime, LocalDateTime firstDateTime,
			LocalDateTime lastDateTime){
		int col = getColumnByDateTime(dateTime, firstDateTime, lastDateTime);
		DayArea da = (DayArea) eventsFrame.getComponent(col, firstDataRow);
		return da;
	}
	
	/*
	 * Return value: a column of the eventsFrame GridLayout.
	 */
	private int getColumnByDateTime(LocalDateTime dateTime, LocalDateTime firstDateTime,
			LocalDateTime lastDateTime){
		LocalDate date = dateTime.toLocalDate();
		LocalDate firstDate = firstDateTime.toLocalDate();
		LocalDate lastDate = lastDateTime.toLocalDate();
		int col = firstDataColumn;
		for(LocalDate ld = firstDate; ld.compareTo(lastDate) <= 0; ld = ld.plusDays(1)){
			if(ld.equals(date)){
				return col;
			}
			col++;
		}
		p("Varoitus: getColumnByDateTime palauttaa -1");
		return -1;
	}
	
	private CourseSession[] getLecturesInWeek(LocalDate weekStartDate, Course[] courses){
		LocalDateTime weekStartTime = weekStartDate.atTime(0, 0);
		LocalDateTime weekEndTime = weekStartTime.plusDays(weekDaysCount);
		ArrayList<CourseSession> courseSessions = new ArrayList<CourseSession>();
		Course course;
		ArrayList<CourseSession> courseSessionsOfOneCourse;
		for(int i = 0; i < courses.length; i++){
			course = courses[i];
			courseSessionsOfOneCourse = course.getCourseSessionsInInterval(weekStartTime,
					weekEndTime);
			courseSessions.addAll(courseSessionsOfOneCourse);
		}
		return courseSessions.toArray(new CourseSession[0]);
	}
	
	private LocalDate getFirstDateOfWeek(LocalDate lt){
		return lt.with(DayOfWeek.MONDAY);
	}
	
	private void p(String s){
		System.out.println(s);
	}
	
}
