package eu.etaxonomy.cdm.io.common;

import java.sql.Connection;

import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.io.common.Source;

public class TestConnection {
	
	
	public static void main(String[] args){
		
		System.out.println("start");
		Source source = new Source(Source.SQL_SERVER_2008, "localhost", "PESI_v12", true);
//		Source source = new Source(Source.SQL_SERVER_2008, "(local)", "PESI_v12", "SELECT DISTINCT AreaId FROM Area");
		
//  	Source source = new Source(Source.SQL_SERVER_2008, "PESIIMPORT3", "PESI_v122", "SELECT DISTINCT AreaId FROM Area");
//		Source source = new Source(Source.SQL_SERVER_2008, "LENOVO-T61", "globis", "SELECT DISTINCT AreaId FROM Area");
		String user = "pesiexport";
		source.setUsername(user);
		String pwd = AccountStore.readOrStorePassword("SQL Server 2008", "localhost", user, null);
		source.setPassword(pwd);
		System.out.println("connect");
		Connection con = source.getConnection();
		System.out.println(con);
	}
}
