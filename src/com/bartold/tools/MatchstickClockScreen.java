package com.bartold.tools;

import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.peer.ComponentPeer;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public final class MatchstickClockScreen {

	private MatchstickClockEngine msce;

	private Frame screenSaverFrame;
	
	private Image offscreenBuffer;
	
	private Canvas canvas;

	private MatchstickClockScreen(String config) {

		config();
	}

	private MatchstickClockScreen() {

 		screenSaverFrame = new Frame();
 		
		init();

		run();
	}

	// this is a special constructor to attache the clock to a parent window
	public MatchstickClockScreen(Integer hwid) {
		
 		screenSaverFrame = new Frame();
 		
 		System.out.println(screenSaverFrame);

 		ComponentPeer p = screenSaverFrame.getPeer();
 		
 		System.out.println(p);

		init();

		run();
	}

	public void paint(Graphics g) {

		// get the buffer graphic and draw the image there before displaying it
		// we could draw to g directly, but that will not look good
/*
		if ( msce == null || screenSaverFrame == null || canvas == null ) {
			return;
		}
*/
		// get the size of our current window (it changes)
		Dimension dim = screenSaverFrame.getSize();
		
		// create a fresh image buffer
		offscreenBuffer = screenSaverFrame.createImage(dim.width, dim.height);

		// paint the clock into the off screen buffer
		msce.clock(offscreenBuffer, dim);

		// draw the offscreen image to the screen like a normal image.
		// Since offscreen is the screen width we start at 0,0.
		g.drawImage(offscreenBuffer, 0, 0, null);

	}
	
	// this main class is runnable it also uses a keyboard listener to change options in the engine

	
	/**
	 * Initialize this executable - we still need a way to read and set options
	 */
	public void run() {
		
		// Create an offscreen image to draw on
		// by doing this everything that is drawn by bufferGraphics
		//offscreenBuffer = createImage(dim.width, dim.height);

		// need to figure out when to break out of this loop
		
		while (true) {
			// the paint routine is designed to create a new image about 30
			// times per second
			// paint image on visible canvas/panel
			paint(canvas.getGraphics());
		}
	}
	
 	/**
 	 * Initialize this screen saver
 	 */
 	public void init() {

 		// Note that there are no 'keyboard' options in the saver version - the saver would stop on any keypress
 		Properties properties = new Properties();
 		try {
 		    properties.load(new FileInputStream("matchstickclock.properties"));
 		} catch (IOException e) {
 		}
 		
		msce = new MatchstickClockEngine();

		// read in the configuration settings from a properties file
 		String[] keys = {"color", "wilds", "hires", "pause", "fixed", "obvio", "round", "count"};
 		for (int i=0;i<7;i++) {
 			String key = keys[i];

 			// the only properties we allow are 1 or 0, for true or false
 			String value = properties.getProperty(key);
 	
 			// if there's no value, don't do anything
 			// if it's an invalid value, don't bother to complain
 			// can't actually throw an exception when running a screen saver 
 			//throw new RuntimeException(name+" option must be true(1) or false(0)");
 			if (value != null && value.length() == 1 && (value.equals("0") || value.equals("1"))) {
 				boolean flag = value.equals("1");
 				if (key.equals("color")) msce.setColor(flag);
 				if (key.equals("wilds")) msce.setWilds(flag);
 				if (key.equals("hires")) msce.setHires(flag);
 				if (key.equals("pause")) msce.setPause(flag);
 				if (key.equals("fixed")) msce.setFixed(flag);
 				if (key.equals("obvio")) msce.setObvio(flag);
 				if (key.equals("round")) msce.setRound(flag);
 				if (key.equals("count")) msce.setCount(flag);
 			}
 		}
 		
 		// need to exit when the screen window is deactivated
		screenSaverFrame.addWindowListener(new WindowAdapter() {
			public void windowDeactivated(WindowEvent e) {
				//System.out.println(e);
				System.exit(0);
			}
		});
		
        screenSaverFrame.setUndecorated(true);
        screenSaverFrame.setResizable(false);
        
		// make it visible
		screenSaverFrame.setVisible(true);

        // once the dimension is set, there's no need to check it again, the screen won't change size
		// need to set up an initial canvas 
		// update dim and grid (allows for resizing) only in executable version
		// determine the physical screen size and set a single visible area
		// it's possible to check for multiple screens, but we don't do that
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		screenSaverFrame.setSize(dim.width, dim.height);
		screenSaverFrame.setBackground(Color.black);

        screenSaverFrame.validate();
        GraphicsEnvironment.getLocalGraphicsEnvironment()
                  .getDefaultScreenDevice()
                  .setFullScreenWindow(screenSaverFrame);
 		
		// add a panel to the window so we can draw to it
		canvas = new Canvas();
		screenSaverFrame.add(canvas);

 	}
     
    List<Checkbox> checkboxes = new ArrayList<Checkbox>();
	Properties properties = new Properties();

    /**
 	 * Loads the config properties, displays the dialog, then saves the new config
 	 */
 	public void config() {

		// in the case of config, we show the properties editor
 		screenSaverFrame = new Frame();

 		// Note that there are no 'keyboard' options in the saver version - the saver would stop on any keypress
 		try {
 		    properties.load(new FileInputStream("matchstickclock.properties"));
 		} catch (IOException e) {
 		}
 		
 		
 		// need to exit when the screen window is deactivated
		screenSaverFrame.addWindowListener(new WindowAdapter() {
			public void windowDeactivated(WindowEvent e) {
				//System.out.println(e);
				System.exit(0);
			}
		});
		
        screenSaverFrame.setResizable(false);
        
		// make it visible
		//screenSaverFrame.setVisible(true);

        // once the dimension is set, there's no need to check it again, the screen won't change size
		// need to set up an initial canvas 
		// update dim and grid (allows for resizing) only in executable version
		// determine the physical screen size and set a single visible area
		// it's possible to check for multiple screens, but we don't do that

        screenSaverFrame.validate();
        GraphicsEnvironment.getLocalGraphicsEnvironment()
                  .getDefaultScreenDevice()
                  .setFullScreenWindow(screenSaverFrame);
 		
		// add a panel to the window so we can draw to it
        Label la=new Label("What settings:");
        screenSaverFrame.setLayout(new GridLayout(0, 1));
        screenSaverFrame.add(la);

    	String[] keys = {"color", "wilds", "hires", "pause", "fixed", "obvio", "round", "kount"};

    	// read in the configuration settings from a properties file
 		for (int i=0;i<keys.length;i++) {
 			// the only properties we allow are 1 or 0, for true or false
 			String value = properties.getProperty(keys[i]);
 			// if there's no value, don't do anything
 			// if it's an invalid value, don't bother to complain
 			// can't actually throw an exception when running a screen saver 
 			//throw new RuntimeException(name+" option must be true(1) or false(0)");
			// assume false, unless otherwise set
			boolean flag = false;
			if (value.length() == 1) {
				flag = value.equals("1");
			}
			if (value.length() == 4) {
				flag = value.equals("true");
			}
 			Checkbox checkbox = new Checkbox(keys[i],flag);
 			screenSaverFrame.add(checkbox);
			checkboxes.add(checkbox);
 		}
 		screenSaverFrame.setVisible(true);

        screenSaverFrame.setSize(250,200);

        // wait for a save button to be pressed, and read and save the options
        Button b1 = new Button("save");
        Button b2 = new Button("exit");
        screenSaverFrame.add(b1);
        screenSaverFrame.add(b2);
        b1.addActionListener(new ActionListener(){
        	  public void actionPerformed(ActionEvent e){
        		  // save, but don't exit
        		  // get the status of each checkbox, and update
        		  Iterator<Checkbox> it = checkboxes.iterator();
        		  while(it.hasNext()) {
        			  Checkbox cb = it.next();
        			  String key = cb.getLabel();
        			  boolean flag = cb.getState();
            		  properties.setProperty(key, java.lang.String.valueOf(flag));
        		  }
        		  // now save to the file
        		  try {
        			  properties.store(new FileOutputStream("matchstickclock.properties"), null);
        		  } catch (IOException ex) {
        			  // should warn that properties were not saved
        		  }
        	  }
          });
          b2.addActionListener(new ActionListener(){
        	  public void actionPerformed(ActionEvent e){
      		  // do not save, just exit
      		  System.exit(0);
      	  }
          });
          screenSaverFrame.addWindowListener(new WindowAdapter(){
        	  public void windowClosing(WindowEvent e){
        		  // do not save, just exit
        		  System.exit(0);
        	  }
        });

 	}
     
	public static void main(String[] args) {

		// this is just to look for an integer argument
		Pattern p = Pattern.compile( "([0-9]+)");
		int hwnd = 0;
		Character mode = 'c';
		
		for (int i = 0; i < args.length; i++) {
			// could add some command line arguments here
			
			// could add some command line arguments here
			/*
				ScreenSaver           - Show the Settings dialog box.
				ScreenSaver /c        - Show the Settings dialog box, modal to the foreground window.
				ScreenSaver /p <HWND> - Preview Screen Saver as child of window <HWND>.
				ScreenSaver /s        - Run the Screen Saver.
			 */
			if (args[i].equals("/c")) {
				// settings?
				// show the ini file in a text box that can be edited - using awt?
				//new EditIni();
				mode = 'c';
			} else if  (args[i].equals("/s")) {
				// run saver
				mode = 's';
			} else if  (args[i].equals("/p") && args.length > i+1 && p.matcher(args[i+1]).matches()) {
				// preview saver
				mode = 'p';
				hwnd = Integer.valueOf(args[i+1]);
				i++;
			}
			
		}

 		if (mode.equals('c')) {
	 		new MatchstickClockScreen("config");
		} else if (mode.equals('p')) { 
			new MatchstickClockScreen(hwnd);
		} else {
			new MatchstickClockScreen();
		}
		
	}
}
