package robotboy;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import robotboy.MouseHandler;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Main {

	static JFrame f;
	static Dimension frameSize = new Dimension(1000, 650);
	static Insets insets;

	static JPanel p;
	static Image field;
	static JTextArea codeDisplay;
	static JScrollPane scrollCode;

	static JFrame settings;
	static JButton submitSettings;
	static JTabbedPane settingsTab;

	static JPanel appearanceSettings;
	static JTextField lineStroke;
	static JTextField pointSizeSet;
	static JTextField lineColourSet;
	static JTextField pointColourSet;
    static JTextField spawnColourSet;
    static JTextField diagramNumberColourSet;

	static JPanel moveMethods;
	static JTextField moveForwardEntry;
	static JTextField moveBackwardEntry;
	static JTextField turnLEntry;
	static JTextField turnREntry;

	static JButton undo;
	static JButton reset;
	static JButton comment;
	static JButton switchDirection;
	static JButton exportImage;
	static JButton programSettings;

	static ActionHandler action;
	static MouseHandler mouse;
	static BufferedReader in;
	static BufferedWriter out;

	static String robotName = "";
    static boolean currentlyExportingDiagram = false;
	static String code = "";
	static int fieldWidth = frameSize.height - 20;
	static double currentAngle = 0;
    static int stage = 0;
	static int pointSize = 50;
	static int strokeSize = 2;

	static int[] pointColour = {0, 127, 255, 127};
	static int[] lineColour = {255, 255, 255, 127};
	static int[] spawnColour = {255, 255, 255, 127};
    static int[] diagramNumberColour = {0, 255, 255, 255};

    static boolean movingBackward = false;

	static ArrayList<Integer> points = new ArrayList<Integer>();
	static ArrayList<Double> oldAngles = new ArrayList<Double>();

	public static void main(String[] args) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();


		try {
			field = ImageIO.read(Main.class.getClassLoader().getResourceAsStream("images/field2016.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		mouse = new MouseHandler();
		action = new ActionHandler();

		settings = new JFrame("Settings");
		settings.setSize(600, 600);
		settings.setLayout(new BorderLayout());
		settings.setResizable(false);
		settings.setLocation(screenSize.width / 2 - settings.getSize().width / 2, screenSize.height/2 - settings.getSize().height / 2);

		settingsTab = new JTabbedPane();
		settings.add(settingsTab, BorderLayout.PAGE_START);

		appearanceSettings = new JPanel();

		appearanceSettings.setLayout(new GridLayout(15, 1));

		lineStroke = new JTextField();
		pointSizeSet = new JTextField();
		lineColourSet = new JTextField();
		pointColourSet = new JTextField();
		spawnColourSet = new JTextField();
        diagramNumberColourSet = new JTextField();

		appearanceSettings.add(new JLabel("Line Thickness"));
		appearanceSettings.add(lineStroke);
		appearanceSettings.add(new JLabel("Point Size"));
		appearanceSettings.add(pointSizeSet);
		appearanceSettings.add(new JLabel("Line Colour (R,G,B,A)"));
		appearanceSettings.add(lineColourSet);
		appearanceSettings.add(new JLabel("Point Colour (R,G,B,A)"));
		appearanceSettings.add(pointColourSet);
        appearanceSettings.add(new JLabel("Spawn Point Colour (R,G,B,A)"));
        appearanceSettings.add(spawnColourSet);
        appearanceSettings.add(new JLabel("Diagram Number Colour (R,G,B,A)"));
        appearanceSettings.add(diagramNumberColourSet);

		settingsTab.add("Appearance", appearanceSettings);

		moveMethods = new JPanel();
		moveMethods.setLayout(new GridLayout(15, 1));

		String[] methodsToUse = readInSettings();

		moveForwardEntry = new JTextField(methodsToUse[0]);

		moveBackwardEntry = new JTextField(methodsToUse[1]);

		turnLEntry = new JTextField(methodsToUse[2]);

		turnREntry = new JTextField(methodsToUse[3]);

		moveMethods.add(new JLabel("    Driving Forward Method"));
		moveMethods.add(moveForwardEntry);

		moveMethods.add(new JLabel("    Driving Backward Method"));
		moveMethods.add(moveBackwardEntry);

		moveMethods.add(new JLabel("    Left Turn Method"));
		moveMethods.add(turnLEntry);

		moveMethods.add(new JLabel("    Right Turn Method"));
		moveMethods.add(turnREntry);

		moveMethods.add(new JLabel("	Use [degrees] in a turning method to use degrees"));
		moveMethods.add(new JLabel("	Use [radians] in a turning method to use radians"));
		moveMethods.add(new JLabel("	Use [mm] in a driving method to use millimetres"));
		moveMethods.add(new JLabel("	Use [cm] in a driving method to use centimetres"));

		settingsTab.addTab("Move Methods", moveMethods);

		submitSettings = new JButton("Submit");
		submitSettings.addActionListener(action);
		settings.add(submitSettings, BorderLayout.PAGE_END);

		f = new JFrame("RobotBoy");
		f.addMouseListener(mouse);
		f.addMouseMotionListener(mouse);
		f.setSize(frameSize);
		f.setResizable(false);
		f.setLayout(null);
		insets = f.getInsets();
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		f.setLocation(screenSize.width / 2 - f.getSize().width / 2, screenSize.height/2 - f.getSize().height / 2);

		p = new JPanel() {

			public void paintComponent (Graphics g) {
				super.paintComponent(g);
				paintPanel((Graphics2D) g);
			}

		};

		p.setBounds(insets.left, insets.top, fieldWidth, fieldWidth);

		f.add(p);

		p.repaint();

		codeDisplay = new JTextArea();
		codeDisplay.setEditable(false);
		scrollCode = new JScrollPane(codeDisplay);
		scrollCode.setBounds(insets.left + fieldWidth, insets.top, f.getWidth() - fieldWidth, fieldWidth - 100);

		f.add(scrollCode);

		undo = new JButton("Undo Last Move");
		reset = new JButton("Reset All Points");
		comment = new JButton("Insert Reminder");
		switchDirection = new JButton("Drive Backward");
		exportImage = new JButton("Export Diagram");
		programSettings = new JButton("Settings");
		undo.addActionListener(action);
		reset.addActionListener(action);
		comment.addActionListener(action);
		switchDirection.addActionListener(action);
		exportImage.addActionListener(action);
		programSettings.addActionListener(action);

		Dimension buttonSize = new Dimension(150, 30);
		undo.setBounds(insets.left + fieldWidth + (f.getWidth() - fieldWidth) / 2 - buttonSize.width - 10, insets.top + fieldWidth - 100, buttonSize.width, buttonSize.height);
		f.add(undo);

		reset.setBounds(insets.left + fieldWidth + (f.getWidth() - fieldWidth) / 2 - buttonSize.width - 10, insets.top + fieldWidth - 66, buttonSize.width, buttonSize.height);
		f.add(reset);

		comment.setBounds(insets.left + fieldWidth + (f.getWidth() - fieldWidth) / 2 - buttonSize.width - 10, insets.top + fieldWidth - 33, buttonSize.width, buttonSize.height);
		f.add(comment);

		switchDirection.setBounds(insets.left + fieldWidth + (f.getWidth() - fieldWidth) / 2 + 10, insets.top + fieldWidth - 100, buttonSize.width, buttonSize.height);
		f.add(switchDirection);

		exportImage.setBounds(insets.left + fieldWidth + (f.getWidth() - fieldWidth) / 2 + 10, insets.top + fieldWidth - 66, buttonSize.width, buttonSize.height);
		f.add(exportImage);

		programSettings.setBounds(insets.left + fieldWidth + (f.getWidth() - fieldWidth) / 2 + 10, insets.top + fieldWidth - 33, buttonSize.width, buttonSize.height);
		f.add(programSettings);

		f.setVisible(true);
	}

	public static void convertToCode (double oldX, double oldY, double newX, double newY) {

		double radians;
		double length;

		double radiansToTurn = 0;

		if (newX == oldX) {
			radians = (newY - oldY < 0)? 0 : (currentAngle < 0)? -Math.PI : Math.PI;
		} else {
			radians = Math.atan((newY - oldY) / (newX - oldX));


            if (newX > oldX) {
                if (radians < 0) {
                    radians = Math.PI / 2 - Math.abs(radians);
                } else {
                    radians += Math.PI / 2;
                }
            } else {
                if (radians < 0) {
                    radians = (Math.PI / 2 - Math.abs(radians)) + Math.PI;
                } else {
                    radians += 3 * Math.PI / 2;
                }
            }

		}

        radiansToTurn = radians - currentAngle;

//        if (currentAngle / radians >= 0) {
//            radiansToTurn = Math.abs(radians - currentAngle);
//            if (radians < currentAngle) {
//                radiansToTurn *= -1;
//            }
//        } else if (newY > oldY) {
//            radiansToTurn = 2*Math.PI - Math.abs(radians) - Math.abs(currentAngle);
//            if (radians > currentAngle) {
//                radiansToTurn *= -1;
//            }
//        } else {
//            radiansToTurn = Math.abs(radians) + Math.abs(currentAngle);
//            if (radians < currentAngle) {
//                radiansToTurn *= -1;
//            }
//        }
//
//        System.out.println("Abs. Rad: " + radians);
//        System.out.println("Current: " + currentAngle);
//        System.out.println("Turn Rad: " + radiansToTurn);

		if (radiansToTurn > Math.PI) {
			radiansToTurn = 2 * Math.PI - radiansToTurn;
			radiansToTurn *= -1;
		} else if (radiansToTurn < -Math.PI) {
			radiansToTurn = 2 * Math.PI + radiansToTurn;
		}

        if (Math.toDegrees(Math.abs(radiansToTurn)) < 8) {
            radiansToTurn = 0;
        }

		length = Math.sqrt(Math.pow(newX - oldX, 2) + Math.pow(newY - oldY, 2));
		length *= 144.0 / (fieldWidth);
		length *= 25.4;


        if (radiansToTurn != 0) {
            if (stage > 0) {
				code += " else if (stage == " + stage++ + " && " + robotName + "lily.allReady()) {\n" +
						"   ";

				if (radiansToTurn < 0) {
					String toProcess = turnLEntry.getText();
					code += "   " + toProcess.replace("[degrees]", Math.toDegrees(Math.abs(radiansToTurn)) + "").replace("[radians]", "" + Math.abs(radiansToTurn)).replace("[speed]", "0.25") + ";\n";
				} else if (radiansToTurn > 0){
					String toProcess = turnREntry.getText();
					code += "   " + toProcess.replace("[degrees]", Math.toDegrees(Math.abs(radiansToTurn)) + "").replace("[radians]", "" + Math.abs(radiansToTurn)).replace("[speed]", "0.25") + ";\n";
				}

				code += "   stage++;\n}";
			}
        }

		if (stage > 0) {
			code += " else if (stage == " + stage++ + " && lily.allReady()) {\n" +
					"   ";
		} else {
			code += " if (stage == " + stage++ + " && " + robotName + "lily.allReady()) {\n" +
					"   ";
		}
        
        if (length != 0) {

            String toProcess = (movingBackward)? moveBackwardEntry.getText() : moveForwardEntry.getText();

            code += "   " + toProcess.replace("[mm]", "" + (int) length + ".0").replace("[cm]", "" + (int) (length / 10) + ".0").replace("[speed]", "0.5") + ";\n";

		}

        code += "   stage++;\n}";

        oldAngles.add(currentAngle);
		currentAngle = radians;

		codeDisplay.setText(code.replace("?||?", ""));
	}


	public static void submitSettings() {

		String toWrite = "";

		toWrite += moveForwardEntry.getText() + "\n" +
					moveBackwardEntry.getText() + "\n" +
					turnLEntry.getText() + "\n" +
					turnREntry.getText() + "\nAPPEARANCE\n";

		try {
			strokeSize = Integer.parseInt(lineStroke.getText());
			p.repaint();
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(settings, "You didn't enter a number for the line stroke thickness!");
		}

		toWrite += "StrokeSize: " + strokeSize + "\n";

		try {

			pointColour = stringArrayToIntArray(pointColourSet.getText().split(","));

		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(settings, "You didn't enter the point colour properly!");
		}

		toWrite += "PointColour: {" + pointColour[0] + "," + pointColour[1] + "," + pointColour[2] + "," + pointColour[3] + "}\n";

		try {

			lineColour = stringArrayToIntArray(lineColourSet.getText().split(","));

		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(settings, "You didn't enter the line colour properly!");
		}
		toWrite += "LineColour: {" + lineColour[0] + "," + lineColour[1] + "," + lineColour[2] + "," + lineColour[3] + "}\n";

        try {

            spawnColour = stringArrayToIntArray(spawnColourSet.getText().split(","));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(settings, "You didn't enter the spawn point colour properly!");
        }
        toWrite += "SpawnColour: {" + spawnColour[0] + "," + spawnColour[1] + "," + spawnColour[2] + "," + spawnColour[3] + "}\n";

        try {

            diagramNumberColour = stringArrayToIntArray(diagramNumberColourSet.getText().split(","));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(settings, "You didn't enter the diagram number colour properly!");
        }
        toWrite += "DiagramNumberColour: {" + diagramNumberColour[0] + "," + diagramNumberColour[1] + "," + diagramNumberColour[2] + "," + diagramNumberColour[3] + "}\n";

		pointSize = Integer.parseInt(pointSizeSet.getText());

		toWrite += "PointSize: " + pointSize + "\n";

		try {
			out = new BufferedWriter(new FileWriter("settings.txt"));

			out.write(toWrite);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String[] readInSettings() {
		String[] methods = new String[4];

		try {
			in = new BufferedReader(new FileReader("settings.txt"));
			in.mark(Short.MAX_VALUE);

			String toAdd = in.readLine();
			System.out.println(toAdd);
			String[] total = new String[11];
			int count = 0;


			while (toAdd != null) {
				total[count++] = toAdd;
				toAdd = in.readLine();
			}

			methods = new String[]{total[0], total[1], total[2], total[3]};
			strokeSize = Integer.parseInt(total[5].replace("StrokeSize: ", ""));
			pointColour = stringArrayToIntArray(total[6].replace("PointColour: {", "").replace("}", "").split(","));
			lineColour = stringArrayToIntArray(total[7].replace("LineColour: {", "").replace("}", "").split(","));
			spawnColour = stringArrayToIntArray(total[8].replace("SpawnColour: {", "").replace("}", "").split(","));
            diagramNumberColour = stringArrayToIntArray(total[9].replace("DiagramNumberColour: {", "").replace("}", "").split(","));
			pointSize = Integer.parseInt(total[10].replace("PointSize: ", ""));

			lineStroke.setText("" + strokeSize);
			pointColourSet.setText(pointColour[0] + "," + pointColour[1] + "," + pointColour[2] + "," + pointColour[3]);
			lineColourSet.setText(lineColour[0] + "," + lineColour[1] + "," + lineColour[2] + "," + lineColour[3]);
			spawnColourSet.setText(spawnColour[0] + "," + spawnColour[1] + "," + spawnColour[2] + "," + spawnColour[3]);
            diagramNumberColourSet.setText(diagramNumberColour[0] + "," + diagramNumberColour[1] + "," + diagramNumberColour[2] + "," + diagramNumberColour[3]);
			pointSizeSet.setText("" + pointSize);

			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return methods;

	}

	public static void undoMove() {

		if (oldAngles.size() == 0) {
			resetMoves();
			return;
		}

		points.remove(points.size() - 1);

		String analyze = code.substring(code.lastIndexOf("?||?"));

		int numStagesFound = (analyze.length() - analyze.replaceAll("stage++", "").length()) / 7;

		if (numStagesFound > 1) {
			stage--;
		}

		code = code.substring(0, code.lastIndexOf("?||?"));

		currentAngle = oldAngles.get(oldAngles.size() - 1);
		oldAngles.remove(oldAngles.size() - 1);

		stage--;
	}

	public static void resetMoves() {
		points.clear();
		code = "";

		if (!movingBackward) {
			currentAngle = 0;
		} else {
			currentAngle = Math.PI;
		}

		stage = 0;
	}

	public static int[] stringArrayToIntArray(String[] array) throws NumberFormatException {
		int[] intarray = new int[array.length];

		for (int i = 0; i < array.length; i++) {
			intarray[i] = Integer.parseInt(array[i]);
		}

		return intarray;
	}

	public static void paintPanel (Graphics2D g) {

        g.drawImage(field, 0, 0, fieldWidth, fieldWidth, p);
        g.setStroke(new BasicStroke(strokeSize));

        for (int i = 1; i < points.size(); i++) {

			g.setColor(new Color(pointColour[0], pointColour[1], pointColour[2], pointColour[3]));
            g.fillRect((points.get(i) % f.getWidth()) - pointSize / 2,
                    (points.get(i) / f.getWidth()) - pointSize / 2,
                    pointSize, pointSize);

			g.setColor(new Color(lineColour[0], lineColour[1], lineColour[2], lineColour[3]));
			g.drawLine((points.get(i) % f.getWidth()),
					(points.get(i) / f.getWidth()),
					(points.get(i - 1) % f.getWidth()),
					(points.get(i - 1) / f.getWidth()));
        }

		if (!points.isEmpty()) {
			g.setColor(new Color(spawnColour[0], spawnColour[1], spawnColour[2], spawnColour[3]));
			g.fillRect((points.get(0) % f.getWidth()) - pointSize / 2,
					(points.get(0) / f.getWidth()) - pointSize / 2,
					pointSize, pointSize);
		}

        if (currentlyExportingDiagram) {
            g.setColor(new Color(diagramNumberColour[0], diagramNumberColour[1], diagramNumberColour[2], diagramNumberColour[3]));
            g.setFont(new Font("Arial", 24, 24));
            for (int i = 0; i < points.size(); i++) {
                Rectangle2D stringSize = g.getFontMetrics().getStringBounds("" + i, g);
                g.drawString("" + (i + 1), (int) ((points.get(i) % f.getWidth()) - stringSize.getWidth() / 2), (int) ((points.get(i) / f.getWidth()) + stringSize.getHeight() / 3));
            }
        }
	}

	private static class ActionHandler implements ActionListener {

		public void actionPerformed(ActionEvent a) {

			if (a.getSource().equals(undo) && points.size() > 0) {
				undoMove();

			} else if (a.getSource().equals(reset)) {
				resetMoves();
			} else if (a.getSource().equals(comment)) {
				String reminder = JOptionPane.showInputDialog(f, "Please enter comment.");
				if (reminder != null) {

					code += " else if (stage == " + stage++ + " && " + robotName + "lily.allReady()) {\n" +
							"   ";
					code += "//TODO: " + reminder + "\n";

					code += "   stage++;\n}";
				}
			} else if (a.getSource().equals(programSettings)) {
				settings.setVisible(true);
			} else if (a.getSource().equals(switchDirection)) {

				if (!movingBackward) {
					switchDirection.setText("Drive Forward");
					movingBackward = true;
					currentAngle += Math.PI;
					currentAngle %= 2 * Math.PI;
				} else {
					switchDirection.setText("Drive Backward");
					movingBackward = false;
					currentAngle -= Math.PI;
					currentAngle %= 2 * Math.PI;
				}

            } else if (a.getSource().equals(exportImage)) {

				BufferedImage image = new BufferedImage(p.getWidth(), p.getHeight(), BufferedImage.TYPE_INT_ARGB);
                currentlyExportingDiagram = true;
				p.paint(image.getGraphics());
                currentlyExportingDiagram = false;

				String name = JOptionPane.showInputDialog("Please name the diagram");

				if (name != null) {
					try {
						ImageIO.write(image, "PNG", new File("diagrams/" + name + ".png"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			} else if (a.getSource().equals(submitSettings)) {
				submitSettings();
				settings.setVisible(false);
			}

			codeDisplay.setText(code.replace("?||?", ""));

			p.repaint();
		}

	}

}
