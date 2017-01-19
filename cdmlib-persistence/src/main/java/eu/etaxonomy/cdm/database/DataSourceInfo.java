/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.datasource.AbstractDriverBasedDataSource;

/**
 * @author a.kohlbecker
 * @date 12.01.2010
 *
 */
public class DataSourceInfo {
	
	String url;
	String username;
	List<String> problems;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String userName) {
		this.username = userName;
	}
	public List<String> getProblems() {
		if(problems == null){
			problems = new ArrayList<String>();
		}
		return problems;
	}
	public void setProblems(List<String> problems) {
		this.problems = problems;
	}
	public boolean hasProblems() {
		return getProblems().size() > 0;
	}
	
	public DataSourceInfo(AbstractDriverBasedDataSource dataSource){
		if(dataSource != null){
			setUrl(dataSource.getUrl());
			setUsername(dataSource.getUsername());
		}
	}
	
	
	

}
