package edu.tamu.csce470.mir;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.tamu.csce470.mir.CalibrationView.CalibrationState;

public class CalibrationActivity extends Activity
{
	private CalibrationSettings settings;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calibration);
		
		CalibrationView calView = (CalibrationView) findViewById(R.id.calibrationView1);
		calView.setImage((Uri) getIntent().getExtras().getParcelable("image"));
		
		settings = new CalibrationSettings();
		settings.imageWidth = calView.getImageWidth();
		settings.imageHeight = calView.getImageHeight();
		
		hideTextField();
		setInstructions("Choose sample line");
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		
		CalibrationView calView = (CalibrationView) findViewById(R.id.calibrationView1);
		calView.releaseImage();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.calibration, menu);
		return true;
	}
	
	public void onOKClicked(View view)
	{
		CalibrationView calView = (CalibrationView) findViewById(R.id.calibrationView1);
		
		EditText textBox = (EditText) findViewById(R.id.calibrationEntry);
		
		switch (calView.state)
		{
		case SampleRow:
			calView.state = CalibrationState.LeftBoundary;
			settings.sampleRow = calView.getSampleRow();
			setInstructions("Choose left boundary");
			break;
		case LeftBoundary:
			calView.state = CalibrationState.RightBoundary;
			settings.startPixel = calView.getLeftBoundaryPixel();
			setInstructions("Choose right boundary");
			break;
		case RightBoundary:
			calView.state = CalibrationState.LeftWavelength;
			settings.endPixel = calView.getRightBoundaryPixel();
			showTextField();
			setInstructions("Choose left wavelength");
			break;
		case LeftWavelength:
			calView.state = CalibrationState.RightWavelength;
			settings.pixel1 = calView.getLeftWavelengthPixel();
			settings.wavelength1 = Double.parseDouble(textBox.getText().toString());
			resetTextField();
			setInstructions("Choose right wavelength");
			break;
		case RightWavelength:
			settings.pixel2 = calView.getRightWavelengthPixel();
			settings.wavelength2 = Double.parseDouble(textBox.getText().toString());
			if (settings.validateSettings())
			{
				saveCalibration();
				finish();
			}
			else
			{
				Toast.makeText(this, "Invalid settings, try again", Toast.LENGTH_LONG).show();
				calView.state = CalibrationState.SampleRow;
				resetTextField();
				hideTextField();
				setInstructions("Choose sample line");
			}
			break;
		default:
			assert(false);
		}
		
		calView.invalidate();
	}
	
	private void saveCalibration()
	{
		assert(settings != null);
		
		try
		{
			FileOutputStream file = getApplicationContext().openFileOutput("calibration.bin", MODE_PRIVATE);
			ObjectOutputStream objectStream = new ObjectOutputStream(file);
			objectStream.writeObject(settings);
			objectStream.close();
			file.close();
			Toast.makeText(this, "Successfully saved calibration settings!", Toast.LENGTH_LONG).show();
		}
		catch (Exception e)
		{
			Toast.makeText(this, "Failed to save the calibration settings to disk.", Toast.LENGTH_LONG).show();
			Log.e("CalibrationActivity", "Failed to save calibration settings: " + e);
		}
	}
	
	private void showTextField()
	{
		findViewById(R.id.calibrationEntry).setVisibility(View.VISIBLE);
	}
	
	private void hideTextField()
	{
		findViewById(R.id.calibrationEntry).setVisibility(View.GONE);
	}
	
	private void resetTextField()
	{
		EditText field = (EditText) findViewById(R.id.calibrationEntry);
		field.setText("");
	}
	
	private void setInstructions(String instructions)
	{
		TextView textView = (TextView) findViewById(R.id.calibrationInstructions);
		textView.setText(instructions);
	}
}
