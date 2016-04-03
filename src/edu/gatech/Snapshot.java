package edu.gatech;

import java.util.ArrayList;

public class Snapshot {
	public String code;
	public int pc;
	public int step;
	public ArrayList<Ref> state;
	
	public Snapshot(String code, int pc, int step)
	{
		this.code = code;
		this.pc = pc;
		this.step = step;
		this.state = new ArrayList<Ref>();
	}
	
	public void add(Ref r)
	{
		state.add(r);
	}
	
	public Ref get(int i)
	{
		return state.get(i);
	}
}
