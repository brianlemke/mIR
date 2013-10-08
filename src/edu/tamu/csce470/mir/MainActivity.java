package edu.tamu.csce470.mir;

import java.io.File;
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

public class MainActivity extends Activity {

	private static final int REQUEST_CODE_CAPTURE_IMAGE = 100;
	
	private Uri capturedImageUri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("MainActivity", "onCreate called");
		setContentView(R.layout.activity_main);
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
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.d("MainActivity", "onRestoreInstanceState called");
		
		capturedImageUri = (Uri) savedInstanceState.getParcelable("capturedImageUri");
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.d("MainActivity", "onStart called");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d("MainActivity", "onResume called");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d("MainActivity", "onPause called");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.d("MainActivity", "onStop called");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("MainActivity", "onDestroy called");
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("MainActivity", "onActivityResult called");
		if (requestCode == REQUEST_CODE_CAPTURE_IMAGE) {
			if (resultCode == RESULT_OK) {
				// Launch image display view with the resulting picture
				Log.d("MainActivity", "Image successfully captured from " + capturedImageUri.toString());
				Intent displayImageIntent = new Intent(this, ImageDisplayActivity.class);
				displayImageIntent.putExtra("imageUri", capturedImageUri);
				startActivity(displayImageIntent);
			}
		}
		else {
			assert(false);
		}
	}

	public void onCaptureImage(View view) {
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
		startActivityForResult(captureIntent, REQUEST_CODE_CAPTURE_IMAGE);
	}
}
