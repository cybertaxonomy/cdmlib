/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */


package eu.etaxonomy.cdm.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;


public class AccountStore {

    private static Logger logger = Logger.getLogger(AccountStore.class);

    public final static File accountsFile = new File(CdmUtils.perUserCdmFolder + File.separator + ".dbaccounts.properties");

    public static String getAccountsFileName(){
        try {
            return accountsFile.getCanonicalPath();
        } catch (IOException e) {
            logger.warn(e);
            return "AN ERROR OCCURRED WHEN RETRIEVING THE FILE NAME";
        }
    }

    public String getPassword(String dbms, String strServer, String userName){
        String pwd = null;

        Properties accounts = loadAccounts();
        String key = strServer+'.'+dbms+'.'+userName;
        pwd = accounts.getProperty(key);
        return pwd;
    }

    private Properties loadAccounts() {
        Properties accounts = new Properties();
        try {
            accountsFile.createNewFile();

            // this gives me errors. Properties object doesn't have a method
            // with this signature
            //FileReader in = new FileReader(accountsFile);
            //accounts.load(in);
            //in.close();

            FileInputStream inStream = new FileInputStream(accountsFile);
            accounts.load(inStream);
            inStream.close();

        } catch (IOException e) {
            logger.error(e);
        }
        return accounts;
    }

    public void setPassword(String dbms, String strServer, String userName, String pwd){
        Properties accounts = loadAccounts();
        String key = strServer+'.'+dbms+'.'+userName;
        accounts.setProperty(key, pwd);
        saveAccounts(accounts);
    }

    private void saveAccounts(Properties accounts) {
        //FileWriter out;
        FileOutputStream outStream;
        try {
            //out = new FileWriter(accountsFile);
            //accounts.store(out, "");
            //out.close();
            outStream = new FileOutputStream(accountsFile);
            accounts.store(outStream, "");
            outStream.close();
        } catch (FileNotFoundException e) {
            logger.error("File " + accountsFile.toString() + " not found", e);
        } catch (IOException e) {
            logger.error("Unable to write properties", e);
        }
    }

    public void removePassword(String dbms, String strServer, String userName){
        String key = strServer+'.'+dbms+'.'+userName;
        Properties accounts = loadAccounts();
        accounts.remove(key);
        saveAccounts(accounts);
    }

    public static String readOrStorePassword(String dbms, String strServer, String userName, String pwd){
        AccountStore accounts = new AccountStore();
        boolean doStore = false;
        try {
            if (pwd == null){
                pwd = accounts.getPassword(dbms, strServer, userName);
                if(pwd == null){
                    doStore = true;
                    pwd = CdmUtils.readInputLine("Please insert password for user '" + CdmUtils.Nz(userName) + "': ");
                } else {
                    logger.info("using stored password for user '" + CdmUtils.Nz(userName) + "'");
                }
            }
            // on success store userName, pwd in property file
            if(doStore){
                accounts.setPassword(dbms, strServer, userName, pwd);
                logger.info("password stored in " + AccountStore.getAccountsFileName());
            }
        } catch (Exception e) {
            if(doStore){
                accounts.removePassword(dbms, strServer, userName);
                logger.info("password removed from " + AccountStore.getAccountsFileName());
            }
            logger.error(e);
            return null;
        }
        return pwd;
    }

    public static void main(String[] args) {
        AccountStore a = new AccountStore();
//		BerlinModel - EditWp6
        String dbms = "SQLServer";
        String strServer = "BGBM111";
        String userName = "webUser";
        a.setPassword(dbms, strServer, userName, "psst");
        logger.info(a.getPassword(dbms, strServer, userName));
        a.removePassword(dbms, strServer, userName);
        logger.info(a.getPassword(dbms, strServer, userName));

    }
}
