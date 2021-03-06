package edu.tamu.csce470.mir;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class SpectrumDisplayActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_spectrum_display);
		
		SpectrumView spectrumView = (SpectrumView) findViewById(R.id.spectrumView);
		
		spectrumView.setSpectrum((Spectrum) getIntent().getExtras().getParcelable("spectrum"));
		spectrumView.setCalibration((CalibrationSettings) getIntent().getExtras().getSerializable("calibrationSettings"));
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		
		SpectrumView spectrumView = (SpectrumView) findViewById(R.id.spectrumView);
		
		spectrumView.releaseSpectrum();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.spectrum_display, menu);
		return true;
	}
}
