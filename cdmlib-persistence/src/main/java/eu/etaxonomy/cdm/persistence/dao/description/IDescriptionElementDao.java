package eu.etaxonomy.cdm.persistence.dao.description;

import java.util.List;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;

public interface IDescriptionElementDao extends ICdmEntityDao<DescriptionElementBase> {
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
	 * (i.e. indexing the properties of subclasses when the entitiy is only
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
	
	public int countTextData(String queryString);
	
    public List<Media> getMedia(DescriptionElementBase descriptionElement, Integer pageSize, Integer pageNumber);
	
	public int countMedia(DescriptionElementBase descriptionElement);
}
