/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.description;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.validation.Level2;

/**
 * This class represents information pieces expressed in categorical type of
 * data (in opposition to {@link QuantitativeData quantitative data} on one side and to literal data on
 * the other side). Only {@link TaxonDescription taxon descriptions} and
 * {@link SpecimenDescription specimen descriptions} may contain categorical data.<BR>
 * The "color of petals" {@link Feature feature} for instance can be described with
 * {@link State state terms} such as "blue" or "white". If the color of petals of a
 * particular tree is described as "mostly blue" and "exceptionally white" two
 * {@link StateData state data} instances must be assigned to an instance of the
 * present class: the first one with the state "blue" and the {@link Modifier modifier}
 * "mostly" and the second one with the state "white" and the modifier "exceptionally".
 * Whenever more than one state data belongs to a categorical data they should be
 * interpreted as being related by the inclusive disjunction "or".
 * <P>
 * This class corresponds partially to CodedDescriptionType according to
 * the SDD schema.
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:15
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CategoricalData", propOrder = {
    "orderRelevant",
    "stateData",
    "unknownData"
})
@XmlRootElement(name = "CategoricalData")
@Entity
@Audited
@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptionElementBase")
public class CategoricalData extends DescriptionElementBase {

    private static final long serialVersionUID = -6298361966947668998L;
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    //whether the sequence of ordered states is important
    @XmlElement(name = "OrderRelevant")
    private boolean orderRelevant;

    @XmlElementWrapper(name = "States")
    @XmlElement(name = "State")
    @OneToMany(fetch = FetchType.LAZY, mappedBy="categoricalData", orphanRemoval=true)
    @Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @IndexedEmbedded(depth = 3)
    @NotEmpty(groups = Level2.class)
    private List<StateData> stateData = new ArrayList<>();

    @XmlElement(name = "UnknownData")
    @Deprecated   //will be replaced by #noDataStatus
    private final Boolean unknownData = false;

    @XmlAttribute(name ="NoDataStatus")
    @Column(name="noDataStatus", length=10)
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name = "enumClass", value = "eu.etaxonomy.cdm.model.description.NoDescriptiveDataStatus")}
    )
    //see also QuantitativeData.noDataStatus
    private NoDescriptiveDataStatus noDataStatus;

//****************************** FACTORY METHOD *******************************/

    /**
     * Creates a new empty categorical data instance.
     */
    public static CategoricalData NewInstance(){
        return new CategoricalData();
    }


    /**
     * @param habitat
     * @return
     */
    public static CategoricalData NewInstance(Feature feature) {
        return new CategoricalData( new ArrayList<>() , feature);
    }

    /**
     * Creates a new empty categorical data instance.
     */
    public static CategoricalData NewInstance(DefinedTermBase<?> state, Feature feature){
        return new CategoricalData( Arrays.asList( new DefinedTermBase<?>[]{state}) , feature);
    }

//*******************  CONSTRUCTOR *********************************************/

    /**
     * Class constructor: creates a new empty categorical data instance.
     */
    protected CategoricalData() {
        super(null);
    }

    /**
     * Class constructor: creates a new empty categorical data instance.
     */
    protected CategoricalData(List<DefinedTermBase<?>> states, Feature feature) {
        super(feature);
        for (DefinedTermBase<?> state : states){
            addStateData(state);
        }
    }

// ****************** GETTER / SETTER *********************************************/

    /**
     * Returns the (ordered) list of {@link State states} describing the {@link Feature feature}
     * corresponding to <i>this</i> categorical data.
     */
    public List<StateData> getStateData(){
        return this.stateData;
    }

    @Deprecated
    protected void setStateData(List<StateData> stateData){
        this.stateData = stateData;
    }

    /**
     * Adds a {@link State state} to the list of {@link #getStateData() states}
     * describing the {@link Feature feature} corresponding to <i>this</i> categorical data.
     *
     * @param state	the state to be added to <i>this</i> categorical data
     * @see    	   	#getStateData()
     */
    @SuppressWarnings("deprecation")
    public void addStateData(StateData stateData){
        this.stateData.add(stateData);
        stateData.setCategoricalData(this);
    }

    /**
     * Convenience method which creates a state data from a given state with no modifiers
     * and adds it to the list of state data
     * @see #addStateData(StateData)
     * @param state
     */
    public StateData addStateData(DefinedTermBase<?> state){
        StateData stateData = StateData.NewInstance(state);
        addStateData(stateData);
        return stateData;
    }

    /**
     * Removes one element from the set of {@link #getStateData() states}
     * describing the {@link Feature feature} corresponding to <i>this</i> categorical data.
     *
     * @param  state	the state which should be removed
     * @see     		#getStateData()
     * @see     		#addStateData(State)
     */
    @SuppressWarnings("deprecation")
    public void removeStateData(StateData stateData){
        this.stateData.remove(stateData);
        stateData.setCategoricalData(null);
    }

    //rename to isStateSequenceIntentional ??
    /**
     * Returns the boolean value of the flag indicating whether the sequence of
     * {@link StateData state data} belonging to <i>this</i> categorical data is intentional
     * (true) and therefore relevant for interpretation or analysis or not (false).
     * The use of this flag depends mostly on the {@link Feature feature} of <i>this</i> categorical data.
     *
     * @return  the boolean value of the orderRelevant flag
     */
    public boolean getOrderRelevant(){
        return this.orderRelevant;
    }
    /**
     * @see	#getOrderRelevant()
     */
    public void setOrderRelevant(boolean orderRelevant){
        this.orderRelevant = orderRelevant;
    }

    //no data status, #2975
    public NoDescriptiveDataStatus getNoDataStatus() {
        return noDataStatus;
    }
    public void setNoDataStatus(NoDescriptiveDataStatus noDataStatus) {
        this.noDataStatus = noDataStatus;
    }

// ********************* CONVENIENCE ******************************************/

    /**
     * Convenience method to test the existence of a given state in the state data.
     * Note: the method ignores modifiers so state data having the state may still be
     * modified by its modifiers.
     *
     * @param state the given {@link State}
     * @return <code>true</code> if the state exists
     */
    public boolean hasState(DefinedTermBase<?> state) {
        return getStatesOnly().contains(state);
    }

    /**
     * Convenience method returning only the list of states. Leaving out modifiers and modifying text.
     */
    @Transient
    public List<eu.etaxonomy.cdm.model.term.DefinedTermBase<?>> getStatesOnly(){
        List<DefinedTermBase<?>> result = new ArrayList<>();
        for (StateData stateData : getStateData()){
            DefinedTermBase<?> state = stateData.getState();
            if (state != null){
                result.add(state);
            }
        }
        return result;
    }

    /**
     * Convenience method which to set the list of states (no modifiers or modifying text allowed).
     * All existing state data are removed.
     * @return
     */
    public List<StateData> setStateDataOnly(List<? extends DefinedTermBase<?>> states){
        List<StateData> stateDataList = new ArrayList<>(getStateData());
        for (StateData stateData : stateDataList) {
            removeStateData(stateData);
        }
        for (DefinedTermBase<?> state : states) {
            addStateData(state);
        }
        return this.stateData;
    }

    @Transient
    @XmlTransient
    @Override
    public boolean isCharacterData() {
        return true;
    }

//********************************** toString **************************************/

    @Override
    public String toString() {
        return (getFeature()!=null ? getFeature().getLabel(): "") +
                "[" + stateData +
                    (orderRelevant? ", orderRelevant=" + orderRelevant:"") +
                    (noDataStatus != null ? ", noDataStatus=" + noDataStatus.getLabel() :"")
                + "]";
    }

//*********************************** CLONE *****************************************/

    /**
     * Clones <i>this</i> categorical data. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> categorical data by
     * modifying only some of the attributes.
     * @throws CloneNotSupportedException
     *
     * @see eu.etaxonomy.cdm.model.description.DescriptionElementBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public CategoricalData clone() {

        CategoricalData result = (CategoricalData)super.clone();

        //states
        result.stateData = new ArrayList<>();
        for (StateData stateData : getStateData()){
            StateData newState = stateData.clone();
            result.addStateData(newState);
        }

        //no changes to: orderRelevant
        return result;
    }
}