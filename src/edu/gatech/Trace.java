package edu.gatech;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bsh.EvalError;
import bsh.Interpreter;

public class Trace
{
	public static final int TEST_NUMBER = 1;
	public static final String TEST_FILE = "res/Test" + TEST_NUMBER + ".java";
	public static final String ASSIGNMENT_PATTERN = "^([\\w<>\\[\\]]+)\\s+(\\w+)\\s*=\\s*(.*)$";
	public static final int VARIABLE_GROUP = 2;
	
	public static Pattern assignmentPattern;
	public static Interpreter i;
	public static ArrayList<String> code;
	public static ArrayList<String> refs;
	
	public static int step = 0;

	// Wrapper method for Interpreter.eval
	public static void eval(String statement)
	{
		
		try
		{
			i.eval(statement);
		}
		catch (EvalError e)
		{
			System.out.println("EvalError: " + statement);
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
			System.out.println("EvalError: get");
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
	
	public static String toJSON(String line)
	{
		step++;
		String json = "{\n\t\"code\" : \"" + line + "\",\n\t\"step\" : " + step + ",\n\t\"state\" : {\n";
		
		for (String r : refs)
		{
			Object o = get(r);
			String description = o.toString();
			
			// handle if o is an array
			if (o.getClass().isArray())
			{
				description = "{";
				for (int i = 0; i < Array.getLength(o)-1; i++)
				{
					description += Array.get(o, i) + ", ";
				}
				description += Array.get(o, Array.getLength(o)-1) + "}";
			}
			else if (o instanceof String)
			{
				description = "\"" + o + "\"";
			}
			
			json += "\t\t\"" + r + "\" : " + description + ",\n";
		}
		
		return json + "\t}\n},";
	}
	
	public static void main(String[] args)
	{
		i = new Interpreter();
		assignmentPattern = Pattern.compile(ASSIGNMENT_PATTERN);
		
		if (args.length > 0)
		{
			code = getCodeFromFile(args[0]);
		}
		else
		{
			code = getCodeFromFile(TEST_FILE);
		}
		
		refs = new ArrayList<String>();
		
		String var = null;
		
		for (String line : code)
		{	
			eval(line);
			
			if ((var = isAssignment(line)) != null)
			{
				refs.add(var);
			}
			
			System.out.println(toJSON(line));
		}
	}

}
