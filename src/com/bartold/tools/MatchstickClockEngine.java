package com.bartold.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;

import java.util.Calendar;
import java.util.Random;

/**
 * MatchstickClockEngine
 * 
 * The engine is used strictly to paint the clock
 * Any sort of keyboard, or saver control should be exernal to the engine
 *
 * @author Thom Bartold
 * @version 2.6
 * VERSION HISTORY 
 * 2.6 (03/05/2011) - separated Engine from control - saver version uses same engine
 */
public class MatchstickClockEngine {

	// constructor
	public MatchstickClockEngine() {
	}

	// the toggle methods are used to avoid having to get and set
	public void toggleHires() { hires = !hires; }
	public void toggleColor() { color = !color; }
	public void toggleWilds() { wilds = !wilds; }
	public void togglePause() { pause = !pause; }
	public void toggleObvio() { obvio = !obvio; }
	public void toggleRound() { round = !round; }
	public void toggleFixed() { fixed = !fixed; }
	public void toggleCount() { count = !count; }

	public void setHires(boolean flag) { hires = flag; }
	public void setColor(boolean flag) { color = flag; }
	public void setWilds(boolean flag) { wilds = flag; }
	public void setPause(boolean flag) { pause = flag; }
	public void setObvio(boolean flag) { obvio = flag; }
	public void setRound(boolean flag) { round = flag; }
	public void setFixed(boolean flag) { fixed = flag; }
	public void setCount(boolean flag) { count = flag; }	
	
	// the first thing to do when painting is to select the right form
	public void clock(Image offscreenBuffer, Dimension dim) {

		// before constructing the clock, insert the optional pause
		if (pause) {
			try {
				Thread.sleep(60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Graphics offscreenGraphic = offscreenBuffer.getGraphics();
		
		// grid will be recomputed, in case the size changed
		this.dim = dim;
		if (round) {
			roundClock(offscreenGraphic);
		} else {
			flatClock(offscreenGraphic);
		}
		//return offscreenGraphic;
	}

	
	private Dimension dim;
	private int grid;
	
	private Random random = new Random();

	// the default values are compiled in
	private boolean hires = true;
	private boolean color = true;
	// wilds only applies to color, it's either random of fixed color
	private boolean wilds = false;
	private boolean pause = true;
	private boolean obvio = true;
	private boolean round = true;
	// only applies to round
	private boolean fixed = false;
	private boolean count = false;

	private void paintBoom(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, dim.width, dim.height);
		// paint a thousand random hands on the screen
		
		int cx = dim.width / grid / 4 - 1;
		int cy = dim.height / grid / 4 - 1;
		int R = Math.min(cx, cy)-2;
		
		for (int i = 0; i < 1000; i++) {

			// random direction inside the bounding circle
			double r = random.nextFloat()*2*Math.PI;
			// random distance from center
			double w = random.nextFloat()*R+2;
			// random x y location inside the bounding circle
			double x = (cx - (w - 1) * Math.sin(r)) * 2;
			double y = (cy + (w - 1) * Math.cos(r)) * 2;
			// random hand direction
			double d = random.nextFloat() * 360;
			paintHand(g, x, y, 0, 0, d, 0, 0, 0);
		}
	}

	private void paintDead(Graphics g) {

		g.setColor(Color.black);
		g.fillRect(0, 0, dim.width, dim.height);

		// Draw text in the center of the screen
		// make it big and bold
		Font font = new Font("Times", Font.BOLD, 128);
		g.setFont(font);
		FontMetrics fontMetrics = g.getFontMetrics();

		Color randomColor = Color.getHSBColor(random.nextFloat(), 1.0F, 1.0F);
		g.setColor(randomColor);
		String string = "No More Time";
		int x = (dim.width - fontMetrics.stringWidth(string)) / 2;
		int y = dim.height / 2 - fontMetrics.getAscent();
		g.drawString(string, x, y);

		randomColor = Color.getHSBColor(random.nextFloat(), 1.0F, 1.0F);
		g.setColor(randomColor);
		string = "World Over";
		x = (dim.width - fontMetrics.stringWidth(string)) / 2;
		y = dim.height / 2;
		g.drawString(string, x, y);
	}

	private void flatClock(Graphics g) {
		// the locations of digits on the screen assumes about 40 grid locations
		// across
		// and the hand length is the same, about 1/40 times the width of the
		// screen
		grid = Math.min(dim.width, dim.height) / 40;

		// number of degrees offset should be based on the current time
		Calendar c = Calendar.getInstance();

		// Wipe off everything that has been drawn before
		// black background
		g.setColor(Color.black);
		g.fillRect(0, 0, dim.width, dim.height);

		// number of degrees offset should be based on the time in hours
		// complete circuit every 12 hours
		int deg_hour = 180 + c.get(Calendar.HOUR) * 30;
		if (hires) {
			// make it finer resolution than every 36 degrees
			deg_hour += (int) Math.round(c.get(Calendar.MINUTE) * 30 / 60.0);
		}

		// the tens digit of minutes - complete circuit in 60 minutes
		int deg_tens = 180 + (c.get(Calendar.MINUTE) - (c.get(Calendar.MINUTE) % 10)) * 6;
		if (hires) {
			// make it finer resolution than every 36 degrees
			deg_tens += (c.get(Calendar.MINUTE) % 10) * 6;
			deg_tens += (int) Math.round(c.get(Calendar.SECOND) * 6 / 60.0);
		}

		// the ones digit of minutes - complete circuit in 10 minutes
		int deg_min = 180 + (c.get(Calendar.MINUTE) % 10) * 36;
		if (hires) {
			// make it finer resolution than every 36 degrees
			deg_min += (int) Math.round(c.get(Calendar.SECOND) * 36 / 60.0);
		}

		// the seconds - complete circuit in 60 seconds
		int deg_sec = 180 + c.get(Calendar.SECOND) * 6;
		if (hires) {
			// make it finer resolution than every 6 degrees
			deg_sec += (int) Math
					.round(c.get(Calendar.MILLISECOND) * 6 / 1000.0);
		}

		// for the following hands, the first 2 coords are the center of the
		// hand, the next is the degree of the direction it faces at time zero

		// so a '1' will start out skewed by 30 degrees so
		// that 1 hour after zero it will be correctly lined up

		int cx = dim.width / grid / 4 - 1;
		int cy = dim.height / grid / 4 - 1;
		int w = Math.min(cx, cy);
		
		// first row is the hour
		for (int i = 1; i < 10; i++) {
			int x = i * 2 + 2*cx - w - w/2;
			int y = 2*cy-w;
			paintX(g, i, x, y, 30, i, deg_hour);
		}
		for (int i = 10; i <= 12; i++) {
			int x = i * 2 + 2*cx - w - w/2;
			int y = 2*cy-w;
			paintXX(g, i, x, y, 30, i, deg_hour);
		}

		// next the 10s digit of minutes
		for (int i = 0; i < 6; i++) {
			int x = i * 2 + 2*cx - w - w/2;
			int y = 2*cy;
			paintX(g, i, x, y, 60, i, deg_tens);
		}

		// next the 1s digit of minutes
		for (int i = 0; i < 10; i++) {
			int x = i * 2 + 2*cx;
			int y = 2*cy;
			paintX(g, i, x, y, 36, i, deg_min);
		}

		// the seconds in multiples of 5 is the last row
		for (int i = 0; i < 12; i++) {
			int x = i * 4 + 2*cx - 2*w - w/4;
			int y = 2*cy+w;
			paintXX(g, i * 5, x, y, 30, i, deg_sec);
		}

	}

	// these need to be global so they are retained on subsequent paint calls
	private Calendar t0;
	private Calendar t1;
	
	private void roundClock(Graphics g) {
		// the locations of digits on the screen assumes about 80 grid locations
		// across
		// and the hand length is the same, about 1/64 times the minimum width
		// or height of the screen
		grid = Math.min(dim.width, dim.height) / 64;

		// number of degrees offset should be based on the current time
		Calendar c = Calendar.getInstance();

		/*
		// this bit of code is used to enable - end of world - end of clock
		Calendar t0 = Calendar.getInstance();
		t0.set(Calendar.YEAR, 2012);
		t0.set(Calendar.MONTH, Calendar.DECEMBER);
		t0.set(Calendar.DAY_OF_MONTH, 21);
		t0.set(Calendar.HOUR_OF_DAY,11);
		t0.add(Calendar.HOUR_OF_DAY,c.get(Calendar.ZONE_OFFSET)/3600000);
		t0.set(Calendar.MINUTE, 11);
		t0.set(Calendar.SECOND, 0);
		t0.set(Calendar.MILLISECOND, 0);
		Calendar t1 = (Calendar)t0.clone();
		t1.add(Calendar.MINUTE, 1);
		 */
		// this bit of code is used to set the end timer to 1 minute from now
		if (count && t0 == null) {
			t0 = (Calendar) c.clone();
			t0.set(Calendar.SECOND, 0);
			t0.set(Calendar.MILLISECOND, 0);
			t0.add(Calendar.MINUTE, 1);
			t1 = (Calendar)t0.clone();
			t1.add(Calendar.MINUTE, 1);
		}
		// if the date goes beyond 2012 12 21 11:11 UT then we should stop
		if (c.after(t1)) {
			// time is over
			paintDead(g);
			return;
		} else if (c.after(t0)) {
			// this is the critical minute
			paintBoom(g);
			return;
		}

		if (count) {
			// rather than a normal clock, want a count down to 2012/12/21 11:11 UT (6:11 EST)
		}
		
		// all of the degree offsets are relative to a zero
		// the year is broken into 4 single digits
		int tt = c.get(Calendar.YEAR);
		int cc = tt % 1000;
		tt = (tt - cc) / 1000;
		int dd = cc % 100;
		cc = (cc - dd) / 100;
		int yy = dd % 10;
		dd = (dd - yy) / 10;
		// the rest are double digit numbers
		int mm = c.get(Calendar.MONTH);
		int DD = c.get(Calendar.DAY_OF_MONTH) - 1;
		int HH = c.get(Calendar.HOUR_OF_DAY);
		int MM = c.get(Calendar.MINUTE);
		int SS = c.get(Calendar.SECOND);
		int ms = c.get(Calendar.MILLISECOND);

		// need to know how many numbers fit on the ring
		double months_in_year = 12;
		// adjusts to right number of days in current month
		double days_in_month = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		double hours_in_day = 24;
		double minutes_in_hour = 60;
		double seconds_in_minute = 60;
		double millis = 1000;
		
		// year
		double deg_t = digitmath(  tt,  10.0,   cc,  10.0);
		double deg_c = digitmath(  cc,  10.0,   dd,  10.0);
		double deg_d = digitmath(  dd,  10.0,   yy,  10.0);
		double deg_y = digitmath(  yy,  10.0,   mm,  months_in_year);

		// month
		double deg_mnth = digitmath(   mm,  months_in_year, DD,  days_in_month);

		// day
		double deg_day = digitmath(    DD,  days_in_month, HH, hours_in_day);

		// hours complete one circuit every 24 hours
		double deg_hour = digitmath(    HH,  hours_in_day, MM, minutes_in_hour);

		// the minutes - complete circuit, 360 degrees, in 60 minutes
		double deg_min = digitmath(    MM, minutes_in_hour, SS, seconds_in_minute);

		// the seconds - complete circuit in 60 seconds
		double deg_sec = digitmath(    SS, seconds_in_minute, ms, millis);

		// for the following hands, the first 2 coords are the center of the
		// hand, the next is the degree of the direction it faces at time zero

		// so a '1' will start out skewed by 30 degrees so
		// that 1 hour after zero it will be correctly lined up

		// all calculations are done before any drawing, so that the blank screen time is minimized.
		int cx = dim.width / grid / 4 - 1;
		int cy = dim.height / grid / 4 - 1;
		int w = Math.min(cx, cy);

		// Wipe off everything that has been drawn before
		// black background
		g.setColor(Color.black);
		g.fillRect(0, 0, dim.width, dim.height);

		// millenium - century - decade - year
		chunkX(10, deg_t, cx, cy, w, g, 13, 0);
		chunkX(10, deg_c, cx, cy, w, g, 12, 0);
		chunkX(10, deg_d, cx, cy, w, g, 11, 0);
		chunkX(10, deg_y, cx, cy, w, g, 10, 0);

		// month - first day in month is 1 (not zero - need to watch calculations)
		// first month in year is 1, but java says zero
		// month - starts at 1
		chunkXX(months_in_year, deg_mnth, cx, cy, w, g, 9, 1);

		// days - starts at 1
		chunkXX(days_in_month, deg_day, cx, cy, w, g, 7, 1);

		// the hours are in a ring just inside the minutes ring
		chunkXX(hours_in_day, deg_hour, cx, cy, w, g, 5, 0);

		// the minutes are in a ring just inside the seconds ring
		chunkXX(minutes_in_hour, deg_min, cx, cy, w, g, 3, 0);

		// let's paint all 60 seconds around the edge of the screen
		// starting with 0 directly on the top
		chunkXX(seconds_in_minute, deg_sec, cx, cy, w, g, 1, 0);
	}

	private double digitmath( int yy, double range,  int mm, double divisions){
		// start at 180 degrees - i.e. up - makes drawing simpler
		double degrees = 180.0 + yy % range * 360.0 / range;
		if (hires) {
			// make it finer resolution than every 36 degrees
			degrees += mm / divisions * (360.0 / range);
		}
		return degrees;
	}

	// this actually draws an entire ring of numbers
	private void chunkXX(double divisions, double degrees, int cx, int cy, int w, Graphics g, int off, int zero) {
		// this setting indicates whether the numbers should rotate so that the
		// current time appears on the right, or if the numbers should be fixed
		int right = 1;
		if (fixed)
			right = 0;

		double chunk = 360.0 / divisions;
		for (int i = 0; i < (int) Math.round(360.0 / chunk); i++) {
			double r = Math
					.toRadians((180 + i * chunk + .5 * chunk - (90 + degrees)
							* right) % 360);
			double x = (cx - (w - off) * Math.sin(r)) * 2;
			double y = (cy + (w - off) * Math.cos(r)) * 2;
			paintXX(g, i + zero, x, y, chunk, i, degrees);
		}
	}

	// the only difference between chunkXX and chunkX is whether to call paintXX or paintX
	private void chunkX(double divisions, double degrees, int cx, int cy, int w, Graphics g, int off, int zero) {
		// this setting indicates whether the numbers should rotate so that the
		// current time appears on the right, or if the numbers should be fixed
		int right = 1;
		if (fixed)
			right = 0;

		double chunk = 360.0 / divisions;
		for (int i = 0; i < (int) Math.round(360.0 / chunk); i++) {
			double r = Math
					.toRadians((180 + i * chunk + .5 * chunk - (90 + degrees)
							* right) % 360);
			double x = (cx - (w - off) * Math.sin(r)) * 2;
			double y = (cy + (w - off) * Math.cos(r)) * 2;
			paintX(g, i + zero, x, y, chunk, i, degrees);
		}
	}

	// composite numbers are easy, just put a couple digits together
	// paint two digits side by side
	private void paintXX(Graphics g, int d, double x, double y, double chunk,
			int skew, double deg) {
		int d2 = d % 10;
		int d1 = (d - d2) / 10;
		paintX(g, d1, x, y, chunk, skew, deg);
		if (d2 == 1) {
			paintX(g, d2, x + 1, y, chunk, skew, deg);
		} else {
			paintX(g, d2, x + 2, y, chunk, skew, deg);
		}
	}

	// paint the specified digit - use subroutines to keep this simple
	// if there is more than 1 digit, use 2 digit painting instead
	private void paintX(Graphics g, int d, double x, double y, double chunk,
			int skew, double deg) {
		switch (d) {
		case 0:
			paint0(g, x, y, chunk, skew, deg);
			break;
		case 1:
			paint1(g, x, y, chunk, skew, deg);
			break;
		case 2:
			paint2(g, x, y, chunk, skew, deg);
			break;
		case 3:
			paint3(g, x, y, chunk, skew, deg);
			break;
		case 4:
			paint4(g, x, y, chunk, skew, deg);
			break;
		case 5:
			paint5(g, x, y, chunk, skew, deg);
			break;
		case 6:
			paint6(g, x, y, chunk, skew, deg);
			break;
		case 7:
			paint7(g, x, y, chunk, skew, deg);
			break;
		case 8:
			paint8(g, x, y, chunk, skew, deg);
			break;
		case 9:
			paint9(g, x, y, chunk, skew, deg);
			break;
		default:
			paintXX(g, d, x, y, chunk, skew, deg);
			break;
		}
	}

	// this paints an lcd style zero at time zero (unless skewed)
	private void paint0(Graphics g, double x, double y, double chunk, int skew,
			double deg) {
		// x and y are some sort of relative positions in the panel
		// the skew should be a simple way of setting the time offset from time
		// zero
		// the deg parameter should range from 0 to 360
		paintHand1r(g, x, y, chunk, skew, deg);
		paintHand3r(g, x, y, chunk, skew, deg);
		paintHand6r(g, x, y, chunk, skew, deg);
		paintHand7(g, x, y, chunk, skew, deg);
		paintHand5(g, x, y, chunk, skew, deg);
		paintHand2(g, x, y, chunk, skew, deg);
	}

	private void paint1(Graphics g, double x, double y, double chunk, int skew,
			double deg) {

		paintHand3(g, x, y, chunk, skew, deg);
		paintHand6r(g, x, y, chunk, skew, deg);
	}

	private void paint2(Graphics g, double x, double y, double chunk, int skew,
			double deg) {

		paintHand1(g, x, y, chunk, skew, deg);
		paintHand3(g, x, y, chunk, skew, deg);
		paintHand4r(g, x, y, chunk, skew, deg);
		paintHand5(g, x, y, chunk, skew, deg);
		paintHand7(g, x, y, chunk, skew, deg);
	}

	private void paint3(Graphics g, double x, double y, double chunk, int skew,
			double deg) {

		paintHand1(g, x, y, chunk, skew, deg);
		paintHand3(g, x, y, chunk, skew, deg);
		paintHand4(g, x, y, chunk, skew, deg);
		paintHand6r(g, x, y, chunk, skew, deg);
		paintHand7(g, x, y, chunk, skew, deg);
	}

	private void paint4(Graphics g, double x, double y, double chunk, int skew,
			double deg) {

		paintHand2(g, x, y, chunk, skew, deg);
		paintHand4(g, x, y, chunk, skew, deg);
		paintHand3(g, x, y, chunk, skew, deg);
		paintHand6(g, x, y, chunk, skew, deg);
	}

	private void paint5(Graphics g, double x, double y, double chunk, int skew,
			double deg) {

		paintHand1r(g, x, y, chunk, skew, deg);
		paintHand2(g, x, y, chunk, skew, deg);
		paintHand4(g, x, y, chunk, skew, deg);
		paintHand6(g, x, y, chunk, skew, deg);
		paintHand7(g, x, y, chunk, skew, deg);
	}

	private void paint6(Graphics g, double x, double y, double chunk, int skew,
			double deg) {

		paintHand1r(g, x, y, chunk, skew, deg);
		paintHand2(g, x, y, chunk, skew, deg);
		paintHand5(g, x, y, chunk, skew, deg);
		paintHand4r(g, x, y, chunk, skew, deg);
		paintHand7(g, x, y, chunk, skew, deg);
		paintHand6r(g, x, y, chunk, skew, deg);
	}

	private void paint7(Graphics g, double x, double y, double chunk, int skew,
			double deg) {

		paintHand1(g, x, y, chunk, skew, deg);
		paintHand3(g, x, y, chunk, skew, deg);
		paintHand6(g, x, y, chunk, skew, deg);
	}

	private void paint8(Graphics g, double x, double y, double chunk, int skew,
			double deg) {

		paintHand1(g, x, y, chunk, skew, deg);
		paintHand2r(g, x, y, chunk, skew, deg);
		paintHand3(g, x, y, chunk, skew, deg);
		paintHand4r(g, x, y, chunk, skew, deg);
		paintHand5r(g, x, y, chunk, skew, deg);
		paintHand6r(g, x, y, chunk, skew, deg);
		paintHand7(g, x, y, chunk, skew, deg);
	}

	private void paint9(Graphics g, double x, double y, double chunk, int skew,
			double deg) {

		paintHand1r(g, x, y, chunk, skew, deg);
		paintHand2(g, x, y, chunk, skew, deg);
		paintHand3r(g, x, y, chunk, skew, deg);
		paintHand4(g, x, y, chunk, skew, deg);
		paintHand6r(g, x, y, chunk, skew, deg);
		paintHand7(g, x, y, chunk, skew, deg);
	}

	// there are seven individual segments in an lcd, and each is painted with a
	// rotating hand
	private void paintHand1(Graphics g, double x, double y, double chunk,
			int skew, double deg) {

		paintHand(g, x, y, chunk, skew, deg, 0, 0, 90);
	}

	private void paintHand2(Graphics g, double x, double y, double chunk,
			int skew, double deg) {

		paintHand(g, x, y, chunk, skew, deg, 0, 0, 180);
	}

	private void paintHand3(Graphics g, double x, double y, double chunk,
			int skew, double deg) {

		paintHand(g, x, y, chunk, skew, deg, 1, 0, 180);
	}

	private void paintHand4(Graphics g, double x, double y, double chunk,
			int skew, double deg) {

		paintHand(g, x, y, chunk, skew, deg, 0, 1, 90);
	}

	private void paintHand5(Graphics g, double x, double y, double chunk,
			int skew, double deg) {

		paintHand(g, x, y, chunk, skew, deg, 0, 1, 180);
	}

	private void paintHand6(Graphics g, double x, double y, double chunk,
			int skew, double deg) {

		paintHand(g, x, y, chunk, skew, deg, 1, 1, 180);
	}

	private void paintHand7(Graphics g, double x, double y, double chunk,
			int skew, double deg) {

		paintHand(g, x, y, chunk, skew, deg, 0, 2, 90);
	}

	private void paintHand1r(Graphics g, double x, double y, double chunk,
			int skew, double deg) {

		paintHand(g, x, y, chunk, skew, deg, 1, 0, 270);
	}

	private void paintHand2r(Graphics g, double x, double y, double chunk,
			int skew, double deg) {

		paintHand(g, x, y, chunk, skew, deg, 0, 1, 0);
	}

	private void paintHand3r(Graphics g, double x, double y, double chunk,
			int skew, double deg) {

		paintHand(g, x, y, chunk, skew, deg, 1, 1, 0);
	}

	private void paintHand4r(Graphics g, double x, double y, double chunk,
			int skew, double deg) {

		paintHand(g, x, y, chunk, skew, deg, 1, 1, 270);
	}

	private void paintHand5r(Graphics g, double x, double y, double chunk,
			int skew, double deg) {

		paintHand(g, x, y, chunk, skew, deg, 0, 2, 0);
	}

	private void paintHand6r(Graphics g, double x, double y, double chunk,
			int skew, double deg) {

		paintHand(g, x, y, chunk, skew, deg, 1, 2, 0);
	}

	
/*	
 * the 7-reversed hand is not actually needed
 * 
	private void paintHand7r(Graphics g, double x, double y, double chunk,
			int skew, double deg) {

		paintHand(g, x, y, chunk, skew, deg, 1, 2, 270);
	}
*/
	private void paintHand(Graphics g, double x, double y, double chunk,
			int skew, double deg, int dx, int dy, int dir) {
		// the last three parameters are used in the drawing of the lcd segment,
		// the offset from
		// the upper left and the direction it faces when showing the 'correct'
		// line

		// usually chunk is an integer, but will not be for days of the month
		// (360/31)

		// length of the hand is the basic unit of scale
		// offset from left or top must always be at least 2
		// scaling on the screen is based on allowing for 40 elements to be
		// drawn
		// double scale = dim.width/40;
		// offset is how many units of free space are at top of screen
		// need 18 rows for the digits - offset down half of the extras

		double r = Math.toRadians(-chunk * (skew + .5) + deg + dir);

		double x1 = x * grid + dx * grid;
		double y1 = y * grid + dy * grid;
		double x2 = x1 - grid * Math.sin(r);
		double y2 = y1 + grid * Math.cos(r);

		Color randomColor = Color.getHSBColor(random.nextFloat(), 1.0F, 1.0F);

		// if obvio is selected, the current time will be made obvio
		// using the opposite of white or color for the digit
		// it should be 30 degrees for seconds, hours
		// it should be 60 degrees for tens of minutes
		// it should be 36 degrees for minutes

		// need to have the angle in the 0-360 range
		// this must be rounded down to make sense
		boolean highlight = obvio;
		if (obvio) {
			int arcAngle = (int) Math.floor(-chunk * skew + deg) % 360;
			if (arcAngle < 0)
				arcAngle += 360;
			highlight &= 180 <= arcAngle && arcAngle < Math.floor(180 + chunk);
		}

		if ((color && !highlight) || (!color && highlight)) {
			if (wilds) {
				g.setColor(randomColor);
			} else {
				g.setColor(Color.red);
			}
		} else {
			g.setColor(Color.white);
		}

		int i1 = (int) Math.round(x1);
		int j1 = (int) Math.round(y1);
		int i2 = (int) Math.round(x2);
		int j2 = (int) Math.round(y2);
		g.fillOval(i1 - 2, j1 - 2, 4, 4);
		g.drawLine(i1, j1, i2, j2);
	}


}
