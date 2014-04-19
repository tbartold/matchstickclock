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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Component;

import org.jdesktop.jdic.screensaver.SimpleScreensaver;
import org.jdesktop.jdic.screensaver.ScreensaverSettings;

/**
 * MatchstickClockSaver
 * 
 * @author Thom Bartold
 * @version 2.6
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
 */
public class MatchstickClockSaver extends SimpleScreensaver {

	private static MatchstickClockEngine msce;

	private Dimension dim;
	Image offscreenBuffer;

	public void paint(Graphics g) {

		// get the buffer graphic and draw the image there before displaying it
		// we could draw to g directly, but that will not look good
		if (null == msce || null == offscreenBuffer)
			return;

		// get the size of our current window (it changes)


		// create an image buffer


		// paint the clock into the off screen buffer
		msce.clock(offscreenBuffer, dim);

		// draw the offscreen image to the screen like a normal image.
		// Since offscreen is the screen width we start at 0,0.
		g.drawImage(offscreenBuffer, 0, 0, null);

	}
	
	/**
	 * Initialize this screen saver
	 */
	public void init() {

		msce = new MatchstickClockEngine();

        // once the dimension is set, there's no need to check it again, the screen won't change size
		Component m = getContext().getComponent();
		dim = m.getSize();
		offscreenBuffer = m.createImage(dim.width, dim.height);
		m.setVisible(true);

		// Note that there are no 'keyboard' options in the saver version - the saver would stop on any keypress
		
		// read in the configuration settings
		ScreensaverSettings settings = getContext().getSettings();
		String[] keys = {"color", "wilds", "hires", "pause", "fixed", "obvio", "round", "count"};
		for (int i=0;i<7;i++) {
			String key = keys[i];

			// the only properties we allow are 1 or 0, for true or false
			String value = settings.getProperty(key);
	
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
	}

}