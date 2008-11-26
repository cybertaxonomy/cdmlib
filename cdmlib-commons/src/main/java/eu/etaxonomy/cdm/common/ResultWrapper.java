/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.common;

import org.apache.log4j.Logger;

//import org.apache.log4j.Logger;

/**
 * Wrapps a result object so it can be used as method parameter and changed within the method.
 * This is useful especially for simple data types like <code>Boolean</code> etc.<br>
 * Example (usage):<br><code>
 * 	public String myMethod(String oneParameter, ResultWrapper<Boolean> success){<br>
 * 	    __if (oneParameter.equals("foo")){<br>
 * 	    ____success = success.setValue(false);<br>
 * 	    ____return "Foo";<br>
 * 	__}else{<br>
 * 	____//don't change success<br>
 * 	____return "All the best";<br>
 * 	__}<br>
 * 	}
 * </code>
 * Here a String is returned but the boolean value may also be changed and it's value is useable 
 * by the calling method
 *  
 * @author a.mueller
 * @created 01.11.2008
 * @version 1.0
 */
public class ResultWrapper<T> {
	private static final Logger logger = Logger.getLogger(ResultWrapper.class);

	public static final ResultWrapper<Boolean> NewInstance(Boolean value){
		ResultWrapper<Boolean> result = new ResultWrapper<Boolean>();
		result.setValue(value);
		if (logger.isDebugEnabled()){logger.debug("New Instance");}
		return result;
	}
	
	T object;

	/**
	 * @return the object
	 */
	public T getValue() {
		return object;
	}

	/**
	 * @param object the object to set
	 */
	public void setValue(T value) {
		this.object = value;
	}
}
