package eu.etaxonomy.cdm.io.csv.caryophyllales.out;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

public class NameRecord {
	private HashMap<String,String> record;
	private boolean isFirst;


	public NameRecord(HashMap<String,String> record, boolean isFirst){
		record = record;
		isFirst = isFirst;
	}


protected void print(PrintWriter writer, CsvNameExportConfigurator config) {
	String strToPrint ="";
	if (isFirst){
		for (String valueName:record.keySet()){
			strToPrint+=valueName;
		}
		strToPrint.concat(config.getLinesTerminatedBy());
	}
	if (!record.isEmpty() ){
		//Replace quotes by double quotes
		String value ;
		Iterator<String> it = record.values().iterator();
			while (it.hasNext()){
				value = it.next();
				value = value.replace("\"", "\"\"");

				value = value.replace(config.getLinesTerminatedBy(), "\\r");

				//replace all line brakes according to best practices: http://code.google.com/p/gbif-ecat/wiki/BestPractices
				value = value.replace("\r\n", "\\r");
				value = value.replace("\r", "\\r");
				value = value.replace("\n", "\\r");

				strToPrint += config.getFieldsEnclosedBy() + value + config.getFieldsEnclosedBy();
		}
			strToPrint.concat(config.getLinesTerminatedBy());
			writer.print(strToPrint);
	}


}


public HashMap<String,String> getRecord() {
	return record;
}


public void setRecord(HashMap<String,String> record) {
	this.record = record;
}


public boolean isFirst() {
	return isFirst;
}


public void setFirst(boolean isFirst) {
	this.isFirst = isFirst;
}

}
