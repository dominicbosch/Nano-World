package nano.remexp.client.awt;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import nano.remexp.StreamReceiver;
import nano.remexp.client.ClientGUI;

/**
 * listens to the input stream of what is scanned at the moment
 * 
 * @author Tibor Gyalog
 * @version 1.0.1 20.08.01 (documented)
 */

public class NetObserver extends Canvas implements StreamReceiver {
	private static final long serialVersionUID = 1L;
	private Graphics g; // added to increase performance
	private Image zeroLineImage;
	private MemoryImageSource zeroLineSource;
	private Image[] lineImage = new Image[256];
	private MemoryImageSource[] lineSource;
	private ColorScala colorScala;
	private int lineData[][] = new int[256][256];
	private byte rawData[][] = new byte[256][256];
	private int nr_old = 0;
	private byte[] oldline = new byte[256];
	private int[] zeroData = new int[256];
	
	public NetObserver() {
		colorScala = new ColorScala();
		colorScala.setColors(Color.blue, Color.white);
		ClientGUI.setupComponent(this, new Dimension(256, 256));
		setSize(256, 256);
		setBackground(Color.white);
		setForeground(Color.red);
		setFont(new Font("Arial", Font.BOLD, 20));
		lineSource = new MemoryImageSource[256];
		for (int i = 0; i < 256; i++) {
			for (int j = 0; j < 256; j++) {
				lineData[i][j] = (i + j) / 2;
			}
			lineSource[i] = new MemoryImageSource(256, 1, lineData[i], 0, 256);
			lineImage[i] = createImage(lineSource[i]);
			zeroData[i] = (255 << 24 | 255 << 16);
		}
		zeroLineSource = new MemoryImageSource(256, 1, zeroData, 0, 256);
		zeroLineImage = createImage(zeroLineSource);
	}
	
	public void write(byte[] newLine) {
		int nr = newLine[0] + 128;
		for (int i = 0; i < 256; i++) rawData[nr][i] = newLine[i];
		replaceLine(nr, nr_old, oldline);
		oldline = newLine;
		nr_old = nr;
	}

	public int getCurrentLine() {
		return nr_old;
	}
	
	public void paint(Graphics g) {
		for (int i = 0; i < 256; i++) g.drawImage(lineImage[i], 0, i, null);
	}

	public void update(Graphics g) {paint(g);}
	
	public void setColorScala(ColorScala newScala) {
		colorScala = newScala;
	}

	public ColorScala getColorScala() {
		return colorScala;
	}
	
	protected void saveData(String filename) {
		DataFrame dFrame = new DataFrame("Raw Data", this.getColorScala());
		dFrame.setData(rawData);
	}

	private void replaceLine(int line, int oldLine, byte[] newData) {
		for (int i = 0; i < 256; i++) {
			lineData[line][i] = colorScala.getColor(newData[i] + 128);
		}
		lineImage[line].flush();
		lineImage[line] = createImage(lineSource[line]);
		if (g == null || ((line % 10) == 0)) g = getGraphics();
		if (g != null) {
			g.drawImage(zeroLineImage, 0, line, null);
			g.drawImage(lineImage[oldLine], 0, oldLine, null);
		}
	}

	public void saveImage(File file) {
		BufferedImage bi = new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);
		Graphics grph = bi.createGraphics();
		for (int i = 0; i < 256; i++) {
			grph.drawImage(lineImage[i], 0, i, null);
		}
		grph.dispose();
		BufferedImageOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        try {
			ImageIO.write(op.filter(bi, null), "PNG", file);
		} catch (IOException e) {}
	}
}