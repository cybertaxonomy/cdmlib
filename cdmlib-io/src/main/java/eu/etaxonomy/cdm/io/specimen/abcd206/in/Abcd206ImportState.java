// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.specimen.SpecimenImportStateBase;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;

/**
 * @author a.mueller
 * @created 11.05.2009
 * @version 1.0
 */
public class Abcd206ImportState extends SpecimenImportStateBase<Abcd206ImportConfigurator<Abcd206ImportState, InputStream>, Abcd206ImportState>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(Abcd206ImportState.class);


	private String prefix;
	MapWrapper<TeamOrPersonBase<?>> personStore;

	private List<String> associatedUnitIds = new ArrayList<String>();




	//private Abcd206DataHolder dataHolder;





//****************** CONSTRUCTOR ***************************************************/

	/**
     * @return the personStore
     */
    public MapWrapper<TeamOrPersonBase<?>> getPersonStore() {
        return personStore;
    }

    /**
     * @param personStore the personStore to set
     */
    public void setPersonStore(MapWrapper<TeamOrPersonBase<?>> personStore) {
        this.personStore = personStore;
    }

    public Abcd206ImportState(Abcd206ImportConfigurator config) {
		super(config);
        setReport(new SpecimenImportReport());
        setTransformer(new AbcdTransformer());
	}

//************************ GETTER / SETTER *****************************************/



    @Override
    public Abcd206DataHolder getDataHolder() {
        return (Abcd206DataHolder)dataHolder;
    }

    public void setDataHolder(Abcd206DataHolder dataHolder) {
        this.dataHolder = dataHolder;
    }







    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }


    @Override
    public byte[] getReportAsByteArray() {
        ByteArrayOutputStream importStream = new ByteArrayOutputStream();
        getReport().printReport(new PrintStream(importStream));
        return importStream.toByteArray();
    }

    public void setAssociatedUnitIds(List<String> associatedUnitIds){
        this.associatedUnitIds = associatedUnitIds;
    }

    public List<String> getAssociatedUnitIds(){
        return this.associatedUnitIds;
    }

//
//    public void reset() {
//        getDataHolder().reset();
//        setDerivedUnitBase(null);
//    }
}
