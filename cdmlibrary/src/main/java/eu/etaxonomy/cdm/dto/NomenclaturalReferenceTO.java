package eu.etaxonomy.cdm.dto;

public class NomenclaturalReferenceTO{
	
	/**
	 * 	URIs like DOIs, LSIDs or Handles for this reference
	 */
	private String uri;
	/**
	 * 
	 */
	private String citation;
	/**
	 * Details of the nomenclatural reference (protologue). These are mostly (implicitly) pages but can also be figures or
	 * tables or any other element of a publication. {only if a nomenclatural reference exists}
	 */
	private String microReference;
	/**
	 * year of the publication 
	 */
	private String year;
}
