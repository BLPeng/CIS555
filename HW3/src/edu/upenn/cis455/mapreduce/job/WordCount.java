package edu.upenn.cis455.mapreduce.job;

import java.util.StringTokenizer;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

public class WordCount implements Job {

	public void map(String key, String value, Context context)
	{
    // Your map function for WordCount goes here
		StringTokenizer tokenizer = new StringTokenizer(value);
		while(tokenizer.hasMoreTokens()) {
			context.write(tokenizer.nextToken(), "1");
		}  
	  
	}
  
	public void reduce(String key, String[] values, Context context)
  	{
		// Your reduce function for WordCount goes here
		
		context.write(key, String.valueOf(values.length));
  	}
  
}
