package edu.tamu.csce470.mir;

import java.io.Serializable;
import java.util.ArrayList;

public class SpectrumResult implements Serializable
{
	private static final long serialVersionUID = -7520960371400627733L;
	
	public ArrayList<Double> absorbancies;
	public double startWavelength;
	public double endWavelength;
	public String name;

	public SpectrumResult(Spectrum spectrum, CalibrationSettings settings)
	{
		assert(spectrum != null);
		assert(settings != null);
		assert(settings.validateSettings());
		
		ArrayList<Integer> fullAbsorbancies = spectrum.getAbsorbancies();
		assert(fullAbsorbancies != null && fullAbsorbancies.size() > 0);
		assert(settings.endPixel < fullAbsorbancies.size());
		
		double deltaWavelength = (settings.wavelength2 - settings.wavelength1) / (settings.pixel2 - settings.pixel1);
		assert(deltaWavelength > 0.0);
		
		int distanceStartTo1 = settings.pixel1 - settings.startPixel;
		assert(distanceStartTo1 >= 0);
		int distance2ToEnd = settings.endPixel - settings.pixel2;
		assert(distance2ToEnd >= 0);
		
		startWavelength = settings.wavelength1 - deltaWavelength * distanceStartTo1;
		assert(startWavelength <= settings.wavelength1);
		endWavelength = settings.wavelength2 + deltaWavelength * distance2ToEnd;
		assert(endWavelength >= settings.wavelength2);
		
		absorbancies = new ArrayList<Double>(settings.endPixel - settings.startPixel);
		for (int i = settings.startPixel; i <= settings.endPixel; i++)
		{
			absorbancies.add((double) fullAbsorbancies.get(i).intValue());
		}
	}
	
	public double getAbsorbanceAtWavelength(double wavelength)
	{
		assert(wavelength >= startWavelength && wavelength <= endWavelength);
		
		double deltaWavelength = (endWavelength - startWavelength) / absorbancies.size();
		
		// wavelength == startWavelength + deltaWavelength * exactIndex
		double exactIndex = (wavelength - startWavelength) / deltaWavelength;
		assert(exactIndex >= 0.0 && exactIndex < absorbancies.size());
		
		int index = (int) Math.round(exactIndex);
				
		return absorbancies.get(index);
	}
	
	public double getWavelengthAtIndex(int index)
	{
		assert(index >= 0);
		assert(index < absorbancies.size());
		
		double wavelength = startWavelength + index * (endWavelength - startWavelength) / absorbancies.size();
		
		return wavelength;
	}
	
	public void standardizeValues()
	{
		double mean = 0.0;
		for (int i = 0; i < absorbancies.size(); i++)
		{
			mean += absorbancies.get(i);
		}
		mean = mean / absorbancies.size();
		
		double variance = 0.0;
		for (int i = 0; i < absorbancies.size(); i++)
		{
			variance += Math.pow(absorbancies.get(i) - mean, 2);
		}
		variance = variance / absorbancies.size();
		
		double standardDeviation = Math.sqrt(variance);
		
		for (int i = 0; i < absorbancies.size(); i++)
		{
			absorbancies.set(i, (absorbancies.get(i) - mean) / standardDeviation);
		}
	}
	
	public double getMeanStandardError(SpectrumResult other)
	{
		double error = 0.0;
		
		double lowSharedWavelength = Math.max(startWavelength, other.startWavelength);
		double highSharedWavelength = Math.min(endWavelength, other.endWavelength);
		
		int count = 0;
		for (int i = 0; i < absorbancies.size(); i++)
		{
			double wavelength = getWavelengthAtIndex(i);
			
			if (wavelength >= lowSharedWavelength && wavelength <= highSharedWavelength)
			{
				double diff = absorbancies.get(i) - other.getAbsorbanceAtWavelength(wavelength);
				error += Math.pow(diff, 2);
				count++;
			}
		}
		
		return error / count;
	}
}
