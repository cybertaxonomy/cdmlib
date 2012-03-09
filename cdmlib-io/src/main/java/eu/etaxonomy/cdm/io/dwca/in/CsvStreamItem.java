// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.util.Map;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.dwca.TermUri;

/**
 * @author a.mueller
 * @date 23.11.2011
 *
 */
public class CsvStreamItem implements IConverterInput<CsvStreamItem> {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CsvStream.class);

	public TermUri term;
	public Map<String, String> map;
	private CsvStream stream;
	
	/**
	 * @param term
	 * @param map
	 * @param stream
	 */
	public CsvStreamItem(TermUri term, Map<String, String> map, CsvStream stream) {
		super();
		this.term = term;
		this.map = map;
		this.stream = stream;
	}

	public String get(String mapKey){
		return this.map.get(mapKey);
	}

	public String get(TermUri termUri){
		return this.map.get(termUri.getUriString());
	}
	
	public void remove(TermUri termUri){
		this.map.remove(termUri.getUriString());
	}
	

	public void remove(String string) {
		this.map.remove(string);
	}

	
	/**
	 * Returns the location in the stream origin. For event messaging and maybe in future also
	 * for state analysis.
	 * @return
	 */
	public String getLocation() {
		return CdmUtils.concat("/", stream.getFilesLocation() ,String.valueOf(stream.getLine()));
	}
	
	@Override
	public String toString(){
		if (term == null && map == null){
			return super.toString();
		}else{
			return "[" + CdmUtils.concat("|", term.getUriString(), map.toString()) + "]";
		}
	}


	
}
