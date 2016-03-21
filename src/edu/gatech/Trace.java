package edu.gatech;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bsh.EvalError;
import bsh.Interpreter;

public class Trace
{
	public static final int TEST_NUMBER = 1;
	public static final String TEST_FILE = "res/Test" + TEST_NUMBER + ".java";
	public static final String ASSIGNMENT_PATTERN = "^([\\w<>\\[\\]]+)\\s+(\\w+)\\s*=\\s*(.*)$";
	public static final String IF_PATTERN = "^if \\((.*)\\)$";
	public static final String WHILE_PATTERN = "^while \\((.*)\\)$";
	public static final String FOR_PATTERN = "^for \\((.*); (.*); (.*)\\)";
	public static final int VARIABLE_GROUP = 2;
	
	public static Pattern assignmentPattern;
	public static Pattern ifPattern;
	public static Pattern whilePattern;
	public static Pattern forPattern;
	public static Interpreter i;
	public static ArrayList<String> code; // code stored line by line
	public static ArrayList<String> refs; // variable names
	public static ArrayList<String> scopeRefs;
	
	public static int programCounter = 0; // current line of execution
	public static ArrayList<Integer> forLoops;
	public static Deque<Integer> loopAnchors;
	
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
	
	// Reads code from file
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
	
	// If line is an assignment (i.e. int x = 5), returns the name of the variable reference, null otherwise
	public static String isAssignment(String line)
	{	
		Matcher m = assignmentPattern.matcher(line);
		
		if (m.matches())
		{
			return m.group(VARIABLE_GROUP);
		}
		
		return null;
	}
	
	// If line is an if (i.e. if (x < 10), returns the boolean expression as a string, null otherwise
	public static String isIf(String line)
	{
		Matcher m = ifPattern.matcher(line);
		
		if (m.matches())
		{
			return m.group(1);
		}
		return null;
	}
	
	public static boolean isElse(String line)
	{
		return line.equals("else");
	}
	
	public static String isWhile(String line)
	{
		Matcher m = whilePattern.matcher(line);
		
		if (m.matches())
		{
			return m.group(1);
		}
		return null;
	}
	
	public static ArrayList<String> isFor(String line)
	{
		Matcher m = forPattern.matcher(line);
		ArrayList<String> components = new ArrayList<String>();
		
		if (m.matches())
		{
			components.add(m.group(1));
			components.add(m.group(2));
			components.add(m.group(3));
		}
		
		if (components.size() > 0)
			return components;
		
		return null;
	}
	
	// Returns true if line is a single { or }
	public static boolean isBrace(String line)
	{
		return isOpeningBrace(line) || isClosingBrace(line);
	}
	
	public static boolean isOpeningBrace(String line)
	{
		return line.equals("{");
	}
	
	public static boolean isClosingBrace(String line)
	{
		return line.equals("}");
	}
	
	public static void scanToNextOpeningBrace()
	{
		while (!isOpeningBrace(code.get(programCounter)))
		{
			programCounter++;
		}
		programCounter++;
	}
	
	public static void scanToNextClosingBrace()
	{
		while (!isClosingBrace(code.get(programCounter)))
		{
			programCounter++;
		}
		programCounter++;
	}
	
	public static void skipNextBlock()
	{
		int braceCounter = 1;
		scanToNextOpeningBrace();
		String line;
		while (braceCounter > 0)
		{
			line = code.get(programCounter);
			if (isOpeningBrace(line))
			{
				braceCounter++;
			}
			else if (isClosingBrace(line))
			{
				braceCounter--;
			}
			programCounter++;
		}
	}
	
	public static void cleanUpScope()
	{
		for (String var : scopeRefs)
		{
			refs.remove(var);
		}
		scopeRefs.clear();
	}
	
	public static String toJSON(String line)
	{
		step++;
		
		String json = "{\n\t\"code\" : \"" + line + "\",\n\t\"pc\" : " + (programCounter+1) + ",\n\t\"step\" : " + step + ",\n\t\"state\" : {\n";
		
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
		ifPattern = Pattern.compile(IF_PATTERN);
		whilePattern = Pattern.compile(WHILE_PATTERN);
		forPattern = Pattern.compile(FOR_PATTERN);
		loopAnchors = new ArrayDeque<Integer>();
		forLoops = new ArrayList<Integer>();
		scopeRefs = new ArrayList<String>();
		
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
		String condition = null;
		ArrayList<String> forComponents = new ArrayList<String>();
		
		while (programCounter < code.size())
		{	
			String line = code.get(programCounter);
			
			if ((var = isAssignment(line)) != null)
			{
				//System.out.println("DEBUG: assignment statement found");
				refs.add(var);
				if (loopAnchors.size() > 0)
				{
					scopeRefs.add(var);
				}
				eval(line);
				System.out.println(toJSON(line));
				programCounter++;
			}
			else if ((condition = isIf(line)) != null)
			{
				//System.out.println("DEBUG: conditional statement found");
				eval("last_boolean_expression = " + condition);
				boolean b = (Boolean) get("last_boolean_expression");
				System.out.println(toJSON(condition + " -> " + b));
				if (b)
				{
					scanToNextOpeningBrace();
				}
				else
				{
					skipNextBlock();
				}
			}
			else if (isElse(line))
			{
				//System.out.println("DEBUG: else statement found");
				boolean b = (Boolean) get("last_boolean_expression");
				if (b)
				{
					skipNextBlock();
				}
				else
				{
					scanToNextOpeningBrace();
				}
			}
			else if ((condition = isWhile(line)) != null)
			{
				eval("last_boolean_expression = " + condition);
				boolean b = (Boolean) get("last_boolean_expression");
				System.out.println(toJSON(condition + " -> " + b));
				if (b)
				{
					loopAnchors.push(programCounter);
					scanToNextOpeningBrace();
				}
				else
				{
					cleanUpScope();
					skipNextBlock();
				}
			}
			else if ((forComponents = isFor(line)) != null)
			{	
				String init = forComponents.get(0);
				condition = forComponents.get(1);
				String update = forComponents.get(2);
				
				if (forLoops.contains(programCounter))
				{
					eval(update);
					System.out.println(toJSON(update));
				}
				else
				{
					forLoops.add(programCounter);
					var = isAssignment(init);
					refs.add(var);
					eval(init);
					System.out.println(toJSON(init));
				}
				
				eval("last_boolean_expression = " + condition);
				boolean b = (Boolean) get("last_boolean_expression");
				System.out.println(toJSON(condition + " -> " + b));
				
				if (b)
				{
					loopAnchors.push(programCounter);
					scanToNextOpeningBrace();
				}
				else
				{
					var = isAssignment(init);
					refs.remove(var);
					forLoops.remove(forLoops.indexOf(programCounter));
					cleanUpScope();
					skipNextBlock();
				}
			}
			else if (isClosingBrace(line))
			{
				if (!loopAnchors.isEmpty())
				{
					programCounter = loopAnchors.pop();
				}
				else
				{
					programCounter++;
				}
			}
			else if (!isBrace(line))
			{
				//System.out.println("DEBUG: executing statement");
				eval(line);
				System.out.println(toJSON(line));
				programCounter++;
			}
			else
			{
				programCounter++;
			}
		}
	}

}
