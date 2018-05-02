/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.persistence.fetch;

import org.apache.log4j.Logger;


/**
 * @author a.mueller
 * @since 30.04.2008
 * @version 1.0
 */
public class CdmFetch {
	private static final Logger logger = Logger.getLogger(CdmFetch.class);

	public static CdmFetch NO_FETCH() {return new CdmFetch(0);}
	public static CdmFetch FETCH_NOTHING() {return new CdmFetch(0);}
	
	public static CdmFetch FETCH_DESCRIPTIONS() {return new CdmFetch(1);}
	public static CdmFetch FETCH_CHILDTAXA() {return  new CdmFetch(2);}
	public static CdmFetch FETCH_PARENT_TAXA() {return  new CdmFetch(3);}
	public static CdmFetch FETCH_ANNOTATIONS_ALL() {return  new CdmFetch(4);}
	public static CdmFetch FETCH_MARKER_ALL() {return  new CdmFetch(5);}
	public static CdmFetch FETCH_SYNONYMS() {return  new CdmFetch(6);}

	protected int fetchSum;

	private CdmFetch(int fetchId) {
		this.fetchSum = (int)Math.pow(2, fetchId);
	}

	public void add(CdmFetch cdmFetch) {
		this.fetchSum = (this.fetchSum | cdmFetch.fetchSum);
	}
	
	public boolean includes(CdmFetch cdmFetch){
		int andFetch = cdmFetch.fetchSum & this.fetchSum;
		if (andFetch == cdmFetch.fetchSum){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CdmFetch fetch1 = FETCH_DESCRIPTIONS();
		CdmFetch fetch2 = FETCH_MARKER_ALL();
		CdmFetch fetch3 = FETCH_SYNONYMS();
		CdmFetch fetch4 = FETCH_DESCRIPTIONS();
		
		fetch1.add(fetch2);
		logger.warn(fetch3.includes(fetch1));
		logger.warn(fetch1.includes(fetch2));
		logger.warn(fetch2.includes(fetch1));
		fetch2.add(fetch3);
		logger.warn(fetch2.includes(fetch1));
		fetch2.add(fetch3);
		logger.warn(fetch1.includes(fetch4));	
	}
	
}
