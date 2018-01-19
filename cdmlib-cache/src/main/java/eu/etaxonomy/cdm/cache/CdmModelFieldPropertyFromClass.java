package eu.etaxonomy.cdm.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CdmModelFieldPropertyFromClass implements Serializable {

	private static final long serialVersionUID = 5726395976531887526L;
	private String className;
	private String parentClassName;
	
	private List<String> fields = new ArrayList<String>();
	
	
	public CdmModelFieldPropertyFromClass(String className) {
		this.setClassName(className);
	}
	
	public String getParentClassName() {
		return parentClassName;
	}

	public void setParentClassName(String parentClassName) {
		this.parentClassName = parentClassName;
	}

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}
	
	public void addGetMethods(String getMethod) {
		this.fields.add(getMethod);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	

	
	
}
