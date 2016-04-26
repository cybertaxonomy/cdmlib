/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.search.annotations.ClassBridge;

import au.com.bytecode.opencsv.CSVWriter;
import eu.etaxonomy.cdm.hibernate.search.DefinedTermBaseClassBridge;
import eu.etaxonomy.cdm.model.ICdmUuidCacher;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;


/**
 * workaround for enumerations, base type according to TDWG.  For linear ordering
 * use partOf relation and BreadthFirst. Default iterator order should therefore
 * be BreadthFirst (not DepthFirst)
 * @author m.doering
 * @created 08-Nov-2007 13:06:19
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DefinedTermBase", propOrder = {
    "media",
    "vocabulary",
    "idInVocabulary",
    "symbol"
})
@XmlRootElement(name = "DefinedTermBase")
@XmlSeeAlso({
    AnnotationType.class,
    DerivationEventType.class,
    DefinedTerm.class,
    ExtensionType.class,
    Feature.class,
    Language.class,
    MarkerType.class,
    MeasurementUnit.class,
    NamedAreaType.class,
    NomenclaturalCode.class,
    PreservationMethod.class,
    ReferenceSystem.class,
    RightsType.class,
    StatisticalMeasure.class,
    TextFormat.class
})
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@ClassBridge(impl=DefinedTermBaseClassBridge.class)
//TODO Comparable implemented only for fixing failing JAXB import, may be removed when this is fixed
public abstract class DefinedTermBase<T extends DefinedTermBase> extends TermBase implements ILoadableTerm<T>, IDefinedTerm<T>, Comparable<T> {
    private static final long serialVersionUID = 2931811562248571531L;
    private static final Logger logger = Logger.getLogger(DefinedTermBase.class);

//	@XmlElement(name = "KindOf")
//    @XmlIDREF
//    @XmlSchemaType(name = "IDREF")
    @XmlTransient
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DefinedTermBase.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private T kindOf;
    /**
     * FIXME - Hibernate returns this as a collection of CGLibProxy$$DefinedTermBase objects
     * which can't be cast to instances of T - can we explicitly initialize these terms using
     * Hibernate.initialize(), does this imply a distinct load, and find methods in the dao?
     */
//	@XmlElementWrapper(name = "Generalizations")
//	@XmlElement(name = "GeneralizationOf")
//    @XmlIDREF
//    @XmlSchemaType(name = "IDREF")
    @XmlTransient
    @OneToMany(fetch=FetchType.LAZY, mappedBy = "kindOf", targetEntity = DefinedTermBase.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private Set<T> generalizationOf = new HashSet<T>();

//	@XmlElement(name = "PartOf")
//	@XmlIDREF
//  @XmlSchemaType(name = "IDREF")
    @XmlTransient
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DefinedTermBase.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    protected T partOf;

    /**
     * FIXME - Hibernate retuns this as a collection of CGLibProxy$$DefinedTermBase objects
     * which can't be cast to instances of T - can we explicitly initialize these terms using
     * Hibernate.initialize(), does this imply a distinct load, and find methods in the dao?
     */
//	@XmlElementWrapper(name = "Includes")
//	@XmlElement(name = "Include")
//	@XmlIDREF
//    @XmlSchemaType(name = "IDREF")
    @XmlTransient
    @OneToMany(fetch=FetchType.LAZY, mappedBy = "partOf", targetEntity = DefinedTermBase.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private Set<T> includes = new HashSet<T>();

    @XmlElementWrapper(name = "Media")
    @XmlElement(name = "Medium")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private Set<Media> media = new HashSet<Media>();

    @XmlElement(name = "TermVocabulary")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    protected TermVocabulary<T> vocabulary;

  //the unique iedentifier/name this term uses in its given vocabulary #3479
   //open issues: is null allowed? If not, implement unique constraint

    @XmlElement(name = "idInVocabulary")
    @Column(length=255)
    //TODO Val #3379, #4245
//  @NullOrNotEmpty
    private String idInVocabulary;  //the unique identifier/name this term uses in its given vocabulary #3479

    @XmlElement(name = "symbol")
    @Column(length=30)
    //the symbol to be used in String representations for this term  #5734
    //this term can be changed by the database instance even if the term is not managed by this instance as it is only for representation and has no semantic or identifying character
    //empty string is explicitly allowed and should be distinguished from NULL!
    private String symbol;

//***************************** CONSTRUCTOR *******************************************/



	//for javassit only
    @Deprecated
    protected DefinedTermBase(){};

    protected DefinedTermBase(TermType type) {
        super(type);
    }
    public DefinedTermBase(TermType type, String description, String label, String labelAbbrev) {
        super(type, description, label, labelAbbrev);
    }


//********************** GETTER /SETTER *************************************

      @Override
      public String getIdInVocabulary() {
          return idInVocabulary;
      }

      @Override
      public void setIdInVocabulary(String idInVocabulary) {

          this.idInVocabulary = StringUtils.isBlank(idInVocabulary)? null : idInVocabulary;
      }

      @Override
      public T getKindOf(){

          if (this instanceof HibernateProxy) {
              HibernateProxy proxy = (HibernateProxy) this;
              LazyInitializer li = proxy.getHibernateLazyInitializer();
              return (T) ((T)li.getImplementation()).getKindOf();
          } else {
              return (T)DefinedTermBase.deproxy(this.kindOf, this.getClass());
          }
      }

      public void setKindOf(T kindOf){
          this.kindOf = kindOf;
      }


      @Override
      public Set<T> getGeneralizationOf(){
          return this.generalizationOf;
      }

      protected void setGeneralizationOf(Set<T> value) {
          this.generalizationOf = value;
      }

      public void addGeneralizationOf(T generalization) {
          generalization.setKindOf(this);
          this.generalizationOf.add(generalization);
      }


      public void removeGeneralization(T generalization) {
          if(generalizationOf.contains(generalization)){
              generalization.setKindOf(null);
              this.generalizationOf.remove(generalization);
          }
      }

      @Override
      public T getPartOf(){
          if (this instanceof HibernateProxy) {
              HibernateProxy proxy = (HibernateProxy) this;
              LazyInitializer li = proxy.getHibernateLazyInitializer();
              return (T) ((T)li.getImplementation()).getPartOf();
          } else {
              return (T)DefinedTermBase.deproxy(this.partOf, this.getClass());
          }
      }

      /**
       * @see #getPartOf()
      */
      public void setPartOf(T partOf){
          this.partOf = partOf;
      }


    //TODO Comparable implemented only for fixing failing JAXB imports, may be removed when this is fixed
  	@Override
  	@Deprecated //for inner use only
  	public int compareTo(T other) {
		return ((Integer)this.getId()).compareTo(other.getId());
	}

	@Override
      public Set<T> getIncludes(){
          return this.includes;
      }

      /**
       * @see #getIncludes()
      */
      protected void setIncludes(Set<T> includes) {
          this.includes = includes;
      }

      /**
       * @see #getIncludes()
       */
      public void addIncludes(T includes) {
          includes.setPartOf(this);
          this.includes.add(includes);
      }

      /**
       * @see #getIncludes()
       */
      public void removeIncludes(T includes) {
          if(this.includes.contains(includes)) {
              includes.setPartOf(null);
              this.includes.remove(includes);
          }
      }

      @Override
      public Set<Media> getMedia(){
          return this.media;
      }

      public void addMedia(Media media) {
          this.media.add(media);
      }
      public void removeMedia(Media media) {
          this.media.remove(media);
      }

      /**
       * @return
       */
      public TermVocabulary<T> getVocabulary() {
          return this.vocabulary;
      }

      //for bedirectional use only, use vocabulary.addTerm instead
      /**
       * @param newVocabulary
       */
      protected void setVocabulary(TermVocabulary<T> newVocabulary) {
          this.vocabulary = newVocabulary;
    }

//******************************* METHODS ******************************************************/


      @Override
      public boolean isKindOf(T ancestor) {
          if (kindOf == null || ancestor == null){
            return false;
        }else if (kindOf.equals(ancestor)){
            return true;
        }else{
            return kindOf.isKindOf(ancestor);
        }
      }

      @Override
      public Set<T> getGeneralizationOf(boolean recursive) {
          Set<T> result = new HashSet<T>();
        result.addAll(this.generalizationOf);
        if (recursive){
            for (T child : this.generalizationOf){
                result.addAll(child.getGeneralizationOf());
            }
        }
        return result;
      }



    public abstract void resetTerms();

    protected abstract void setDefaultTerms(TermVocabulary<T> termVocabulary);


    @Override
    public T readCsvLine(Class<T> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms, boolean abbrevAsId) {
        try {
            T newInstance = getInstance(termClass);
            readCsvLine(newInstance, csvLine, Language.CSV_LANGUAGE(), abbrevAsId);
            readIsPartOf(newInstance, csvLine, terms);
            return newInstance;
        } catch (Exception e) {
            logger.error(e);
            for(StackTraceElement ste : e.getStackTrace()) {
                logger.error(ste);
            }
            throw new RuntimeException(e);
        }
    }

    protected static <TERM extends DefinedTermBase> TERM readCsvLine(TERM newInstance, List<String> csvLine, Language lang, boolean abbrevAsId) {
        newInstance.setUuid(UUID.fromString(csvLine.get(0)));
        newInstance.setUri( URI.create(csvLine.get(1)));
        String label = csvLine.get(2).trim();
        String description = csvLine.get(3);
        String abbreviatedLabel = csvLine.get(4);
        if (StringUtils.isBlank(abbreviatedLabel)){
            abbreviatedLabel = null;
        }
        if (abbrevAsId){
            newInstance.setIdInVocabulary(abbreviatedLabel);  //new in 3.3
        }
        newInstance.addRepresentation(Representation.NewInstance(description, label, abbreviatedLabel, lang) );

        return newInstance;
    }

    protected void readIsPartOf(T newInstance, List<String> csvLine, Map<UUID, DefinedTermBase> terms){
        int index = partOfCsvLineIndex();
         if (index != -1){
            String partOfString = csvLine.get(index);
             if(StringUtils.isNotBlank(partOfString)) {
                 UUID partOfUuid = UUID.fromString(partOfString);
                 DefinedTermBase partOf = terms.get(partOfUuid);
                 partOf.addIncludes(newInstance);
             }
         }

    }

    /**
     * Get the
     * @return
     */
    protected int partOfCsvLineIndex() {
        return -1;
    }


    private  <T extends DefinedTermBase> T getInstance(Class<? extends DefinedTermBase> termClass) {
        try {
            Constructor<T> c = ((Class<T>)termClass).getDeclaredConstructor();
            c.setAccessible(true);
            T termInstance = c.newInstance();
            return termInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeCsvLine(CSVWriter writer, T term) {
        String [] line = new String[4];
        line[0] = term.getUuid().toString();
        line[1] = term.getUri().toString();
        line[2] = term.getLabel();
        line[3] = term.getDescription();
        writer.writeNext(line);
    }

    @Transient
    public T getByUuid(UUID uuid){
        return this.vocabulary.findTermByUuid(uuid);
    }


//*********************** CLONE ********************************************************/

    /**
     * Clones <i>this</i> DefinedTermBase. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> defined term base by
     * modifying only some of the attributes.
     *
     * @see eu.etaxonomy.cdm.model.common.TermBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        DefinedTermBase result;
        try {
            result = (DefinedTermBase) super.clone();
        }catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }

        result.generalizationOf = new HashSet<DefinedTermBase>();
        for (DefinedTermBase generalizationOf : this.generalizationOf){
            result.generalizationOf.add(generalizationOf.clone());
        }

        result.includes = new HashSet<DefinedTermBase>();

        for (DefinedTermBase include: this.includes){
            result.includes.add(include.clone());
        }

        result.media = new HashSet<Media>();

        for (Media media: this.media){
            result.addMedia(media);
        }

        return result;
    }

    // Currently the CDM Caching mechanism is only used for caching terms
    private static ICdmUuidCacher cacher;


    /**
     * Gets the CDM cacher object
     *
     * @return the CDM cacher object
     */
    public static ICdmUuidCacher getCacher() {
		return cacher;
	}

	/**
	 * Sets the CDM cacher object
	 *
	 * @param cacher the CDM cacher object
	 */
	public static void setCacher(ICdmUuidCacher cacher) {
		DefinedTermBase.cacher = cacher;
	}

	public static <T extends DefinedTermBase> T getTermByClassAndUUID(Class<T> clazz, UUID uuid) {
	    if(cacher != null) {
	        Object obj = getCacher().load(uuid);
	        if(obj != null && obj.getClass().equals(clazz)) {
	            return (T)obj;
	        }
	    }
	    return null;
	}
}