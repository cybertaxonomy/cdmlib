package eu.etaxonomy.cdm.model.reference;

public interface IPublicationBase extends IReferenceBase {
	
	public String getPublisher() ;
	
	public void setPublisher(String publisher) ;
	/**
	 * @param publisher the publisher to set
	 * @param placePublished the place where the publication was published
	 */
	public void setPublisher(String publisher, String placePublished);
		

	public String getPlacePublished() ;
	
	public void setPlacePublished(String placePublished) ;
}
