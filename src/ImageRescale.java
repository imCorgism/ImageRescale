import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Scanner;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

public class ImageRescale 
{

	//Main
	public static void main(String args[])
	{
		//Get the input file directory
		System.out.println("Enter the input file directory : ");
		Scanner inFileDir = new Scanner(System.in);
	    	String inputFileDirectory = inFileDir.nextLine();
	    	File inputDirectory = new File(inputFileDirectory);
	    
	    	//Get the output file directory
		System.out.println("Enter the output file directory : ");
		Scanner outFileDir = new Scanner(System.in);
	    	String outputFileDirectory = outFileDir.nextLine();
	    	File outputDirectory = new File(outputFileDirectory);
	    	outputDirectory.mkdir();
		
	    	//Get the scale factor
	    	System.out.println("Enter the scale factor : ");
		Scanner scaleVal = new Scanner(System.in);
	    	double scaleFactor = Double.parseDouble(scaleVal.nextLine());
	    
	    	//Rescaling
	    	System.out.println("**Start**");
		for (File inputFile : inputDirectory.listFiles()) 
	    	{
			if (inputFile.isFile())
			    imageRescaleProcess(inputFile, outputDirectory, scaleFactor);
	    	}
		System.out.println("**End**");
	}
	
	//Function : Write Image
	public static void writeImage(File outputFile, BufferedImage image) throws FileNotFoundException, IOException 
	{
		//Extract the file extension 
		String name = outputFile.getName().toLowerCase();
		String suffix = name.substring(name.lastIndexOf('.') + 1);
		
		//JPG extension check
		boolean isJPG = suffix.endsWith("jpg");
		
		//Image writer
		Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix(suffix);
		if (!writers.hasNext())
			System.err.println("Unrecognized image file extension " + suffix);

		//Create output file
		outputFile.createNewFile();
		ImageWriter writer = writers.next();
		writer.setOutput(new FileImageOutputStream(outputFile));

		//Get writer parameters
		ImageWriteParam param = writer.getDefaultWriteParam();
		
		//Set compression for .jpg extensions
		if (isJPG) 
		{
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(0.5f); //High quality
		}
		
		//Write file
		IIOImage iioImage = new IIOImage(image, null, null);
		writer.write(null, iioImage, param);
	}
	
	//Function : Image Rescale
	public static void imageRescale(File inputFile, File outputFile, double scaleFactor)
	{
		System.out.println("Rescaling process started for " + inputFile);
		
		try 
		{
			//Read the input file
			BufferedImage source = ImageIO.read(inputFile);
			
			//Check for Alpha
			boolean hasAlpha = source.getColorModel().hasAlpha();

			//Get image width & height
			int sourceW = source.getWidth();
			int sourceH = source.getHeight();

			//Calculate new width & height
			int w = (int) (sourceW * scaleFactor + 0.5);
			int h = (int) (sourceH * scaleFactor + 0.5);
			
			//Find the output file format
			int format = hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

			//Perform rescaling using bicubic interpolation
			BufferedImage output = new BufferedImage(w, h, format);
			Graphics2D g = output.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.drawImage(source, 0, 0, w, h, null);
			
			//Write the image file
			writeImage(outputFile, output);
		} 
		catch (IOException e) 
		{
			System.err.println(e.getMessage());
			return;
		}
	}
	
	//Function : Image Rescale Process 
	public static void imageRescaleProcess(File inputFile, File outputDirectory, double scaleFactor)
	{
		//Create a sub directory in the output directory
		Calendar calendar = Calendar.getInstance();
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");//("yyyyMMddHHmmss");
	    	String time = sdf.format(calendar.getTime()).toString();
	    	File subDir = new File(outputDirectory, time + "_s" + scaleFactor);
		subDir.mkdir();
		System.out.println("Created sub directory " + subDir + " in output directory " + outputDirectory);
		
		//Use the input filename as the output filename
		String fileName = inputFile.getName();
		File outputFile = new File(subDir, fileName);
		imageRescale(inputFile, outputFile, scaleFactor);
		System.out.println("Rescaling process complete for " + inputFile);		
	}	

}
