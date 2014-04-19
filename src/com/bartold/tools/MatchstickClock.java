/*
GNU General Public License

MatchstickClock - A Meta Clock which uses multiple clock hands to 'spell' out numbers
Copyright (C) 2009  Thom Bartold

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.bartold.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Canvas;
import java.awt.Frame;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * MatchstickClock
 * 
 * @author Thom Bartold
 * @version 2.7
 * VERSION HISTORY 
 * 0.0 (11/10/2009) - initial creation 
 * 0.1 (11/11/2009) - added smoother hand rotation 
 * 0.2 (11/12/2009) - established screen saver version 
 * 0.3 (11/13/2009) - made it colorful 
 * 0.4 (11/14/2009) - slowed down the flicker rate 
 * 1.0 (11/21/2009) - added obvious option 
 * 2.0 (11/28/2009) - added round style clock as default option 
 * 2.1 (12/04/2009) - implemented double buffering in saver version
 * 2.2 (12/05/2009) - added end of time
 * 2.3 (12/06/2009) - allow resizing
 * 2.4 (05/07/2010) - added key press listener to change options
 * 2.5 (03/04/2011) - modified year to be 4 digits, each with a ring
 * 2.6 (03/05/2011) - separated Engine from control - saver version uses same engine
 * 2.7 (03/16/2011) - corrected year parsing
 */
public class MatchstickClock extends Frame implements Runnable {

	/**
	 * on a 1280 x 1024 screen round clock hand size will be 16 pixels
	 * on a 1600 x 1200 screen round clock hand size will be 20 pixels
	 */
	private static final long serialVersionUID = 1L;

	private static MatchstickClockEngine msce;

	public void paint(Graphics g) {

		// get the buffer graphic and draw the image there before displaying it
		// we could draw to g directly, but that will not look good
		if (null == msce)
			return;

		// get the size of our current window (it changes)
		Dimension dim = getSize();

		// create an image buffer
		Image offscreenBuffer = this.createImage(dim.width, dim.height);

		// paint the clock into the off screen buffer
		msce.clock(offscreenBuffer, dim);

		// draw the offscreen image to the screen like a normal image.
		// Since offscreen is the screen width we start at 0,0.
		g.drawImage(offscreenBuffer, 0, 0, null);

	}
	
	// this main class is runnable it also uses a keyboard listener to change options in the engine
	private MatchstickClock() {
		super("MatchstickClock");

		msce = new MatchstickClockEngine();
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});

	    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor( new KeyEventPostProcessor() {
	    	public boolean postProcessKeyEvent(KeyEvent e) {
	    		if (e.getID() == KeyEvent.KEY_TYPED) {
	    			char c = e.getKeyChar();
	    			if (c == 'c') {        msce.toggleColor();
	    			} else if (c == 'w') { msce.toggleWilds();
	    			} else if (c == 'h') { msce.toggleHires();
	    			} else if (c == 'p') { msce.togglePause();
	    			} else if (c == 'o') { msce.toggleObvio();
	    			} else if (c == 'r') { msce.toggleRound();
	    			} else if (c == 'f') { msce.toggleFixed();
	    			} else if (c == '0') { msce.toggleCount();
	    			}
	    		}
	    		return true;
		   	}
	    });
		
		run();
	}
	
	/**
	 * Initialize this executable - we still need a way to read and set options
	 */
	public void run() {
		
		// need to set up an initial canvas 
		// update dim and grid (allows for resizing) only in executable version
		// determine the physical screen size and set a single visible area
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(dim.width, dim.height);
		this.setBackground(Color.black);

		// add a panel to the window so we can draw to it
		// make it visible
		Canvas canvas = new Canvas();
		this.setVisible(true);
		this.add(canvas);

		// Create an offscreen image to draw on
		// by doing this everything that is drawn by bufferGraphics
		//offscreenBuffer = createImage(dim.width, dim.height);

		while (true) {
			// the paint routine is designed to create a new image about 30
			// times per second
			// paint image on visible canvas/panel
			paint(canvas.getGraphics());
		}
	}

	public static void main(String[] args) {
		for (int i = 0; i < args.length; i++) {
			// could add some command line arguments here
		}
		new MatchstickClock();
	}
}
