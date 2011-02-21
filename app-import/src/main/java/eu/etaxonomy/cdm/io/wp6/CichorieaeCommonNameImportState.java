package eu.etaxonomy.cdm.io.wp6;

import eu.etaxonomy.cdm.io.excel.common.ExcelImportState;

public class CichorieaeCommonNameImportState extends ExcelImportState<CommonNameImportConfigurator> {
	CommonNameRow row;
	
	public CichorieaeCommonNameImportState(CommonNameImportConfigurator config) {
		super(config);
	}

	public void setCommonNameRow(CommonNameRow row) {
		this.row = row;
	}
	
	public CommonNameRow getCommonNameRow(){
		return row;
	}

}
