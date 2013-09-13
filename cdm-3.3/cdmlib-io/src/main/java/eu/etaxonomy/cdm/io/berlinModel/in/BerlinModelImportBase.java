/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.common.DbImportBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.EDITOR;
import eu.etaxonomy.cdm.io.common.IPartitionedIO;
import eu.etaxonomy.cdm.io.common.TdwgAreaProvider;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public abstract class BerlinModelImportBase extends DbImportBase<BerlinModelImportState, BerlinModelImportConfigurator>  implements ICdmIO<BerlinModelImportState>, IPartitionedIO<BerlinModelImportState> {
	private static final Logger logger = Logger.getLogger(BerlinModelImportBase.class);
	
	public BerlinModelImportBase(String tableName, String pluralString ) {
		super(tableName, pluralString);
	}
	

	/**
	 * @return
	 */
	protected String getIdQuery(BerlinModelImportState state){
		String result = " SELECT " + getTableName() + "id FROM " + getTableName();
		return result;
	}

	
	protected boolean doIdCreatedUpdatedNotes(BerlinModelImportState state, DescriptionElementBase descriptionElement, ResultSet rs, String id, String namespace) throws SQLException{
		boolean success = true;
		//id
		success &= doId(state, descriptionElement, id, namespace);
		//createdUpdateNotes
		success &= doCreatedUpdatedNotes(state, descriptionElement, rs);
		return success;
	}
	
	protected boolean doIdCreatedUpdatedNotes(BerlinModelImportState state, IdentifiableEntity identifiableEntity, ResultSet rs, long id, String namespace, boolean excludeUpdated)	
				throws SQLException{
		boolean success = true;
		//id
		success &= doId(state, identifiableEntity, id, namespace);
		//createdUpdateNotes
		success &= doCreatedUpdatedNotes(state, identifiableEntity, rs, excludeUpdated);
		return success;
	}

	
	protected boolean doIdCreatedUpdatedNotes(BerlinModelImportState state, IdentifiableEntity identifiableEntity, ResultSet rs, long id, String namespace)
			throws SQLException{
		boolean excludeUpdated = false;
		return doIdCreatedUpdatedNotes(state, identifiableEntity, rs, id, namespace, excludeUpdated);
	}
	
	protected boolean doCreatedUpdatedNotes(BerlinModelImportState state, AnnotatableEntity annotatableEntity, ResultSet rs)
			throws SQLException{
		boolean excludeUpdated = false;
		return doCreatedUpdatedNotes(state, annotatableEntity, rs, excludeUpdated);
	}
	
	protected boolean doCreatedUpdatedNotes(BerlinModelImportState state, AnnotatableEntity annotatableEntity, ResultSet rs, boolean excludeUpdated)
			throws SQLException{

		BerlinModelImportConfigurator config = state.getConfig();
		Object createdWhen = rs.getObject("Created_When");
		String createdWho = rs.getString("Created_Who");
		createdWho = handleHieraciumPilosella(createdWho);
		Object updatedWhen = null;
		String updatedWho = null;
		if (excludeUpdated == false){
			try {
				updatedWhen = rs.getObject("Updated_When");
				updatedWho = rs.getString("Updated_who");
			} catch (SQLException e) {
				//Table "Name" has no updated when/who
			}
		}
		String notes = rs.getString("notes");
		
		boolean success  = true;
		
		//Created When, Who, Updated When Who
		if (config.getEditor() == null || config.getEditor().equals(EDITOR.NO_EDITORS)){
			//do nothing
		}else if (config.getEditor().equals(EDITOR.EDITOR_AS_ANNOTATION)){
			String createdAnnotationString = "Berlin Model record was created By: " + String.valueOf(createdWho) + " (" + String.valueOf(createdWhen) + ") ";
			if (updatedWhen != null && updatedWho != null){
				createdAnnotationString += " and updated By: " + String.valueOf(updatedWho) + " (" + String.valueOf(updatedWhen) + ")";
			}
			Annotation annotation = Annotation.NewInstance(createdAnnotationString, Language.DEFAULT());
			annotation.setCommentator(config.getCommentator());
			annotation.setAnnotationType(AnnotationType.TECHNICAL());
			annotatableEntity.addAnnotation(annotation);
		}else if (config.getEditor().equals(EDITOR.EDITOR_AS_EDITOR)){
			User creator = getUser(state, createdWho);
			User updator = getUser(state, updatedWho);
			DateTime created = getDateTime(createdWhen);
			DateTime updated = getDateTime(updatedWhen);
			annotatableEntity.setCreatedBy(creator);
			annotatableEntity.setUpdatedBy(updator);
			annotatableEntity.setCreated(created);
			annotatableEntity.setUpdated(updated);
		}else {
			logger.warn("Editor type not yet implemented: " + config.getEditor());
		}
		
		
		//notes
		doNotes(annotatableEntity, notes);
		return success;
	}
	
	/**
	 * Special usecase for EDITWP6 import where in the createdWho field the original ID is stored
	 * @param createdWho
	 * @return
	 */
	private String handleHieraciumPilosella(String createdWho) {
		String result = createdWho;
		if (result == null){
			return null;
		}else if (result.startsWith("Hieracium_Pilosella import from EM")){
			return "Hieracium_Pilosella import from EM";
		}else{
			return result;
		}
	}

	private DateTime getDateTime(Object timeString){
		if (timeString == null){
			return null;
		}
		DateTime dateTime = null;
		if (timeString instanceof Timestamp){
			Timestamp timestamp = (Timestamp)timeString;
			dateTime = new DateTime(timestamp);
		}else{
			logger.warn("time ("+timeString+") is not a timestamp. Datetime set to current date. ");
			dateTime = new DateTime();
		}
		return dateTime;
	}
	
	/**
	 * @param state
	 * @param newTaxonId
	 * @param taxonMap
	 * @param factId
	 * @return
	 */
	protected Taxon getTaxon(BerlinModelImportState state, int taxonId, Map<String, TaxonBase> taxonMap, int factId) {
		TaxonBase<?> taxonBase = taxonMap.get(String.valueOf(taxonId));
		
		//TODO for testing
		if (taxonBase == null && ! state.getConfig().isDoTaxa()){
			taxonBase = Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
		}
		
		Taxon taxon;
		if ( taxonBase instanceof Taxon ) {
			taxon = (Taxon) taxonBase;
		} else if (taxonBase != null) {
			logger.warn("TaxonBase (" + taxonId + ") for Fact(Specimen) with factId " + factId + " was not of type Taxon but: " + taxonBase.getClass().getSimpleName());
			return null;
		} else {
			logger.warn("TaxonBase (" + taxonId + ") for Fact(Specimen) with factId " + factId + " is null.");
			return null;
		}
		return taxon;
	}


	/**
	 * 	Searches first in the detail maps then in the ref maps for a reference.
	 *  Returns the reference as soon as it finds it in one of the map, according
	 *  to the order of the map.
	 *  If nomRefDetailFk is <code>null</code> no search on detail maps is performed.
	 *  If one of the maps is <code>null</code> no search on the according map is
	 *  performed. <BR>
	 *  You may define the order of search by the order you pass the maps but
	 *  make sure to always pass the detail maps first.
	 * @param firstDetailMap
	 * @param secondDetailMap
	 * @param firstRefMap
	 * @param secondRefMap
	 * @param nomRefDetailFk
	 * @param nomRefFk
	 * @return
	 */
	protected Reference getReferenceFromMaps(
			Map<String, Reference> firstDetailMap,
			Map<String, Reference> secondDetailMap, 
			Map<String, Reference> firstRefMap,
			Map<String, Reference> secondRefMap,
			String nomRefDetailFk,
			String nomRefFk) {
		Reference ref = null;
		ref = getReferenceDetailFromMaps(firstDetailMap, secondDetailMap, nomRefDetailFk);
		if (ref == null){
			ref = getReferenceOnlyFromMaps(firstRefMap, secondRefMap, nomRefFk);
		}
		return ref;
	}
	
	/**
	 * As getReferenceFromMaps but search is performed only on references, not on
	 * detail maps.
	 * @param firstRefMap
	 * @param secondRefMap
	 * @param nomRefFk
	 * @return
	 */
	protected Reference getReferenceOnlyFromMaps(
			Map<String, Reference> firstRefMap,
			Map<String, Reference> secondRefMap,
			String nomRefFk) {
		Reference ref = null;
		if (firstRefMap != null){
			ref = firstRefMap.get(nomRefFk);
		}else{
			logger.warn("First reference map does not exist");
		}
		if (ref == null){
			if (secondRefMap != null){
				ref = secondRefMap.get(nomRefFk);
			}else{
				logger.warn("Second reference map does not exist");		
			}
		}
		return ref;
	}

	/**
	 * Searches for a reference in the first detail map. If it does not exist it 
	 * searches in the second detail map. Returns null if it does not exist in any map.
	 * A map may be <code>null</code> to avoid search on this map.
	 * @param secondDetailMap 
	 * @param firstDetailMap 
	 * @param nomRefDetailFk 
	 * @return
	 */
	private Reference getReferenceDetailFromMaps(Map<String, Reference> firstDetailMap, Map<String, Reference> secondDetailMap, String nomRefDetailFk) {
		Reference<?> result = null;
		if (nomRefDetailFk != null){
			//get ref
			if (firstDetailMap != null){
				result = firstDetailMap.get(nomRefDetailFk);
			}
			if (result == null && secondDetailMap != null){
				result = secondDetailMap.get(nomRefDetailFk);
			}
		}
		return result;
	}
	
	protected NamedArea getOtherAreas(BerlinModelImportState state, String emCodeString, String tdwgCodeString) {
		String em = CdmUtils.Nz(emCodeString).trim();
		String tdwg = CdmUtils.Nz(tdwgCodeString).trim();
		//Cichorieae + E+M
		if ("EM".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.euroMedUuid, "Euro+Med", "Euro+Med area", "EM", null, null);
		}else if("Rf".equals(em)){
			return Country.RUSSIANFEDERATION();
		
		}else if("KRY-OO;UKR-UK".equals(tdwg)){
			return Country.UKRAINE();
		
		}else if("TCS-AZ;TCS-NA".equals(tdwg)){
			return Country.AZERBAIJANREPUBLICOF();
		}else if("TCS-AB;TCS-AD;TCS-GR".equals(tdwg)){
			return Country.GEORGIA();
		
		
		}else if("Cc".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidCaucasia, "Caucasia (Ab + Ar + Gg + Rf(CS))", "Euro+Med area 'Caucasia (Ab + Ar + Gg + Rf(CS))'", "Cc", null, null);
		}
		
		//E+M
		else if("EUR".equals(em)){
			return TdwgAreaProvider.getAreaByTdwgAbbreviation("1");
		}else if("14".equals(em)){
			return TdwgAreaProvider.getAreaByTdwgAbbreviation("14");
		}else if("21".equals(em)){
			return TdwgAreaProvider.getAreaByTdwgAbbreviation("21");  // Macaronesia
		}else if("33".equals(em)){
			return TdwgAreaProvider.getAreaByTdwgAbbreviation("33");
		
		}else if("SM".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidSerbiaMontenegro, "Serbia & Montenegro", "Euro+Med area 'Serbia & Montenegro'", "SM", NamedAreaType.ADMINISTRATION_AREA(), null);
		}else if("Sr".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidSerbia, "Serbia", "Euro+Med area 'Serbia' (including Kosovo and Vojvodina)", "Sr", NamedAreaType.ADMINISTRATION_AREA(), null);
		
		
		//see #2769
		}else if("Rs".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidUssr, "Former USSR", "Euro+Med area 'Former USSR'", "Rs", NamedAreaType.ADMINISTRATION_AREA(), null);
		}else if("Rs(N)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidRussiaNorthern, "Russia Northern", "Euro+Med area 'Russia Northern'", "Rs(N)", null, null);
		}else if("Rs(B)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidRussiaBaltic, "Russia Baltic", "Euro+Med area 'Russia Baltic'", "Rs(B)", null, null);
		}else if("Rs(C)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidRussiaCentral, "Russia Central", "Euro+Med area 'Russia Central'", "Rs(C)", null, null);
		}else if("Rs(W)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidRussiaSouthWest, "Russia Southwest", "Euro+Med area 'Russia Southwest'", "Rs(W)", null, null);
		}else if("Rs(E)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidRussiaSouthEast, "Russia Southeast", "Euro+Med area 'Russia Southeast'", "Rs(E)", null, null);
			
		//see #2770
		}else if("AE".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidEastAegeanIslands, "East Aegean Islands", "Euro+Med area 'East Aegean Islands'", "AE", null, null);
		}else if("AE(T)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidTurkishEastAegeanIslands, "Turkish East Aegean Islands", "Euro+Med area 'Turkish East Aegean Islands'", "AE(T)", null, null);
		}else if("Tu".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidTurkey, "Turkey", "Euro+Med area 'Turkey' (without AE(T))", "Tu", null, null);
		
		//TODO Azores, Canary Is. 
		}else if("Md(D)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidDesertas, "Desertas", "Euro+Med area 'Desertas'", "Md(D)", null, null);
		}else if("Md(M)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidMadeira, "Madeira", "Euro+Med area 'Madeira'", "Md(M)", null, null);
		}else if("Md(P)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidPortoSanto, "Porto Santo", "Euro+Med area 'Porto Santo'", "Md(P)", null, null);
		//Azores
		}else if("Az(L)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidFlores, "Flores", "Euro+Med area 'Flores'", "Az(L)", null, null);
		}else if("Az(C)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidCorvo, "Corvo", "Euro+Med area 'Corvo'", "Az(C)", null, null);
		}else if("Az(F)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidFaial, "Faial", "Euro+Med area 'Faial'", "Az(F)", null, null);
		}else if("Az(G)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidGraciosa, "Graciosa", "Euro+Med area 'Graciosa'", "Az(G)", null, null);
		}else if("Az(J)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidSaoJorge, "S\u00E3o Jorge", "Euro+Med area 'S\u00E3o Jorge'", "Az(J)", null, null);
		}else if("Az(M)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidSaoMiguel, "S\u00E3o Miguel", "Euro+Med area 'S\u00E3o Miguel'", "Az(M)", null, null);
		}else if("Az(P)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidPico, "Pico", "Euro+Med area 'Pico'", "Az(P)", null, null);
		}else if("Az(S)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidSantaMaria, "Santa Maria", "Euro+Med area 'Santa Maria'", "Az(S)", null, null);
		}else if("Az(T)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidTerceira, "Terceira", "Euro+Med area 'Terceira'", "Az(T)", null, null);
		//Canary Islands
		}else if("Ca(C)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidGranCanaria, "Gran Canaria", "Euro+Med area 'Gran Canaria'", "Ca(C)", null, null);
		}else if("Ca(F)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidFuerteventura, "Fuerteventura with Lobos", "Euro+Med area 'Fuerteventura with Lobos'", "Ca(F)", null, null);
		}else if("Ca(G)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidGomera, "Gomera", "Euro+Med area 'Gomera'", "Ca(G)", null, null);
		}else if("Ca(H)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidHierro, "Hierro", "Euro+Med area 'Hierro'", "Ca(H)", null, null);
		}else if("Ca(L)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidLanzaroteWithGraciosa, "Lanzarote with Graciosa", "Euro+Med area 'Lanzarote with Graciosa'", "Ca(L)", null, null);
		}else if("Ca(P)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidLaPalma, "La Palma", "Euro+Med area 'La Palma'", "Ca(P)", null, null);
		}else if("Ca(T)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidTenerife, "Tenerife", "Euro+Med area 'Tenerife'", "Ca(T)", null, null);
			//Baleares
		}else if("Bl(I)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidIbizaWithFormentera, "Ibiza with Formentera", "Euro+Med area 'Ibiza with Formentera'", "Bl(I)", null, null);
		}else if("Bl(M)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidTerceira, "Mallorca", "Euro+Med area 'Mallorca'", "Bl(M)", null, null);
		}else if("Bl(N)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidTerceira, "Menorca", "Euro+Med area 'Menorca'", "Bl(N)", null, null);
		}
		
		logger.warn("Area(em: '" + em + "', tdwg: '" + tdwg +"') could not be found for occurrence import");
		
		return null;
	}

	
}
