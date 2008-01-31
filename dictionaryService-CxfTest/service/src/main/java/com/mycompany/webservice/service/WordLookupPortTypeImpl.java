package com.mycompany.webservice.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.example.wordlookup.BasicFault;
import org.example.wordlookup.EntryAlreadyExistsFault;
import org.example.wordlookup.EntryNotFoundFault;
import org.example.wordlookup.LookupEJ;
import org.example.wordlookup.LookupJE;
import org.example.wordlookup.LookupResponse;
import org.example.wordlookup.WordLookupPortType;

@javax.jws.WebService(portName = "WordLookupPort", serviceName = "WordLookupService", 
        targetNamespace = "http://www.example.org/WordLookup", 
        endpointInterface="org.example.wordlookup.WordLookupPortType")
public class WordLookupPortTypeImpl implements WordLookupPortType {

    private static Connection dbConn;    
    
    static {
        try {
            DriverManager.registerDriver(
                    new com.mysql.jdbc.Driver());

            dbConn = DriverManager.getConnection( 
                "jdbc:mysql://localhost/LangLookup",    
                "root", ""); 
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalStateException(
                "Database connection cannot be made: " +
                "Code: " + e.getErrorCode() + "; Message: " + e.getMessage());
        }
    }
    
    public String addWordEntry(String enWord, String jaWord)
            throws EntryAlreadyExistsFault {
        
        try {
            Statement stmt = dbConn.createStatement();
            stmt.execute("insert into wordlookup(en_word, ja_word) values " +
                    "('" + enWord.toUpperCase() + "', '" 
                    + jaWord.toUpperCase() + "')");
            dbConn.commit();
        } catch (SQLException e) {
            BasicFault bf = new BasicFault();
            bf.setErrorMessage("Cannot add to dictionary: " +
            		"one or both words already exist");
            bf.setErrorDetails("Attempted to add: (" + enWord +
                    ", " + jaWord + ")");
            
            throw new EntryAlreadyExistsFault("SQL Error -- Code: " 
               + e.getErrorCode(), bf);
        }
        return "Successful!";
    }

    public LookupResponse enToJaLookup(LookupEJ parameters)
            throws EntryNotFoundFault {
        return dictionaryLookup(parameters.getWordToTranslate(),
                "JA");
    }

    public LookupResponse jaToEnLookup(LookupJE parameters)
            throws EntryNotFoundFault {
        return dictionaryLookup(parameters.getWordToTranslate(),
                "EN");
    }
    
    private LookupResponse dictionaryLookup(String inWord,
            String toLang) throws EntryNotFoundFault {

        String outWord = null;

        String sqlStr = ("JA".equals(toLang)) ?
            "SELECT ja_word as out_word " +
            "from APP.wordlookup where en_word = UPPER(?)"
        :
            "SELECT en_word as out_word " +
            "from APP.wordlookup where ja_word = UPPER(?)";
        
        String notFoundMsg = ("JA".equals(toLang) ? "Japanese" : "English") +
            " word for \"" + inWord + "\" could not be found";
        
        try {
            PreparedStatement pstmt = dbConn.prepareStatement(sqlStr);
            pstmt.setString(1, inWord);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                outWord = rs.getString("out_word");
            } else {
                BasicFault bf = new BasicFault();
                bf.setErrorMessage(notFoundMsg);
                bf.setErrorDetails(inWord);
                throw new EntryNotFoundFault("Entry Not Found", bf);
            }
            rs.close();
            pstmt.close();

        } catch (SQLException e) {
            BasicFault bf = new BasicFault();
            bf.setErrorMessage(notFoundMsg);
            bf.setErrorDetails(inWord);
            throw new EntryNotFoundFault(e.getMessage(), bf);
        }
        LookupResponse lr = new LookupResponse();
        lr.setTranslatedWord(outWord);
        return lr;
    }
}
