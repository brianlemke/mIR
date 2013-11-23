package edu.tamu.csce470.mir;

public class ClassifierResult
{
	public String name;
	public double standardizedMSE;
	public double kScaledMSE;
	
	public int standardizedRank;
	public int kScaledRank;
	public int overallRank;
	
	public ClassifierResult(String name)
	{
		this.name = name;
		this.standardizedRank = -1;
		this.kScaledRank = -1;
		this.overallRank = -1;
	}

}
