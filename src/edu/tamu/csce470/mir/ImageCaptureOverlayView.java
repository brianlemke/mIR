package edu.tamu.csce470.mir;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class ImageCaptureOverlayView extends View
{
	private CalibrationSettings settings;
	
	int sampleY;
	int boundaryLeftX;
	int boundaryRightX;
	int wavelengthLeftX;
	int wavelengthRightX;
	
	private Paint samplePaint;
	private Paint boundaryPaint;
	private Paint wavelengthPaint;
	
	public ImageCaptureOverlayView(Context context, CalibrationSettings settings)
	{
		super(context);
		
		this.settings = settings;
		
		samplePaint = new Paint();
		samplePaint.setColor(Color.RED);
		
		boundaryPaint = new Paint();
		boundaryPaint.setColor(Color.GREEN);
		
		wavelengthPaint = new Paint();
		wavelengthPaint.setColor(Color.CYAN);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		if (this.settings != null)
		{
			int width = canvas.getWidth();
			int height = canvas.getHeight();
			
			int sampleY = (int) (((double) this.settings.sampleRow / (double) this.settings.imageHeight) * height);
			int boundaryLeftX = (int) (((double) this.settings.startPixel / (double) this.settings.imageWidth) * width);
			int boundaryRightX = (int) (((double) this.settings.endPixel / (double) this.settings.imageWidth) * width);
			int wavelengthLeftX = (int) (((double) this.settings.pixel1 / (double) this.settings.imageWidth) * width);
			int wavelengthRightX = (int) (((double) this.settings.pixel2 / (double) this.settings.imageWidth) * width);
			
			if (sampleY >= 0)
			{
				canvas.drawLine(0, sampleY, width, sampleY, samplePaint);
			}
			
			if (boundaryLeftX >= 0)
			{
				canvas.drawLine(boundaryLeftX, 0, boundaryLeftX, height, boundaryPaint);
			}
			
			if (boundaryRightX >= 0)
			{
				canvas.drawLine(boundaryRightX, 0, boundaryRightX, height, boundaryPaint);
			}
			
			if (wavelengthLeftX >= 0)
			{
				canvas.drawLine(wavelengthLeftX, 0, wavelengthLeftX, height, wavelengthPaint);
			}
			
			if (wavelengthRightX >= 0)
			{
				canvas.drawLine(wavelengthRightX, 0, wavelengthRightX, height, wavelengthPaint);
			}
		}
	}
}
