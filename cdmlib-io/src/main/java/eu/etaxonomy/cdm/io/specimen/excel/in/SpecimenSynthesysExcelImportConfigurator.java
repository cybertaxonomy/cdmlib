/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.excel.in;


import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author p.kelbert
 * @since 29.10.2008
 * @version 1.0
 */
public class SpecimenSynthesysExcelImportConfigurator extends ImportConfiguratorBase<SpecimenSynthesysExcelImportState, URI> implements IImportConfigurator {
	private static final Logger logger = Logger.getLogger(SpecimenSynthesysExcelImportConfigurator.class);
	private boolean doParsing = false;
	private boolean reuseMetadata = false;
	private boolean reuseTaxon = false;
	private String taxonReference = null;
    private boolean askForDate = false;
    private Map<String, Team> titleCacheTeam;
    private Map<String, Person> titleCachePerson;
    private String defaultAuthor="";

    private Map<String,UUID> namedAreaDecisions = new HashMap<String,UUID>();
    private Reference dataReference;
    private boolean debugInstitutionOnly = false;


	/**
     * @return the debugInstitutionOnly
     */
    public boolean isDebugInstitutionOnly() {
        return debugInstitutionOnly;
    }

    //TODO
	private static IInputTransformer defaultTransformer = null;


	@Override
    @SuppressWarnings("unchecked")
	protected void makeIoClassList(){
		ioClassList = new Class[]{
			SpecimenSythesysExcelImport.class,
		};
	};

	public static SpecimenSynthesysExcelImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new SpecimenSynthesysExcelImportConfigurator(uri, destination);
	}

	public static SpecimenSynthesysExcelImportConfigurator NewInstance(URI uri, ICdmDataSource destination, boolean interact){
        return new SpecimenSynthesysExcelImportConfigurator(uri, destination,interact);
    }


	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private SpecimenSynthesysExcelImportConfigurator(URI uri, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(uri);
		setDestination(destination);
	}


    /**
     * @param berlinModelSource
     * @param sourceReference
     * @param destination
     */
    private SpecimenSynthesysExcelImportConfigurator(URI uri, ICdmDataSource destination, boolean interact) {
        super(defaultTransformer);
        setSource(uri);
        setDestination(destination);
        setInteractWithUser(interact);
    }



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	@Override
    public SpecimenSynthesysExcelImportState getNewState() {
		return new SpecimenSynthesysExcelImportState(this);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public Reference getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = ReferenceFactory.newDatabase();
			sourceReference.setTitleCache("Specimen import", true);
		}
		return sourceReference;
	}

	public void setTaxonReference(String taxonReference) {
		this.taxonReference = taxonReference;
	}

	public Reference getTaxonReference() {
		//TODO
		if (this.taxonReference == null){
			logger.info("getTaxonReference not yet fully implemented");
		}
		return sourceReference;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	@Override
    public String getSourceNameString() {
		if (this.getSource() == null){
			return null;
		}else{
			return this.getSource().toString();
		}
	}

	public void setDoAutomaticParsing(boolean doParsing){
		this.doParsing=doParsing;
	}

	public boolean getDoAutomaticParsing(){
		return this.doParsing;
	}

	public void setReUseExistingMetadata(boolean reuseMetadata){
		this.reuseMetadata = reuseMetadata;
	}

	public boolean getReUseExistingMetadata(){
		return this.reuseMetadata;
	}

	public void setReUseTaxon(boolean reuseTaxon){
		this.reuseTaxon = reuseTaxon;
	}

	public boolean getDoReUseTaxon(){
		return this.reuseTaxon;
	}

    public boolean doAskForDate() {
        return askForDate;
    }

    public void setAskForDate(boolean askForDate) {
        this.askForDate = askForDate;
    }

    /**
     * @param titleCacheTeam
     */
    public void setTeams(Map<String, Team> titleCacheTeam) {
       this.titleCacheTeam = titleCacheTeam;
       System.out.println(titleCacheTeam);

    }

    /**
     * @param titleCachePerson
     */
    public void setPersons(Map<String, Person> titleCachePerson) {
       this.titleCachePerson=titleCachePerson;
       System.out.println(titleCachePerson);

    }

    public Map<String, Team> getTeams() {
        return titleCacheTeam;
    }


    public Map<String, Person> getPersons() {
        return titleCachePerson;
    }

    /**
     * @param string
     */
    public void setDefaultAuthor(String string) {
      defaultAuthor=string;

    }

    public String getDefaultAuthor(){
     return defaultAuthor;
    }


    public Map<String,UUID> getNamedAreaDecisions() {
        return namedAreaDecisions;
    }

    public void setNamedAreaDecisions(Map<String,UUID> namedAreaDecisions) {
        this.namedAreaDecisions = namedAreaDecisions;
    }

    public void putNamedAreaDecision(String areaStr,UUID uuid){
        this.namedAreaDecisions.put(areaStr,uuid);
    }

    public UUID getNamedAreaDecision(String areaStr){
        return namedAreaDecisions.get(areaStr);
    }

    /**
     * @param ref
     */
    public void setDataReference(Reference ref) {
        this.dataReference=ref;

    }

    public Reference getDataReference() {
        return dataReference;
    }

    /**
     * @param b
     */
    public void setDebugInstitutionOnly(boolean b) {
       this.debugInstitutionOnly=b;

    }



}
