/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.reference;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
//import eu.etaxonomy.cdm.model.reference.CdDvd;

public class CdDvdDefaultCacheStrategy <T extends ReferenceBase> extends NomRefDefaultCacheStrategyBase<T>  implements  INomenclaturalReferenceCacheStrategy<T> {
	private static final Logger logger = Logger.getLogger(CdDvdDefaultCacheStrategy.class);
	
	private String prefixEdition = "ed.";
	private String prefixSeries = "ser.";
	private String prefixVolume = "vol.";
	private String blank = " ";
	private String comma = ",";
	private String dot =".";
	
	final static UUID uuid = UUID.fromString("68076ca5-d517-489c-8ae2-01d3c38cc788");
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.StrategyBase#getUuid()
	 */
	@Override
	protected UUID getUuid() {
		return uuid; 
	}
	
	
	/**
	 * Factory method
	 * @return
	 */
	public static CdDvdDefaultCacheStrategy NewInstance(){
		return new CdDvdDefaultCacheStrategy<ReferenceBase>();
	}
	
	/**
	 * Constructor
	 */
	private CdDvdDefaultCacheStrategy(){
		super();
	}


	@Override
	public String getNomRefTitleWithoutYearAndAuthor(T nomenclaturalReference){
		if (nomenclaturalReference == null){
			return null;
		}
		String nomRefCache = "";
		//TODO
		String titelAbbrev = CdmUtils.Nz(nomenclaturalReference.getTitle()).trim();
//		String publisher = CdmUtils.Nz(nomenclaturalReference.getPublisher());
		
		boolean needsComma = false;
		//titelAbbrev
		String titelAbbrevPart = "";
		if (!"".equals(titelAbbrev)){
			nomRefCache = titelAbbrev + blank; 
		}
//		//publisher
//		String publisherPart = "";
//		if (!"".equals(publisher)){
//			publisherPart = publisher;
//			needsComma = true;
//		}
//		nomRefCache += publisherPart;

		
		//delete .
		while (nomRefCache.endsWith(".")){
			nomRefCache = nomRefCache.substring(0, nomRefCache.length()-1);
		}
		return nomRefCache.trim();
	}
	
	private boolean isNumeric(String string){
		if (string == null){
			return false;
		}
		try {
			Double.valueOf(string);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
		
	}



}
