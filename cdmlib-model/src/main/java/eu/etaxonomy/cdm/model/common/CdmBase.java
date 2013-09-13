/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TermVector;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.hibernate.search.DateTimeBridge;
import eu.etaxonomy.cdm.hibernate.search.NotNullAwareIdBridge;
import eu.etaxonomy.cdm.hibernate.search.UuidBridge;
import eu.etaxonomy.cdm.jaxb.DateTimeAdapter;
import eu.etaxonomy.cdm.jaxb.UUIDAdapter;
import eu.etaxonomy.cdm.strategy.match.Match;
import eu.etaxonomy.cdm.strategy.match.MatchMode;




/**
 * The base class for all CDM domain classes implementing UUIDs and bean property change event firing.
 * It provides a globally unique UUID and keeps track of creation date and person.
 * The UUID is the same for different versions (see {@link VersionableEntity}) of a CDM object, so a locally unique id exists in addition
 * that allows to safely access and store several objects (=version) with the same UUID.
 *
 * This class together with the {@link eu.etaxonomy.cdm.aspectj.PropertyChangeAspect}
 * will fire bean change events to all registered listeners. Listener registration and event firing
 * is done with the help of the {@link PropertyChangeSupport} class.
 *
 * @author m.doering
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CdmBase", propOrder = {
    "created",
    "createdBy"
})
@MappedSuperclass
public abstract class CdmBase implements Serializable, ICdmBase, Cloneable{
    private static final long serialVersionUID = -3053225700018294809L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(CdmBase.class);

    @Transient
    @XmlTransient
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    //@XmlAttribute(name = "id", required = true)
    @XmlTransient
    @Id
//	@GeneratedValue(generator = "system-increment")
//	@GeneratedValue(generator = "enhanced-table")
    @GeneratedValue(generator = "custom-enhanced-table")
    @DocumentId
    @FieldBridge(impl=NotNullAwareIdBridge.class)
    @Field(store=Store.YES, termVector=TermVector.NO)
    @Match(MatchMode.IGNORE)
    @NotNull
    @Min(0)
    @Audited
    private int id;

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(UUIDAdapter.class)
    @Type(type="uuidUserType")
    @NaturalId // This has the effect of placing a "unique" constraint on the database column
    @XmlID
    @Column(length=36)  //TODO needed? Type UUID will always assure that is exactly 36
    @Match(MatchMode.IGNORE)
    @NotNull
    @Field(store = Store.YES, index = Index.YES, analyze = Analyze.NO)
    @FieldBridge(impl = UuidBridge.class)
    @Audited
    protected UUID uuid;

    @XmlElement (name = "Created", type= String.class)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    @Type(type="dateTimeUserType")
    @Basic(fetch = FetchType.LAZY)
    @Match(MatchMode.IGNORE)
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = DateTimeBridge.class)
    @Audited
    private DateTime created;

    @XmlElement (name = "CreatedBy")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch=FetchType.LAZY)
    @Match(MatchMode.IGNORE)
    @Audited
    private User createdBy;

    /**
     * Class constructor assigning a unique UUID and creation date.
     * UUID can be changed later via setUuid method.
     */
    public CdmBase() {
        this.uuid = UUID.randomUUID();
        this.created = new DateTime().withMillisOfSecond(0);
    }

    /**
     * see {@link PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)}
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * see {@link PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)}
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * see {@link PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)}
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * @see PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    public boolean hasListeners(String propertyName) {
        return propertyChangeSupport.hasListeners(propertyName);
    }

    public void firePropertyChange(String property, String oldval, String newval) {
        propertyChangeSupport.firePropertyChange(property, oldval, newval);
    }
    public void firePropertyChange(String property, int oldval, int newval) {
        propertyChangeSupport.firePropertyChange(property, oldval, newval);
    }
    public void firePropertyChange(String property, float oldval, float newval) {
        propertyChangeSupport.firePropertyChange(property, oldval, newval);
    }
    public void firePropertyChange(String property, boolean oldval, boolean newval) {
        propertyChangeSupport.firePropertyChange(property, oldval, newval);
    }
    public void firePropertyChange(String property, Object oldval, Object newval) {
        propertyChangeSupport.firePropertyChange(property, oldval, newval);
    }
    public void firePropertyChange(PropertyChangeEvent evt) {
        propertyChangeSupport.firePropertyChange(evt);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.ICdmBase#getUuid()
     */
    @Override
    public UUID getUuid() {
        return uuid;
    }
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.ICdmBase#setUuid(java.util.UUID)
     */
    @Override
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.ICdmBase#getId()
     */
    @Override
    public int getId() {
        return this.id;
    }
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.ICdmBase#setId(int)
     */
    @Override
    public void setId(int id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.ICdmBase#getCreated()
     */
    @Override
    public DateTime getCreated() {
        return created;
    }
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.ICdmBase#setCreated(java.util.Calendar)
     */
    @Override
    public void setCreated(DateTime created) {
        if (created != null){
            new DateTime();
            created = created.withMillisOfSecond(0);
            //created.set(Calendar.MILLISECOND, 0);  //old, can be deleted
        }
        this.created = created;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.ICdmBase#getCreatedBy()
     */
    @Override
    public User getCreatedBy() {
        return this.createdBy;
    }
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.ICdmBase#setCreatedBy(eu.etaxonomy.cdm.model.agent.Person)
     */
    @Override
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

// ************************** Hibernate proxies *******************/
    /**
     * These methods are present due to HHH-1517 - that in a one-to-many
     * relationship with a superclass at the "one" end, the proxy created
     * by hibernate is the superclass, and not the subclass, resulting in
     * a classcastexception when you try to cast it.
     *
     * Hopefully this will be resolved through improvements with the creation of
     * proxy objects by hibernate and the following methods will become redundant,
     * but for the time being . . .
     * @param <T>
     * @param object
     * @param clazz
     * @return
     * @throws ClassCastException
     */
    //non-static does not work because javassist already unwrapps the proxy before calling the method
     public static <T extends CdmBase> T deproxy(Object object, Class<T> clazz) throws ClassCastException {
         return HibernateProxyHelper.deproxy(object, clazz);
     }

     public boolean isInstanceOf(Class<? extends CdmBase> clazz) throws ClassCastException {
         return HibernateProxyHelper.isInstanceOf(this, clazz);
     }

// ************* Object overrides *************************/

    /**
     * Is true if UUID is the same for the passed Object and this one.
     * @see java.lang.Object#equals(java.lang.Object)
     * See {@link http://www.hibernate.org/109.html hibernate109}, {@link http://www.geocities.com/technofundo/tech/java/equalhash.html geocities}
     * or {@link http://www.ibm.com/developerworks/java/library/j-jtp05273.html ibm}
     * for more information about equals and hashcode.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this){
            return true;
        }
        if (obj == null){
            return false;
        }
        if (!CdmBase.class.isAssignableFrom(obj.getClass())){
            return false;
        }
        ICdmBase cdmObj = (ICdmBase)obj;
        boolean uuidEqual = cdmObj.getUuid().equals(this.getUuid());
        boolean createdEqual = cdmObj.getCreated().equals(this.getCreated());
        if (! uuidEqual || !createdEqual){
                return false;
        }
        return true;
    }


    /** Overrides {@link java.lang.Object#hashCode()}
     *  See {@link http://www.hibernate.org/109.html hibernate109}, {@link http://www.geocities.com/technofundo/tech/java/equalhash.html geocities}
     * or {@link http://www.ibm.com/developerworks/java/library/j-jtp05273.html ibm}
     * for more information about equals and hashcode.
     */
    @Override
    public int hashCode() {
           int hashCode = 7;
           if(this.getUuid() != null) {
               //this unfortunately leads to errors when loading maps via hibernate
               //as hibernate computes hash values for CdmBase objects used as key at
               // a time when the uuid is not yet loaded from the database. Therefore
               //the hash values later change and give wrong results when retrieving
               //data from the map (map.get(key) returns null, though there is an entry
               //for key in the map.
               //see further comments in #2114
               int result = 29 * hashCode + this.getUuid().hashCode();
//		       int shresult = 29 * hashCode + Integer.valueOf(this.getId()).hashCode();
               return result;
           } else {
               return 29 * hashCode;
           }
    }

    /**
     * Overrides {@link java.lang.Object#toString()}.
     * This returns an String that identifies the object well without being necessarily unique. Internally the method is delegating the
     * call to {link {@link #instanceToString()}.<br>
     * <b>Specification:</b> This method should never call other object' methods so it can be well used for debugging
     * without problems like lazy loading, unreal states etc.
     * <p>
     * <b>Note</b>: If overriding this method's javadoc always copy or link the above requirement.
     * If not overwritten by a subclass method returns the class, id and uuid as a string for any CDM object.
     * <p>
     * <b>For example</b>: Taxon#13&lt;b5938a98-c1de-4dda-b040-d5cc5bfb3bc0&gt;
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return instanceToString();
    }

    /**
     * This returns an String that identifies the cdm instacne well without being necessarily unique.
     * The string representation combines the class name the {@link #id} and {@link #uuid}.
     * <p>
     * <b>For example</b>: Taxon#13&lt;b5938a98-c1de-4dda-b040-d5cc5bfb3bc0&gt;
     * @return
     */
    public String instanceToString() {
        return this.getClass().getSimpleName()+"#"+this.getId()+"<"+this.getUuid()+">";
    }

// **************** invoke methods **************************/

    protected void invokeSetMethod(Method method, Object object){
        try {
            method.invoke(object, this);
        } catch (Exception e) {
            e.printStackTrace();
            //TODO handle exceptioin;
        }
    }

    protected void invokeSetMethodWithNull(Method method, Object object){
        try {
            Object[] nul = new Object[]{null};
            method.invoke(object, nul);
        } catch (Exception e) {
            e.printStackTrace();
            //TODO handle exceptioin;
        }
    }

//********************** CLONE *****************************************/

    protected void clone(CdmBase clone){
        clone.setCreatedBy(createdBy);
        clone.setId(id);
        clone.propertyChangeSupport=new PropertyChangeSupport(clone);
        //Constructor Attributes
        //clone.setCreated(created);
        //clone.setUuid(getUuid());

    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException{
        CdmBase result = (CdmBase)super.clone();
        result.propertyChangeSupport=new PropertyChangeSupport(result);

        //TODO ?
        result.setId(0);
        result.setUuid(UUID.randomUUID());
        result.setCreated(new DateTime());
        result.setCreatedBy(null);

        //no changes to: -
        return result;
    }

}
