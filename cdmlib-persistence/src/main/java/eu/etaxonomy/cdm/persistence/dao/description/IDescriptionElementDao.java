package eu.etaxonomy.cdm.persistence.dao.description;

import java.util.List;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;
import eu.etaxonomy.cdm.persistence.dao.common.ISearchableDao;

public interface IDescriptionElementDao extends IAnnotatableDao<DescriptionElementBase>,ISearchableDao<DescriptionElementBase> {
	/**
	 * This query is designed to search the the descriptions. 
	 * This is complicated somewhat by the 1 ... n relation between
	 * Descriptions and their descriptionElements, and also by the language aspect
	 * (i.e. that every feature can be written in many languages). 
	 * 
	 * What we do here is return a list of TextData objects where 
	 * the partOfDescription property is hydrated
	 * 
	 * If the description is a TaxonDescription, then the Taxon and 
	 * is hydrated too. If the description is a TaxonNameDescription
	 * the TaxonName is hydrated. If the description is a SpecimenDescription, the
	 * specimens are hydrated
	 * 
	 * HOWEVER, until hibernate search changes the way it handles subclasses
	 * (i.e. indexing the properties of subclasses when the entity is only
	 * typed as a superclass), we'll not be able to sort by a property of the TaxonDescription without 
	 * a pretty nasty performance penalty (remember, we're potentially searching
	 * all of the textual content here, so sorting this one-to-n by description.taxon or description.
	 * name is a bit of a no go). 
	 * 
	 * @param queryString
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 * @throws QueryParseException
	 */
	public List<TextData> searchTextData(String queryString, Integer pageSize, Integer pageNumber);
	
	/**
	 * Return a count of TextData elements who's content matches the query string
	 * 
	 * @param queryString a query string following Lucene Query Parser syntax
	 * @return a count of matching TextData elements
	 */
	public int countTextData(String queryString);
	
    /**
     * Returns a List of Media that are associated with a given description element
     * 
	 * @param descriptionElement the description element associated with these media
	 * @param pageSize The maximum number of media returned (can be null for all related media)
	 * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
     * @return a List of media instances
     */
    public List<Media> getMedia(DescriptionElementBase descriptionElement, Integer pageSize, Integer pageNumber);
	
    /**
     * Returns a count of Media that are associated with a given description element
     * 
	 * @param descriptionElement the description element associated with these media
     * @return a count of media instances
     */
	public int countMedia(DescriptionElementBase descriptionElement);
}
