import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import utilitaireAgreg.MaBibliothequeTraitementImage;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class Bibliotheque {
	//Contient toutes les méthodes necessaires à la transformation des images


	//Methode qui permet de transformer une matrice intialement au  format BGR au format HSV
	public static Mat transformeBGRversHSV(Mat matriceBGR){
		Mat matriceHSV=new Mat(matriceBGR.height(),matriceBGR.cols(),matriceBGR.type());
		Imgproc.cvtColor(matriceBGR,matriceHSV,Imgproc.COLOR_BGR2HSV);
		return matriceHSV;

	}

	//Methode qui convertit une matrice avec 3 canaux en un vecteur de 3 matrices monocanal (un canal par couleur)
	public static Vector<Mat> splitHSVChannels(Mat input) {
		Vector<Mat> channels = new Vector<Mat>(); 
		Core.split(input, channels);
		return channels;
	}

	//Methode qui permet d'afficher une image sur un panel
	public static void afficheImage(String title, Mat img){
		MatOfByte matOfByte=new MatOfByte();
		Highgui.imencode(".png",img,matOfByte);
		byte[] byteArray=matOfByte.toArray();
		BufferedImage bufImage=null;
		try{
			InputStream in=new ByteArrayInputStream(byteArray);
			bufImage=ImageIO.read(in);
			JFrame frame=new JFrame();
			frame.setTitle(title);
			frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
			frame.pack();
			frame.setVisible(true);

		}
		catch(Exception e){
			e.printStackTrace();
		}


	}

	

	//Methode qui permet d'extraire les contours d'une image donnee
	public static List<MatOfPoint> ExtractContours(Mat input) {
		// Detecter les contours des formes trouvées
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
		//afficheImage("Contours",drawing);

		return contours;
	}

	//Methode qui permet de decouper et identifier les contours carrés, triangulaires ou rectangulaires. 
	//Renvoie null si aucun contour rond n'a été trouvé.	
	//Renvoie une matrice carrée englobant un contour rond si un contour rond a été trouvé
	public static Mat DetectForm(Mat img,MatOfPoint contour) {
		MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		float[] radius = new float[1];
		Point center = new Point();
		Rect rect = Imgproc.boundingRect(contour);
		double contourArea = Imgproc.contourArea(contour);


		matOfPoint2f.fromList(contour.toList());
		// Cherche le plus petit cercle entourant le contour
		Imgproc.minEnclosingCircle(matOfPoint2f, center, radius);
		//System.out.println(contourArea+" "+Math.PI*radius[0]*radius[0]);
		//on dit que c'est un cercle si l'aire occupé par le contour est à supérieure à  80% de l'aire occupée par un cercle parfait
		if ((contourArea / (Math.PI*radius[0]*radius[0])) >=0.8) {
			//System.out.println("Cercle");
			Core.circle(img, center, (int)radius[0], new Scalar(255, 0, 0), 2);
			Core.rectangle(img, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar (0, 255, 0), 2);
			Mat tmp = img.submat(rect.y,rect.y+rect.height,rect.x,rect.x+rect.width);
			Mat sign = Mat.zeros(tmp.size(),tmp.type());
			tmp.copyTo(sign);
			return sign;
		}else {

			Imgproc.approxPolyDP(matOfPoint2f, approxCurve, Imgproc.arcLength(matOfPoint2f, true) * 0.02, true);
			long total = approxCurve.total();
			if (total == 3 ) { // is triangle
				//System.out.println("Triangle");
				Point [] pt = approxCurve.toArray();
				Core.line(img, pt[0], pt[1], new Scalar(255,0,0),2);
				Core.line(img, pt[1], pt[2], new Scalar(255,0,0),2);
				Core.line(img, pt[2], pt[0], new Scalar(255,0,0),2);
				Core.rectangle(img, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar (0, 255, 0), 2);
				Mat tmp = img.submat(rect.y,rect.y+rect.height,rect.x,rect.x+rect.width);
				Mat sign = Mat.zeros(tmp.size(),tmp.type());
				tmp.copyTo(sign);
				return null;
			}
			if (total >= 4 && total <= 6) {
				List<Double> cos = new ArrayList<>();
				Point[] points = approxCurve.toArray();
				for (int j = 2; j < total + 1; j++) {
					cos.add(angle(points[(int) (j % total)], points[j - 2], points[j - 1]));
				}
				Collections.sort(cos);
				Double minCos = cos.get(0);
				Double maxCos = cos.get(cos.size() - 1);
				boolean isRect = total == 4 && minCos >= -0.1 && maxCos <= 0.3;
				boolean isPolygon = (total == 5 && minCos >= -0.34 && maxCos <= -0.27) || (total == 6 && minCos >= -0.55 && maxCos <= -0.45);
				if (isRect) {
					double ratio = Math.abs(1 - (double) rect.width / rect.height);
					//drawText(rect.tl(), ratio <= 0.02 ? "SQU" : "RECT");
					//System.out.println("Rectangle");
					Core.rectangle(img, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar (0, 255, 0), 2);
					Mat tmp = img.submat(rect.y,rect.y+rect.height,rect.x,rect.x+rect.width);
					Mat sign = Mat.zeros(tmp.size(),tmp.type());
					tmp.copyTo(sign);
					return null;
				}
				if (isPolygon) {
					//System.out.println("Polygon");
					//drawText(rect.tl(), "Polygon");
				}
			}			
		}
		return null;

	}

	

	public static double angle(Point a, Point b, Point c) {
		Point ab = new Point( b.x - a.x, b.y - a.y );
		Point cb = new Point( b.x - c.x, b.y - c.y );
		double dot = (ab.x * cb.x + ab.y * cb.y); // dot product
		double cross = (ab.x * cb.y - ab.y * cb.x); // cross product
		double alpha = Math.atan2(cross, dot);
		return Math.floor(alpha * 180. / Math.PI + 0.5);
	}

	
	//methode à completer
	public static double Similitude(Mat object,String signfile) {

		// Conversion du signe de reference en niveaux de gris et normalisation
		Mat panneauref = Highgui.imread(signfile);
		Mat graySign = new Mat(panneauref.rows(), panneauref.cols(), panneauref.type());
		Imgproc.cvtColor(panneauref, graySign, Imgproc.COLOR_BGRA2GRAY);
		Core.normalize(graySign, graySign, 0, 255, Core.NORM_MINMAX);
		Mat signeNoirEtBlanc=new Mat();
		


		//Conversion du panneau extrait de l'image en gris et normalisation et redimensionnement à la taille du panneau de réference
		Mat sObject=new Mat();
		Imgproc.resize(object, sObject,panneauref.size() );
		Mat grayObject = new Mat(panneauref.rows(), panneauref.cols(), panneauref.type());
		Imgproc.resize(object, object, graySign.size());
		//afficheImage("Panneau extrait de l'image",object);
		Imgproc.cvtColor(object, grayObject, Imgproc.COLOR_BGRA2GRAY);
		Core.normalize(grayObject, grayObject, 0, 255, Core.NORM_MINMAX);
		//Imgproc.resize(grayObject, grayObject, graySign.size());	
		
		
		
		
		//à compléter...
		/*Mat mat=new Mat();
		Core.bitwise_xor(grayObject,graySign,mat);
		int score=0;
		score=Core.countNonZero(mat);
		return (mat.rows()*mat.cols())-score;*/
		
		//extraction des caractéristiques
	      
		FeatureDetector orbDetector =FeatureDetector.create(FeatureDetector.ORB);
		DescriptorExtractor orbExtractor =DescriptorExtractor.create(DescriptorExtractor.ORB);
		MatOfKeyPoint objectKeypoints =new MatOfKeyPoint();
		orbDetector.detect(grayObject , objectKeypoints);
		
		MatOfKeyPoint signKeypoints =new MatOfKeyPoint();
		orbDetector.detect(graySign , signKeypoints);
		
		Mat objectDescriptor =new Mat (object.rows(),object.cols(),object.type());
		orbExtractor.compute(grayObject, objectKeypoints,  objectDescriptor);
		
		Mat signDescriptor =new Mat (panneauref.rows(),panneauref.cols(),panneauref.type());
		orbExtractor.compute(graySign, signKeypoints,  signDescriptor);
		
		//Matching
		MatOfDMatch matchs =new MatOfDMatch();
		DescriptorMatcher matcher =DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
		matcher.match(objectDescriptor, signDescriptor,matchs);
		//System.out.println(matchs.dump());
		Mat matchedImage =new Mat(panneauref.rows(),panneauref.cols()*2,panneauref.type());
		Features2d.drawMatches(sObject, objectKeypoints,panneauref,signKeypoints,matchs,matchedImage);
		//afficheImage("match", matchedImage);
		
		float somme=0;
		
		float moyenne=0;;
		List<DMatch>  l = matchs.toList();
		
		
		for(int i=0;i<l.size();i++)
		{ DMatch dmatch= l.get(i);
		   somme=somme+dmatch.distance;
		
		
		}
		moyenne=somme/l.size();
		
		return moyenne;
		


	}

	private static void Imshow(String string, Mat matchedImage) {
		// TODO Auto-generated method stub
		
	}

	public static BufferedImage Mat2bufferedImage(Mat image) {
		MatOfByte bytemat = new MatOfByte();
		Highgui.imencode(".jpg", image, bytemat);
		byte[] bytes = bytemat.toArray();
		InputStream in = new ByteArrayInputStream(bytes);
		BufferedImage img = null;
		try {
			img = ImageIO.read(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return img;
	}
	
	public static Mat seuillage(Mat input, int seuilRougeOrange, int seuilRougeViolet,int seuilSaturation){
		//à completer
		// Decomposition en 3 cannaux HSV
		Vector<Mat> channels = splitHSVChannels(input);
		//création d'un seuil 
		Scalar rougeviolet = new Scalar(seuilRougeViolet);
		Scalar rougeorange = new Scalar(seuilRougeOrange);
		Scalar saturation = new Scalar(seuilSaturation);
		//Création d'une matrice
		Mat rouges=new Mat();
		//Comparaison et saturation des pixels dont la composante rouge est plus grande que le seuil rougeViolet
		Core.compare(channels.get(0), rougeviolet, rouges, Core.CMP_GT);
		Mat rouges2=new Mat();
		Core.compare(channels.get(0), rougeorange, rouges2, Core.CMP_LT);
		Mat mat=new Mat();
		Core.bitwise_or(rouges2, rouges,mat);
		Mat sat=new Mat();
		Core.compare(channels.get(1), saturation, sat, Core.CMP_GT);
		//image saturée à retourner
		Core.bitwise_and(mat, sat, mat);
		return mat;

	}
	//Méthode XOR
	public static double Similitude2(Mat object,String signfile) {

		// Conversion du signe de reference en niveaux de gris et normalisation
		Mat panneauref = Highgui.imread(signfile);
		Mat graySign = new Mat(panneauref.rows(), panneauref.cols(), panneauref.type());
		Imgproc.cvtColor(panneauref, graySign, Imgproc.COLOR_BGRA2GRAY);
		Core.normalize(graySign, graySign, 0, 255, Core.NORM_MINMAX);
		Mat signeNoirEtBlanc=new Mat();
		


		//Conversion du panneau extrait de l'image en gris et normalisation et redimensionnement à la taille du panneau de réference
		Mat sObject=new Mat();
		Imgproc.resize(object, sObject,panneauref.size() );
		Mat grayObject = new Mat(panneauref.rows(), panneauref.cols(), panneauref.type());
		Imgproc.resize(object, object, graySign.size());
		//afficheImage("Panneau extrait de l'image",object);
		Imgproc.cvtColor(object, grayObject, Imgproc.COLOR_BGRA2GRAY);
		Core.normalize(grayObject, grayObject, 0, 255, Core.NORM_MINMAX);
		//Imgproc.resize(grayObject, grayObject, graySign.size());	
		
		
		Mat mat=new Mat();
		Core.bitwise_xor(grayObject,graySign,mat);
		int score=0;
		score=Core.countNonZero(mat);
		return score;
	}
	
	public static int identifiepanneau(Mat objetrond){
		double [] scores=new double [6];
		int indexmax=-1;
		if (objetrond!=null){
			/*scores[0]=Similitude(objetrond,"ref30.jpg");
			scores[1]=Similitude(objetrond,"ref50.jpg");
			scores[2]=Similitude(objetrond,"ref70.jpg");
			scores[3]= Similitude(objetrond,"ref90.jpg");
			scores[4]=Similitude(objetrond,"ref110.jpg");
			scores[5]=Similitude(objetrond,"refdouble.jpg");*/
			
			scores[0]=10000-MaBibliothequeTraitementImage.tauxDeSimilitude(objetrond,"ref30.jpg");
			scores[1]=10000-MaBibliothequeTraitementImage.tauxDeSimilitude(objetrond,"ref50.jpg");
			scores[2]=10000-MaBibliothequeTraitementImage.tauxDeSimilitude(objetrond,"ref70.jpg");
			scores[3]=10000-MaBibliothequeTraitementImage.tauxDeSimilitude(objetrond,"ref90.jpg");
			scores[4]=10000-MaBibliothequeTraitementImage.tauxDeSimilitude(objetrond,"ref110.jpg");
			scores[5]=10000-MaBibliothequeTraitementImage.tauxDeSimilitude(objetrond,"refdouble.jpg");
			

			double scoremax=Double.POSITIVE_INFINITY;

			for(int j=0;j<scores.length;j++){
				if (scores[j]<scoremax){
					scoremax=scores[j];
					indexmax=j;}}	


		}
		return indexmax;
	}
	
	
	//METHODE 3 avec  TESSERACT
	
    public static String reconnaissance(Mat Image) {
    	String tout = "";
    	int i = 100;
    	
    	Mat Im = transformation(Image,i);
    	Tesseract tesseract = new Tesseract();
    	String text = "";
    	 try {
    		  
             tesseract.setDatapath("C:\\Users\\STAGIAIRE\\Documents\\Tess4J\\tessdata");
             // the path of your tess data folder
             // inside the extracted file
             BufferedImage Atester = Mat2bufferedImage(Im);
             afficheImage("test",Im);
            tout = text + tesseract.doOCR(Atester);
             // path of your image file
         }
         catch (TesseractException e) {
             e.printStackTrace();
         }
    	 
    	 
    	 return tout;
    }
    
    public static Mat transformation(Mat imageOriginale,int seuil)
    {
	
	Mat imageTransformee=transformeBGRversHSV(imageOriginale);
	Mat Image = seuillage_V(imageTransformee, seuil);
	/*for (int i = 0;i<Image.height();i++){
		for (int j = 0;j<Image.width();j++){
			if(i<30 || i>130||j<30||j>130){
				Image.put(i,j,255);
			}
		}
	}*/
	return Image;
    }
    
    public static Mat seuillage_V(Mat input, int seuilRougeViolet){
		// Decomposition en 3 cannaux HSV
		Vector<Mat> channels =splitHSVChannels(input);
		//création d'un seuil 
		Scalar rougeviolet = new Scalar(seuilRougeViolet);
		//Création d'une matrice
		Mat rouges=new Mat();
		//Comparaison et saturation des pixels dont la composante rouge est plus grande que le seuil rougeViolet
		Core.compare(channels.get(2), rougeviolet, rouges, Core.CMP_GT);
		//image saturée à retourner
		return rouges;

	}


} 

