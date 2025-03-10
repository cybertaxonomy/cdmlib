/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Iterator;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.search.annotations.ClassBridge;

import au.com.bytecode.opencsv.CSVWriter;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.hibernate.search.DefinedTermBaseClassBridge;
import eu.etaxonomy.cdm.model.ICdmUuidCacher;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.PreservationMethod;

/**
 * workaround for enumerations, base type according to TDWG.  For linear ordering
 * use partOf relation and BreadthFirst. Default iterator order should therefore
 * be BreadthFirst (not DepthFirst)
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:19
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DefinedTermBase", propOrder = {
    "media",
    "vocabulary",
    "orderIndex",
    "idInVocabulary",
    "symbol",
    "symbol2",
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
    TextFormat.class,
    RelationshipTermBase.class,
    PresenceAbsenceTerm.class,
    State.class,
    NamedArea.class,
    NamedAreaLevel.class,
    NomenclaturalStatusType.class,
    Rank.class
})
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@ClassBridge(impl=DefinedTermBaseClassBridge.class)
//TODO Comparable implemented only for fixing failing JAXB import, may be removed when this is fixed
public abstract class DefinedTermBase<T extends DefinedTermBase>
            extends TermBase
            implements IDefinedTerm<T>, Comparable<T> {

    private static final long serialVersionUID = 2931811562248571531L;
    private static final Logger logger = LogManager.getLogger();

    //Order index, value < 1 or null means that this term is not in order yet
    @XmlElement(name = "OrderIndex")
    protected Integer orderIndex;

//	@XmlElement(name = "KindOf")
//    @XmlIDREF
//    @XmlSchemaType(name = "IDREF")
    @XmlTransient
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DefinedTermBase.class)
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
    private Set<T> generalizationOf = new HashSet<>();

//	@XmlElement(name = "PartOf")
//	@XmlIDREF
//  @XmlSchemaType(name = "IDREF")
    @XmlTransient
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = DefinedTermBase.class)
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
    private Set<T> includes = new HashSet<>();

    @XmlElementWrapper(name = "Media")
    @XmlElement(name = "Medium")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToMany(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private Set<Media> media = new HashSet<>();

    @XmlElement(name = "TermVocabulary")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    protected TermVocabulary<T> vocabulary;

    //the unique identifier/name this term uses in its given vocabulary #3479
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

    @XmlElement(name = "symbol2")
    @Column(length=30)
    //the second symbol to be used in String representations for this term #7096
    //this term can be changed by the database instance even if the term is not managed by this instance as it is only for representation and has no semantic or identifying character
    //empty string is explicitly allowed and should be distinguished from NULL!
    private String symbol2;


//***************************** CONSTRUCTOR *******************************************/

    //for hibernate use only, *packet* private required by bytebuddy
    //2022-06-17: currently still needed protected as TaxEditor.TaxonRelationshipTypeInverseContainer inherits from DefinedTermBase
    @Deprecated
    protected DefinedTermBase(){}

    protected DefinedTermBase(TermType type) {
        super(type);
    }

    public DefinedTermBase(TermType type, String description, String label, String labelAbbrev, Language lang) {
        super(type, description, label, labelAbbrev, lang);
    }
    public DefinedTermBase(TermType type, String description, String label, String labelAbbrev) {
        super(type, description, label, labelAbbrev, null);
    }

//********************** GETTER /SETTER *************************************

      @Override
      public String getIdInVocabulary() {
          return idInVocabulary;
      }

      @Override
      public void setIdInVocabulary(String idInVocabulary) {
          this.idInVocabulary = CdmUtils.isBlank(idInVocabulary)? null : idInVocabulary;
      }

      @Override
      public T getKindOf(){
          if (this instanceof HibernateProxy) {
              HibernateProxy proxy = (HibernateProxy) this;
              LazyInitializer li = proxy.getHibernateLazyInitializer();
              return (T)((T)li.getImplementation()).getKindOf();
          } else {
              return DefinedTermBase.deproxy(this.kindOf);
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
          checkTermType(generalization);
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
              return (T)((T)li.getImplementation()).getPartOf();
          } else {
              return DefinedTermBase.deproxy(this.partOf);
          }
      }

      /**
       * @see #getPartOf()
      */
      public void setPartOf(T partOf){
          this.partOf = partOf;
      }


	@Override
    public Set<T> getIncludes(){
	    Set<T> toAdd = new HashSet<>();

    	    Iterator<T> it = this.includes.iterator();
            while (it.hasNext()) {
                T term = it.next();
                if (term instanceof HibernateProxy) {
                    HibernateProxy proxy = (HibernateProxy) term;
                    LazyInitializer li = proxy.getHibernateLazyInitializer();
                    T t = (T)li.getImplementation();
                    it.remove();
                    toAdd.add(t);
                }
            }
            this.includes.addAll(toAdd);
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
        checkTermType(includes);
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

    public TermVocabulary<T> getVocabulary() {
        return this.vocabulary;
    }
    //for bedirectional use only, use vocabulary.addTerm instead
    protected void setVocabulary(TermVocabulary<T> newVocabulary) {
        this.vocabulary = newVocabulary;
    }

    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol2() {
        return symbol2;
    }
    public void setSymbol2(String symbol2) {
        this.symbol2 = symbol2;
    }

//************************** ordered **********************************/

    public boolean isOrderRelevant() {
        return orderIndex != null;
    }

    /**
     * Higher ordered terms have a lower order index,
     * lower ordered terms have a higher order index:
     * <p>
     * <b>a.oderIndex &lt; b.oderIndex : a &gt; b</b>
     * @return the order index of a term or <code>null</code> if the term is not ordered in it's vocabulary
     */
    public Integer getOrderIndex() {
        return orderIndex;
    }

    @Override
    public int compareTo(T other) {
        if (isOrderRelevant()) {
            /**
             * Compares this DefinedTermBase with the specified DefinedTermBase for
             * order. Returns a -1, 0, or +1 if the orderIndex of this object is greater
             * than, equal to, or less than the specified object. In case the parameter
             * is <code>null</code> the
             * <p>
             * <b>Note:</b> The compare logic of this method might appear to be <b>inverse</b>
             * to the one mentioned in
             * {@link java.lang.Comparable#compareTo(java.lang.Object)}. This is, because the logic here
             * is that the lower the orderIndex the higher the term. E.g. the very high {@link Rank}
             * Kingdom may have an orderIndex close to 1.
             *
             * @param orderedTerm
             *            the DefinedTermBase to be compared
             * @throws NullPointerException
             *             if the specified object is null
             */
            return performCompareTo(other, false);
        }else {
            //@Deprecated //for inner use only
            //TODO Comparable for non-ordered implemented only for fixing failing JAXB imports, may be removed when this is fixed
            return ((Integer)this.getId()).compareTo(other.getId());
        }
    }

    /**
     * Compares this {@link DefinedTermBase ordered term} with the given {@link DefinedTermBase thatTerm} for
     * order.  Returns a -1, 0, or +1 if the orderId of this object is greater
     * than, equal to, or less than the specified object.
     * <p>
     * <b>Note:</b> The compare logic of this method is the <b>inverse logic</b>
     * of the the one implemented in
     * {@link java.lang.Comparable#compareTo(java.lang.Object)}
     *
     * @param orderedTerm
     *            the DefinedTermBase to be compared
     * @param skipVocabularyCheck
     *            whether to skip checking if both terms to compare are in the
     *            same vocabulary
     * @throws NullPointerException
     *             if the specified object is null
     */
    //overriden by PresenceAbsenceTerm, HybridRelationshipType and Rank
    protected int performCompareTo(T thatTerm , boolean skipVocabularyCheck ) {

        T thatTermLocal = CdmBase.deproxy(thatTerm);
        if(!skipVocabularyCheck){
            if (this.vocabulary == null || thatTermLocal.vocabulary == null){
                throw new IllegalStateException("An ordered term (" + this.toString() + " or " + thatTermLocal.toString() + ") of class " + this.getClass() + " or " + thatTermLocal.getClass() + " does not belong to a vocabulary and therefore can not be compared");
            }
        }

        int vocCompare = compareVocabularies(thatTermLocal);
        if (vocCompare != 0){
            return vocCompare;
        }

        int orderThat;
        int orderThis;
        try {
            orderThat = thatTermLocal.orderIndex;
            orderThis = orderIndex;
        } catch (RuntimeException e) {
            throw e;
        }
        if (orderThis > orderThat){
            return -1;
        }else if (orderThis < orderThat){
            return 1;
        } else {
            if (skipVocabularyCheck){
                String errorStr = "The term %s (ID: %s) is not attached to any vocabulary. This should not happen. "
                        + "Please add the term to an vocabulary";
                if (this.vocabulary == null){
                    throw new IllegalStateException(String.format(errorStr, this.getLabel(), String.valueOf(this.getId())));
                }else if (thatTermLocal.vocabulary == null){
                    throw new IllegalStateException(String.format(errorStr, thatTermLocal.getLabel(), String.valueOf(thatTermLocal.getId())));
                }
            }
            return 0;
        }
    }

    //protected as overriden by PresenceAbsenceTerm
    protected int compareVocabularies(T thatTerm) {
        //if vocabularies are not equal order by voc.uuid to get a defined behavior
        //ordering terms from 2 different vocabularies is generally not recommended
        UUID thisVocUuid = this.vocabulary == null? null:this.vocabulary.getUuid();
        UUID thatVocUuid = thatTerm.getVocabulary() == null? null:thatTerm.getVocabulary().getUuid();
        int vocCompare = CdmUtils.nullSafeCompareTo(thisVocUuid, thatVocUuid);
        return vocCompare;
    }

    /**
     * If this term is lower than the parameter term, <code>true</code> is returned,
     * else <code>false</code>.
     * If the parameter term is <code>null</code> an Exception is thrown.
     * See {@link #compareTo(DefinedTermBase)} for semantics of comparison.
     *
     * @param orderedTerm the term to compare with
     * @return boolean result of the comparison
     * @deprecated use a comparator instead
     */
    @Deprecated
    public boolean isLower(T orderedTerm){
        return (this.compareTo(orderedTerm) < 0 );
    }

    /**
     * If this term is higher than the parameter term, <code>true</code> is returned,
     * else false.
     * If the parameter term is <code>null</code> an exception is thrown.
     * See {@link #compareTo(DefinedTermBase)} for semantics of comparison.
     *
     * @param orderedTerm the term to compare with
     * @see #compareTo(DefinedTermBase)
     * @return boolean result of the comparison
     * @deprecated use a comparator instead
     */
    @Deprecated
    public boolean isHigher(T orderedTerm){
        return (this.compareTo(orderedTerm) > 0 );
    }

    /**
     * @deprecated To be used only by OrderedTermVocabulary
     **/
    @Deprecated
    boolean decreaseIndex(OrderedTermVocabulary vocabulary){
        if (vocabulary.indexChangeAllowed(this) == true){
            orderIndex--;
            return true;
        }else{
            return false;
        }
    }

    /**
     * @deprecated to be used only by OrderedTermVocabulary
     **/
    @Deprecated
    boolean incrementIndex(OrderedTermVocabulary vocabulary){
        if (vocabulary.indexChangeAllowed(this) == true){
            orderIndex++;
            return true;
        }else{
            return false;
        }
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
        Set<T> result = new HashSet<>();
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
    public T readCsvLine(Class<T> termClass, List<String> csvLine, TermType termType, Map<UUID,DefinedTermBase> terms, boolean abbrevAsId) {
        try {
            T newInstance = getInstance(termClass, termType);
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
        String uriStr = CdmUtils.Ne(csvLine.get(1));
        newInstance.setUri(uriStr == null? null: URI.create(uriStr));
        String label = csvLine.get(2).trim();
        String description = CdmUtils.Ne(csvLine.get(3).trim());
        String abbreviatedLabel = CdmUtils.Ne(csvLine.get(4).trim());
        if (CdmUtils.isBlank(abbreviatedLabel)){
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
             if(CdmUtils.isNotBlank(partOfString)) {
                 UUID partOfUuid = UUID.fromString(partOfString);
                 DefinedTermBase partOf = terms.get(partOfUuid);
                 partOf.addIncludes(newInstance);
             }
         }
    }

    protected int partOfCsvLineIndex() {
        return -1;
    }

    private  <T extends DefinedTermBase> T getInstance(Class<? extends DefinedTermBase> termClass, TermType termType) {
        try {
            Constructor<T> c = ((Class<T>)termClass).getDeclaredConstructor();
            c.setAccessible(true);
            T termInstance = c.newInstance();
            termInstance.setTermType(termType);
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

    /**
     * Throws {@link IllegalArgumentException} if the given
     * term has not the same term type as this term or if term type is null.
     * @param term
     */
    private void checkTermType(IHasTermType term) {
        IHasTermType.checkTermTypes(term, this);
    }


//*********************** CLONE ********************************************************/

    /**
     * Clones <i>this</i> DefinedTermBase. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> defined term base by
     * modifying only some of the attributes.
     *
     * @see eu.etaxonomy.cdm.model.term.TermBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public DefinedTermBase<T> clone() {
        try {
            DefinedTermBase<T> result = (DefinedTermBase<T>) super.clone();

            result.generalizationOf = new HashSet<>();
            for (DefinedTermBase<T> generalizationOf : this.generalizationOf){
                result.generalizationOf.add((T)generalizationOf.clone());
            }

            result.includes = new HashSet<>();

            for (DefinedTermBase<?> include: this.includes){
                result.includes.add((T)include.clone());
            }

            result.media = new HashSet<>();

            for (Media media: this.media){
                result.addMedia(media);
            }

            return result;
        }catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }
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

    /**
     * Returns the term with the given {@link UUID} and the exact given {@link Class} from
     * the cache or loads it from the database. If the term exists but has a
     * different class <code>null</code> is returned. This is also the case for
     * if the class is assignable from the given class.
     *
     * @see #getTermByUUID(UUID, Class)
     */
	public static <T extends DefinedTermBase> T getTermByClassAndUUID(Class<T> clazz, UUID uuid) {
	    if(cacher != null) {
	        CdmBase cdmBase = HibernateProxyHelper.deproxy(getCacher().load(uuid));

	        if(cdmBase != null && cdmBase.getClass().equals(clazz)) {
	            return (T)cdmBase;
	        }
	    }
	    return null;
	}

    /**
     * Returns the term from the cache or loads it from the database
     * and casts it to the given class.
     * In contrary to {@link #getTermByClassAndUUID(Class, UUID)} it
     * throws an exception if the term can not be casted.
     */
    public static <T extends DefinedTermBase> T getTermByUUID(UUID uuid, Class<T> clazz) {
        if(cacher != null) {
            return HibernateProxyHelper.deproxy(getCacher().load(uuid), clazz);
        }
        return null;
    }
}