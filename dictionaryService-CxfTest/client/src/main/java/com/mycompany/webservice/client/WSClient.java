package com.mycompany.webservice.client;

import org.example.wordlookup.BasicFault;
import org.example.wordlookup.EntryAlreadyExistsFault;
import org.example.wordlookup.EntryNotFoundFault;
import org.example.wordlookup.LookupEJ;
import org.example.wordlookup.LookupJE;
import org.example.wordlookup.LookupResponse;
import org.example.wordlookup.WordLookupPortType;
import org.example.wordlookup.WordLookupService;

public class WSClient {
    public static void main (String[] args) {
        WordLookupService ss = new WordLookupService();
        WordLookupPortType port = ss.getWordLookupPort();
        
        // lookup features for words already in DB
        dictionaryLookup(port, "elephant", "JA");
        dictionaryLookup(port, "rakuda", "EN");
        
        // lookup words not yet in DB
        dictionaryLookup(port, "fox", "JA");
        dictionaryLookup(port, "kitsune", "EN");

        // adding word pair to dictionary
        addWordEntry(port, "fox", "kitsune");
        
        // words should be found now
        dictionaryLookup(port, "fox", "JA");
        dictionaryLookup(port, "kitsune", "EN");
        
    }  
    
    public static void dictionaryLookup (WordLookupPortType port, 
            String inWord, String target) {
        try {
            if ("EN".equals(target)) {
                LookupJE lookup = new LookupJE();
                lookup.setWordToTranslate(inWord);
                LookupResponse resp = port.jaToEnLookup(lookup);
                System.out.println("Japanese \"" + lookup.getWordToTranslate()
                   + "\" is English \"" 
                   + resp.getTranslatedWord().toLowerCase() + "\"");
            } else {
                LookupEJ lookup = new LookupEJ();
                lookup.setWordToTranslate(inWord);
                LookupResponse resp = port.enToJaLookup(lookup);
                System.out.println("English \"" + lookup.getWordToTranslate()
                   + "\" is Japanese \"" 
                   + resp.getTranslatedWord().toLowerCase() + "\"");
            }
        } catch (EntryNotFoundFault e) {
            System.out.println("Exception: " +  e.getMessage());
            BasicFault bf = e.getFaultInfo();
            System.out.println(bf.getErrorMessage());
        }        
    }
    
    public static void addWordEntry (WordLookupPortType port, 
            String enWord, String jaWord) {
        try {
            String result = port.addWordEntry(enWord, jaWord);
            System.out.println("Result of adding {" + enWord + ", " + jaWord +
                    "} to dictionary is: " + result);
        } catch (EntryAlreadyExistsFault e) {
            System.out.println("Exception: " +  e.getMessage());
            BasicFault bf = e.getFaultInfo();
            System.out.println(bf.getErrorMessage());
            System.out.println(bf.getErrorDetails());
        }        
    }
}
