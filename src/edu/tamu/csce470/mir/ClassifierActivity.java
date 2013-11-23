package edu.tamu.csce470.mir;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
				
				ArrayList<ClassifierResult> comparisonResults = findRankedClassification(returnedSpectrum, spectra);
				
				populateKnownSpectraView(comparisonResults);
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
	
	private void addListListener(ListView listView)
	{
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				ClassifierResult result = (ClassifierResult) parent.getAdapter().getItem(position);
				int savedIndex = 0;
				for (int i = 0; i < spectra.size(); i++)
				{
					if (result.name == spectra.get(i).name)
					{
						savedIndex = i;
					}
				}
				
				final int removeIndex = savedIndex;
				
				AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
				builder.setMessage("Delete this saved spectrum?");
				builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						spectra.remove(removeIndex);
						populateKnownSpectraView();
						saveSpectra(spectra);
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {	
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Do nothing
					}
				});
				
				builder.create().show();
			}
		});
	}
	
	private void populateKnownSpectraView()
	{
		ListView view = (ListView) findViewById(R.id.knownSampleListView);
		addListListener(view);
		
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
	
	private void populateKnownSpectraView(ArrayList<ClassifierResult> results)
	{
		ListView view = (ListView) findViewById(R.id.knownSampleListView);
		addListListener(view);
		
		ClassifierResult[] resultsArray = new ClassifierResult[results.size()];
		
		for (int i = 0; i < results.size(); i++)
		{
			resultsArray[i] = results.get(i);
		}
		
		ClassifierListAdapter spectraAdapter = new ClassifierListAdapter(this, R.layout.view_classifier_list_item, resultsArray);
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
	
	private ArrayList<ClassifierResult> findRankedClassification(SpectrumResult test, ArrayList<SpectrumResult> knownList)
	{
		ArrayList<ClassifierResult> results = new ArrayList<ClassifierResult>();
		
		// First calculate the MSEs with each known sample
		for (int i = 0; i < knownList.size(); i++)
		{
			SpectrumResult known = knownList.get(i);
			ClassifierResult result = new ClassifierResult(known.name);
			result.kScaledMSE = test.getKScaledMeanStandardError(known);
			result.standardizedMSE = test.getStandardizedMeanStandardError(known);
			results.add(result);
		}
		
		// Now sort based on the standardized score
		ArrayList<ClassifierResult> sortedResults = new ArrayList<ClassifierResult>();
		while (results.size() > 0)
		{
			double bestScore = results.get(0).standardizedMSE;
			int bestIndex = 0;
			for (int i = 1; i < results.size(); i++)
			{
				double score = results.get(i).standardizedMSE;
				if (score < bestScore)
				{
					bestScore = score;
					bestIndex = i;
				}
			}
			
			ClassifierResult best = results.remove(bestIndex);
			best.standardizedRank = sortedResults.size() + 1;
			best.overallRank = best.standardizedRank;
			sortedResults.add(best);
		}
		
		// Now calculate the k-scaled score for each sorted item
		for (int i = 0; i < sortedResults.size(); i++)
		{
			double best = 0.0;
			int bestIndex = -1;
			for (int j = 0; j < sortedResults.size(); j++)
			{
				if (sortedResults.get(j).kScaledRank <= 0)
				{
					if (bestIndex == -1 || sortedResults.get(j).kScaledMSE < best)
					{
						best = sortedResults.get(j).kScaledMSE;
						bestIndex = j;
					}
				}
			}
			sortedResults.get(bestIndex).kScaledRank = i + 1;
		}
		
		return sortedResults;
	}
}
