package edu.tamu.csce470.mir;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class ClassifierActivity extends Activity
{
	private static int REQUEST_CODE_GET_TEST_SPECTRUM = 100;
	private static int REQUEST_CODE_GET_NEW_SPECTRUM = 200;
	
	private ArrayList<SpectrumResult> spectra;
	
	private SpectrumResult returnedSpectrum;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_classifier);
		
		spectra = loadSpectra();
		populateKnownSpectraView();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.classifier, menu);
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQUEST_CODE_GET_TEST_SPECTRUM)
		{
			if (resultCode == RESULT_OK)
			{
				returnedSpectrum = (SpectrumResult) data.getSerializableExtra("spectrumResult");
				
				ArrayList<Double> standardizedErrors = findStandardizedErrorValues(returnedSpectrum, spectra);
				ArrayList<Double> kScaledErrors = findKScaledErrorValues(returnedSpectrum, spectra);
				
				populateKnownSpectraView(standardizedErrors, kScaledErrors);
			}
		}
		else if (requestCode == REQUEST_CODE_GET_NEW_SPECTRUM)
		{
			if (resultCode == RESULT_OK)
			{
				returnedSpectrum = (SpectrumResult) data.getSerializableExtra("spectrumResult");
				
				setContentView(R.layout.activity_classifier_name_spectrum);
			}
		}
		else
		{
			assert(false);
		}
	}
	
	private void saveSpectra(ArrayList<SpectrumResult> spectraList)
	{
		try
		{
			FileOutputStream file = getApplicationContext().openFileOutput("classifierSpectra.bin", MODE_PRIVATE);
			ObjectOutputStream objectStream = new ObjectOutputStream(file);
			objectStream.writeObject(spectraList);
			objectStream.close();
			file.close();
		}
		catch (Exception e)
		{
			Toast.makeText(this, "Could not save the spectra list", Toast.LENGTH_LONG).show();
			Log.e("ClassifierActivity", "Could not save spectra list: " + e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<SpectrumResult> loadSpectra()
	{
		ArrayList<SpectrumResult> loadedSpectra = new ArrayList<SpectrumResult>();
		
		try
		{
			FileInputStream file = getApplicationContext().openFileInput("classifierSpectra.bin");
			ObjectInputStream objectStream = new ObjectInputStream(file);
			loadedSpectra = (ArrayList<SpectrumResult>) objectStream.readObject();
			objectStream.close();
			file.close();
		}
		catch (Exception e)
		{
			Toast.makeText(this, "No saved spectra to load", Toast.LENGTH_LONG).show();
		}
		
		return loadedSpectra;
	}
	
	private void populateKnownSpectraView()
	{
		ListView view = (ListView) findViewById(R.id.knownSampleListView);
		
ClassifierResult[] results = new ClassifierResult[spectra.size()];
		
		for (int i = 0; i < spectra.size(); i++)
		{
			ClassifierResult result = new ClassifierResult(spectra.get(i).name);
			result.kScaledMSE = -1;
			result.standardizedMSE = -1;
			results[i] = result;
		}
		
		ClassifierListAdapter spectraAdapter = new ClassifierListAdapter(this, R.layout.view_classifier_list_item, results);
		view.setAdapter(spectraAdapter);
	}
	
	private void populateKnownSpectraView(ArrayList<Double> standardizedErrorValues, ArrayList<Double> kScaledErrorValues)
	{
		ListView view = (ListView) findViewById(R.id.knownSampleListView);
		
		ClassifierResult[] results = new ClassifierResult[spectra.size()];
		
		for (int i = 0; i < spectra.size(); i++)
		{
			ClassifierResult result = new ClassifierResult(spectra.get(i).name);
			result.kScaledMSE = kScaledErrorValues.get(i);
			result.standardizedMSE = standardizedErrorValues.get(i);
			results[i] = result;
		}
		
		ClassifierListAdapter spectraAdapter = new ClassifierListAdapter(this, R.layout.view_classifier_list_item, results);
		view.setAdapter(spectraAdapter);
	}

	public void onClearKnownSamples(View view)
	{
		spectra.clear();
		getApplicationContext().deleteFile("classifierSpectra.bin");
		populateKnownSpectraView();
	}
	
	public void onAnalyzeSample(View view)
	{
		Intent intent = new Intent(this, MainActivity.class);
		startActivityForResult(intent, REQUEST_CODE_GET_TEST_SPECTRUM);
	}
	
	public void onAddKnownSample(View view)
	{
		Intent intent = new Intent(this, MainActivity.class);
		startActivityForResult(intent, REQUEST_CODE_GET_NEW_SPECTRUM);
	}
	
	public void onSaveKnownSpectrum(View view)
	{
		EditText nameView = (EditText) findViewById(R.id.spectrumNameInput);
		returnedSpectrum.name = nameView.getText().toString();
		spectra.add(returnedSpectrum);
		saveSpectra(spectra);
		
		returnedSpectrum = null;
		
		setContentView(R.layout.activity_classifier);
		populateKnownSpectraView();
	}
	
	public void onCancelKnownSpectrum(View view)
	{
		returnedSpectrum = null;
		
		setContentView(R.layout.activity_classifier);
		populateKnownSpectraView();
	}
	
	private ArrayList<Double> findStandardizedErrorValues(SpectrumResult test, ArrayList<SpectrumResult> knownList)
	{
		ArrayList<Double> errorValues = new ArrayList<Double>(knownList.size());
		
		for (int i = 0; i < knownList.size(); i++)
		{
			errorValues.add(test.getStandardizedMeanStandardError(knownList.get(i)));
		}
		
		return errorValues;
	}
	
	private ArrayList<Double> findKScaledErrorValues(SpectrumResult test, ArrayList<SpectrumResult> knownList)
	{
		ArrayList<Double> errorValues = new ArrayList<Double>(knownList.size());
		
		for (int i = 0; i < knownList.size(); i++)
		{
			errorValues.add(test.getKScaledMeanStandardError(knownList.get(i)));
		}
		
		return errorValues;
	}
}
