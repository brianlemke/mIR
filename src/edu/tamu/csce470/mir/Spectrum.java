package edu.tamu.csce470.mir;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Spectrum implements Parcelable
{
	// NOTE: If you add any class members, remember to add them
	// to the Parcelable functions as well.
	private ArrayList<Integer> baselineIntensities;
	private ArrayList<Integer> sampleIntensities;
	int sampleRow;
	
	Uri baselineImageUri;
	Uri sampleImageUri;
	
	enum DisplayMode { BASELINE_IMAGE, SAMPLE_IMAGE, SPECTRUM_GRAPH }
	
	DisplayMode displayMode;

	public Spectrum()
	{
		baselineIntensities = null;
		sampleIntensities = null;
		sampleRow = 0;
		
		baselineImageUri = null;
		sampleImageUri = null;
		
		displayMode = DisplayMode.BASELINE_IMAGE;
	}
	
	public static final Parcelable.Creator<Spectrum> CREATOR
			= new Parcelable.Creator<Spectrum>()
	{
		@SuppressWarnings("unchecked")
		public Spectrum createFromParcel(Parcel parcel)
		{
			Spectrum spectrum = new Spectrum();
			
			Bundle bundle = parcel.readBundle();
			
			if (bundle.containsKey("baselineIntensities"))
			{
				spectrum.baselineIntensities = (ArrayList<Integer>) bundle.getSerializable("baselineIntensities");
			}
			
			if (bundle.containsKey("sampleIntensities"))
			{
				spectrum.sampleIntensities = (ArrayList<Integer>) bundle.getSerializable("sampleIntensities");
			}
			
			spectrum.sampleRow = bundle.getInt("sampleRow");
			
			if (bundle.containsKey("baselineImageUri"))
			{
				spectrum.baselineImageUri = bundle.getParcelable("baselineImageUri");
			}
			
			if (bundle.containsKey("sampleImageUri"))
			{
				spectrum.sampleImageUri = bundle.getParcelable("sampleImageUri");
			}
			
			spectrum.displayMode = (DisplayMode) bundle.getSerializable("displayMode");
			
			return spectrum;
		}
		
		public Spectrum[] newArray(int size)
		{
			return new Spectrum[size];
		}
	};
	
	@Override
	public void writeToParcel(Parcel out, int flags)
	{
		Bundle bundle = new Bundle();
		
		if (baselineIntensities != null)
		{
			bundle.putSerializable("baselineIntensities", baselineIntensities);
		}
		
		if (sampleIntensities != null)
		{
			bundle.putSerializable("sampleIntensities", sampleIntensities);
		}
		
		bundle.putInt("sampleRow", sampleRow);
		
		if (baselineImageUri != null)
		{
			bundle.putParcelable("baselineImageUri", baselineImageUri);
		}
		
		if (sampleImageUri != null)
		{
			bundle.putParcelable("sampleImageUri", sampleImageUri);
		}
		
		bundle.putSerializable("displayMode", displayMode);
		
		out.writeBundle(bundle);
	}
	
	@Override
	public int describeContents()
	{
		return 0;
	}
	
	public boolean assignBaselineSpectrum(Uri imageUri)
	{
		boolean success = false;
		
		try
		{
			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			Bitmap baselineImage = BitmapFactory.decodeFile(imageUri.getPath(), bitmapOptions);
			baselineIntensities = getIntensities(baselineImage, true);
			baselineImageUri = imageUri;
			baselineImage.recycle();
			success = true;
		}
		catch (Exception e)
		{
			Log.e("Spectrum", "Could not decode file " + imageUri.getPath() + ": " + e);
		}
		
		return success;
	}
	
	public boolean assignSampleSpectrum(Uri imageUri)
	{
		boolean success = false;
		
		try
		{
			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			Bitmap sampleImage = BitmapFactory.decodeFile(imageUri.getPath(), bitmapOptions);
			sampleIntensities = getIntensities(sampleImage, true);
			sampleImageUri = imageUri;
			sampleImage.recycle();
			success = true;
		}
		catch (Exception e)
		{
			Log.e("Spectrum", "Could not decode file " + imageUri.getPath() + ": " + e);
		}
		
		return success;
	}
	
	public void setSampleRow(int row)
	{
		sampleRow = row;
		
		if (baselineImageUri != null)
		{
			assignBaselineSpectrum(baselineImageUri);
		}
		
		if (sampleImageUri != null)
		{
			assignBaselineSpectrum(sampleImageUri);
		}
	}
	
	public ArrayList<Integer> getBaselineIntensities()
	{
		return baselineIntensities;
	}
	
	public ArrayList<Integer> getSampleIntensities()
	{
		return sampleIntensities;
	}
	
	public Uri getBaselineUri()
	{
		return baselineImageUri;
	}
	
	public Uri getSampleUri()
	{
		return sampleImageUri;
	}
	
	public void setDisplayMode(DisplayMode mode)
	{
		displayMode = mode;
	}
	
	public DisplayMode getDisplayMode()
	{
		return displayMode;
	}
	
	public ArrayList<Integer> getAbsorbancies()
	{
		if (baselineIntensities == null || sampleIntensities == null)
		{
			return null;
		}
		else
		{
			ArrayList<Integer> absorbancies = new ArrayList<Integer>(sampleIntensities.size());
			
			assert(sampleIntensities.size() == baselineIntensities.size());
			
			for (int i = 0; i < sampleIntensities.size(); i++)
			{
				int sampleI = sampleIntensities.get(i);
				int baselineI = baselineIntensities.get(i);
				float absorbance;
				
				if (sampleI >= baselineI - 3)
				{
					absorbance = 0.0f;
				}
				else
				{
					absorbance = 255.0f - 255.0f * (float) sampleI / ((float) baselineI + 1.0f);
				}
				
				absorbancies.add(Math.round(absorbance));
			}
			
			ArrayList<Double> proportions = new ArrayList<Double>();
			proportions.add(0.2);
			proportions.add(0.1);
			proportions.add(0.1);
			proportions.add(0.1);
			proportions.add(0.1);
			return getSmoothedValues(absorbancies, proportions);
		}
	}
	
	private ArrayList<Integer> getIntensities(Bitmap spectrum, boolean smoothed)
	{
		assert(sampleRow >= 0 && sampleRow <= 1.0);
		
		int sampleRowIndex = (int) ((spectrum.getHeight() - 1) * sampleRow);
		assert(sampleRowIndex >= 0 && sampleRowIndex < spectrum.getHeight());
		
		ArrayList<Integer> intensities = new ArrayList<Integer>(spectrum.getHeight());
		
		for (int i = 0; i < spectrum.getWidth(); i++)
		{
			int color = spectrum.getPixel(i, sampleRowIndex);
			assert(Color.alpha(color) == 255);
			
			float intensity = (Color.red(color) + Color.green(color) + Color.blue(color)) / 3.0f;
			
			intensities.add(Math.round(intensity));
		}
		
		if (smoothed)
		{
			ArrayList<Double> proportions = new ArrayList<Double>();
			proportions.add(0.2);
			proportions.add(0.1);
			proportions.add(0.1);
			proportions.add(0.1);
			proportions.add(0.1);
			return getSmoothedValues(intensities, proportions);
		}
		else
		{
			return intensities;
		}
	}
	
	private ArrayList<Integer> getSmoothedValues(ArrayList<Integer> values, ArrayList<Double> proportions)
	{
		assert(values != null);
		assert(proportions != null);
		assert(proportions.size() > 0);
		
		// The proportions must total to 1 (the first index is the center, all the others are doubled)
		double totalProportion = proportions.get(0);
		for (int i = 1; i < proportions.size(); i++)
		{
			totalProportion += 2 * proportions.get(i);
		}
		assert(totalProportion == 1.0);
		
		ArrayList<Integer> smoothedValues = new ArrayList<Integer>(values.size());
		
		// For each pixel, smooth it to a weighted average of neighboring pixels
		for (int i = 0; i < values.size(); i++)
		{
			double smoothedValue = proportions.get(0) * values.get(i);
			
			for (int j = 1; j < proportions.size(); j++)
			{
				int leftPixel = Math.max(0, i - j);
				int rightPixel = Math.min(values.size() - 1, i + j);
				
				smoothedValue += proportions.get(j) * values.get(leftPixel);
				smoothedValue += proportions.get(j) * values.get(rightPixel);
			}
			
			smoothedValues.add((int) Math.round(smoothedValue));
		}
		
		return smoothedValues;
	}
}
