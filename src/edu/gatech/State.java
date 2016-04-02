package edu.gatech;

import java.util.ArrayList;

public class State {
	private String code;
	private int pc;
	private int step;
	private ArrayList<Ref> refs;
	
	public State(String json)
	{
		refs = new ArrayList<Ref>();
	}
}
