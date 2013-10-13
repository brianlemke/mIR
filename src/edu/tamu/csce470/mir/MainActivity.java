package edu.tamu.csce470.mir;

import java.io.File;
import java.util.UUID;

import edu.tamu.csce470.mir.Spectrum.DisplayMode;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	private static final int REQUEST_CODE_CAPTURE_BASELINE = 100;
	private static final int REQUEST_CODE_CAPTURE_SAMPLE = 200;
	
	private Uri capturedImageUri;
	
	private Spectrum spectrum;
	
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
		else {
			assert(false);
		}
	}

	public void onCaptureBaseline(View view) {
		captureImage(DisplayMode.BASELINE_IMAGE);
	}
	
	public void onCaptureSample(View view) {
		captureImage(DisplayMode.SAMPLE_IMAGE);
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
	
	private void captureImage(DisplayMode mode)
	{
		// We're going to offload image capture to the default camera application
		Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
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
}
