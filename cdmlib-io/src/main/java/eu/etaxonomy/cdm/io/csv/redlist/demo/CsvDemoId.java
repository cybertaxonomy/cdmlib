/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.csv.redlist.demo;

import java.net.URI;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.LSID;

/**
 * @author a.mueller
 \* @since 29.04.2011
 *
 */
public class CsvDemoId {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CsvDemoId.class);

	private Integer intId;
	private String strId;
	private UUID uuidId;
	private URI uriId;
	private LSID lsidId;
	
	private CsvDemoExportConfigurator config;
	
	public CsvDemoId(CsvDemoExportConfigurator config){
		this.config = config;
	}
	
	
	public void setId(Integer id){
		this.intId = id;
	}
	public void setId(UUID uuid){
		this.uuidId = uuid;
	}
	public void setId(LSID lsid){
		this.lsidId = lsid;
	}
	

	public void setId(CdmBase cdmBase) {
		this.setId(cdmBase.getId());
		this.setId(cdmBase.getUuid());
		if (cdmBase.isInstanceOf(IdentifiableEntity.class)){
			this.setId(CdmBase.deproxy(cdmBase,IdentifiableEntity.class).getLsid());
		}
	}
	
	public String getId(){
		Object object;
		if (config.isUseIdWherePossible()){
			object = intId;
		}else if (lsidId != null){
			object = lsidId;
		}else if (uriId != null){
			object = uriId;
		}else if(uuidId != null){
			object = uuidId;
		}else if(intId != null){
			object = intId;
		}else{
			object = strId;
		}
		return nullSafe(object);
	}
	
	private String nullSafe(Object o){
		return o == null ? null : o.toString();
	}

	
}
