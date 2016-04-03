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
import java.util.HashMap;
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
	
	public Pattern assignmentPattern;
	public Pattern ifPattern;
	public Pattern whilePattern;
	public Pattern forPattern;
	public Interpreter i;
	public ArrayList<String> code; // code stored line by line
	public ArrayList<String> refs; // variable names
	public HashMap<String, String> refTypes;
	public ArrayList<String> scopeRefs;
	
	public int programCounter = 0; // current line of execution
	public ArrayList<Integer> forLoops;
	public Deque<Integer> loopAnchors;
	
	public int step = 0;
	
	private ArrayList<String> jsons;
	private ArrayList<Snapshot> snaps;
	private String jsonForm;
	
	public String getJSON()
	{
		return jsonForm;
	}
	
	public ArrayList<String> getJSONArrayList()
	{
		return jsons;
	}
	
	public ArrayList<Snapshot> getSnapshots()
	{
		return snaps;
	}
	
	public void jsonPrintln(String s)
	{
		jsonForm += s + "\n";
	}
	
	public void jsonPrint(String s)
	{
		jsonForm += s;
	}

	// Wrapper method for Interpreter.eval
	public void eval(String statement)
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
	public Object get(String arg)
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
	public ArrayList<String> getCodeFromFile(String filePath)
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
//	public String isAssignment(String line)
//	{	
//		Matcher m = assignmentPattern.matcher(line);
//		
//		if (m.matches())
//		{
//			return m.group(VARIABLE_GROUP);
//		}
//		
//		return null;
//	}
	
	public Ref isAssignment(String line)
	{	
		Matcher m = assignmentPattern.matcher(line);
		
		if (m.matches())
		{
			return new Ref(m.group(VARIABLE_GROUP), m.group(1));
		}
		
		return null;
	}
	
	// If line is an if (i.e. if (x < 10), returns the boolean expression as a string, null otherwise
	public String isIf(String line)
	{
		Matcher m = ifPattern.matcher(line);
		
		if (m.matches())
		{
			return m.group(1);
		}
		return null;
	}
	
	public boolean isElse(String line)
	{
		return line.equals("else");
	}
	
	public String isWhile(String line)
	{
		Matcher m = whilePattern.matcher(line);
		
		if (m.matches())
		{
			return m.group(1);
		}
		return null;
	}
	
	public ArrayList<String> isFor(String line)
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
	public boolean isBrace(String line)
	{
		return isOpeningBrace(line) || isClosingBrace(line);
	}
	
	public boolean isOpeningBrace(String line)
	{
		return line.equals("{");
	}
	
	public boolean isClosingBrace(String line)
	{
		return line.equals("}");
	}
	
	public void scanToNextOpeningBrace()
	{
		while (!isOpeningBrace(code.get(programCounter)))
		{
			programCounter++;
		}
		programCounter++;
	}
	
	public void scanToNextClosingBrace()
	{
		while (!isClosingBrace(code.get(programCounter)))
		{
			programCounter++;
		}
		programCounter++;
	}
	
	public void skipNextBlock()
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
	
	public void cleanUpScope()
	{
		for (String var : scopeRefs)
		{
			refs.remove(var);
			refTypes.remove(var);
		}
		scopeRefs.clear();
	}
	
	public String toJSON(String line)
	{
		step++;
		
		String json = "{\n\t\"code\" : \"" + line + "\",\n\t\"pc\" : " + (programCounter+1) + ",\n\t\"step\" : " + step + ",\n\t\"state\" : [ \n";
		Snapshot snap = new Snapshot(line, programCounter+1, step);
		
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
			
			json += "\t\t\"" + r + "\" : {\n\t\t\t\"type\" : \"" + refTypes.get(r) + "\",\n\t\t\t\"hashCode\" : " + o.hashCode() + ",\n\t\t\t\"contents\" : " + description;
			
			Ref currentRef = new Ref(r, refTypes.get(r));
			currentRef.hashCode = o.hashCode();
			currentRef.contents = description;
			snap.add(currentRef);
			
			if (refs.indexOf(r) == refs.size() - 1)
			{
				json += "\n\t\t}\n";
			}
			else
			{
				json += "\n\t\t},\n";
			}
		}
		json += "\t]\n}";
		
		jsons.add(json);
		snaps.add(snap);
		
		return json;
	}
	
	public Trace(String fileName)
	{
		jsons = new ArrayList<String>();
		snaps = new ArrayList<Snapshot>();
		i = new Interpreter();
		assignmentPattern = Pattern.compile(ASSIGNMENT_PATTERN);
		ifPattern = Pattern.compile(IF_PATTERN);
		whilePattern = Pattern.compile(WHILE_PATTERN);
		forPattern = Pattern.compile(FOR_PATTERN);
		loopAnchors = new ArrayDeque<Integer>();
		forLoops = new ArrayList<Integer>();
		scopeRefs = new ArrayList<String>();
		
		if (fileName != null)
		{
			code = getCodeFromFile(fileName);
		}
		else
		{
			code = getCodeFromFile(TEST_FILE);
		}
		
		refs = new ArrayList<String>();
		refTypes = new HashMap<String, String>();
		
		Ref var = null;
		String condition = null;
		ArrayList<String> forComponents = new ArrayList<String>();
		jsonForm = "";
		
		while (programCounter < code.size())
		{	
			String line = code.get(programCounter);
			
			if ((var = isAssignment(line)) != null)
			{
				//System.out.println("DEBUG: assignment statement found");
				refs.add(var.name);
				refTypes.put(var.name, var.type);
				if (loopAnchors.size() > 0)
				{
					scopeRefs.add(var.name);
				}
				eval(line);
				jsonPrintln(toJSON(line));
				programCounter++;
			}
			else if ((condition = isIf(line)) != null)
			{
				//System.out.println("DEBUG: conditional statement found");
				eval("last_boolean_expression = " + condition);
				boolean b = (Boolean) get("last_boolean_expression");
				jsonPrintln(toJSON(condition + " -> " + b));
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
				jsonPrintln(toJSON(condition + " -> " + b));
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
					jsonPrintln(toJSON(update));
				}
				else
				{
					forLoops.add(programCounter);
					var = isAssignment(init);
					refs.add(var.name);
					refTypes.put(var.name, var.type);
					scopeRefs.add(var.name);
					eval(init);
					jsonPrintln(toJSON(init));
				}
				
				eval("last_boolean_expression = " + condition);
				boolean b = (Boolean) get("last_boolean_expression");
				jsonPrintln(toJSON(condition + " -> " + b));
				
				if (b)
				{
					loopAnchors.push(programCounter);
					scanToNextOpeningBrace();
				}
				else
				{
					//var = isAssignment(init);
					//refs.remove(var.name);
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
				jsonPrintln(toJSON(line));
				programCounter++;
			}
			else
			{
				programCounter++;
			}
		}
	}

}
