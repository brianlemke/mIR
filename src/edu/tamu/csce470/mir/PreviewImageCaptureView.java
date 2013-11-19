package edu.tamu.csce470.mir;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

@SuppressLint("ViewConstructor")
public class PreviewImageCaptureView extends SurfaceView implements SurfaceHolder.Callback
{
	private SurfaceHolder holder;
	private Camera camera;
	
	@SuppressWarnings("deprecation")
	public PreviewImageCaptureView(Context context, Camera camera)
	{
		super(context);
		
		this.camera = camera;
		
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder)
	{
		// Tell the camera where to draw the preview
		try
		{
			this.camera.setPreviewDisplay(holder);
			this.camera.startPreview();
		}
		catch (Exception e)
		{
			Log.d("PreviewImageCaptureView", "Error setting camera preview: " + e.getMessage());
		}
	}
	
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		// Make sure we release the camera in the activity
	}
	
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
	{
		if (this.holder.getSurface() == null)
		{
			return;
		}
		
		try
		{
			this.camera.stopPreview();
		}
		catch (Exception e)
		{
			// Ignore this
		}
		
		try
		{
			this.camera.setPreviewDisplay(this.holder);
			this.camera.startPreview();
		}
		catch (Exception e)
		{
			Log.d("PreviewImageCaptureView", "Error setting camera preview: " + e.getMessage());
		}
	}
}
