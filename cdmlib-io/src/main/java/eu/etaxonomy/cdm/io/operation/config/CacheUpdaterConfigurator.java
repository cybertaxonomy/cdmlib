/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.operation.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.DefaultImportState;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.operation.CacheUpdater;
import eu.etaxonomy.cdm.io.operation.CacheUpdaterWithNewCacheStrategy;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @since 02-Jul-2010 13:06:43
 *
 */
public class CacheUpdaterConfigurator extends ImportConfiguratorBase<DefaultImportState<CacheUpdaterConfigurator>, Object> implements IImportConfigurator{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CacheUpdaterConfigurator.class);


	private boolean updateCacheStrategies = false;

	public static CacheUpdaterConfigurator NewInstance(ICdmDataSource destination){
		return new CacheUpdaterConfigurator(destination, false);
	}

	/**
	 * Returns a new Configurator with all boolean values set to false
	 * @param allFalse
	 * @return
	 */
	public static CacheUpdaterConfigurator NewInstance(ICdmDataSource destination, boolean allTrue){
		return new CacheUpdaterConfigurator(destination, allTrue);
	}

	public static CacheUpdaterConfigurator NewInstance(ICdmDataSource destination, List<Class<? extends IdentifiableEntity>> classList){
		CacheUpdaterConfigurator result = new CacheUpdaterConfigurator(destination, true);
		result.setClassList(classList);
		return result;
	}

	public static CacheUpdaterConfigurator NewInstance(ICdmDataSource destination, Collection<String> classListNames) throws ClassNotFoundException{
		CacheUpdaterConfigurator result = new CacheUpdaterConfigurator(destination, true);
		List<Class<? extends IdentifiableEntity>> classList = new ArrayList<Class<? extends IdentifiableEntity>>();
		for (String className : classListNames){
			Class clazz = Class.forName(className);
			classList.add(clazz);
		}
		result.setClassList(classList);
		return result;
	}

	public static CacheUpdaterConfigurator NewInstance(ICdmDataSource destination, Collection<String> classListNames, boolean doUpdateCacheStrategies) throws ClassNotFoundException{
		CacheUpdaterConfigurator result = new CacheUpdaterConfigurator(destination, false);
		List<Class<? extends IdentifiableEntity>> classList = new ArrayList<Class<? extends IdentifiableEntity>>();
		for (String className : classListNames){
			Class clazz = Class.forName(className);
			classList.add(clazz);
		}
		result.setClassList(classList);
		result.setUpdateCacheStrategy(doUpdateCacheStrategies);
		return result;
	}



//	//DescriptionBase
//	private boolean doTaxonDescription = true;
//	private boolean doSpecimenDescription = true;
//	private boolean doNameDescription = true;
//
//	//AgentBase
//	private boolean doPerson = true;
//	private boolean doTeam = true;
//	private boolean doInstitution = true;
//
//	//MediaEntities
//	private boolean doCollection = true;
//	private boolean doReferenceBase = true;
//
//	//SpecimenOrObservationBase
//	private boolean doFieldUnit = true;
//	private boolean doDeriveUnit = true;
//	private boolean doDnaSample = true;
//
//	//Media
//	private boolean doMedia = true;
//	private boolean doMediaKey = true;
//	private boolean doPhylogenticTree = true;
//
//
//	//TaxonBase
//	private boolean doTaxon = true;
//	private boolean doSynonym = true;
//
//	private boolean doSequence = true;
//
//	//Names
//	private boolean doTaxonName = true;
//
//	private boolean doClassification = true;
//
//	//TermBase
//	private boolean doFeatureTree = true;
//	private boolean doPolytomousKey = true;
//
//	private boolean doTermVocabulary = true;
//	private boolean doDefinedTermBase = true;
	private List<Class<? extends IdentifiableEntity>> classList;

	private CacheUpdaterConfigurator(ICdmDataSource destination, boolean allFalse){
		super(null);
		this.setDestination(destination);
		this.classList = new ArrayList<Class<? extends IdentifiableEntity>>();
		if (allFalse){
			//DescriptionBase
//			doTaxonDescription = false;
//			doSpecimenDescription = false;
//			doNameDescription = false;
//
//			//AgentBase
//			doPerson = false;
//			doTeam = false;
//			doInstitution = false;
//
//			//MediaEntities
//			doCollection = false;
//			doReferenceBase = false;
//
//			//SpecimenOrObservationBase
//			doFieldUnit = false;
//			doDeriveUnit = false;
//			doDnaSample = false;
//
//			//Media
//			doMedia = false;
//			doMediaKey = false;
//			doPhylogenticTree = false;
//
//
//			//TaxonBase
//			doTaxon = false;
//			doSynonym = false;
//
//			doSequence = false;
//
//			//Names
//			doTaxonName = false;
//
//			doClassification = false;
//
//			//TermBase
//			doFeatureTree = false;
//			doPolytomousKey = false;
//
//			doTermVocabulary = false;
//			doDefinedTermBase = false;
		}
	}



// **************** GETTER / SETTER ************************************


	public List<Class<? extends IdentifiableEntity>> getClassList(){
//	    classList.sort(new ClassComparator());
		return classList;
	}
	private void setClassList(List<Class<? extends IdentifiableEntity>> classList) {
		this.classList = classList;
	}


//	public void setDoTaxonDescription(boolean doTaxonDescription) {
//		this.doTaxonDescription = doTaxonDescription;
//	}
//	public boolean isDoTaxonDescription() {
//		return doTaxonDescription;
//	}
//	public void setDoSpecimenDescription(boolean doSpecimenDescription) {
//		this.doSpecimenDescription = doSpecimenDescription;
//	}
//	public boolean isDoSpecimenDescription() {
//		return doSpecimenDescription;
//	}
//	public void setDoNameDescription(boolean doNameDescription) {
//		this.doNameDescription = doNameDescription;
//	}
//	public boolean isDoNameDescription() {
//		return doNameDescription;
//	}
//	public void setDoPerson(boolean doPerson) {
//		this.doPerson = doPerson;
//	}
//	public boolean isDoPerson() {
//		return doPerson;
//	}
//	public void setDoTeam(boolean doTeam) {
//		this.doTeam = doTeam;
//	}
//	public boolean isDoTeam() {
//		return doTeam;
//	}
//	public void setDoInstitution(boolean doInstitution) {
//		this.doInstitution = doInstitution;
//	}
//	public boolean isDoInstitution() {
//		return doInstitution;
//	}
//	public void setDoCollection(boolean doCollection) {
//		this.doCollection = doCollection;
//	}
//	public boolean isDoCollection() {
//		return doCollection;
//	}
//	public void setDoReferenceBase(boolean doReferenceBase) {
//		this.doReferenceBase = doReferenceBase;
//	}
//	public boolean isDoReferenceBase() {
//		return doReferenceBase;
//	}
//	public void setDoFieldUnit(boolean doFieldUnit) {
//		this.doFieldUnit = doFieldUnit;
//	}
//	public boolean isDoFieldUnit() {
//		return doFieldUnit;
//	}
//	public void setDoDeriveUnit(boolean doDeriveUnit) {
//		this.doDeriveUnit = doDeriveUnit;
//	}
//	public boolean isDoDeriveUnit() {
//		return doDeriveUnit;
//	}
//	public void setDoDnaSample(boolean doDnaSample) {
//		this.doDnaSample = doDnaSample;
//	}
//	public boolean isDoDnaSample() {
//		return doDnaSample;
//	}
//	public void setDoMedia(boolean doMedia) {
//		this.doMedia = doMedia;
//	}
//	public boolean isDoMedia() {
//		return doMedia;
//	}
//	public void setDoMediaKey(boolean doMediaKey) {
//		this.doMediaKey = doMediaKey;
//	}
//	public boolean isDoMediaKey() {
//		return doMediaKey;
//	}
//	public void setDoPhylogenticTree(boolean doPhylogenticTree) {
//		this.doPhylogenticTree = doPhylogenticTree;
//	}
//	public boolean isDoPhylogenticTree() {
//		return doPhylogenticTree;
//	}
//	public void setDoTaxon(boolean doTaxon) {
//		this.doTaxon = doTaxon;
//	}
//	public boolean isDoTaxon() {
//		return doTaxon;
//	}
//	public void setDoSynonym(boolean doSynonym) {
//		this.doSynonym = doSynonym;
//	}
//	public boolean isDoSynonym() {
//		return doSynonym;
//	}
//	public void setDoSequence(boolean doSequence) {
//		this.doSequence = doSequence;
//	}
//	public boolean isDoSequence() {
//		return doSequence;
//	}
//	public void setDoClassification(boolean doClassification) {
//		this.doClassification = doClassification;
//	}
//	public boolean isDoClassification() {
//		return doClassification;
//	}
//	public void setDoFeatureTree(boolean doFeatureTree) {
//		this.doFeatureTree = doFeatureTree;
//	}
//	public boolean isDoFeatureTree() {
//		return doFeatureTree;
//	}
//	public void setDoPolytomousKey(boolean doPolytomousKey) {
//		this.doPolytomousKey = doPolytomousKey;
//	}
//	public boolean isDoPolytomousKey() {
//		return doPolytomousKey;
//	}
//	public void setDoTermVocabulary(boolean doTermVocabulary) {
//		this.doTermVocabulary = doTermVocabulary;
//	}
//	public boolean isDoTermVocabulary() {
//		return doTermVocabulary;
//	}
//	public void setDoDefinedTermBase(boolean doDefinedTermBase) {
//		this.doDefinedTermBase = doDefinedTermBase;
//	}
//	public boolean isDoDefinedTermBase() {
//		return doDefinedTermBase;
//	}

	@Override
	public Reference getSourceReference() {
		//not needed here
		return null;
	}

	@Override
	protected void makeIoClassList() {
		if (this.doUpdateCacheStrategy()){
			ioClassList = new Class[]{
					 CacheUpdaterWithNewCacheStrategy.class
			};
		}else{
			ioClassList = new Class[]{
					 CacheUpdater.class
			};
			}
	}

	@Override
	public <STATE extends ImportStateBase> STATE getNewState() {
		return (STATE) new DefaultImportState(this);
	}

	@Override
	public boolean isValid() {
		//as no source needs to exist
		return true;
	}

	@Override
	public String getSourceNameString() {
		//not needed here
		return null;
	}

	public boolean doUpdateCacheStrategy() {
		return updateCacheStrategies;
	}

	public void setUpdateCacheStrategy(boolean doUpdateCacheStrategies){
		this.updateCacheStrategies = doUpdateCacheStrategies;
	}

//	private class ClassComparator implements Comparator<Class>{
//
//        /**
//         * {@inheritDoc}
//         */
//        @Override
//        public int compare(Class o1, Class o2) {
//            if (o1.equals(o2)){
//                return 0;
//            }
//            if (o1 == null){
//                return -1;
//            }
//            if (o2 == null){
//                return 1;
//            }
//            if (o1.equals(AgentBase.class)){
//                return -1;
//            }
//            if (o1.equals(Reference.class)){
//                if (o2.equals(AgentBase.class)){
//                    return 1;
//                }else{
//                    return -1;
//                }
//            }
//            if (o1.equals(TaxonName.class)){
//                if (o2.equals(AgentBase.class) || o2.equals(Reference.class)){
//                    return 1;
//                }else{
//                    return -1;
//                }
//            }
//            if (o1.equals(TaxonBase.class)){
//                if (o2.equals(AgentBase.class) || o2.equals(Reference.class) || o2.equals(TaxonName.class)){
//                    return 1;
//                }else{
//                    return -1;
//                }
//            }
//            if (o1.equals(SpecimenOrObservationBase.class)){
//                if (o2.equals(AgentBase.class) || o2.equals(Reference.class) || o2.equals(TaxonName.class) || o2.equals(TaxonBase.class)){
//                    return 1;
//                }else{
//                    return -1;
//                }
//            }
//            return 0;
//
//        }
//
//	}

}
