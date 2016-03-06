package edu.gatech;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bsh.EvalError;
import bsh.Interpreter;

public class Trace
{
	public static final String TEST_FILE = "res/Test.java";
	public static final String ASSIGNMENT_PATTERN = "^(\\w+)\\s+(\\w+)\\s*=\\s*(.*)$";
	public static final int VARIABLE_GROUP = 2;
	
	public static Pattern assignmentPattern;
	public static Interpreter i;

	// Wrapper method for Interpreter.eval
	public static void eval(String statement)
	{
		try
		{
			i.eval(statement);
		}
		catch (EvalError e)
		{
			e.printStackTrace();
		}
	}
	
	// Wrapper method for Interpreter.get
	public static Object get(String arg)
	{
		Object o = null;
		
		try
		{
			o = i.get(arg);
		}
		catch (EvalError e)
		{
			e.printStackTrace();
		}
		
		return o;
	}
	
	public static ArrayList<String> getCodeFromFile(String filePath)
	{
		ArrayList<String> code = new ArrayList<String>();

		try
		{
			File file = new File(filePath);
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			
			while ((line = reader.readLine()) != null)
			{
				code.add(line.trim());
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return code;
	}
	
	public static String isAssignment(String line)
	{	
		Matcher m = assignmentPattern.matcher(line);
		
		if (m.matches())
		{
			return m.group(VARIABLE_GROUP);
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		i = new Interpreter();
		assignmentPattern = Pattern.compile(ASSIGNMENT_PATTERN);
		
		ArrayList<String> code = getCodeFromFile(TEST_FILE);
		ArrayList<String> refs = new ArrayList<String>();
		String var = null;
		
		for (String line : code)
		{
			eval(line);
			System.out.println(line);
			
			if ((var = isAssignment(line)) != null)
			{
				refs.add(var);
			}
			
			for (String r : refs)
			{
				System.out.printf("\t%s = %s\n", r, get(r));
			}
		}
	}

}
