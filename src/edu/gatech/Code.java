package edu.gatech;

public class Code
{
	public static int STATEMENT = 0;
	public static int IF_BLOCK = 1;
	public static int WHILE_BLOCK = 2;
	
	private int type;
	private String code;
	
	public Code(String code, int type)
	{
		setCode(code);
		setType(type);
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}
}
