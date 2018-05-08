/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.Partial;
import org.joda.time.ReadableInstant;

/**
 * @author a.mueller
 * @since 08.05.2018
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VerbatimTimePeriod", propOrder = {
    "verbatimDate"
})
@XmlRootElement(name = "VerbatimTimePeriod")
@Embeddable
public class VerbatimTimePeriod extends TimePeriod {

    private static final long serialVersionUID = -6543644293635460526L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(VerbatimTimePeriod.class);

    @XmlElement(name = "FreeText")
    private String verbatimDate;


 // ********************** FACTORY METHODS **************************/

     /**
      * Factory method
      * @return
      */
     public static final VerbatimTimePeriod NewVerbatimInstance(){
         return new VerbatimTimePeriod();
     }


     /**
      * Factory method
      * @return
      */
     public static final VerbatimTimePeriod NewVerbatimInstance(Partial startDate){
         return new VerbatimTimePeriod(startDate);
     }


     /**
      * Factory method
      * @return
      */
     public static final VerbatimTimePeriod NewVerbatimInstance(Partial startDate, Partial endDate){
         return new VerbatimTimePeriod(startDate, endDate);
     }


     /**
      * Factory method
      * @return
      */
     public static final VerbatimTimePeriod NewVerbatimInstance(Integer year){
         Integer endYear = null;
         return NewVerbatimInstance(year, endYear);
     }

     /**
      * Factory method
      * @return
      */
     public static final VerbatimTimePeriod NewVerbatimInstance(Integer startYear, Integer endYear){
         Partial startDate = null;
         Partial endDate = null;
         if (startYear != null){
             startDate = new Partial().with(YEAR_TYPE, startYear);
         }
         if (endYear != null){
             endDate = new Partial().with(YEAR_TYPE, endYear);
         }
         return new VerbatimTimePeriod(startDate, endDate);
     }



     /**
      * Factory method to create a TimePeriod from a <code>Calendar</code>. The Calendar is stored as the starting instant.
      * @return
      */
     public static final VerbatimTimePeriod NewVerbatimInstance(Calendar startCalendar){
         return NewVerbatimInstance(startCalendar, null);
     }

     /**
      * Factory method to create a TimePeriod from a <code>ReadableInstant</code>(e.g. <code>DateTime</code>).
      * The <code>ReadableInstant</code> is stored as the starting instant.
      * @return
      */
     public static final VerbatimTimePeriod NewVerbatimInstance(ReadableInstant readableInstant){
         return NewVerbatimInstance(readableInstant, null);
     }

     /**
      * Factory method to create a TimePeriod from a starting and an ending <code>Calendar</code>
      * @return
      */
     public static final VerbatimTimePeriod NewVerbatimInstance(Calendar startCalendar, Calendar endCalendar){
         Partial startDate = null;
         Partial endDate = null;
         if (startCalendar != null){
             startDate = calendarToPartial(startCalendar);
         }
         if (endCalendar != null){
             endDate = calendarToPartial(endCalendar);
         }
         return new VerbatimTimePeriod(startDate, endDate);
     }

     /**
      * Factory method to create a TimePeriod from a starting and an ending <code>Date</code>
      * @return TimePeriod
      */
     public static final VerbatimTimePeriod NewVerbatimInstance(Date startDate, Date endDate){
         //TODO conversion untested, implemented according to http://www.roseindia.net/java/java-conversion/datetocalender.shtml
         Calendar calStart = null;
         Calendar calEnd = null;
         if (startDate != null){
             calStart = Calendar.getInstance();
             calStart.setTime(startDate);
         }
         if (endDate != null){
             calEnd = Calendar.getInstance();
             calEnd.setTime(endDate);
         }
         return NewVerbatimInstance(calStart, calEnd);
     }


     /**
      * Factory method to create a TimePeriod from a starting and an ending <code>ReadableInstant</code>(e.g. <code>DateTime</code>)
      * @return
      */
     public static final VerbatimTimePeriod NewVerbatimInstance(ReadableInstant startInstant, ReadableInstant endInstant){
         Partial startDate = null;
         Partial endDate = null;
         if (startInstant != null){
             startDate = readableInstantToPartial(startInstant);
         }
         if (endInstant != null){
             endDate = readableInstantToPartial(endInstant);
         }
         return new VerbatimTimePeriod(startDate, endDate);
     }


//*********************** CONSTRUCTOR *********************************/

    /**
     * Constructor
     */
    protected VerbatimTimePeriod() {
        super();
    }
    public VerbatimTimePeriod(Partial startDate) {
        super(startDate);
    }
    public VerbatimTimePeriod(Partial startDate, Partial endDate) {
        super(startDate, endDate);
    }
    public VerbatimTimePeriod(Partial startDate, Partial endDate, String verbatimDate) {
        super(startDate, endDate);
        this.verbatimDate = verbatimDate;
    }

// ***************************** GETTER /SETTER *********************/

    public String getVerbatimDate() {
        return verbatimDate;
    }
    public void setVerbatimDate(String verbatimDate) {
        this.verbatimDate = verbatimDate;
    }

//****************** CONVERTERS ******************/

    public static TimePeriod fromVerbatim(VerbatimTimePeriod verbatimTimePeriod){
        if (verbatimTimePeriod == null){
            return null;
        }
        TimePeriod result = TimePeriod.NewInstance();
        copyCloned(verbatimTimePeriod, result);
        if (StringUtils.isNotBlank(verbatimTimePeriod.verbatimDate) &&
                StringUtils.isBlank(result.getFreeText())){
            result.setFreeText(verbatimTimePeriod.toString());
        }
        return result;
    }
    public static VerbatimTimePeriod toVerbatim(TimePeriod timePeriod){
        if (timePeriod == null){
            return null;
        }
        VerbatimTimePeriod result = VerbatimTimePeriod.NewVerbatimInstance();
        copyCloned(timePeriod, result);
        return result;
    }



// ************************************ TRANSIENT **************************/

    @Override
    /**
     * True, if there is no start date and no end date and no freetext representation exists.
     * @return
     */
    @Transient
    public boolean isEmpty(){
        boolean result = super.isEmpty();
        return result && StringUtils.isBlank(this.getVerbatimDate());
    }

//*********** EQUALS **********************************/

    //we want VerbatimTimePeriod and TimePeriod to be equals
    //if both are equal in the TimePeriod part and if
    //VerbatimTimePeriod has no verbatimDate defined

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }


    @Override
    public int hashCode() {
        int hashCode = super.hashCode();
        hashCode += (verbatimDate == null)? 0: verbatimDate.hashCode();
        return hashCode;
    }



//*********** CLONE **********************************/

    @Override
    public Object clone()  {
            VerbatimTimePeriod result = (VerbatimTimePeriod)super.clone();
            result.setVerbatimDate(this.verbatimDate);
            return result;
    }
}
