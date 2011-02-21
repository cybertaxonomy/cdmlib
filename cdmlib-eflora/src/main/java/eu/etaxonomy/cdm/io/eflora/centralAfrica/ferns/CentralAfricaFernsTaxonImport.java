/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.DerivedUnitType;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.mapping.DbImportAnnotationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportExtensionMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMethodMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbNotYetImplementedMapper;
import eu.etaxonomy.cdm.io.common.mapping.IMappingImport;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns.validation.CentralAfricaFernsTaxonImportValidator;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;


/**
 * @author a.mueller
 * @created 20.02.2010
 * @version 1.0
 */
@Component
public class CentralAfricaFernsTaxonImport  extends CentralAfricaFernsImportBase<TaxonBase> implements IMappingImport<TaxonBase, CentralAfricaFernsImportState>{
	private static final Logger logger = Logger.getLogger(CentralAfricaFernsTaxonImport.class);
	
	public static final UUID TNS_EXT_UUID = UUID.fromString("41cb0450-ac84-4d73-905e-9c7773c23b05");
	
	
	private DbImportMapping mapping;
	
	//second path is not used anymore, there is now an ErmsTaxonRelationImport class instead
	private boolean isSecondPath = false;
	
	private static final String pluralString = "taxa";
	private static final String dbTableName = "[African pteridophytes]";
	private static final Class cdmTargetClass = TaxonBase.class;

	public CentralAfricaFernsTaxonImport(){
		super(pluralString, dbTableName, cdmTargetClass);
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns.CentralAfricaFernsImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery() {
		String strQuery = " SELECT [Taxon number] FROM " + dbTableName ;
		return strQuery;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns.CentralAfricaFernsImportBase#getMapping()
	 */
	protected DbImportMapping getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping();
			
			mapping.addMapper(DbImportObjectCreationMapper.NewInstance(this, "Taxon number", TAXON_NAMESPACE)); //id + tu_status

			mapping.addMapper(DbImportMethodMapper.NewInstance(this, "mapTypes", ResultSet.class, CentralAfricaFernsImportState.class));
			mapping.addMapper(DbImportAnnotationMapper.NewInstance("Notes", AnnotationType.EDITORIAL()));

			mapping.addMapper(DbImportMethodMapper.NewInstance(this, "mapReferences", ResultSet.class, CentralAfricaFernsImportState.class));
			mapping.addMapper(DbImportMethodMapper.NewInstance(this, "mapNomRemarks", ResultSet.class, CentralAfricaFernsImportState.class));
			
			mapping.addMapper(DbImportExtensionMapper.NewInstance("Illustrations - non-original", CentralAfricaFernsTransformer.uuidIllustrationsNonOriginal, "Illustrations - non-original", "Illustrations - non-original", null));

//			mapping.addMapper(DbImportTextDataCreationMapper.NewInstance("Illustrations - non-original", objectToCreateNamespace, dbTaxonFkAttribute, this.TAXON_NAMESPACE, "Illustrations - non-original", Language.ENGLISH(), Feature, null);
//			mapping.addMapper(DbImportTextDataCreationMapper.NewInstance(dbIdAttribute, objectToCreateNamespace, dbTaxonFkAttribute, taxonNamespace, dbTextAttribute, Language.ENGLISH(), Feature.ECOLOGY(), null));

			//not yet implemented or ignore
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("Types XXX", "Method Mapper does not work yet. Needs implementation for all 5 types. FIXMEs in implementation"));

			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("Basionym of", "Needs better understanding"));
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("Synonym of", "Needs better understanding. Strange values like "));
			
			
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("Chromosome number" , "Wrong data. Seems to be 'reference full'"));
			
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("Book Publisher & Place" , "How to access the reference via String mapper?"));
			
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("Reprint no" , "What's this?"));
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("Date verified" , "Needed?"));
			
			
//			
//			UUID credibilityUuid = ErmsTransformer.uuidCredibility;
//			mapping.addMapper(DbImportExtensionMapper.NewInstance("tu_credibility", credibilityUuid, "credibility", "credibility", "credibility")); //Werte: null, unknown, marked for deletion
//			
			//ignore
//			mapping.addMapper(DbIgnoreMapper.NewInstance("cache_citation", "citation cache not needed in PESI"));
			
			//not yet implemented or ignore
//			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("tu_hidden", "Needs DbImportMarkerMapper implemented"));
			
		}
		return mapping;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(CentralAfricaFernsImportConfigurator config) {
		String strSelect = " SELECT * ";
		String strFrom = " FROM [African pteridophytes] as ap";
		String strWhere = " WHERE ( ap.[taxon number] IN (" + ID_LIST_TOKEN + ") )";
		String strOrderBy = " ORDER BY [Taxon number]";
		String strRecordQuery = strSelect + strFrom + strWhere + strOrderBy;
		return strRecordQuery;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
				Set<String> nameIdSet = new HashSet<String>();
				Set<String> referenceIdSet = new HashSet<String>();
				while (rs.next()){
	//				handleForeignKey(rs, nameIdSet, "PTNameFk");
	//				handleForeignKey(rs, referenceIdSet, "PTRefFk");
				}

			//reference map
//			nameSpace = "Reference";
//			cdmClass = Reference.class;
//			Map<String, Person> referenceMap = (Map<String, Person>)getCommonService().getSourcedObjectsByIdInSource(Person.class, teamIdSet, nameSpace);
//			result.put(Reference.class, referenceMap);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	private TaxonBase mapTypes(ResultSet rs, CentralAfricaFernsImportState state) throws SQLException{
		TaxonBase<?> taxonBase = state.getRelatedObject(state.CURRENT_OBJECT_NAMESPACE, state.CURRENT_OBJECT_ID, TaxonBase.class);
		TaxonNameBase name = taxonBase.getName();
		for (int i = 1; i <= 5; i++){
			String[] typeInfo = new String[3];
			typeInfo = getTypeInfo(rs, i);
			if (StringUtils.isBlank(typeInfo[0]) && StringUtils.isBlank(typeInfo[1]) && StringUtils.isBlank(typeInfo[2])){
				continue;
			}
			makeSingleType(state, name, typeInfo[0], typeInfo[1], typeInfo[2]);
		}
		return taxonBase;
	}
	
	
	private String[] getTypeInfo(ResultSet rs, int i) throws SQLException {
		String[] typeInfo = new String[3];
		String number;
		if (i == 1){
			number = "";
		}else{
			number = String.valueOf(i);
		}
		typeInfo[0] = rs.getString("Type" + number);
		typeInfo[1] = rs.getString("Type collector and number" + number);
		typeInfo[2] = rs.getString("Type location" + number);
		
		return typeInfo;
	}



	private void makeSingleType(CentralAfricaFernsImportState state, TaxonNameBase name, String typeString, String typeCollectorString, String typeLocationString) {
		if (name.getRank().isHigher(Rank.SPECIES())){
			//TODO move to TaxonRelationImport
			handleNameType(state, name, typeString, typeCollectorString, typeLocationString);
		}else{
			handleSpecimenType(state, name, typeString, typeCollectorString, typeLocationString);
		}
	}



	private void handleSpecimenType(CentralAfricaFernsImportState state, TaxonNameBase name, String typeString, String typeCollectorString, String typeLocationString) {
		List<SpecimenTypeDesignation> designations = new ArrayList<SpecimenTypeDesignation>();
		typeLocationString = CdmUtils.Nz(typeLocationString);
		if (typeLocationString.equalsIgnoreCase("not located")){
			
		}else{
			String[] splits = typeLocationString.split(";");
			for (String split : splits){
				List<SpecimenTypeDesignation> splitDesignations = handleTypeLocationPart(state, typeString, typeCollectorString, split);
				designations.addAll(splitDesignations);
			}
		}
		if (designations.size() == 0){
			logger.error(state.getTaxonNumber() + " - No designations defined. TypeString: " + CdmUtils.Nz(typeString) + ", CollectorString: " + typeCollectorString);
		}
		//type and collector
		DerivedUnitFacade lastFacade = null;
		for (SpecimenTypeDesignation designation: designations){
			name.addTypeDesignation(designation, false);
			if (typeString != null && (typeString.contains("Not designated.")|| typeString.contains("No type designated."))){
				designation.setNotDesignated(true);
			}

			DerivedUnitBase specimen = designation.getTypeSpecimen();

			if (lastFacade != null){
				lastFacade.addDuplicate(specimen);
			}else{

				try {
					lastFacade = DerivedUnitFacade.NewInstance(specimen);
				} catch (DerivedUnitFacadeNotSupportedException e) {
					throw new RuntimeException(e);
				}
				
				//TODO not so nice
				lastFacade.setLocality(typeString);
				makeTypeCollectorInfo(lastFacade, typeCollectorString);
				
			}
		}
			
	}



	private List<SpecimenTypeDesignation> handleTypeLocationPart(CentralAfricaFernsImportState state, 
				String typeString, String typeCollectorString, String typeLocationPart) {
		List<SpecimenTypeDesignation> result = new ArrayList<SpecimenTypeDesignation>();
		String[] splits = typeLocationPart.split(","); 
		String typeTypePattern = "(holo.|lecto.|iso.|isolecto.|syn.|isosyn.|neo.|isoneo.)";
		String collectionPattern = "^[A-Z]+(\\-[A-Z]+)?";
		String numberPattern = "([0-9]+([\\-\\s\\.\\/][0-9]+)?)?";
		String addInfoPattern = "[!\\+\\?]?";
		String typeCollectionPattern = collectionPattern + "\\s?" + numberPattern + addInfoPattern;
		SpecimenTypeDesignation lastDesignation = null;
		
		for (String split: splits){
			split = split.trim();
			if (StringUtils.isBlank(split)){
				continue;
			}else if(split.trim().startsWith("designated by")){
				split = handleDesignatedBy(lastDesignation, split);
			}else if (split.trim().matches(typeTypePattern)){
				makeSpecimentTypeStatus(lastDesignation, split);
			}else if(split.matches(typeCollectionPattern)){
				
				lastDesignation = makeSpecimenTypeCollection(lastDesignation, split, collectionPattern, numberPattern, addInfoPattern);
			}else if(split.equalsIgnoreCase("not located")){
				lastDesignation = makeCachedSpecimenDesignation(split);
			}else{
				logger.error(state.getTaxonNumber() + " - Unknown type location part: " +  split);
				if (lastDesignation == null){
					lastDesignation = makeCachedSpecimenDesignation(split);
				}
			}
			if (lastDesignation != null && ! result.contains(lastDesignation)){
				result.add(lastDesignation);
			}else if (lastDesignation == null){
				logger.warn("Last Designation is null");
			}
		}
		
		return result;
	}



	/**
	 * @param split
	 * @return
	 */
	private SpecimenTypeDesignation makeCachedSpecimenDesignation(String split) {
		SpecimenTypeDesignation lastDesignation;
		lastDesignation = SpecimenTypeDesignation.NewInstance();
		Specimen specimen = Specimen.NewInstance();
		specimen.setTitleCache(split, true);
		lastDesignation.setTypeSpecimen(specimen);
		return lastDesignation;
	}



	private SpecimenTypeDesignation makeSpecimenTypeCollection(SpecimenTypeDesignation designation, String collectionString, String strCollectionPattern, String strNumberPattern, String strAddInfoPattern) {
		SpecimenTypeDesignation result = SpecimenTypeDesignation.NewInstance();
		Specimen specimen = Specimen.NewInstance();
		result.setTypeSpecimen(specimen);
		
		//collection
		Pattern collectionPattern = Pattern.compile(strCollectionPattern);
		Matcher matcher = collectionPattern.matcher(collectionString);
		if (! matcher.find()){
			throw new RuntimeException("collectionString doesn't match: " + collectionString);
		}
		String strCollection = matcher.group();
		Collection collection = getCollection(strCollection);
		specimen.setCollection(collection);
		collectionString = collectionString.substring(strCollection.length()).trim();
		
		
		//collection number
		Pattern numberPattern = Pattern.compile(strNumberPattern);
		matcher = numberPattern.matcher(collectionString);
		if (matcher.find()){
			String strNumber = matcher.group();
			collectionString = collectionString.substring(strNumber.length()).trim();
			if (StringUtils.isNotBlank(strNumber)){
				specimen.setCatalogNumber(strNumber);
			}
		}else{
			//throw new RuntimeException("numberString doesn't match: " + collectionString);
		}
		
		//additional info
		Pattern addInfoPattern = Pattern.compile(strAddInfoPattern);
		matcher = addInfoPattern.matcher(collectionString);
		if (matcher.find()){
			String strAddInfo = matcher.group();
			collectionString = collectionString.substring(strAddInfo.length()).trim();
			if (StringUtils.isBlank(strAddInfo)){
				//do nothing
			}else if (strAddInfo.equals("!")){
				//TODO add seen by author
			}else if (strAddInfo.equals("?")){
				//TODO add doubtful
			}else if (strAddInfo.equals("+")){
				//TODO add +
			}
		}else{
			//throw new RuntimeException("addInfoString doesn't match: " + collectionString);
		}
		if (StringUtils.isNotBlank(collectionString)){
			logger.error("Collection string is not empty: " + collectionString );
		}
		return result;
	}



	private Collection getCollection(String strCollection) {
		//TODO use BCI and deduplication
		Collection result = Collection.NewInstance();
		return result;
	}



	private void makeSpecimentTypeStatus(SpecimenTypeDesignation designation, String type) {
		SpecimenTypeDesignationStatus status; 
		if (type.equalsIgnoreCase("iso.")){
			status = SpecimenTypeDesignationStatus.ISOTYPE();
		}else if (type.equalsIgnoreCase("isolecto.")){
			status = SpecimenTypeDesignationStatus.ISOLECTOTYPE();
		}else if (type.equalsIgnoreCase("syn.")){
			status = SpecimenTypeDesignationStatus.SYNTYPE();
		}else if (type.equalsIgnoreCase("holo.")){
			status = SpecimenTypeDesignationStatus.HOLOTYPE();
		}else if (type.equalsIgnoreCase("lecto.")){
			status = SpecimenTypeDesignationStatus.LECTOTYPE();
		}else if (type.equalsIgnoreCase("isosyn.")){
			status = SpecimenTypeDesignationStatus.ISOSYNTYPE();
		}else if (type.equalsIgnoreCase("neo.")){
			status = SpecimenTypeDesignationStatus.NEOTYPE();
		}else if (type.equalsIgnoreCase("isoneo.")){
			status = SpecimenTypeDesignationStatus.ISONEOTYPE();
		}else{
			logger.error("Type Status not supported: " + type);
			throw new RuntimeException("Type Status not supported: " + type);
		}
		if (designation == null){
			logger.error("Designation is null");
		}else{
			designation.setTypeStatus(status);
		}
	}



	private void handleNameType(CentralAfricaFernsImportState state, TaxonNameBase name, String typeString, String typeCollectorString, String typeLocation) {
		String originalString = typeString; //just for testing
		if (StringUtils.isNotBlank(typeCollectorString)){
			logger.error(state.getTaxonNumber() + " - Type collector string for name type is not empty: " + typeCollectorString);
		}
		if (StringUtils.isNotBlank(typeLocation)){
			logger.error(state.getTaxonNumber() + " - Type location string for name type is not empty: " + typeLocation);
		}
		NameTypeDesignation nameTypeDesignation = NameTypeDesignation.NewInstance();
		NameTypeDesignationStatus status = null;
		if (StringUtils.isBlank(typeString)){
			logger.warn(state.getTaxonNumber() + " - TypeString is empty");
		}
		if (typeString.startsWith("None designated.")){
			nameTypeDesignation.setNotDesignated(true);
			typeString = typeString.replaceFirst("None designated\\.", "").trim();
		}
		if (typeString.contains("Lectotype: ")|| typeString.contains(", lecto." )){
			status = NameTypeDesignationStatus.LECTOTYPE();
			typeString = typeString.replace("Lectotype: ", "");
			typeString = typeString.replace(", lecto.", "");
			typeString = handleDesignatedBy(nameTypeDesignation, typeString);
		}else{
			typeString = handleDesignatedBy(nameTypeDesignation, typeString);
		}
		
//		String strSecondNamePattern = "([^\\(]*|\\(.*\\))+;.+"; //never ending story
		String strSecondNamePattern = ".+;.+";
		String firstName;
		String secondName = null;
		if (typeString.matches(strSecondNamePattern)){
			String[] split = typeString.split(";");
			firstName = split[0].trim();
			secondName = split[1].trim();
			if (split.length > 2){
				logger.warn(state.getTaxonNumber() + " - There are more than 2 name types: " + typeString);
			}
		}else{
			firstName = typeString;
		}
		if (StringUtils.isNotBlank(firstName)){
			BotanicalName[] nameTypeNames = getNameTypeName(firstName);
			BotanicalName nameTypeName = nameTypeNames[0];
			BotanicalName nameTypeAcceptedName = nameTypeNames[1];
			nameTypeDesignation.setTypeName(nameTypeName);
			if (nameTypeName.isProtectedTitleCache()){
				logger.error(state.getTaxonNumber() + " - Name type could not be parsed: " + nameTypeName.getTitleCache());
			}
			if (! nameTypeName.getRank().equals(Rank.SPECIES())){
				logger.warn(state.getTaxonNumber() + " - Name type is not of rank species: " + nameTypeName.getTitleCache());
			}
		}
		if (StringUtils.isNotBlank(secondName)){
			TaxonNameBase secondNameType = handleSecondNameTypeName(secondName);
			if (secondNameType.isProtectedTitleCache()){
				logger.error(state.getTaxonNumber() + " - Second name type could not be parsed: " + secondNameType.getTitleCache());
			}
			if (! secondNameType.getRank().equals(Rank.SPECIES())){
				logger.error(state.getTaxonNumber() + " - Second name type is not of rank species: " + secondNameType.getTitleCache());
			}

		}
		nameTypeDesignation.setTypeStatus(status);
		name.addTypeDesignation(nameTypeDesignation, false);
		
	}



	private TaxonNameBase handleSecondNameTypeName(String strName) {
		//TODO needs feedbacke from Thomas
		logger.info("Not yet implemented");
		if (strName.endsWith(",")){
			strName = strName.substring(0, strName.length() -1);
		}
		BotanicalName result = (BotanicalName)NonViralNameParserImpl.NewInstance().parseFullName(strName, NomenclaturalCode.ICBN, Rank.SPECIES());
		return result;
	}



	private BotanicalName[] getNameTypeName(String strName) {
		//TODO implement get existing names 
		logger.info("Not yet fully implemented");
		
		BotanicalName[] result = new BotanicalName[2];
		if (strName.endsWith(",")){
			strName = strName.substring(0, strName.length() -1);
		}
		String acceptedName;
		String acceptedNamePattern = "\\(.*\\)\\.?$";
		if (strName.matches(".*" + acceptedNamePattern)){
			int accStart = strName.lastIndexOf("(");
			String notAcceptedName = strName.substring(0, accStart -1 );
			acceptedName = strName.substring(accStart + 1, strName.length() - 1);
			if (acceptedName.endsWith(")")){
				acceptedName = acceptedName.substring(0, acceptedName.length()-1);
			}
			acceptedName = acceptedName.replaceFirst("=", "").trim();
			result[1] = (BotanicalName)NonViralNameParserImpl.NewInstance().parseFullName(acceptedName, NomenclaturalCode.ICBN, null);
			strName = notAcceptedName;
		}
		
		result[0] = (BotanicalName)NonViralNameParserImpl.NewInstance().parseFullName(strName, NomenclaturalCode.ICBN, Rank.SPECIES());
		return result;
	}



	private String handleDesignatedBy(TypeDesignationBase typeDesignation, String typeString) {
		String[] splitDesignated = typeString.split(", designated by ");
		if (splitDesignated.length > 1){
			Reference designationCitation = getDesignationCitation(typeDesignation, splitDesignated[1]);
			typeDesignation.setCitation(designationCitation);
			if (splitDesignated.length > 2){
				throw new IllegalStateException("More than one designation is not expected");
			}
		}
		return splitDesignated[0].trim();
	}



	private Reference getDesignationCitation(TypeDesignationBase typeDesignation, String citationString) {
		// TODO try to find an existing Reference
		Reference result = ReferenceFactory.newGeneric();
		String strBracketPattern = "\\((10 Oct. )?\\d{4}:\\s?(\\d{1,3}(--\\d{1,3})?|[XLVI]{1,7}|\\.{1,8})\\)\\.?";

		String strStandardPattern = ".*" + strBracketPattern;
//		strStandardPattern = ".*";
		if (Pattern.matches(strStandardPattern, citationString)){
			parseStandardPattern(typeDesignation, result, citationString, strBracketPattern);
		}else{
			logger.error("Can't parse designation citation: " + citationString);
			result.setTitleCache(citationString);
		}
		return result;
	}



	private void parseStandardPattern(TypeDesignationBase typeDesignation, Reference result, String citationString, String bracketPattern) {
		String authorPart = citationString.split(bracketPattern)[0];
		String bracket = citationString.substring(authorPart.length()+1, citationString.length()-1).trim();
		authorPart = authorPart.trim();
		if (bracket.endsWith(")")){
			bracket = bracket.substring(0, bracket.length()-1);
		}
		Team team = Team.NewTitledInstance(authorPart, authorPart);
		result.setAuthorTeam(team);
		String[] bracketSplit = bracket.split(":");
		TimePeriod datePublished = TimePeriod.parseString(bracketSplit[0].trim());
		result.setDatePublished(datePublished);
		String citationMicroReference = bracketSplit[1].trim();
		citationMicroReference = citationMicroReference.replace("--", "-");
		typeDesignation.setCitationMicroReference(citationMicroReference);
	}



	private void makeTypeCollectorInfo(DerivedUnitFacade specimen, String collectorAndNumberString) {
		if (StringUtils.isBlank(collectorAndNumberString)){
			return;
		}
		String reNumber = "(s\\.n\\.|\\d.*)";
		Pattern reNumberPattern = Pattern.compile(reNumber);
		Matcher matcher = reNumberPattern.matcher(collectorAndNumberString);
		
		if ( matcher.find()){
			int numberStart = matcher.start();
			String number = collectorAndNumberString.substring(numberStart).trim();
			if (numberStart > 0){
				numberStart = numberStart -1;
			}
			String collectorString = collectorAndNumberString.substring(0, numberStart).trim();
			specimen.setFieldNumber(number);
			TeamOrPersonBase team = getTeam(collectorString);
			specimen.setCollector(team);
			
		}else{
			logger.warn("collector string did not match number pattern: " + collectorAndNumberString);
			
		}
	}


	private TeamOrPersonBase getTeam(String teamString) {
		//TODO check existing team
		TeamOrPersonBase result = Team.NewTitledInstance(teamString, teamString);
		return result;
	}



	/**
	 * for internal use only, used by MethodMapper
	 */
	private TaxonBase mapReferences(ResultSet rs, CentralAfricaFernsImportState state) throws SQLException{
		String taxonNumber = state.getTaxonNumber();
		String referenceFullString = rs.getString("Reference full");
		String referenceAbbreviatedString = rs.getString("Reference - abbreviated");
		String volume = rs.getString("Book / Journal volume");
		String pages = rs.getString("Book / Journal pages");
		String illustrations = rs.getString("Illustration/s");
		
		String fascicle = rs.getString("Book / Journal fascicle");
		String part = rs.getString("Book / Journal part");
		String paperTitle = rs.getString("Book / Paper title");
		
		String datePublishedString = rs.getString("Date published");
		String referenceString = referenceFullString;
		if (StringUtils.isBlank(referenceString)){
			referenceString = referenceAbbreviatedString;
		}
		
		TaxonBase<?> taxonBase = state.getRelatedObject(state.CURRENT_OBJECT_NAMESPACE, state.CURRENT_OBJECT_ID, TaxonBase.class);
		if (StringUtils.isNotBlank(referenceString) || StringUtils.isNotBlank(volume) || 
					StringUtils.isNotBlank(pages) || StringUtils.isNotBlank(illustrations) || 
					StringUtils.isNotBlank(datePublishedString) || StringUtils.isNotBlank(paperTitle)){
			NonViralName name = CdmBase.deproxy(taxonBase.getName(), NonViralName.class);
			Reference reference = ReferenceFactory.newGeneric();
			reference.setAuthorTeam((TeamOrPersonBase)name.getCombinationAuthorTeam());
			reference.setTitle(referenceString);
			reference.setVolume(volume);
			reference.setEdition(part);
			Reference inrefernce = null;
			//TODO parser
			TimePeriod datePublished = TimePeriod.parseString(datePublishedString);
			reference.setDatePublished(datePublished);
			if (StringUtils.isNotBlank(paperTitle)){
				Reference innerReference = ReferenceFactory.newGeneric();
				innerReference.setDatePublished(datePublished);
				name.setNomenclaturalReference(innerReference);
				innerReference.setInReference(reference);
				reference = innerReference;
			}else{
				name.setNomenclaturalReference(reference);
			}
			
			//details
			String details = CdmUtils.concat(", ", pages, illustrations);
			details = StringUtils.isBlank(details) ? null : details.trim();
			name.setNomenclaturalMicroReference(details);
			try {
				UUID uuidFascicle = state.getTransformer().getExtensionTypeUuid("fascicle");
				ExtensionType extensionType = getExtensionType(state, uuidFascicle, "Fascicle", "Fascicle", null);
				reference.addExtension(fascicle, extensionType);
			} catch (UndefinedTransformerMethodException e) {
				e.printStackTrace();
			}
			
		}else{
			logger.warn(taxonNumber + " - Taxon has no reference");
		}
		return taxonBase;
	}

	/**
	 * for internal use only, used by MethodMapper
	 * @throws Exception 
	 */
	private TaxonBase mapNomRemarks(ResultSet rs, CentralAfricaFernsImportState state) throws Exception{
		try {
			String taxonNumber = state.getTaxonNumber();
			String nomRemarksString = rs.getString("Nom  remarks");
			String taxonStatus = rs.getString("Current/Synonym");
			
			TaxonBase<?> taxonBase = state.getRelatedObject(state.CURRENT_OBJECT_NAMESPACE, state.CURRENT_OBJECT_ID, TaxonBase.class);
			if (StringUtils.isNotBlank(nomRemarksString)){
				NonViralName name = CdmBase.deproxy(taxonBase.getName(), NonViralName.class);
				parseNomRemark(state, name, nomRemarksString.trim(),taxonStatus, taxonNumber);
			}
			return taxonBase;
		} catch (Exception e) {
			throw e;
		}
	}

	
	private void parseNomRemark(CentralAfricaFernsImportState state, NonViralName name, String nomRemarksString, String taxonStatus, String taxonNumber) {
		
		if (nomRemarksString.equalsIgnoreCase("comb. illeg.")){
			name.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.COMBINATION_ILLEGITIMATE()));
			return;
//		}else if (nomRemarksString.startsWith("comb. inval.")){
//			//TODO
//			nomRemarksString = nomRemarksString.replace("comb. inval.", "");
//		}else if (nomRemarksString.equals("comb. nov.")){
//			name.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.COMBINATION_NOVUM);
		}else if (nomRemarksString.equals("nom. ambig.")){
			name.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.AMBIGUOUS()));
			return;
		}else if (nomRemarksString.matches("nom\\. cons(erv)?\\.")){
			name.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONSERVED()));
			return;
		}else if (nomRemarksString.matches("nom\\. illeg(it)?\\.")){
			name.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE()));
			return;
		}else if (nomRemarksString.matches("nom\\. inval(id)?\\.")){
			name.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.INVALID()));
			return;
		}else if (nomRemarksString.matches("nom\\. nov\\.")){
			name.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.NOVUM()));
			return;
		}else if (nomRemarksString.matches("nom\\. nud\\.")){
			name.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.NUDUM()));
			return;
		}else if (nomRemarksString.matches("nom\\. superfl\\.")){
			name.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.SUPERFLUOUS()));
			return;
		}else if (nomRemarksString.matches("p(\\.|ro\\s)?p(\\.|arte)")){
			//pro parte is handled in taxon relationship import
			if (! taxonStatus.equals("s")){
				logger.warn(" - " +  taxonNumber + " Pro parte synonym is not of type synonym");
			}
			return;
		}else if (nomRemarksString.matches("as '.*'")){
			String nameAsString = nomRemarksString.substring(4, nomRemarksString.length()-1);
			//TODO discuss make it a name relationship
			UUID uuidPublishedAs = CentralAfricaFernsTransformer.uuidNamePublishedAs;
			ExtensionType extensionType = getExtensionType(state, uuidPublishedAs, "Name published as", "Name published as", "as");
			name.addExtension(nameAsString, extensionType);
			return;
		}

		
		if (StringUtils.isNotBlank(nomRemarksString)){
			ExtensionType extensionType = getExtensionType(state, CentralAfricaFernsTransformer.uuidNomenclaturalRemarks, "Nomenclatural remarks", "Nomenclatural remarks", null);
			name.addExtension(nomRemarksString, extensionType);
		}
		
		
		
		
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IMappingImport#createObject(java.sql.ResultSet)
	 */
	public TaxonBase createObject(ResultSet rs, CentralAfricaFernsImportState state) throws SQLException {
		BotanicalName taxonName = BotanicalName.NewInstance(null);
		Reference sec = state.getConfig().getSourceReference();
		
		String taxonNumber = rs.getString("Taxon number");
		state.setTaxonNumber(taxonNumber);
		
		String orderName = rs.getString("Order name");
		String subOrderName = rs.getString("Suborder name");
		String familyName = rs.getString("Family name");
		String subFamilyName = rs.getString("Subfamily name");
		String tribusName = rs.getString("Tribus name");
		String subTribusName = rs.getString("Subtribus name");
		String sectionName = rs.getString("Section name");
		String genusName = rs.getString("Genus name");
		String subGenusName = rs.getString("Subgenus name");
		String seriesName = rs.getString("Series name");
		String specificEpihet = rs.getString("Specific epihet");
		String subspeciesName = rs.getString("Subspecies name");
		String varietyName = rs.getString("Variety name");
		String subFormaName = rs.getString("Subforma");
		String subVariety = rs.getString("Subvariery");
		String formaName = rs.getString("Forma name");
		String subsectionName = rs.getString("Subsection name");
		
		String status = rs.getString("Current/Synonym");
		
		TaxonBase taxon = makeTaxon(taxonName, sec, taxonNumber, status);
		
//			Integer parent3Rank = rs.getInt("parent3rank");
		
		//rank and epithets
		Rank lowestRank = setLowestUninomial(taxonName, orderName,  subOrderName, familyName, subFamilyName, tribusName, subTribusName,genusName);
		lowestRank = setLowestInfraGeneric(taxonName, lowestRank, subGenusName, sectionName, subsectionName, seriesName);
		if (StringUtils.isNotBlank(specificEpihet)){
			taxonName.setSpecificEpithet(specificEpihet);
			lowestRank = Rank.SPECIES();
		}
		lowestRank = setLowestInfraSpecific(taxonName, lowestRank, subspeciesName,  varietyName, subVariety, formaName,subFormaName);
		
		taxonName.setRank(lowestRank);
		state.setCurrentRank(taxonName.getRank());
		setAuthor(taxonName, rs, taxonNumber, false);
		
		//add original source for taxon name (taxon original source is added in mapper
//		Reference citation = state.getConfig().getSourceReference();
//		addOriginalSource(taxonName, taxonNumber, TAXON_NAMESPACE, citation);
		return taxon;
		
	}



	/**
	 * Creates the taxon object depending on name, sec and status
	 * @param taxonName
	 * @param sec
	 * @param taxonNumber
	 * @param status
	 * @return
	 */
	private TaxonBase makeTaxon(BotanicalName taxonName, Reference sec, String taxonNumber, String status) {
		TaxonBase taxon;
		if ("c".equalsIgnoreCase(status)|| "incertus".equalsIgnoreCase(status) ){
			taxon = Taxon.NewInstance(taxonName, sec);
			if ("incertus".equalsIgnoreCase(status)){
				taxon.setDoubtful(true);
			}
		}else if ("s".equalsIgnoreCase(status)){
			taxon = Synonym.NewInstance(taxonName, sec);
		}else{
			logger.warn(taxonNumber + ": Status not given for taxon " );
			taxon = Taxon.NewUnknownStatusInstance(taxonName, sec);
		}
		return taxon;
	}


	private Rank setLowestInfraSpecific(BotanicalName taxonName, Rank lowestRank, String subspeciesName, String varietyName,
			String subVariety, String formaName, String subFormaName) {
		if (StringUtils.isNotBlank(subFormaName)){
			taxonName.setInfraSpecificEpithet(subFormaName);
			return Rank.SUBFORM();
		}else if (StringUtils.isNotBlank(formaName)){
			taxonName.setInfraSpecificEpithet(formaName);
			return Rank.FORM();
		}else if (StringUtils.isNotBlank(subVariety)){
			taxonName.setInfraSpecificEpithet(subVariety);
			return Rank.SUBVARIETY();
		}else if (StringUtils.isNotBlank(varietyName)){
			taxonName.setInfraSpecificEpithet(varietyName);
			return Rank.VARIETY();
		}else if (StringUtils.isNotBlank(subspeciesName)){
			taxonName.setInfraSpecificEpithet(subspeciesName);
			return Rank.SUBSPECIES();
		}else{
			return lowestRank;
		}
	}



	private Rank setLowestInfraGeneric(BotanicalName taxonName, Rank lowestRank, String subGenusName, String sectionName, String subSectionName, String seriesName) {
		if (StringUtils.isNotBlank(seriesName)){
			taxonName.setInfraGenericEpithet(seriesName);
			return Rank.SERIES();
		}else if (StringUtils.isNotBlank(subSectionName)){
			taxonName.setInfraGenericEpithet(subSectionName);
			return Rank.SUBSECTION_BOTANY();
		}else if (StringUtils.isNotBlank(sectionName)){
			taxonName.setInfraGenericEpithet(sectionName);
			return Rank.SECTION_BOTANY();
		}else if (StringUtils.isNotBlank(subGenusName)){
			taxonName.setInfraGenericEpithet(subGenusName);
			return Rank.SUBGENUS();
		}else{
			return lowestRank;
		}
	}



	private Rank setLowestUninomial(BotanicalName taxonName, String orderName, String subOrderName, String familyName, String subFamilyName,
			String tribusName, String subTribusName, String genusName) {
		
		if (StringUtils.isNotBlank(genusName)){
			taxonName.setGenusOrUninomial(genusName);
			return Rank.GENUS();
		}else if (StringUtils.isNotBlank(subTribusName)){
			taxonName.setGenusOrUninomial(subTribusName);
			return Rank.SUBTRIBE();
		}else if (StringUtils.isNotBlank(tribusName)){
			taxonName.setGenusOrUninomial(tribusName);
			return Rank.TRIBE();
		}else if (StringUtils.isNotBlank(subFamilyName)){
			taxonName.setGenusOrUninomial(subFamilyName);
			return Rank.SUBFAMILY();
		}else if (StringUtils.isNotBlank(familyName)){
			taxonName.setGenusOrUninomial(familyName);
			return Rank.FAMILY();
		}else if (StringUtils.isNotBlank(subOrderName)){
			taxonName.setGenusOrUninomial(subOrderName);
			return Rank.SUBORDER();
		}else if (StringUtils.isNotBlank(orderName)){
			taxonName.setGenusOrUninomial(orderName);
			return Rank.ORDER();
		}else{
			return null;
		}
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(CentralAfricaFernsImportState state){
		IOValidator<CentralAfricaFernsImportState> validator = new CentralAfricaFernsTaxonImportValidator();
		return validator.validate(state);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(CentralAfricaFernsImportState state){
		return ! state.getConfig().isDoTaxa();
	}



}
