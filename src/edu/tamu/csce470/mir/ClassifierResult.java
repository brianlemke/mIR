package edu.tamu.csce470.mir;

public class ClassifierResult
{
	public String name;
	public double standardizedMSE;
	public double kScaledMSE;
	
	public ClassifierResult(String name)
	{
		this.name = name;
	}

}
