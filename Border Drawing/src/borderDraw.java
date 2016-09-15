import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


public class borderDraw extends JPanel {

	private static final long serialVersionUID = 1L;

	//for drwaing the rectangle
	private static Rectangle rect = null;
	private static boolean drawing = false;

	//to later check if the picture is a real one
	private static File picture = null;

	//to write to the board
	private static double minx = 0;
	private static double miny = 0;
	private static double maxx = 0;
	private static double maxy = 0;


	//the name of the txt file to be created
	private static String fileLocation = "";

	//for creating gui
	private static BufferedImage img = null;
	private static ImagePanel imgPane = null;

	//rectangle properties
	private static final Color DRAWING_RECT_COLOR = new Color(255, 255, 255);
	private static final Color DRAWN_RECT_COLOR = Color.magenta;

	//where the file to be read from is located
	private static String fileName = "";

	//writes to the file
	private static PrintWriter savePoint = null;

	//name of scanner
	private static Scanner file = null;

	//various variables to allow progrm to run efficiently
	private static int check = 0;
	private static final int elements = 2;
	private static final int twoDimension = 4;
	private static final int threeDimension = 4;
	private static final int occluded =2;

	//All variables need in KITTI
	private static final String[] inputs = {"Type: ", "Truncation: (0.0-1.0) ", 
		"Occluded: (0-3)","Observation Angle: (-pi to pi) ",
		"3D Height (m): ","3D Width (m): ","3D Length(m): ",
		"3D X: ","3D Y: ","3D Z: ", "Rotational Y: "};

	//allows to get and preserve values for later usage
	private static List<JTextField> inputLabels = null;
	private static List<Rectangle> listOfRectangles = new ArrayList<Rectangle>();

	//magic
	public static void main(String[] args) throws FileNotFoundException {

		file = new Scanner(System.in);

		System.out.println("Please enter a valid input file");
		fileName = file.nextLine();

		//grab the file name so we can name the text file the same
		int start = fileName.lastIndexOf("\\");
		int end = fileName.lastIndexOf(".");
		fileLocation = fileName.substring(++start, end) +".txt";

		//get the valid file
		picture = new File(fileName);
		while(!(picture.isFile())){
			System.out.println("Please enter a valid input file");
			fileName = file.nextLine();
			picture = new File(fileName);
		}
		
		/*
		 * for (File file: dir.listFiles(){
		 * picture =file;
		 * }
		 */
		//draw on the picture
		new borderDraw();

	}

	//save the file
	private static void savePoint(String sp) throws IOException{

		if(check ==0){
			savePoint = new PrintWriter(new FileWriter(sp),true);
			saveValues();
			check++;
		}
		else{
			saveValues();
		}

	}

	//save the values
	private static void saveValues() {

		//get the values for the 2d characteristics
		for(int i=0;i<twoDimension;i++){
			savePoint.print(inputLabels.get(i).getText()+" ");
		}

		//write minimum values
		float tmpMaxx = (float) Math.max(maxx, minx);
		float tmpMinx = (float) Math.min(maxx, minx); 

		//write the maximum values
		float tmpMaxy = (float) Math.max(maxy, miny);
		float tmpMiny = (float) Math.min(maxy, miny); 

		float[] values = {tmpMinx,tmpMiny,tmpMaxx};

		//write min x, max x, min y
		for(int i =0; i<values.length;i++){
			savePoint.printf("%.2f",+ values[i]);
			savePoint.print(" ");
		}

		//write maximum y
		savePoint.printf("%.2f", + tmpMaxy);

		//write the values for the 3d characteristics
		for(int i = threeDimension; i<inputLabels.size();i++){
			savePoint.print(" " + inputLabels.get(i).getText());
		}

		savePoint.println();
		savePoint.flush();

	}

	//function that brings up image and allows for drawing
	private borderDraw(){
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					} 
					catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
					}

					//method call to show the picture 
					showPicture();

				} 
				catch (IOException ex) {
					Logger.getLogger(borderDraw.class.getName()).log(Level.SEVERE, null, ex);
				}
			}


			//method that shows the picture
			private void showPicture() throws IOException {
				//read in the file and create scrolling capabilities
				img = ImageIO.read(picture);//the file
				imgPane = new ImagePanel(img);
				JScrollPane scrollPane = new JScrollPane(imgPane);
				new JLabel("...");

				//add ability for computer to see mouse tracking
				DrawAndClick mouseAdapter = new DrawAndClick();
				imgPane.addMouseListener(mouseAdapter);
				imgPane.addMouseMotionListener(mouseAdapter);


				//show everything as a GUI
				JFrame pic = new JFrame("Picture");
				pic.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				pic.add(scrollPane);

				pic.pack();
				pic.setLocationRelativeTo(null);
				pic.setVisible(true);

			}

		});
		
		
		
	}
/*
 * private class Button implements ActionListener{
 * 
 * }
 */

	//class that does stuff when you click
	private class DrawAndClick extends MouseAdapter implements ActionListener	{

		private Point mousePress = null; 
		private JFrame textBoxes = null;
		//for mouse pressing down
		@Override
		public void mousePressed(MouseEvent e) {
			mousePress = e.getPoint();

			//get the "minimum"
			imgPane.findMin(mousePress);
			System.out.println(imgPane.findMin(mousePress));
		}

		//for dragging the mouse
		@Override
		public void mouseDragged(MouseEvent e) {
			//draws the rectangle by drawing
			drawing = true;

			int x = Math.min(mousePress.x, e.getPoint().x);
			int y = Math.min(mousePress.y, e.getPoint().y);
			int width = Math.abs(mousePress.x - e.getPoint().x);
			int height = Math.abs(mousePress.y - e.getPoint().y);

			//draw the dynamic rectangle as dragging along
			rect = new Rectangle(x, y, width, height);

			imgPane.repaint();

		}

		//mouse is released and shapes are drawn
		@Override
		public void mouseReleased(MouseEvent e) {

			//draw the final rectangle when the mouse is released
			drawing = false;
			mousePress = e.getPoint();

			//find the "maximum"
			imgPane.findMax(mousePress);
			System.out.println(imgPane.findMax(mousePress));

			imgPane.repaint();
			showTextBoxes();

		}

		private void showTextBoxes() {

			inputLabels= new ArrayList<JTextField>();

			//create the frame and set its layout
			textBoxes = new JFrame("Enter information here");
			textBoxes.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			textBoxes.setLayout(new GridLayout(0,elements));

			//add labels and text fields
			for(int i =0;i<inputs.length;i++){

				//these two case have types that are non-float/double
				String add = ((i==0 || i==occluded)? "0": "0.0");

				JLabel newLabel = new JLabel(inputs[i]);
				textBoxes.add(newLabel);
				JTextField elementToBeAdded = new JTextField(add);
				textBoxes.add(elementToBeAdded);
				inputLabels.add(elementToBeAdded);

			}

			//Add the done button
			JButton doneButton = new JButton("Done");
			textBoxes.add(doneButton);
			doneButton.addActionListener(this);
			doneButton.setActionCommand("done");

			//Add the cancel button
			JButton cancelButton = new JButton("Cancel");
			textBoxes.add(cancelButton);
			cancelButton.addActionListener(this);
			cancelButton.setActionCommand("cancel");
			

			textBoxes.pack();
			textBoxes.setVisible(true);

		}

		@Override
		public void actionPerformed(ActionEvent e) {

			//done
			if(e.getActionCommand().equals("done")){
				try {
					savePoint(fileLocation);
					textBoxes.dispose();
				} 
				catch (IOException e1) {

					e1.printStackTrace();
				}
			}
			//clear
			else{
				listOfRectangles.remove(listOfRectangles.size()-1);
				textBoxes.dispose();
			}
		}

		//end of draw and click class
	}

	//holds all the drawing, finding points capabilities
	private class ImagePanel extends JPanel {


		private static final long serialVersionUID = 1L;
		private BufferedImage img;

		public ImagePanel(BufferedImage img) {
			this.img = img;
		}

		@Override
		public Dimension getPreferredSize() {
			return img == null ? super.getPreferredSize() : new Dimension(img.getWidth(), img.getHeight());
		}

		protected Point getImageLocation() {
			Point p = null;
			if (img != null) {
				int x = (getWidth() - img.getWidth()) / 2;
				int y = (getHeight() - img.getHeight()) / 2;
				p = new Point(x, y);
			}
			return p;
		}

		//find the minimum points
		public Point findMin(Point p) {
			Point imgLocation = getImageLocation();
			Point relative = new Point(p);

			relative.x -= imgLocation.x;
			relative.y -= imgLocation.y;

			//change the y to match (0,0) from bottom left
			relative.y = img.getHeight()-relative.y;

			minx = relative.getX();
			miny = relative.getY();
			return relative;
		}

		//find the maximum points
		public Point findMax(Point p){
			Point imgLocation = getImageLocation();
			Point relative = new Point(p);

			relative.x -= imgLocation.x;
			relative.y -= imgLocation.y;

			//change the y to match (0,0) from bottom left
			relative.y = img.getHeight()-relative.y;

			maxx = relative.getX();
			maxy = relative.getY();

			return relative;
		}

		//draw stuff
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D)g;

			if (img != null) {
				g.drawImage(img, 0, 0, null);
			}

			if (rect == null) {
				return;	
			} 

			//draw the dragged rectangle and other rectangles
			else if (drawing) {
				g2.setColor(DRAWING_RECT_COLOR);
				for (int i=0;i<listOfRectangles.size();i++){
					g2.draw(listOfRectangles.get(i));
				}
				g2.draw(rect);
			}

			//draw the final rectangle and other rectangles
			else {
				g2.setColor(DRAWN_RECT_COLOR);
				listOfRectangles.add(rect);
				for (int i=0;i<listOfRectangles.size();i++){
					g2.draw(listOfRectangles.get(i));
				}
			}
		}

		//end of imagepanel class
	}

	//end of class
}

