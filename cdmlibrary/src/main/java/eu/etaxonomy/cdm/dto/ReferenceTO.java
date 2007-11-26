package eu.etaxonomy.cdm.dto;

import java.net.URI;
import java.util.Calendar;

import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;

public class ReferenceTO extends BaseTO {
	
	/**
	 * title of the publication including authors except if the ReferenceTO 
	 * is used for nomenclatural references. See also {@link INomenclaturalReference}
	 */
	private String title;
	/**
	 * year of the publication 
	 */
	private Calendar year; 
}
