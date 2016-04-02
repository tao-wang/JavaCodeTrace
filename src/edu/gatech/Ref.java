package edu.gatech;

public class Ref {
	public String name;
	public String type;
	public int hashCode;
	@SuppressWarnings("unused")
	private String contents;
	
	public Ref(String name, String type)
	{
		this.name = name;
		this.type = type;
		this.contents = "";
	}
	
	public void setContents(String contents)
	{
		this.contents = contents;
	}
}
