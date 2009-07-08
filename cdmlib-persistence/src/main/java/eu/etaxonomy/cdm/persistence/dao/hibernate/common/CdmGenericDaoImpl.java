/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionType;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Credit;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Figure;
import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.LSIDAuthority;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.IdentificationKey;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.Scope;
import eu.etaxonomy.cdm.model.description.Sex;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.Stage;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.location.Continent;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.media.AudioFile;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.MovieFile;
import eu.etaxonomy.cdm.model.media.ReferencedMedia;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.media.RightsTerm;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.GenBankAccession;
import eu.etaxonomy.cdm.model.molecular.Locus;
import eu.etaxonomy.cdm.model.molecular.PhylogeneticTree;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
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
import eu.etaxonomy.cdm.model.name.ViralName;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.DeterminationModifier;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.Fossil;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.LivingBeing;
import eu.etaxonomy.cdm.model.occurrence.Observation;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.BibtexEntryType;
import eu.etaxonomy.cdm.model.reference.BibtexReference;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.CdDvd;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.InProceedings;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.Map;
import eu.etaxonomy.cdm.model.reference.Patent;
import eu.etaxonomy.cdm.model.reference.PersonalCommunication;
import eu.etaxonomy.cdm.model.reference.PrintSeries;
import eu.etaxonomy.cdm.model.reference.PrintedUnitBase;
import eu.etaxonomy.cdm.model.reference.Proceedings;
import eu.etaxonomy.cdm.model.reference.Publisher;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.Report;
import eu.etaxonomy.cdm.model.reference.SectionBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.reference.Thesis;
import eu.etaxonomy.cdm.model.reference.WebPage;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;

@Repository
public class CdmGenericDaoImpl extends CdmEntityDaoBase<CdmBase> implements ICdmGenericDao{

	public CdmGenericDaoImpl() {
		super(CdmBase.class);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao#getCdmBasesByFieldAndClass(java.lang.Class, java.lang.String, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public List<CdmBase> getCdmBasesByFieldAndClass(Class clazz, String propertyName, CdmBase referencedCdmBase){
		Session session = super.getSession();
		Criteria criteria = session.createCriteria(clazz);
		criteria.add(Restrictions.eq(propertyName, referencedCdmBase));
		return criteria.list();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao#getCdmBasesByFieldAndClass(java.lang.Class, java.lang.String, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public List<CdmBase> getCdmBasesWithItemInCollection(Class itemClass, Class clazz, String propertyName, CdmBase item){
		Session session = super.getSession();
		String thisClassStr = itemClass.getSimpleName();
		String otherClassStr = clazz.getSimpleName();
		String queryStr = " SELECT other FROM "+ thisClassStr + " this, " + otherClassStr + " other " + 
			" WHERE this = :referencedObject AND this member of other."+propertyName ;
		Query query = session.createQuery(queryStr).setEntity("referencedObject", item);
		List result = query.list();
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao#getAllCdmClasses(boolean)
	 */
	public Set<Class<? extends CdmBase>> getAllCdmClasses(boolean includeAbstractClasses){
		
		Class[] result2 = {
				Address.class,
				AgentBase.class,
				Contact.class, 
				Institution.class, 
				InstitutionalMembership.class, 
				InstitutionType.class, 
				Person.class, 
				Team.class, 
				TeamOrPersonBase.class, 
				Annotation.class, 
				AnnotationType.class, 
				Credit.class,
				DefinedTermBase.class, 
				Extension.class, 
				ExtensionType.class, 
				Figure.class, 
				GrantedAuthorityImpl.class, 
				Group.class, 
				Keyword.class, 
				Language.class, 
				LanguageString.class, 
				LSID.class, 
				LSIDAuthority.class, 
				Marker.class, 
				MarkerType.class, 
				OrderedTermBase.class, 
				OrderedTermVocabulary.class, 
				OriginalSource.class, 
				RelationshipTermBase.class, 
				Representation.class, 
				TermVocabulary.class, 
				User.class, 
				
				AbsenceTerm.class, 
				CategoricalData.class, 
				CommonTaxonName.class, 
				DescriptionBase.class, 
				DescriptionElementBase.class, 
				Distribution.class, 
				Feature.class, 
				FeatureNode.class, 
				FeatureTree.class, 
				IdentificationKey.class, 
				IndividualsAssociation.class, 
				MeasurementUnit.class, 
				Modifier.class, 
				PresenceAbsenceTermBase.class, 
				PresenceTerm.class, 
				QuantitativeData.class, 
				Scope.class, 
				Sex.class, 
				SpecimenDescription.class, 
				Stage.class, 
				State.class, 
				StateData.class, 
				StatisticalMeasure.class, 
				StatisticalMeasurementValue.class, 
				TaxonDescription.class, 
				TaxonInteraction.class, 
				TaxonNameDescription.class, 
				TextData.class, 
				TextFormat.class, 
				Continent.class, 
				NamedArea.class, 
				NamedAreaLevel.class, 
				NamedAreaType.class, 
				ReferenceSystem.class, 
				Point.class, 
				TdwgArea.class, 
				WaterbodyOrCountry.class, 
				AudioFile.class, 
				ImageFile.class, 
				Media.class, 
				MediaRepresentation.class, 
				MediaRepresentationPart.class, 
				MovieFile.class, 
				ReferencedMedia.class, 
				Rights.class, 
				RightsTerm.class, 
				DnaSample.class, 
				GenBankAccession.class, 
				Locus.class, 
				PhylogeneticTree.class, 
				Sequence.class, 
				BacterialName.class, 
				BotanicalName.class, 
				CultivarPlantName.class, 
				HomotypicalGroup.class, 
				HybridRelationship.class, 
				HybridRelationshipType.class, 
				NameRelationship.class, 
				NameRelationshipType.class, 
				NameTypeDesignation.class, 
				NameTypeDesignationStatus.class, 
				NomenclaturalCode.class, 
				NomenclaturalStatus.class, 
				NomenclaturalStatusType.class, 
				NonViralName.class, 
				Rank.class, 
				SpecimenTypeDesignation.class, 
				SpecimenTypeDesignationStatus.class, 
				TaxonNameBase.class, 
				TypeDesignationBase.class, 
				ViralName.class, 
				ZoologicalName.class, 
				Collection.class, 
				DerivationEvent.class, 
				DerivationEventType.class, 
				DerivedUnit.class, 
				DerivedUnitBase.class, 
				DeterminationEvent.class, 
				DeterminationModifier.class, 
				FieldObservation.class, 
				Fossil.class, 
				GatheringEvent.class, 
				LivingBeing.class, 
				Observation.class, 
				PreservationMethod.class, 
				Specimen.class, 
				SpecimenOrObservationBase.class, 
				Article.class, 
				BibtexEntryType.class, 
				BibtexReference.class, 
				Book.class, 
				BookSection.class, 
				CdDvd.class, 
				Database.class, 
				Generic.class, 
				InProceedings.class, 
				Journal.class, 
				Map.class, 
				Patent.class, 
				PersonalCommunication.class, 
				PrintedUnitBase.class, 
				PrintSeries.class, 
				Proceedings.class, 
				ReferenceBase.class, 
				Publisher.class, 
				Report.class, 
				SectionBase.class, 
				StrictReferenceBase.class, 
				Thesis.class, 
				WebPage.class, 
				Synonym.class, 
				SynonymRelationship.class, 
				SynonymRelationshipType.class, 
				Taxon.class, 
				TaxonBase.class, 
				TaxonNode.class, 
				TaxonomicTree.class, 
				TaxonRelationship.class, 
				TaxonRelationshipType.class 
		}	;	
		
		Set<Class<? extends CdmBase>> result = new HashSet<Class<? extends CdmBase>>();
		for (Class clazz : result2){
			boolean isAbstractClass = Modifier.isAbstract(clazz.getModifiers());
			if (! isAbstractClass || includeAbstractClasses){
				result.add(clazz);
			}
		}
		return result;
	}
	

	public List getHqlResult(String hqlQuery){
		Query query = getSession().createQuery(hqlQuery);
		List result = query.list();
		return result;
	}
	

}


