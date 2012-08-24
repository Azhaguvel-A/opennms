package org.opennms.features.topology.netutils.internal;

import java.net.MalformedURLException;
import java.net.URL;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The EventsAlarmsWindow class constructs a custom Window component which
 * contains an embedded browser for both the Events page and Alarm page of the selected node.
 * Tabs are used to switch back and forth between the two.
 * @author Leonardo Bell
 * @author Philip Grenon
 * @version 1.0
 */
@SuppressWarnings("serial")
public class EventsAlarmsWindow extends Window {

	private final double sizePercentage = 0.80; // Window size ratio to the main window
	private final int widthCushion = 50; //Border cushion for width of window;
	private final int heightCushion = 110; //Border cushion for height of window
	private Embedded eventsBrowser = null; //Browser component which is directed at the events page
	private Embedded alarmsBrowser = null; //Browser component which is directed at the alarms page
	private final String noLabel = "no such label"; //Label given to vertexes that have no real label.

	/**
	 * The EventsAlarmsWindow method constructs a sub-window instance which can be added to a
	 * main window. The sub-window contains two embedded browsers which are directed at the Events
	 * and Alarms page of the selected node
	 * @param node Selected node
	 * @param width Width of main window
	 * @param height Height of main window
	 * @throws MalformedURLException
	 */
	public EventsAlarmsWindow(final Node node, final URL eventsURL, final URL alarmsURL) throws MalformedURLException {
		eventsBrowser = new Embedded("", new ExternalResource(eventsURL));
		alarmsBrowser = new Embedded("", new ExternalResource(alarmsURL));
		
		String label = node == null? "" : node.getLabel();
		/*Sets up window settings*/
		if (label == null || label.equals("") || label.equalsIgnoreCase(noLabel)) {
			label = "";
		} else {
		    label = " - " + label;
		}
		
		setCaption("Events & Alarms" + label);
		setImmediate(true);
		setResizable(false);
		
		/*Adds the two browsers to separate tabs in a tabsheet layout*/
		TabSheet tabsheet = new TabSheet();
		tabsheet.addTab(eventsBrowser, "Events");
		tabsheet.addTab(alarmsBrowser, "Alarms");
		
		/*Adds tabsheet component to the main layout of the sub-window*/
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(tabsheet);
		
		addComponent(layout);
	}
	
	@Override
	public void attach() {
		super.attach();
		
		int width = (int)getApplication().getMainWindow().getWidth();
    	int height = (int)getApplication().getMainWindow().getHeight();
    	
		/*Sets the browser and window sizes based on the main window*/
		int browserWidth = (int)(sizePercentage * width), browserHeight = (int)(sizePercentage * height);
		int windowWidth = browserWidth + widthCushion, windowHeight = browserHeight + heightCushion;
		setWidth("" + windowWidth + "px");
		setHeight("" + windowHeight + "px");
		setPositionX((width - windowWidth)/2);
		setPositionY((height - windowHeight)/2);
		
		/*Changes the size of the browsers to fit within the sub-window*/
		alarmsBrowser.setType(Embedded.TYPE_BROWSER);
		alarmsBrowser.setWidth("" + browserWidth + "px");
		alarmsBrowser.setHeight("" + browserHeight + "px");
		eventsBrowser.setType(Embedded.TYPE_BROWSER);
		eventsBrowser.setWidth("" + browserWidth + "px");
		eventsBrowser.setHeight("" + browserHeight + "px");
	}
	
}
