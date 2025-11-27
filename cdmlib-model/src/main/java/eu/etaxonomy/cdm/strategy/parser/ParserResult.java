/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.parser;

import eu.etaxonomy.cdm.common.ResultBase;

/**
 * @see IoResultBase
 * @author muellera
 * @since 27.10.2025
 */
public class ParserResult<E extends Object> extends ResultBase<ParserResult<E>.ParserInfo> {

    //not yet really used
//    private ParserResultState state;

    private E entity;

//    public enum ParserResultState{
//        SUCCESS,               //All data parsed
//        SUCCESS_WITH_WARNING,   //All data parsed but with some warnings
//        FINISHED_WITH_ERROR,    //Probably all data parsed but with errors
//        ABORTED,                //An handled exception occurred that lead to abort the parsing
//        ;
//    }

    public class ParserInfo extends ResultBase.ResultInfoBase{
        private static final long serialVersionUID = -7429259794408467489L;

        public ParserInfo(String msg, Exception e, String codeLocation) {
            super(msg, e, codeLocation);
        }

    }

 // ************* GETTERS / SETTERS / ADDERS ***********************/

//    public void addException(Exception e, String message, String codeLocation) {
//        exceptions.add(new ParserInfo(message, e, makeLocation(e, codeLocation)));
//        setExceptionState();
//    }

    public E getEntity() {
        return entity;
    }
    public void setEntity(E entity) {
        this.entity = entity;
    }

   //**************************************************

    @Override
    protected ParserInfo newResultInfo(String message, Exception e, String codeLocation) {
        ParserInfo result = new ParserInfo(message, e, codeLocation);
        return result;
    }

    //not yet really used
//    protected void setExceptionState() {
//        state = ParserResultState.FINISHED_WITH_ERROR;
//    }
}