package edu.tamu.csce470.mir;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import edu.tamu.csce470.mir.Spectrum.DisplayMode;

public class MainActivity extends Activity {

	private static final int REQUEST_CODE_CAPTURE_BASELINE = 100;
	private static final int REQUEST_CODE_CAPTURE_SAMPLE = 200;
	private static final int REQUEST_CODE_SELECT_BASELINE = 300;
	private static final int REQUEST_CODE_SELECT_SAMPLE = 400;
	private static final int REQUEST_CODE_CAPTURE_CALIBRATION = 500;
	private static final int REQUEST_CODE_SET_CALIBRATION = 600;
	
	private Uri capturedImageUri;
	
	private Spectrum spectrum;
	
	private CalibrationSettings calibration;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("MainActivity", "onCreate called");
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState != null && savedInstanceState.containsKey("captureImageUri"))
		{
			capturedImageUri = (Uri) savedInstanceState.getParcelable("captureImageUri");
		}
		
		if (savedInstanceState != null && savedInstanceState.containsKey("spectrum"))
		{
			spectrum = (Spectrum) savedInstanceState.getSerializable("spectrum");
		}
		else
		{
			spectrum = new Spectrum();
		}
		
		loadCalibrationSettings();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		Log.d("MainActivity", "onSaveInstanceState called");
		
		savedInstanceState.putParcelable("capturedImageUri", capturedImageUri);
		savedInstanceState.putParcelable("spectrum", spectrum);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("MainActivity", "onActivityResult called");
		if (requestCode == REQUEST_CODE_CAPTURE_BASELINE) {
			if (resultCode == RESULT_OK) {
				// Launch image display view with the resulting picture
				Log.d("MainActivity", "Image successfully captured from " + capturedImageUri.toString());
				spectrum.assignBaselineSpectrum(capturedImageUri);
				spectrum.setDisplayMode(DisplayMode.BASELINE_IMAGE);
				
				Intent displayImageIntent = new Intent(this, SpectrumDisplayActivity.class);
				displayImageIntent.putExtra("spectrum", spectrum);
				startActivity(displayImageIntent);
			}
		}
		else if (requestCode == REQUEST_CODE_CAPTURE_SAMPLE)
		{
			if (resultCode == RESULT_OK)
			{
				Log.d("MainActivity", "Sample image successfully captured from " + capturedImageUri.toString());
				spectrum.assignSampleSpectrum(capturedImageUri);
				spectrum.setDisplayMode(DisplayMode.SAMPLE_IMAGE);
				
				Intent displayImageIntent = new Intent(this, SpectrumDisplayActivity.class);
				displayImageIntent.putExtra("spectrum", spectrum);
				startActivity(displayImageIntent);
			}
		}
		else if (requestCode == REQUEST_CODE_SELECT_BASELINE)
		{
			if (resultCode == RESULT_OK)
			{
				assert(data != null);
				Uri selectedImageUri = data.getData();
				Log.d("MainActivity", "Baseline image successfully selected from " + selectedImageUri);
				spectrum.assignBaselineSpectrum(selectedImageUri);
				spectrum.setDisplayMode(DisplayMode.BASELINE_IMAGE);
				
				Intent displayImageIntent = new Intent(this, SpectrumDisplayActivity.class);
				displayImageIntent.putExtra("spectrum", spectrum);
				startActivity(displayImageIntent);
			}
		}
		else if (requestCode == REQUEST_CODE_SELECT_SAMPLE)
		{
			if (resultCode == RESULT_OK)
			{
				assert(data != null);
				Uri selectedImageUri = data.getData();
				Log.d("MainActivity", "Sample image successfully selected from " + selectedImageUri);
				spectrum.assignSampleSpectrum(selectedImageUri);
				spectrum.setDisplayMode(DisplayMode.SAMPLE_IMAGE);
				
				Intent displayImageIntent = new Intent(this, SpectrumDisplayActivity.class);
				displayImageIntent.putExtra("spectrum", spectrum);
				startActivity(displayImageIntent);
			}
		}
		else if (requestCode == REQUEST_CODE_CAPTURE_CALIBRATION)
		{
			if (resultCode == RESULT_OK)
			{
				Log.d("MainActivity", "Calibration image successfully captured from " + capturedImageUri.toString());
				
				Intent calibrationIntent = new Intent(this, CalibrationActivity.class);
				calibrationIntent.putExtra("image", capturedImageUri);
				startActivityForResult(calibrationIntent, REQUEST_CODE_SET_CALIBRATION);
			}
		}
		else if (requestCode == REQUEST_CODE_SET_CALIBRATION)
		{
			loadCalibrationSettings();
		}
		else {
			assert(false);
		}
	}
	
	private void loadCalibrationSettings()
	{	
		calibration = null;
		
		try
		{
			FileInputStream file = getApplicationContext().openFileInput("calibration.bin");
			ObjectInputStream objectStream = new ObjectInputStream(file);
			calibration = (CalibrationSettings) objectStream.readObject();
			objectStream.close();
			file.close();
		}
		catch (Exception e)
		{
			Toast.makeText(this, "Failed to load the calibration settings.", Toast.LENGTH_LONG).show();
			Log.e("CalibrationActivity", "Failed to load calibration settings: " + e);
		}
		
		if (calibration == null)
		{
			findViewById(R.id.captureBaselineButton).setEnabled(false);
			findViewById(R.id.captureSampleButton).setEnabled(false);
			findViewById(R.id.selectBaselineButton).setEnabled(false);
			findViewById(R.id.selectSampleButton).setEnabled(false);
			findViewById(R.id.displayBaselineButton).setEnabled(false);
			findViewById(R.id.displaySampleButton).setEnabled(false);
			findViewById(R.id.displaySpectrumButton).setEnabled(false);
		}
		else
		{
			findViewById(R.id.captureBaselineButton).setEnabled(true);
			findViewById(R.id.captureSampleButton).setEnabled(true);
			findViewById(R.id.selectBaselineButton).setEnabled(true);
			findViewById(R.id.selectSampleButton).setEnabled(true);
			findViewById(R.id.displayBaselineButton).setEnabled(true);
			findViewById(R.id.displaySampleButton).setEnabled(true);
			findViewById(R.id.displaySpectrumButton).setEnabled(true);
		}
	}

	public void onCaptureBaseline(View view) {
		captureImage(DisplayMode.BASELINE_IMAGE);
	}
	
	public void onCaptureSample(View view) {
		captureImage(DisplayMode.SAMPLE_IMAGE);
	}
	
	public void onLoadFileBaseline(View view) {
		selectImage(DisplayMode.BASELINE_IMAGE);
	}
	
	public void onLoadFileSample(View view) {
		selectImage(DisplayMode.SAMPLE_IMAGE);
	}
	
	public void onDisplaySpectrum(View view) {
		if (view.getId() == R.id.displayBaselineButton)
		{
			this.spectrum.setDisplayMode(DisplayMode.BASELINE_IMAGE);
		}
		else if (view.getId() == R.id.displaySampleButton)
		{
			this.spectrum.setDisplayMode(DisplayMode.SAMPLE_IMAGE);
		}
		else if (view.getId() == R.id.displaySpectrumButton)
		{
			this.spectrum.setDisplayMode(DisplayMode.SPECTRUM_GRAPH);
		}
		else
		{
			assert(false);
		}
		
		Intent displayImageIntent = new Intent(this, SpectrumDisplayActivity.class);
		displayImageIntent.putExtra("spectrum", spectrum);
		startActivity(displayImageIntent);
	}
	
	public void onCalibrate(View view)
	{
		Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		getNewImageUri();
		captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
		
		startActivityForResult(captureIntent, REQUEST_CODE_CAPTURE_CALIBRATION);
	}
	
	public void onAccept(View view)
	{
		if (spectrum.getAbsorbancies() == null)
		{
			Toast.makeText(this, "Please capture a sample and baseline image first", Toast.LENGTH_LONG).show();
		}
		else
		{
			SpectrumResult result = new SpectrumResult(spectrum, calibration);
			Intent intent = new Intent();
			intent.putExtra("spectrumResult", result);
			setResult(RESULT_OK, intent);
			finish();
		}
	}
	
	private void captureImage(DisplayMode mode)
	{
		// We're going to offload image capture to the default camera application
		Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		getNewImageUri();
		
		// Start the camera application to take our picture
		captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
		
		switch (mode)
		{
		case BASELINE_IMAGE:
			startActivityForResult(captureIntent, REQUEST_CODE_CAPTURE_BASELINE);
			break;
		case SAMPLE_IMAGE:
			startActivityForResult(captureIntent, REQUEST_CODE_CAPTURE_SAMPLE);
			break;
		default:
			assert(false);
		}
	}
	
	private void getNewImageUri()
	{
		// We're going to save it in the application's private images directory
			File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
			if (mediaStorageDir == null) {
				Log.e("MainActivity", "Could not access the external media folder, falling back to internal");
				if (getFilesDir() == null) {
					Log.e("MainActivity", "Could not access the internal media folder, no fallback available");
					return;
				}
				else {
					mediaStorageDir = new File(getFilesDir(), "Pictures");
				}
			}
			
			// Ensure that the pictures directory exists
			if (!mediaStorageDir.exists()) {
				if (!mediaStorageDir.mkdirs()) {
					Log.e("MainActivity", "Could not create image output directory");
					return;
				}
			}
			
			// By now, the directory definitely exists, so create a unique file name
			File capturedImageFile = new File(mediaStorageDir, UUID.randomUUID().toString() + ".jpg");
			capturedImageUri = Uri.fromFile(capturedImageFile);
	}

	private void selectImage(DisplayMode mode)
	{
		// We're going to offload image selection to the default chooser application
		Intent selectIntent = new Intent(Intent.ACTION_GET_CONTENT);
		selectIntent.setType("image/*");
		
		switch (mode)
		{
		case BASELINE_IMAGE:
			startActivityForResult(selectIntent, REQUEST_CODE_SELECT_BASELINE);
			break;
		case SAMPLE_IMAGE:
			startActivityForResult(selectIntent, REQUEST_CODE_SELECT_SAMPLE);
			break;
		default:
			assert(false);
		}
	}
}
