package nano.remexp.client.awt;

import java.awt.Color;

/**
 * 
 * @author Tibor Gyalog
 * @version 1.0 <17.7.01
 */

public class ColorScala {
	private int myColors[] = new int[256];

	public ColorScala() {
		for (int i = 0; i < 256; i++) myColors[i] = (255 << 24 | i << 16 | i << 8 | i);
	}

	public void setColors(Color bottom, Color top) {
		int r, g, b;
		float[] bcol = bottom.getRGBColorComponents(null);
		float[] tcol = top.getRGBColorComponents(null);
		float dr = (tcol[0] - bcol[0]) / 255;
		float dg = (tcol[1] - bcol[1]) / 255;
		float db = (tcol[2] - bcol[2]) / 255;
		for (int i = 0; i < 256; i++) {
			r = Math.round(255 * (bcol[0] + i * dr));
			g = Math.round(255 * (bcol[1] + i * dg));
			b = Math.round(255 * (bcol[2] + i * db));
			myColors[i] = (255 << 24 | r << 16 | g << 8 | b);
		}
	}

	public int getColor(int value) {
		if (value < 0) value = 0;
		if (value > 255) value = 255;
		return myColors[value];
	}
}
