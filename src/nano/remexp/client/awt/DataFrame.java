package nano.remexp.client.awt;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.MemoryImageSource;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DataFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	DataCanvas dataCanvas;
	VerlaufCanvas verlauf;
	ColorScala colorScala;
	JLabel sumIndicator, meanIndicator, sigmaIndicator;
	boolean lineStat = false;
	LineSectionData lineSectionData;
	HistogrammCanvas histogrammCanvas;
	byte[][] data = new byte[256][256];

	public DataFrame(String name, ColorScala netColorScala) {
		super(name);
		setLayout(new GridLayout(2, 2));
		colorScala = netColorScala;
		dataCanvas = new DataCanvas();
		DataPanel dataPanel = new DataPanel(dataCanvas);
		lineSectionData = new LineSectionData();
		histogrammCanvas = new HistogrammCanvas();

		JPanel p1 = new JPanel();
		JPanel p2 = new JPanel();
		p1.setLayout(new FlowLayout());
		p2.setLayout(new FlowLayout());
		p1.add(dataCanvas);
		p1.add(lineSectionData);
		p2.add(dataPanel);
		p2.add(histogrammCanvas);
		add("North", p1);
		add("Center", p2);
		setVisible(true);
		pack();
		setResizable(false);
		addWindowListener((WindowListener) new DataFrameListener());
	}

	private void kill() {dispose();}

	public void setData(byte[][] NewData) {
		for (int i = 0; i < 256; i++) {
			for (int j = 0; j < 256; j++) {
				data[i][j] = NewData[i][j];
			}
		}
		histogrammCanvas.setData(data);
		sumIndicator.setText("Sum=" + getSum());
		meanIndicator.setText("Mean=" + getMean());
		sigmaIndicator.setText("Sigma=" + getSigma());

	}

	public double getSum() {
		double sum = 0;
		if (lineStat) {
			byte[] lineData = lineSectionData.getData();
			if (lineData != null) {
				for (int i = 0; i < 256; i++) sum += lineData[i];
			}
		} else sum = getSum(0, 0, 255, 255);
		return sum;
	}

	public double getMean() {
		double mean = 0;
		if (lineStat) mean = getSum() / 256.0;
		else mean = getSum() / (256.0 * 256.0);
		return mean;
	}

	public double getSigma() {
		double sigma = 0;
		double mean = getMean();
		if (lineStat) {
			byte[] LineData = lineSectionData.getData();
			if (LineData != null) {
				for (int i = 0; i < 256; i++) {
					sigma += Math.pow((LineData[i] - mean), 2);
				}
				sigma = sigma / 256.0;
			}
		} else {
			for (int i = 0; i < 256; i++) {
				for (int j = 0; j < 256; j++) {
					sigma += Math.pow((data[i][j] - mean), 2);
				}
			}
			sigma = sigma / (256.0 * 256.0);
		}
		return sigma;
	}

	public double getSum(int xtopleft, int ytopleft, int xbottomright, int ybottomright) {
		xtopleft = Math.max(xtopleft, 0);
		ytopleft = Math.max(ytopleft, 0);
		xbottomright = Math.min(xbottomright, 256);
		ybottomright = Math.min(ybottomright, 256);
		double sum = 0;
		for (int i = ytopleft; i <= ybottomright; i++) {
			for (int j = xtopleft; j <= xbottomright; j++) {
				sum += data[i][j];
			}
		}
		return sum;
	}

	public void setLineData(byte[] NewData) {
		lineSectionData.setData(NewData);
	}

	public byte[] getLineSection(int x0, int y0, int x1, int y1) {
		int xi, yi;
		byte[] LineSection = new byte[256];
		double dx = (x1 - x0) / 265.0;
		double dy = (y1 - y0) / 265.0;
		for (int i = 0; i < 256; i++) {
			xi = x0 + (int) (i * dx);
			yi = y0 + (int) (i * dy);
			LineSection[i] = data[yi][xi];
		}
		return LineSection;
	}

	public void changeColorScala() {
		ColorScala TheNew = FarbverlaufDialog.getColorScala(new JFrame(),
				colorScala);
		colorScala = TheNew;
		verlauf.setColorScala(TheNew);
		dataCanvas.repaint();
		verlauf.repaint();
	}

	class DataFrameListener extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			kill();
		}
	}

	private class DataPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		JComboBox chooseDataOrLine;
		
		private DataPanel(DataCanvas NewDataCanvas) {
			setLayout(new GridLayout(10, 1));
			chooseDataOrLine = new JComboBox(new String[] { "2D Data", "LineSection" });
			chooseDataOrLine.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(chooseDataOrLine.getSelectedItem().equals("LineSection")) lineStat = true;
					else lineStat = false;
					sumIndicator.setText("Sum=" + getSum());
					meanIndicator.setText("Mean=" + getMean());
					sigmaIndicator.setText("Sigma=" + getSigma());	
				}
			});
			add(chooseDataOrLine);
			
			JLabel info = new JLabel("Statistic informations");
			add(info);
			JLabel info2 = new JLabel("x-y-Range 0..256, 0..256");
			add(info2);
			JLabel info3 = new JLabel("z-Range -128 .. 127");
			add(info3);
			sumIndicator = new JLabel("Sum=" + getSum());
			add(sumIndicator);
			meanIndicator = new JLabel("Mean=" + getMean());
			add(meanIndicator);
			sigmaIndicator = new JLabel("Sigma=" + getSigma());
			add(sigmaIndicator);
			JButton ChangeColorButton = new JButton("Change Color");
			ChangeColorButton.addActionListener(new ChangeColorListener());

			verlauf = new VerlaufCanvas(colorScala);
			add(verlauf);
			add(ChangeColorButton, null);
		}
	}
/*
	private class DataOrLineListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if ((e.getItem()).equals("LineSection")) lineStat = true;
			else lineStat = false;
			sumIndicator.setText("Sum=" + getSum());
			meanIndicator.setText("Mean=" + getMean());
			sigmaIndicator.setText("Sigma=" + getSigma());
			System.out.println("\n" + e.getItem());
		}
	}
*/
	private class ChangeColorListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			changeColorScala();
		}
	}

	private class DataCanvas extends Canvas implements MouseListener {
		private static final long serialVersionUID = 1L;
		private int selectedNumOfPoints = 0;
		private int[] pointX = new int[2];
		private int[] pointY = new int[2];

		private DataCanvas() {
			addMouseListener(this);
		}

		public Dimension getPreferredSize() {
			return new Dimension(256, 256);
		}

		public Dimension getMinimumSize() {
			return new Dimension(256, 256);
		}

		public void mouseReleased(MouseEvent e) {}

		public void mouseClicked(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mouseEntered(MouseEvent e) {}

		public void mousePressed(MouseEvent e) {
			selectedNumOfPoints++;
			if (selectedNumOfPoints >= 3) {
				selectedNumOfPoints = 1;
			}
			pointX[selectedNumOfPoints - 1] = e.getX();
			pointY[selectedNumOfPoints - 1] = e.getY();
			repaint();
			if (selectedNumOfPoints == 2) {
				setLineData(getLineSection(pointX[0], pointY[0], pointX[1],
						pointY[1]));
				sumIndicator.setText("Sum=" + getSum());
				meanIndicator.setText("Mean=" + getMean());
				sigmaIndicator.setText("Sigma=" + getSigma());

			}
		}

		/*
		 * public void save(){ Image offscreen=createImage(256,256); Graphics
		 * g=offscreen.getGraphics(); for(int i=0;i <256;i++){
		 * g.drawImage(MyImages[i],0,i,null); } try{ FileOutputStream out=new
		 * FileOutputStream(new File("MeinBildli.gif")); GifEncoder encoder=new
		 * GifEncoder(offscreen,out); encoder.encode(); out.close();
		 * }catch(IOException e){ //e.printStackTrace(); } }
		 */
		public void paint(Graphics g) {
			Image LineImage;
			final int radius = 3;
			int[] LineData = new int[256 * 256];
			for (int i = 0; i < 256; i++) {
				for (int j = 0; j < 256; j++) {
					LineData[256 * i + j] = colorScala
							.getColor(data[i][j] + 128);
				}
			}
			MemoryImageSource linesource = new MemoryImageSource(256, 256,
					LineData, 0, 256);
			// LineImage.flush();
			LineImage = createImage(linesource);
			g.drawImage(LineImage, 0, 0, null);
			if (selectedNumOfPoints == 1) {
				g.setColor(Color.red);
				g.fillOval(pointX[0] - radius, pointY[0] - radius, 2 * radius, 2 * radius);
				g.setColor(Color.white);
				g.drawOval(pointX[0] - radius, pointY[0] - radius, 2 * radius, 2 * radius);
			}

			if (selectedNumOfPoints == 2) {
				g.setColor(Color.red);
				g.drawLine(pointX[0], pointY[0], pointX[1], pointY[1]);
				g.fillOval(pointX[0] - radius, pointY[0] - radius, 2 * radius, 2 * radius);
				g.fillOval(pointX[1] - radius, pointY[1] - radius, 2 * radius, 2 * radius);
				g.setColor(Color.white);
				g.drawOval(pointX[0] - radius, pointY[0] - radius, 2 * radius, 2 * radius);
				g.drawOval(pointX[1] - radius, pointY[1] - radius, 2 * radius, 2 * radius);
			}
		}
	}

	public class LineSectionData extends Canvas implements MouseListener {
		private static final long serialVersionUID = 1L;
		Graphics myg;
		byte[] linSecData;
		int dx = 4, height = 256, res = 4, width = 256, numpoints = 256, plotlines = 2;
		int[] Flag_x = new int[256];
		int act_x, num_Flags = 0;

		public LineSectionData() {
			setBackground(Color.black);
			setForeground(Color.green);
			setFont(new Font("Arial", Font.BOLD, 10));
			addMouseListener(this);
		}

		public byte[] getData() {
			return linSecData;
		}

		public Dimension getPreferredSize() {
			return new Dimension(256, 256);
		}

		public Dimension getMinimumSize() {
			return new Dimension(256, 256);
		}

		public void setData(byte[] newData) {
			linSecData = newData;
			paintMe();
		}

		public void mouseReleased(MouseEvent e) {}

		public void mouseClicked(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mouseEntered(MouseEvent e) {}

		public void mousePressed(MouseEvent e) {
			boolean add = true;
			act_x = e.getX();
			for (int i = 1; i <= num_Flags; i++) {
				if (Math.abs(act_x - Flag_x[i - 1]) <= 3) {
					deleteFlag(i);
					add = false;
					break;
				}
			}
			if (add) addFlag(act_x);
		}

		public void deleteFlag(int i) {
			Flag_x[i - 1] = Flag_x[num_Flags - 1];
			num_Flags--;
			repaint();
		}

		public void addFlag(int x) {
			Flag_x[num_Flags] = x;
			num_Flags++;
			repaint();
		}

		public void paintMe() {
			height = getHeight();
			width = getWidth();
			numpoints = 256 / res;
			dx = width / numpoints;
			if (myg == null) myg = getGraphics();
			else if (linSecData != null) {
				myg.clearRect(0, 0, dx + 1, height);
				myg.setColor(Color.green);
				for (int xi = 2; xi < numpoints; xi++) {
					myg.clearRect(dx * (xi - 1) + 1, 0, dx, height);
					myg.drawLine(dx * (xi - 1), (height / 2)
							- linSecData[res * (xi - 1)], dx * xi, (height / 2)
							- linSecData[res * xi]);
				}
				myg.setColor(Color.red);
				for (int i = 1; i <= num_Flags; i++) {
					paintFlag(myg, i);
				}
			}
		}

		public void paintFlag(Graphics myg, int index) {
			int[] x = new int[3];
			int[] y = new int[3];
			x[0] = Flag_x[index - 1];
			y[0] = (height / 2) - linSecData[Flag_x[index - 1]];
			x[1] = x[0] - 3;
			x[2] = x[0] + 3;
			y[1] = y[0] - 5;
			y[2] = y[1];
			myg.fillPolygon(x, y, 3);
			String MyText = " " + Flag_x[index - 1];
			myg.drawString(MyText, x[0] - 10, y[0] - 20);
			MyText = " " + linSecData[Flag_x[index - 1]];
			myg.drawString(MyText, x[0] - 10, y[0] - 10);
		}

		public void paint(Graphics g) {
			paintMe();
		}

	}

}