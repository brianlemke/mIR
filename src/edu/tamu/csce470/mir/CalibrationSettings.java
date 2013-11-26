package edu.tamu.csce470.mir;

import java.io.Serializable;

public class CalibrationSettings implements Serializable
{
	private static final long serialVersionUID = 1579508071368959481L;
	
	// Image resolution information
	public int imageWidth;
	public int imageHeight;
	
	// Image location calibration
	public int startPixel;
	public int endPixel;
	public int sampleRow;
	
	// Wavelength calibration
	public int pixel1;
	public int pixel2;
	public double wavelength1;
	public double wavelength2;
	
	// Optional persisted baseline image
	public String baselineImagePath;
	
	public CalibrationSettings()
	{
		imageWidth = -1;
		imageHeight = -1;
		
		startPixel = -1;
		endPixel = -1;
		sampleRow = -1;
		
		pixel1 = -1;
		pixel2 = -1;
		wavelength1 = 0.0;
		wavelength2 = 0.0;
		
		baselineImagePath = "";
	}
	
	public boolean validateSettings()
	{
		boolean valid = true;
		
		if (imageWidth <= 0) valid = false;
		if (imageHeight <= 0) valid = false;
		if (startPixel < 0 || startPixel >= endPixel) valid = false;
		if (endPixel < 0 || endPixel <= startPixel) valid = false;
		if (sampleRow < 0) valid = false;
		if (pixel1 >= pixel2 || pixel1 < startPixel) valid = false;
		if (pixel2 <= pixel1 || pixel2 > endPixel) valid = false;
		if (wavelength1 <= 0.0 || wavelength1 >= wavelength2) valid = false;
		if (wavelength2 <= 0.0 || wavelength2 <= wavelength1) valid = false;
		
		return valid;
	}
}
