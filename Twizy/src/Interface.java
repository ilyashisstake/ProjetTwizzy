import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.opencv.core.*;
import org.opencv.highgui.*;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JTextArea;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Toolkit;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;


import java.awt.image.BufferedImage;



public class Interface extends JFrame {

	static boolean detecte = false;
	private JPanel contentPane;
	private JTextField imgFile;
	private JTextArea panneau;
	private JTextArea texte2;
	private JTextArea texte3;
	private JPanel panel1 ;
	private String path;
	private static JPanel panel2 ;
	
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Interface frame = new Interface();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	
	public Interface() throws IOException {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Twizzy");
		setBounds(0, 1, 1300, 700);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		contentPane.setBackground(Color.blue);
		JPanel panel = new JPanel();
		panel.setBounds(10, 10, 1260, 639);
		contentPane.add(panel);
		panel.setLayout(null);
		
		panel1 = new JPanel();
		panel1.setBackground(Color.lightGray);
		panel1.setBounds(274, 46, 880, 582); //emplacement image analysée
		panel.add(panel1);
		
		panel2 = new JPanel();
		panel2.setBackground(Color.lightGray);
		panel2.setBounds(10, 240, 254, 254); //emplacement panneau detecte methode1
		panel.add(panel2);
		path="";
		JButton btnChargerImage = new JButton("Charger une image");
		btnChargerImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser file = new JFileChooser();
				file.setCurrentDirectory(new File(System.getProperty("user.home")));
				file.setDialogTitle("Selectionnez une image");
			    file.setAcceptAllFileFilterUsed(false);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("*.Images","jpg","png");
				file.addChoosableFileFilter(filter);
				int result=file.showSaveDialog(null);
				if (result==JFileChooser.APPROVE_OPTION) {
					File selectedFile= file.getSelectedFile();
					path = selectedFile.getAbsolutePath();
				}
				else if(result==JFileChooser.CANCEL_OPTION){
					System.out.println("No File Select");
				}
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
				Mat m=Highgui.imread(path,Highgui.CV_LOAD_IMAGE_COLOR);
				panel1.removeAll();
				panel1.repaint();
				panel1.add(new JLabel(new ImageIcon(Bibliotheque.Mat2bufferedImage(m))));
				validate();
			}
		});

		btnChargerImage.setBounds(10, 40, 200, 20);
		panel.add(btnChargerImage);
		
		JButton btnNiveauGris = new JButton("Niveaux de Gris");
		btnNiveauGris.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
				Mat m=Highgui.imread(path,Highgui.CV_LOAD_IMAGE_COLOR);
				//Conversion du panneau extrait de l'image en gris et normalisation et redimensionnement à la taille du panneau de réference
				Mat grayObject = new Mat(m.rows(), m.cols(), m.type());
				Imgproc.cvtColor(m, grayObject, Imgproc.COLOR_BGRA2GRAY);
				panel1.removeAll();
				panel1.repaint();
				panel1.add(new JLabel(new ImageIcon(Bibliotheque.Mat2bufferedImage(grayObject))));
				validate();
			}
		});
		btnNiveauGris.setBounds(274, 10, 150, 20);
		panel.add(btnNiveauGris);
		
		JButton btnButtonHSV = new JButton("HSV");
		btnButtonHSV.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

				Mat imageOriginale=Highgui.imread(path,Highgui.CV_LOAD_IMAGE_COLOR);
				Mat imageTransformee=Bibliotheque.transformeBGRversHSV(imageOriginale);
				panel1.removeAll();
				panel1.repaint();
				panel1.add(new JLabel(new ImageIcon(Bibliotheque.Mat2bufferedImage(imageTransformee))));
				validate();
			}
		});
		btnButtonHSV.setBounds(434, 10, 150, 20);
		panel.add(btnButtonHSV);
		
		JButton btnSaturationRouge = new JButton("Saturation des rouges");
		btnSaturationRouge.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

				Mat imageOriginale=Highgui.imread(path,Highgui.CV_LOAD_IMAGE_COLOR);
				Mat imageTransformee=Bibliotheque.transformeBGRversHSV(imageOriginale);
				Mat imageSaturee=Bibliotheque.seuillage(imageTransformee, 6, 170, 110);
				panel1.removeAll();
				panel1.repaint();
				panel1.add(new JLabel(new ImageIcon(Bibliotheque.Mat2bufferedImage(imageSaturee))));
				validate();
			}
		});
		btnSaturationRouge.setBounds(594, 10, 200, 20);
		panel.add(btnSaturationRouge);
		
		JButton btnContours = new JButton("Detection des contours de l'image");
		btnContours.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panneau.setText("");
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

				Mat imageOriginale=Highgui.imread(path,Highgui.CV_LOAD_IMAGE_COLOR);
				Mat imageTransformee=Bibliotheque.transformeBGRversHSV(imageOriginale);
				Mat input=Bibliotheque.seuillage(imageTransformee, 6, 170, 110);
				int thresh = 100;
				Mat canny_output = new Mat();
				List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
				MatOfInt4 hierarchy = new MatOfInt4();
				Imgproc.Canny( input, canny_output, thresh, thresh*2);


				/// Find extreme outer contours
				Imgproc.findContours( canny_output, contours, hierarchy,Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

				Mat drawing = Mat.zeros( canny_output.size(), CvType.CV_8UC3 );
				Random rand = new Random();
				for( int i = 0; i< contours.size(); i++ )
				{
					Scalar color = new Scalar( rand.nextInt(255 - 0 + 1) , rand.nextInt(255 - 0 + 1),rand.nextInt(255 - 0 + 1) );
					Imgproc.drawContours( drawing, contours, i, color, 1, 8, hierarchy, 0, new Point() );
				}
				panel1.removeAll();
				panel1.repaint();
				panel1.add(new JLabel(new ImageIcon(Bibliotheque.Mat2bufferedImage(drawing))));
				validate();

			}
		});
		btnContours.setBounds(804, 10, 250, 20);
		panel.add(btnContours);
		
		panneau = new JTextArea();
		panneau.setBounds(10, 500, 254, 20);
		panel.add(panneau);
		panneau.setColumns(10);
		
		texte2 = new JTextArea();
		texte2.setBounds(10, 520, 254, 20);
		panel.add(texte2);
		texte2.setColumns(10);
		
		texte3 = new JTextArea();
		texte3.setBounds(10, 540, 254, 20);
		panel.add(texte3);
		texte3.setColumns(10);
		
		JButton btnMatching = new JButton("Detection méthode matching");
		btnMatching.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panneau.setText("");
				texte3.setText("");
				texte2.setText("");
				String fileImg = "";
				//Ouverture de l'image et saturation des rouges
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
				Mat m=Highgui.imread(path,Highgui.CV_LOAD_IMAGE_COLOR);
				Mat transformee=Bibliotheque.transformeBGRversHSV(m);
				//la methode seuillage est ici extraite de l'archivage jar du meme nom 
				Mat saturee=Bibliotheque.seuillage(transformee, 6, 170, 90);
				Mat objetrond = null;

				//Création d'une liste des contours à partir de l'image saturée
				List<MatOfPoint> ListeContours= Bibliotheque.ExtractContours(saturee);
				double [] scores=new double [6];
				//Pour tous les contours de la liste
				int nb=0;
				for (MatOfPoint contour: ListeContours  ){
					objetrond=Bibliotheque.DetectForm(m,contour);

					if (objetrond!=null){
						scores[0]=Bibliotheque.Similitude(objetrond,"ref30.jpg");
						scores[1]=Bibliotheque.Similitude(objetrond,"ref50.jpg");
						scores[2]=Bibliotheque.Similitude(objetrond,"ref70.jpg");
						scores[3]=Bibliotheque.Similitude(objetrond,"ref90.jpg");
						scores[4]=Bibliotheque.Similitude(objetrond,"ref110.jpg");
						scores[5]=Bibliotheque.Similitude(objetrond,"refdouble.jpg");


						//recherche de l'index du maximum et affichage du panneau detecté
						double scoremax=Double.POSITIVE_INFINITY;
						int indexmax=0;
						for(int j=0;j<scores.length;j++){
							if (scores[j]<scoremax){scoremax=scores[j];indexmax=j;}}	
						if(scoremax<0){System.out.println("Aucun Panneau détecté");}
						else{switch(indexmax){

						case -1:;break;
						case 0:
							nb=nb+1;
							if (nb==1) {
							panneau.setText("Panneau 30 détecté par la méthode matching");}
							if (nb==2) {
								texte2.setText("Panneau 30 détecté par la méthode matching");}
							if (nb==3) {
								texte3.setText("Panneau 30 détecté par la méthode matching");}
							fileImg="ref30.jpg";
							break;
						case 1:
							nb=nb+1;
							if (nb==1) {
								panneau.setText("Panneau 50 détecté par la méthode matching");}
							if (nb==2) {
									texte2.setText("Panneau 50 détecté par la méthode matching");}
							if (nb==3) {
									texte3.setText("Panneau 50 détecté par la méthode matching");}
							fileImg="ref50.jpg";
							break;
						case 2:
							nb=nb+1;
							if (nb==1) {
								panneau.setText("Panneau 70 détecté par la méthode matching");}
							if (nb==2) {
									texte2.setText("Panneau 70 détecté par la méthode matching");}
							if (nb==3) {
									texte3.setText("Panneau 70 détecté par la méthode matching");}
							fileImg="ref70.jpg";
							break;
						case 3:
							nb=nb+1;
							if (nb==1) {
								panneau.setText("Panneau 90 détecté par la méthode matching");}
								if (nb==2) {
									texte2.setText("Panneau 90 détecté par la méthode matching");}
								if (nb==3) {
									texte3.setText("Panneau 90 détecté par la méthode matching");}
							fileImg="ref90.jpg";
							break;
						case 4:
							nb=nb+1;
							if (nb==1) {
								panneau.setText("Panneau 110 détecté par la méthode matching");}
								if (nb==2) {
									texte2.setText("Panneau 110 détecté par la méthode matching");}
								if (nb==3) {
									texte3.setText("Panneau 110 détecté par la méthode matching");}
							fileImg="ref110.jpg";
							break;
						case 5:
							nb=nb+1;
							if (nb==1) {
							panneau.setText("Panneau interdiction de dépasser détecté par la méthode matching");}
							if (nb==2) {
								texte2.setText("Panneau interdiction de dépasser détecté par la méthode matching");}
							if (nb==3) {
								texte3.setText("Panneau interdiction de dépasser détecté par la méthode matching");}
							fileImg="refdouble.jpg";
							break;
						}
						}

					}
					
				}
				ImageIcon IMAGE = new ImageIcon(Toolkit.getDefaultToolkit().createImage(fileImg));
				panel2.removeAll();
				panel2.repaint();
				panel2.add(new JLabel(IMAGE));
				panel1.removeAll();
				panel1.repaint();
				panel1.add(new JLabel(new ImageIcon(Bibliotheque.Mat2bufferedImage(m))));
				validate();	
			}
		}
				);
		btnMatching.setBounds(35, 130, 200, 20);
		panel.add(btnMatching);
		
		
		JButton btnMatching2 = new JButton("Detection méthode XOR");
		btnMatching2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileImg = "";
				panneau.setText("");
				texte3.setText("");
				texte2.setText("");
				//Ouverture de l'image et saturation des rouges
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
				Mat m=Highgui.imread(path,Highgui.CV_LOAD_IMAGE_COLOR);
				Mat transformee=Bibliotheque.transformeBGRversHSV(m);
				//la methode seuillage est ici extraite de l'archivage jar du meme nom 
				Mat saturee=Bibliotheque.seuillage(transformee, 6, 170, 90);
				Mat objetrond = null;

				//Création d'une liste des contours à partir de l'image saturée
				List<MatOfPoint> ListeContours= Bibliotheque.ExtractContours(saturee);
				double [] scores=new double [6];
				int nb=0;
				//Pour tous les contours de la liste
				for (MatOfPoint contour: ListeContours  ){
					objetrond=Bibliotheque.DetectForm(m,contour);

					if (objetrond!=null){
						scores[0]=Bibliotheque.Similitude2(objetrond,"ref30.jpg");
						scores[1]=Bibliotheque.Similitude2(objetrond,"ref50.jpg");
						scores[2]=Bibliotheque.Similitude2(objetrond,"ref70.jpg");
						scores[3]=Bibliotheque.Similitude2(objetrond,"ref90.jpg");
						scores[4]=Bibliotheque.Similitude2(objetrond,"ref110.jpg");
						scores[5]=Bibliotheque.Similitude2(objetrond,"refdouble.jpg");


						//recherche de l'index du maximum et affichage du panneau detecté
						double scoremax=Double.POSITIVE_INFINITY;
						int indexmax=0;
						for(int j=0;j<scores.length;j++){
							if (scores[j]<scoremax){scoremax=scores[j];indexmax=j;}}	
						if(scoremax<0){System.out.println("Aucun Panneau détecté");}
						else{switch(indexmax){

						case -1:;break;
						case 0:
							nb=nb+1;
							if (nb==1) {
							panneau.setText("Panneau 30 détecté par la méthode XOR");}
							if (nb==2) {
								texte2.setText("Panneau 30 détecté par la méthode XOR");}
							if (nb==3) {
								texte3.setText("Panneau 30 détecté par la méthode XOR");}
							fileImg="ref30.jpg";
							break;
						case 1:
							nb=nb+1;
							if (nb==1) {
								panneau.setText("Panneau 50 détecté par la méthode XOR");}
							if (nb==2) {
									texte2.setText("Panneau 50 détecté par la méthode XOR");}
							if (nb==3) {
									texte3.setText("Panneau 50 détecté par la méthode XOR");}
							fileImg="ref50.jpg";
							break;
						case 2:
							nb=nb+1;
							if (nb==1) {
								panneau.setText("Panneau 70 détecté par la méthode XOR");}
							if (nb==2) {
									texte2.setText("Panneau 70 détecté par la méthode XOR");}
							if (nb==3) {
									texte3.setText("Panneau 70 détecté par la méthode XOR");}
							fileImg="ref70.jpg";
							break;
						case 3:
							nb=nb+1;
							if (nb==1) {
								panneau.setText("Panneau 90 détecté par la méthode XOR");}
								if (nb==2) {
									texte2.setText("Panneau 90 détecté par la méthode XOR");}
								if (nb==3) {
									texte3.setText("Panneau 90 détecté par la méthode XOR");}
							fileImg="ref90.jpg";
							break;
						case 4:
							nb=nb+1;
							if (nb==1) {
								panneau.setText("Panneau 110 détecté par la méthode XOR");}
								if (nb==2) {
									texte2.setText("Panneau 110 détecté par la méthode XOR");}
								if (nb==3) {
									texte3.setText("Panneau 110 détecté par la méthode XOR");}
							fileImg="ref110.jpg";
							break;
						case 5:
							nb=nb+1;
							if (nb==1) {
							panneau.setText("Panneau interdiction de dépasser détecté par la méthode XOR");}
							if (nb==2) {
								texte2.setText("Panneau interdiction de dépasser détecté par la méthode XOR");}
							if (nb==3) {
								texte3.setText("Panneau interdiction de dépasser détecté par la méthode XOR");}
							fileImg="refdouble.jpg";
							break;
						}
						}

					}
					
				}
				ImageIcon IMAGE = new ImageIcon(Toolkit.getDefaultToolkit().createImage(fileImg));
				panel2.removeAll();
				panel2.repaint();
				panel2.add(new JLabel(IMAGE));
				panel1.removeAll();
				panel1.repaint();
				panel1.add(new JLabel(new ImageIcon(Bibliotheque.Mat2bufferedImage(m))));
				validate();	
			}
		}
				);
		
		btnMatching2.setBounds(35, 170, 200, 20);
		panel.add(btnMatching2);

		JButton btnMatching3 = new JButton("Detection méthode OCR");
		btnMatching3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String fileImg1 = "";
				panneau.setText("");
				texte3.setText("");
				texte2.setText("");
				//Ouverture de l'image et saturation des rouges
				System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
				Mat m=Highgui.imread(path,Highgui.CV_LOAD_IMAGE_COLOR);
				//MaBibliothequeTraitementImageEtendue.afficheImage("Image testée", m);
				Mat transformee=Bibliotheque.transformeBGRversHSV(m);
				//la methode seuillage est ici extraite de l'archivage jar du meme nom 
				Mat saturee=Bibliotheque.seuillage(transformee, 6, 170, 90);
				Mat objetrond = null;

				//Création d'une liste des contours à partir de l'image saturée
				List<MatOfPoint> ListeContours= Bibliotheque.ExtractContours(saturee);
				double [] scores=new double [6];
				//Pour tous les contours de la liste
				BufferedImage transfo = (null);
				for (MatOfPoint contour: ListeContours  ){
					objetrond=Bibliotheque.DetectForm(m,contour);

					if (objetrond!=null){
						String value = Bibliotheque.reconnaissance(objetrond);
						/*Mat transformation = Preparation.transformation(objetrond);
						Function.afficheImage("Differents panneaux", transformation);*/
						System.out.println(value);
						if (value.contains("70")|| value.contains("7o") || value.contains("7O")) {
							panneau.setText("Panneau 70 détecté par la méthode OCR");
							fileImg1="ref70.jpg";
						}
						if (value.contains("90") || value.contains("9o")) {
							panneau.setText("Panneau 90 détecté par la méthode OCR");
							fileImg1="ref90.jpg";
						}
						if (value.contains("30")|| value.contains("3o")) {
							panneau.setText("Panneau 30 détecté par la méthode OCR");
							fileImg1="ref30.jpg";
						}
						if (value.contains("50") || value.contains("5o")) {
							panneau.setText("Panneau 50 détecté par la méthode OCR");
							fileImg1="ref50.jpg";
						}
						if (value.contains("110") || value.contains("11o")) {
							panneau.setText("Panneau 110 détecté par la méthode OCR");
							fileImg1="ref110.jpg";
						}
						/*switch(value) {
						case "30":
							panneau.setText("Panneau 30 détecté");
							fileImg1="ref30.jpg";
							break;
						case "50":
							panneau.setText("Panneau 50 détecté");
							fileImg1="ref50.jpg";
							break;
						case "70" :
							panneau.setText("Panneau 70 détecté");
							fileImg1="ref70.jpg";
							break;
						case "90" :
							panneau.setText("Panneau 90 détecté");
							fileImg1="ref90.jpg";
							break;
						case "110":
							panneau.setText("Panneau 110 détecté");
							fileImg1="ref110.jpg";
							break;
						}*/
						
					}
				}
				ImageIcon IMAGE1 = new ImageIcon(Toolkit.getDefaultToolkit().createImage(fileImg1));
				panel2.removeAll();
				panel2.repaint();
				panel2.add(new JLabel(IMAGE1));
				panel1.removeAll();
				panel1.repaint();
				panel1.add(new JLabel(new ImageIcon(Bibliotheque.Mat2bufferedImage(m))));
				validate();
			
			}
		}
				);
		btnMatching3.setBounds(35, 210, 200, 20);
		panel.add(btnMatching3);
		
		//Bouton Vidéo
		 JButton btnVideo = new JButton("Lire une vidéo");	
			btnVideo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
					System.loadLibrary("opencv_java2413");
					String pathvideo="";
					JFileChooser file = new JFileChooser();
					file.setCurrentDirectory(new File(System.getProperty("user.home")));
					file.setDialogTitle("Selectionnez une vidéo");
				    file.setAcceptAllFileFilterUsed(false);
					FileNameExtensionFilter filter = new FileNameExtensionFilter("*.Video","mp4","avi");
					file.addChoosableFileFilter(filter);
					int result=file.showSaveDialog(null);
					if (result==JFileChooser.APPROVE_OPTION) {
						File selectedFile= file.getSelectedFile();
						pathvideo = selectedFile.getAbsolutePath();
					}
					else if(result==JFileChooser.CANCEL_OPTION){
						System.out.println("No File Select");
					}
					//LectureVideo(pathvideo,panneau,panel1);
					System.load("C:\\Users\\STAGIAIRE\\Downloads\\opencv\\build\\x64\\vc14\\bin\\opencv_ffmpeg2413_64.dll");
					MyThread thread = new MyThread(pathvideo,panneau,panel1);
					thread.start();}
			});
		 btnVideo.setBounds(10, 70, 200, 20);
		 panel.add(btnVideo);
	}
	
	
	public static void LectureVideo(String nomVideo, JTextArea panneau, JPanel panel1 ) {
		System.load("C:\\Users\\STAGIAIRE\\Downloads\\opencv\\build\\x64\\vc14\\bin\\opencv_ffmpeg2413_64.dll");
		Mat frame = new Mat();
		VideoCapture camera = new VideoCapture(nomVideo);

		while (camera.read(frame)) {
			/*
			if (detecte==true) {
				for (int j=0;j<5;j++) {
					camera.read(frame);
				}
			}*/
			String fileImg = "";

			panel1.removeAll();

			
			panel1.add(new JLabel(new ImageIcon(Bibliotheque.Mat2bufferedImage(frame))));
			panel1.repaint();
			panel1.validate();


			Mat transformee=Bibliotheque.transformeBGRversHSV(frame);
			
			Mat saturee=Bibliotheque.seuillage(transformee, 6, 170, 90); 
			Mat objetrond = null;
			//Création d'une liste des contours à partir de l'image saturée
			List<MatOfPoint> ListeContours= Bibliotheque.ExtractContours(saturee);
			int indexmax=0;
			for (MatOfPoint contour: ListeContours  ){
				objetrond=Bibliotheque.DetectForm(frame,contour);
				indexmax=Bibliotheque.identifiepanneau(objetrond);
				if (indexmax>-1) {
					panel1.removeAll();
					panel1.add(new JLabel(new ImageIcon(Bibliotheque.Mat2bufferedImage(frame))));
					panel1.repaint();
					panel1.validate();
				}
				switch(indexmax){
				case -1:
					break;
				case 0:
					panneau.setText("Panneau 30 détecté");
					fileImg="ref30.jpg";
					break;
				case 1:
					panneau.setText("Panneau 50 détecté");
					fileImg="ref50.jpg";
					break;
				case 2:
					panneau.setText("Panneau 70 détecté");
					fileImg="ref70.jpg";
					break;
				case 3:
					panneau.setText("Panneau 90 détecté");
					fileImg="ref90.jpg";
					break;
				case 4:
					panneau.setText("Panneau 110 détecté");
					fileImg="ref110.jpg";
					break;
				case 5:
					panneau.setText("Panneau interdiction de dépasser détecté");
					fileImg="refdouble.jpg";
					break;
				}
				if (indexmax>=0) {
					detecte=true;
				}
			}
			
			
			
			panel2.removeAll();
			panel2.repaint();
			panel2.add(new JLabel(new ImageIcon(fileImg)));
			panel2.validate();	
		}
	}
	
	
	
}
