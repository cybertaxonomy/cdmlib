package eu.etaxonomy.cdm.app.validate;

import java.util.List;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.test.ValidateForeignKeys;
import eu.etaxonomy.cdm.database.update.test.ValidateForeignKeys.FkTestResult;

public class ValidateKeys {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ICdmDataSource source = CdmDestinations.cdm_cichorieae_preview();
//		String server = "160.45.63.";
//		String database = "";
//		int port = 3306;
//		String username = "edit";
//		String pwd = "";
//		DataSource source = CdmDataSource.NewInstance(DatabaseTypeEnum.MySQL, server, database, port, username, pwd);
		ValidateForeignKeys m = new ValidateForeignKeys(source);
		List<FkTestResult> list = m.invoke();
		for (FkTestResult re : list){
			String format = "Table %s, field %s, foreignTable %s, id %d";
			System.out.println(String.format(format, re.table, re.field, re.foreignTable, re.id));
		}
	}
}
