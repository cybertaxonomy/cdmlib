/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common.mapping.out;

import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;


/**
 * Interface for export transformer classes. Mainly to transform defined terms and enumerations.
 *
 * @see IInputTransformer
 *
 * @author a.mueller
 * @since 15.03.2010
 */
public interface IExportTransformer {

	//Feature
	public Object getKeyByFeature(Feature feature) throws UndefinedTransformerMethodException;
	public String getCacheByFeature(Feature feature) throws UndefinedTransformerMethodException;

	//Language
	public Object getKeyByLanguage(Language language) throws UndefinedTransformerMethodException;
	public String getCacheByLanguage(Language language) throws UndefinedTransformerMethodException;

	//Extension Type
	public Object getKeyByExtensionType(ExtensionType extensionType) throws UndefinedTransformerMethodException;
	public String getCacheByExtensionType(ExtensionType language) throws UndefinedTransformerMethodException;

	//Presence Term
	public Object getKeyByPresenceAbsenceTerm(PresenceAbsenceTerm term) throws UndefinedTransformerMethodException;
	public String getCacheByPresenceAbsenceTerm(PresenceAbsenceTerm term) throws UndefinedTransformerMethodException;

	//NamedArea
	public Object getKeyByNamedArea(NamedArea area) throws UndefinedTransformerMethodException;
	public String getCacheByNamedArea(NamedArea area) throws UndefinedTransformerMethodException;

	//Nomenclatural status
	public Object getKeyByNomStatus(NomenclaturalStatusType status) throws UndefinedTransformerMethodException;
	public String getCacheByNomStatus(NomenclaturalStatusType status) throws UndefinedTransformerMethodException;

    //Reference type
    //we use the full reference as parameter as it may contain required information
    public String getCacheByReferenceType(Reference ref) throws UndefinedTransformerMethodException;

    //Nomenclatural code
    public String getCacheByNomenclaturalCode(NomenclaturalCode nomenclaturalCode) throws UndefinedTransformerMethodException;

    //Rank
    public String getCacheByRank(Rank rank) throws UndefinedTransformerMethodException;

	//Quality status,only needed for PESI export -> TODO refactor
	public String getQualityStatusCacheByKey(Integer qualityStatusFk) throws UndefinedTransformerMethodException;

	//Taxon status only needed for PESI export -> TODO refactor
	public String getTaxonStatusCacheByKey(Integer taxonStatusId) throws UndefinedTransformerMethodException;
}