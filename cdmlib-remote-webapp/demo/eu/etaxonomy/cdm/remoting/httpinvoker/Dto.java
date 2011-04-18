package eu.etaxonomy.cdm.remoting.httpinvoker;

import java.io.Serializable;

public class Dto implements Serializable {

	private String text;
	private int number;
	
	public Dto (String text, int number) {
		this.text = text;
		this.number = number;		
	}
	
	public Dto () {
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	
}
