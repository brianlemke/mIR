package edu.tamu.csce470.mir;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ClassifierListAdapter extends ArrayAdapter<ClassifierResult>
{	
	public ClassifierListAdapter(Context context, int resource, ClassifierResult results[])
	{
		super(context, resource, results);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = View.inflate(getContext(), R.layout.view_classifier_list_item, null);
		}
		
		TextView nameView = (TextView) convertView.findViewById(R.id.resultName);
		TextView kScaledView = (TextView) convertView.findViewById(R.id.resultKScaledMSE);
		TextView standardizedView = (TextView) convertView.findViewById(R.id.resultStandardizedMSE);
		
		ClassifierResult result = getItem(position);
		nameView.setText(result.name);
		if (result.kScaledMSE >= 0.0)
		{
			kScaledView.setText("MSE (k-scaled): " + result.kScaledMSE);
		}
		else
		{
			kScaledView.setVisibility(View.GONE);
		}
		
		if (result.standardizedMSE >= 0.0)
		{
			standardizedView.setText("MSE (standardized): " + result.standardizedMSE);
		}
		else
		{
			standardizedView.setVisibility(View.GONE);
		}
		
		return convertView;
	}
}
