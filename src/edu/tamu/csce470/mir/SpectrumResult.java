package edu.tamu.csce470.mir;

import java.io.Serializable;
import java.util.ArrayList;

public class SpectrumResult implements Serializable
{
	private static final long serialVersionUID = -7520960371400627733L;
	
	public ArrayList<Integer> absorbancies;
	public double startWavelength;
	public double endWavelength;

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
		
		absorbancies = new ArrayList<Integer>(settings.endPixel - settings.startPixel);
		for (int i = settings.startPixel; i <= settings.endPixel; i++)
		{
			absorbancies.add(fullAbsorbancies.get(i));
		}
	}
	
	int getAbsorbanceAtWavelength(double wavelength)
	{
		assert(wavelength >= startWavelength && wavelength <= endWavelength);
		
		double deltaWavelength = (endWavelength - startWavelength) / absorbancies.size();
		
		// wavelength == startWavelength + deltaWavelength * exactIndex
		double exactIndex = (wavelength - startWavelength) / deltaWavelength;
		assert(exactIndex >= 0.0 && exactIndex < absorbancies.size());
		
		int index = (int) Math.round(exactIndex);
		
		return absorbancies.get(index);
	}
	
	double getWavelengthAtIndex(int index)
	{
		assert(index >= 0);
		assert(index < absorbancies.size());
		
		double wavelength = startWavelength + (startWavelength - endWavelength) * index;
		
		return wavelength;
	}
}
