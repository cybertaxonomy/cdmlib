/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.io.common;

/**
 * @author a.babadshanjan
 * @created 13.11.2008
 */
public interface IIoConfigurator {

//	public static enum CHECK{
//		CHECK_ONLY,
//		IMPORT_WITHOUT_CHECK,
//		CHECK_AND_IMPORT,
//	}
//	
//	public static enum DO_REFERENCES{
//		NONE,
//		NOMENCLATURAL,
//		CONCEPT_REFERENCES,
//		ALL
//	}
//	
//	public abstract CHECK getCheck();
	
	
	/**
	 * A String representation of the used source may it be a source to be imported (e.g. "BerlinModel Cichorieae Database")
	 * or a source to be exported (e.g. "CDM Cichorieae Database")
	 * @return String representing the source for the io
	 */
	public String getSourceNameString();
	

	/**
	 * A String representation of the destination may it be an import destination and therefore a CDM (e.g. CDM Cichorieae Database)
	 * or an export destination (e.g. CDM XML)
	 * @return
	 */
	public String getDestinationNameString();
	
}
