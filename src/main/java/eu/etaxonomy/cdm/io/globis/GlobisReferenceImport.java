/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.globis;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.mapping.IMappingImport;
import eu.etaxonomy.cdm.io.globis.validation.GlobisReferenceImportValidator;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;


/**
 * @author a.mueller
 * @created 20.02.2010
 */
@Component
public class GlobisReferenceImport  extends GlobisImportBase<Reference> implements IMappingImport<Reference, GlobisImportState>{
	private static final Logger logger = Logger.getLogger(GlobisReferenceImport.class);
	
	private int modCount = 10000;
	private static final String pluralString = "references";
	private static final String dbTableName = "Literatur";
	private static final Class<?> cdmTargetClass = Reference.class;

	public GlobisReferenceImport(){
		super(pluralString, dbTableName, cdmTargetClass);
	}

	@Override
	protected String getIdQuery() {
		String strRecordQuery = 
			" SELECT refID " + 
			" FROM " + dbTableName
			+ " WHERE RefSource like 'Original' or refID in (SELECT fiSpecRefId FROM specTax)"; 
		return strRecordQuery;	
	}

	@Override
	protected String getRecordQuery(GlobisImportConfigurator config) {
		String strRecordQuery = 
			" SELECT l.*, l.DateCreated as Created_When, l.CreatedBy as Created_Who," +
			"        l.ModifiedBy as Updated_who, l.DateModified as Updated_When, l.RefRemarks as Notes " + 
			" FROM " + getTableName() + " l " +
			" WHERE ( l.refId IN (" + ID_LIST_TOKEN + ") )";
		return strRecordQuery;
	}
	
	@Override
	public boolean doPartition(ResultSetPartitioner partitioner, GlobisImportState state) {
		boolean success = true;
		
		Set<Reference> objectsToSave = new HashSet<Reference>();
		
		ResultSet rs = partitioner.getResultSet();

		try {
			
			int i = 0;

			//for each reference
            while (rs.next()){
                
        		if ((i++ % modCount) == 0 && i!= 1 ){ logger.info(pluralString + " handled: " + (i-1));}

        		
				handleSingleRecord(state, objectsToSave, rs); 
                
            }
           
//            logger.warn("Specimen: " + countSpecimen + ", Descriptions: " + countDescriptions );

			logger.warn(pluralString + " to save: " + objectsToSave.size());
			getReferenceService().save(objectsToSave);	
			
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}

	/**
	 * @param state
	 * @param objectsToSave
	 * @param rs
	 * @throws SQLException 
	 */
	private void handleSingleRecord(GlobisImportState state,
			Set<Reference> objectsToSave, ResultSet rs) throws SQLException {
		
		Integer refId = rs.getInt("RefId");
		
		try {
			
			String title = rs.getString("RefTitle");
			String refJournal = rs.getString("RefJournal");
			refJournal = normalizeRefJournal(refJournal);
			String refBookTitle = rs.getString("RefBookTitle");
			
			String refUrl = rs.getString("RefURL");
			String refVolume = rs.getString("RefVolume");
			String refYear = rs.getString("RefYear");
			String refIssn = rs.getString("RefISSN");
			String refRemarks = rs.getString("RefRemarks");
			String refPublisher = rs.getString("RefPublisher");
			String refPlace = rs.getString("RefPlace");
			String refEdition = rs.getString("RefEdition");
			String refEditor = rs.getString("RefEditor");
			String refAuthor = rs.getString("RefAuthor");
			String refPages = rs.getString("RefPages");
			
			String isbn = null;
			String issn = null;
			if (isNotBlank(refIssn)){
				refIssn = refIssn.trim();
				if (refIssn.startsWith("ISBN")){
					isbn = refIssn.replace("ISBN", "").trim();
				}else if (refIssn.startsWith("ISSN")){
					issn = refIssn.replace("ISSN", "").trim();
				}else{
					String pureNumbers = refIssn.replace("-", "").replace(" ", "");
					if (pureNumbers.length() == 8){
						issn = refIssn;
					}else if (pureNumbers.length() == 10){
						isbn = refIssn;
					}else{
						logger.warn("RefISSN could not be parsed: " + refIssn + ",  refId: " + refId);
					}
				}
			}
		
		
			//source ref   //TODO
			Reference<?> sourceRef = state.getTransactionalSourceReference();
		
			Reference<?> ref = createObject(rs, state);
			testIsxnType(ref, isbn, issn, refId);
			ref.setTitle(title);
			
			//refAuthor
			TeamOrPersonBase<?> author = makeAuthor(refAuthor, state);
			ref.setAuthorTeam(author);
			
			//inRef
			if (isNotBlank(refJournal)){
				//Article
				if (ref.getType().equals(ReferenceType.Article) ){
					Reference<?> journal = getJournal(state, rs, refJournal);
					ref.setInJournal(journal);
				}else{
					logger.warn("Reference type not supported for RefJournal. Type: " + ref.getType().toString() + ", refId: " + refId );
				}
			}
			if (isNotBlank(refBookTitle)){
				//BookSection
				//TODO RefSerial
				if (ref.getType().equals(ReferenceType.BookSection) ){
					IBook book = getBook(state, rs, refBookTitle);
					ref.setInBook(book);
				}else if (ref.getType().equals(ReferenceType.Book)) {
					ref.setTitle(refBookTitle);
				}else{
					logger.warn("Reference type not supported for RefBookTitle. Type: " + ref.getType().toString() + ", refId: " + refId );
				}
			}
			
			IBookSection bookSection;
			IBook book;
			IArticle article;
			IJournal journal;
				
			
			//RefVolume
			if (isNotBlank(refVolume)){
				if (ref.getType().isVolumeReference()){
					ref.setVolume(refVolume);
				}else if(ref.getInReference() != null && ref.getInReference().getType().isVolumeReference()){
					ref.getInReference().setVolume(refVolume);
				}else{
					logger.warn(ref.getType() + " does not support volume but volume exists, refId: " + refId);
				}
			}
			
			//RefYear
			//TODO check correct parsing for [] and full dates
			if (isNotBlank(refYear)){
				ref.setDatePublished(TimePeriodParser.parseString(refYear));
			}
			
			//refPages
			if (isNotBlank(refPages)){
				refPages = refPages.trim();
				if (refPages.endsWith(".")){
					refPages = refPages.substring(0, refPages.length()-1).trim();
				}
				ref.setPages(refPages);
			}
			
			//ISXN
			if (isbn != null){
				Reference<?> isbnRef = getIsbnReference(ref, refId);
				if (isbnRef != null){
					isbnRef.setIsbn(isbn);
				}
			}
			if(issn != null){
				Reference<?> issnRef = getIssnReference(ref, refId);
				if (issnRef != null){
					issnRef.setIssn(issn);
				}
			}
			
			//refURL
			if (isNotBlank(refUrl)){
				URI uri = URI.create(refUrl);
				ref.setUri(uri);
			}
			
			//refRemarks
			if (isNotBlank(refRemarks)){
				Annotation anno = Annotation.NewDefaultLanguageInstance(refRemarks);
				anno.setAnnotationType(AnnotationType.EDITORIAL());
				ref.addAnnotation(anno);
			}
			
			//Publisher + Place
			handlePublisherAndPlace(refId, refPublisher, refPlace, ref);
			
			//refEdition
			if (isNotBlank(refEdition)){
				Reference<?> edRef = ref;
				if (ref.getType() == ReferenceType.BookSection){
					edRef = ref.getInReference();
				}
				if (edRef == null || edRef.getType() != ReferenceType.Book){
					logger.warn("Incorrect refType " + ref.getType() + " for refererence with edition or inRef is null, " + refId);
				}
			}
			
			//refEditor
			if (isNotBlank(refEditor)){
				Reference<?> edsRef = ref;
				if (ref.getType() == ReferenceType.BookSection){
					edsRef = ref.getInReference();
				}
				if (edsRef == null || edsRef.getType() != ReferenceType.Book){
					logger.warn("Reference type for RefEditor must be Book or Booksection but was " + ref.getType() + " or inRef was null, refId " + refId);
				}
			}
			
			//id, created, notes
			this.doIdCreatedUpdatedNotes(state, ref, rs, refId, REFERENCE_NAMESPACE);
			
			
			
			//DONE
			//RefType, RefTitle, RefJournal,
			//RefId, ...
								
			//TODO
								
			//RefBookTitle, RefJournal, RefSerial, - mostly done
			
			//RefIll only, RefPages,RefPages only,
		
			//unclear
			//RefDatePublished, RefVolPageFig,
			//RefSource, 
			//RefLibrary, RefMarker,
			//RefGeneralKeywords, RefGeoKeywords,	RefSpecificKeywords, RefTaxKeywords, SpecificKeywordDummy
			
			
			//no data
				//CountryDummy
			
			objectsToSave.add(ref); 
			

		} catch (Exception e) {
			logger.warn("Exception in literature: RefId " + refId + ". " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private TeamOrPersonBase<?> makeAuthor(String refAuthor, GlobisImportState state) {
		TeamOrPersonBase<?> author = GlobisAuthorImport.makeAuthor(refAuthor, state, getAgentService());
//		getAgentService().update(author);
		return author;
	}

//	private TeamOrPersonBase<?> makeAuthor(String refAuthor) {
//		String[] split = refAuthor.split(";");
//		List<String> singleAuthorStrings = new ArrayList<String>();
//		for (String single : split){
//			single = single.trim();
//			if (single.startsWith("&")){
//				single = single.substring(1).trim();
//			}
//			String[] split2 = single.split("&");
//			for (String single2 : split2){
//				singleAuthorStrings.add(single2);
//			}
//		}
//		
//		TeamOrPersonBase<?> result; 
//		if (singleAuthorStrings.size() > 1){
//			Team team= Team.NewInstance();
//			for (String str : singleAuthorStrings){
//				Person person = makePerson(str);
//				team.addTeamMember(person);
//			}
//			result = team;
//		}else{
//			result = makePerson(singleAuthorStrings.get(0));
//		}
//		
//		//TODO deduplicate
//		return result;
//	}
//
//	private Person makePerson(String string) {
//		Person person = Person.NewTitledInstance(string.trim());
//		//TODO deduplicate
//		return person;
//	}

	/**
	 * @param refId
	 * @param refPublisher
	 * @param refPlace
	 * @param ref
	 */
	private void handlePublisherAndPlace(Integer refId, String refPublisher,
			String refPlace, Reference<?> ref) {
		//refPublisher
		if (isNotBlank(refPublisher)){
			if (ref.getType().isPublication()){
				ref.setPublisher(refPublisher);
			}else if (ref.getInReference() != null && ref.getInReference().getType().isPublication()){
				ref.getInReference().setPublisher(refPublisher);
			}else{
				logger.warn("RefPublisher can not be set, " +  ref.getType() + ", refId " + refId);
			}
		}
		
		//refPlace
		if (isNotBlank(refPlace)){
			if (ref.getType().isPublication()){
				ref.setPlacePublished(refPlace);
			}else if (ref.getInReference() != null && ref.getInReference().getType().isPublication()){
				//TODO handle if not empty
				ref.getInReference().setPlacePublished(refPlace);
			}else{
				logger.warn("RefPlace can not be set, " +  ref.getType() + ", refId " + refId);
			}
		}
	}

	private Reference<?> getIssnReference(Reference<?> ref, int refId) {
		if (ref == null){
			return null;
		}
		if (ref.getType() == ReferenceType.Article){
			ref = ref.getInReference();
		}
		if (ref.getType() != ReferenceType.Journal && ref.getType() != ReferenceType.Generic){
			logger.warn("Invalid refType for issn, refId " + refId);
			return null;
		}else{
			return ref;
		}
	}

	private Reference<?> getIsbnReference(Reference<?> ref, int refId) {
		if (ref == null){
			return null;
		}
		if (ref.getType() == ReferenceType.BookSection){
			ref = ref.getInReference();
		}
		if (ref.getType() != ReferenceType.Book && ref.getType() != ReferenceType.Generic){
			logger.warn("Invalid refType for isbn, refId " + refId);
			return null;
		}else{
			return ref;
		}
	}

	private void testIsxnType(Reference<?> ref, String isbn, String issn, int refID) {
		if (isbn != null && ref.getType() != ReferenceType.Book && ref.getType() != ReferenceType.BookSection ){
			logger.warn("Reference has isbn but is not a book type, type " + ref.getType() + ", row " + refID);
		}else if (issn != null && ref.getType() != ReferenceType.Article){
			logger.warn("Reference has issn but is not an article, row " + refID);
		}
	}

	/**
	 * @param refJournal
	 * @return
	 */
	private String normalizeRefJournal(String refJournal) {
		if (refJournal != null){
			refJournal = refJournal.trim();
			if (refJournal.equals(".")){
				refJournal = null;
			}
		}
		return refJournal;
	}



	
	private Reference<?> getJournal(GlobisImportState state, ResultSet rs, String refJournal) throws SQLException {
		
		Reference<?> journal = ReferenceFactory.newJournal();
		//TODO deduplicate
		journal.setTitle(refJournal);
		return journal;
	}
	
	private IBook getBook(GlobisImportState state, ResultSet rs, String refBookTitle) throws SQLException {
		
		Reference<?> book = ReferenceFactory.newBook();
		//TODO deduplicate
		book.setTitle(refBookTitle);
		return book;
	}

	@Override
	public Reference<?> createObject(ResultSet rs, GlobisImportState state)
			throws SQLException {
		String refJournal = rs.getString("RefJournal");
		boolean isInJournal =isNotBlank(refJournal); 
		String refBookTitle = rs.getString("RefBookTitle");
		boolean isInBook =isNotBlank(refBookTitle); 
		
		
		
		Reference<?> ref;
		String refType = rs.getString("RefType");
		if (refType == null){
			if (isInJournal && ! isInBook){
				ref = ReferenceFactory.newArticle();
			}else{
				ref = ReferenceFactory.newGeneric();
			}
		}else if (refType.equals("book")){
			ref = ReferenceFactory.newBook();
		}else if (refType.equals("paper in journal")){
			ref = ReferenceFactory.newArticle();
		}else if (refType.startsWith("unpublished") ){
			ref = ReferenceFactory.newGeneric();
		}else if (refType.endsWith("paper in journal")){
			ref = ReferenceFactory.newArticle();
		}else if (refType.equals("paper in book")){
			ref = ReferenceFactory.newBookSection();
		}else if (refType.matches("paper in journal.*website.*")){
			ref = ReferenceFactory.newArticle();
		}else{
			logger.warn("Unknown reference type: " + refType);
			ref = ReferenceFactory.newGeneric();
		}
		return ref;
	}


	@Override
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs, GlobisImportState state) {
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		return result;  //not needed
	}
	
	@Override
	protected boolean doCheck(GlobisImportState state){
		IOValidator<GlobisImportState> validator = new GlobisReferenceImportValidator();
		return validator.validate(state);
	}
	
	@Override
	protected boolean isIgnore(GlobisImportState state){
		//TODO
		return state.getConfig().getDoReferences() != IImportConfigurator.DO_REFERENCES.ALL;
	}





}
