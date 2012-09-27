<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <xsl:param name="query"/>
    <!-- pass this in as a parameter? -->

    <!--xsl:variable name="url_webservice">../taxon/findTaxaAndNames</xsl:variable-->
    <xsl:variable name="url_webservice">../taxon/findByEverythingFullText</xsl:variable>
    
    <xsl:variable name="url_start">"../taxon/</xsl:variable>
    <xsl:variable name="url_end">/extensions.json"</xsl:variable>


    <xsl:template match="/DefaultPagerImpl">
        <HTML>

            <HEAD>
                <script type="text/javascript" src="http://code.jquery.com/jquery-latest.js"/>
                <link type="text/css" rel="stylesheet" media="all" href="../vibrant.css"/>
            </HEAD>
            <BODY>
                <script type="text/javascript" language="javascript">
                    
                    (function($){
	                    $.getQuery = function( query ) {
                  		query = query.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
                  		var expr = "[\\?&amp;]"+query+"=([^&amp;#]*)";
                  		var regex = new RegExp( expr );
                  		var results = regex.exec( window.location.href );
                  		if( results !== null ) {
                  			return results[1];
                  			return decodeURIComponent(results[1].replace(/\+/g, " "));
                  		} else {
                  			return false;
        		        }
	           };
               })(jQuery);

                    $(document).ready(function() {
            
                    $('.source-url').each(function(index) {
                        
                        var fullurl = '../taxon/' + $(this).attr('ref') + '/extensions.json';
                        //$(this).html(fullurl);
                        var source_url = location.href;
                        
                        //alert($(this).html); 
                        // this is td class="source-url" ref="c51e9c56-3761-4e3b-aef6-db446fa8748c"
                                                        //console.log(this);
                                                        
                        $.ajax({
                            url: fullurl,
                            dataType: "jsonp",
                            async: false, // only for debugging, let's you see more error messages
                            cache: false, // browser will not cache

                                                      
                            success: function(data) {
                                                          
                                var dataRecords = data.records;
                                //alert($(this).html(data.pageSize)); 
                                //the data is the default pager object for the extensions
                                //console.log(data);
                        
                                $.each(data.records, function(i,record){
                                
                                var value = record.value;
                                var ch = 'http';
                                if ( value.indexOf(ch) === 0 ) {
            
                                    source_url = value;
                                    }
                                });
                                },
                            error: function(XMLHttpRequest, statusText, errorThrown){$("#content").html("ERROR: " + statusText + " - " + errorThrown);}                                                     
                        });
            
            //alert(source_url); 
                        //add a href to the source URL to the html table cell element
                        //$(this).html('&lt;a href="' + source_url + '"&gt;' + source_url + '&lt;/a&gt;');            
                        $(this).html('&lt;a href="' + source_url + '"&gt;View summary in source database&lt;/a&gt;');                       
                        });
                      
                     });
            
                    function open(url)
                    {window.open(url);}
  
  (function($){
	$.getQuery = function( query ) {
		query = query.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
		var expr = "[\\?&amp;]"+query+"=([^&amp;#]*)";
		var regex = new RegExp( expr );
		var results = regex.exec( window.location.href );
		if( results !== null ) {
			return results[1];
			return decodeURIComponent(results[1].replace(/\+/g, " "));
		} else {
			return false;
		}
	};
})(jQuery);
 
// Document load
$(document).ready(function() {

//$('#content').append('Showing page ' + parseInt($.getQuery('pageNumber') + 1) + ':');
	var test_query = $.getQuery('query');

$('.page-url').each(function(index) {
	//alert(test_query); 
	//$('#content').append(test_query);
	var displayPage = parseInt($(this).attr('ref')) + 1;
	
    $(this).html('&lt;a href="../taxon/findByEverythingFullText?query=' + test_query + '&amp;hl=1&amp;pageNumber=' + $(this).attr('ref') + '"&gt;' + displayPage + '&lt;/a&gt;');

});
});
            </script>


                <a href="../vibrant_names.html" title="Home">
                    <img src="../acquia_prosper_logo.png" alt="Home"/>
                </a>
                <br/>
                <table cellpadding="1" width="100%">
                    <tr>
                        <td width="5%"/>
                        <td valign="top">

                            <h2>ViBRANT index Common Data Model search</h2>
                            <br/>
                            <BR/>
                            <H4>Source(s) using this search term: </H4>
                            <br/>


                            <xsl:for-each select="records/e">
                                <table width="35%" border="1" cellspacing="4" cellpadding="0"
                                    bordercolor="#66C266" style="border-collapse: collapse">

                                    <xsl:variable name="taxonclass">
                                        <xsl:value-of select="class"/>
                                    </xsl:variable>
                                    <xsl:choose>
                                        <xsl:when test="$taxonclass = 'SearchResult'">
                                            <tr>
                                                <TABLE border="0">

                                                  <TR>
                                                  <td> </td>
                                                  <td/>
                                                  <TD>
                                                  <xsl:variable name="score">
                                                  <xsl:value-of select="score"/>
                                                  </xsl:variable>
                                                  <div class="bars">
                                                  <div class="bar bar-1" style="width:100%">

                                                  <div class="bar bar-2"
                                                  style="width:{$score * 100}%">
                                                  <!--div class="value"><xsl:value-of select="score"/></div-->
                                                  <div class="value">
                                                      <xsl:variable name="percentscore">
                                                          <xsl:choose>
                                                              <xsl:when test="starts-with($score, '0')">
                                                                  <xsl:value-of
                                                                      select="number(substring(substring-after($score,'.'), 1,2))"
                                                                  />%
                                                              </xsl:when>
                                                              <xsl:otherwise>
                                                                  <xsl:value-of select="100"/>%
                                                              </xsl:otherwise>
                                                          </xsl:choose>
                                                         
                                                          
                                                      </xsl:variable>
                                                      
                                                      <xsl:value-of
                                                          select="string($percentscore)"
                                                      />
                                                      <!--xsl:value-of select="translate($percentscore, '^0', '')"></xsl:value-of-->
                                                      <!--xsl:value-of select="format-number($percentscore, '#')"></xsl:value-of-->
                                                  <!--xsl:value-of select="concat('0.', substring(substring-after($score,'.'), 1,2))"-->
                                                  </div>
                                                  </div>
                                                  </div>
                                                  </div>

                                                  </TD>
                                                  </TR>
                                                  <TR>
                                                  <td/>
                                                  <td/>
                                                  </TR>
                                                  <TR>

                                                  <TD>
                                                  <strong>Name:</strong>
                                                  </TD>
                                                  <TD>
                                                  <xsl:apply-templates select="entity/titleCache"/>


                                                  </TD>
                                                  </TR>
                                                  <TR>
                                                  <TD>
                                                  <strong>Status: </strong>
                                                  </TD>
                                                  <TD>
                                                  <xsl:choose>
                                                  <xsl:when test="$taxonclass = 'Taxon'">
                                                  <font color="green">
                                                  <strong>
                                                  <xsl:apply-templates select="entity/class"/>
                                                  </strong>
                                                  </font>
                                                  </xsl:when>
                                                  <xsl:otherwise>
                                                  <font color="purple">
                                                  <strong>
                                                  <xsl:apply-templates select="entity/class"/>
                                                  </strong>
                                                  </font>
                                                  </xsl:otherwise>
                                                  </xsl:choose>
                                                  </TD>
                                                  </TR>
                                                  <TR>
                                                  <TD>
                                                  <strong>Source: </strong>
                                                  </TD>
                                                  <TD>
                                                  <xsl:apply-templates
                                                  select="entity/sec/titleCache"/>
                                                  </TD>
                                                  </TR>

                                                  <!--TR>
                                                <TD>
                                                  <strong>UUID: </strong>
                                                </TD>
                                                <TD>
                                                  <xsl:apply-templates select="uuid"/>
                                                </TD>
                                            </TR-->
                                                  <TR>
                                                  <TD><!--strong>Description: </strong-->
                                                  </TD>
                                                  <TD>
                                                      
                                                  <!--xsl:value-of select="fieldHighlightMap/text.ALL/e"/-->
                                                      
                                                      <xsl:call-template name="add-bold">
                                                          <xsl:with-param name="str" select="fieldHighlightMap/text.ALL/e"/>
                                                      </xsl:call-template>
                                                  <!--xsl:apply-templates select="fieldHighlightMap/text.ALL/e" ></xsl:apply-templates-->
                                                  </TD>
                                                  </TR>
                                                  <TR>
                                                  <TD> </TD>
                                                  <xsl:variable name="uuid">
                                                  <xsl:value-of select="entity/uuid"/>
                                                  </xsl:variable>

                                                  <TD class="source-url" ref="{$uuid}">

                                                  <a>
                                                  <xsl:attribute name="href">
                                                  <div id="content"> </div>
                                                  </xsl:attribute>
                                                  <div id="content"> </div>
                                                  </a>
                                                  </TD>
                                                  </TR>
                                                </TABLE>
                                            </tr>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <tr>
                                                <TABLE border="0">

                                                  <TR>
                                                  <TD>
                                                  <strong>Name:</strong>
                                                  </TD>
                                                  <TD>
                                                  <xsl:apply-templates select="titleCache"/>
                                                  </TD>
                                                  </TR>
                                                  <TR>
                                                  <TD>
                                                  <strong>Status: </strong>
                                                  </TD>
                                                  <TD>
                                                  <xsl:choose>
                                                  <xsl:when test="$taxonclass = 'Taxon'">
                                                  <font color="green">
                                                  <strong>
                                                  <xsl:apply-templates select="class"/>
                                                  </strong>
                                                  </font>
                                                  </xsl:when>
                                                  <xsl:otherwise>
                                                  <font color="purple">
                                                  <strong>
                                                  <xsl:apply-templates select="class"/>
                                                  </strong>
                                                  </font>
                                                  </xsl:otherwise>
                                                  </xsl:choose>
                                                  </TD>
                                                  </TR>
                                                  <TR>
                                                  <TD>
                                                  <strong>Source: </strong>
                                                  </TD>
                                                  <TD>
                                                  <xsl:apply-templates select="sec/titleCache"/>
                                                  </TD>
                                                  </TR>

                                                  <!--TR>
                                                <TD>
                                                  <strong>UUID: </strong>
                                                </TD>
                                                <TD>
                                                  <xsl:apply-templates select="uuid"/>
                                                </TD>
                                            </TR-->
                                                  <TR>
                                                  <TD> </TD>
                                                  <xsl:variable name="uuid">
                                                  <xsl:value-of select="uuid"/>
                                                  </xsl:variable>

                                                  <TD class="source-url" ref="{$uuid}">

                                                  <a>
                                                  <xsl:attribute name="href">
                                                  <div id="content"> </div>
                                                  </xsl:attribute>
                                                  <div id="content"> </div>
                                                  </a>
                                                  </TD>
                                                  </TR>
                                                </TABLE>
                                            </tr>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </table>
                                <br/>
                            </xsl:for-each>
                            <TABLE>
                                <TR>
                                    <TD>
                                        <FORM>
                                            <INPUT type="button" value="Back To Results"
                                                onClick="history.back()"/>
                                        </FORM>
                                    </TD>
                                    <TD>
                                        <!--FORM>
                                            <INPUT type="button" value="New Search" onClick="javascript:open('../vibrant_names.html');"></INPUT>
                                            <button><a href="../vibrant_names.html" title="Home"></a></button>
                                        </FORM -->
                                        <FORM METHOD="LINK" ACTION="../vibrant_names.html">
                                            <INPUT TYPE="submit" VALUE="New Search"/>
                                        </FORM>
                                    </TD>
                                </TR>
                            </TABLE>
                        </td>
                    </tr>
                    <TR>
                        <TD/>
                        <TD>
                            <xsl:apply-templates select="indices"/>
                        </TD>
                        
                    </TR>
                </table>

            </BODY>
        </HTML>
    </xsl:template>

    <xsl:template name="add-bold">
        <xsl:param name="str"/>
        <xsl:choose>
            <xsl:when test="contains($str,&quot;&lt;B&gt;&quot;)">
                <xsl:variable name="before-first-b" select="substring-before($str,&quot;&lt;B&gt;&quot;)"/>
                <xsl:variable name="inside-first-b"
                    select="substring-before(substring-after
                    ($str,'&lt;B&gt;'),'&lt;/B&gt;')"/>
                <xsl:variable name="after-first-b" select="substring-after($str,&quot;&lt;/B&gt;&quot;)"/>
                <xsl:value-of select="$before-first-b"/>

                <!--Highlight the search term in bold and red font-->
                <font color="red"><b><xsl:value-of select="$inside-first-b"/></b></font>
                <xsl:call-template name="add-bold">
                    <xsl:with-param name="str" select="$after-first-b"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$str"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
    <xsl:template match="indices">
        
        <table>
            <tr>
                
                <xsl:variable name="query_string"><xsl:value-of select="$url_webservice"/>?query=&amp;hl=1</xsl:variable>
                <!-- If there is more than one page show links to other pages -->
                <xsl:if test="count(e) > 1">              
                    <xsl:for-each select="e">                      
                        <xsl:variable name="page_number"><xsl:value-of select="."/></xsl:variable>
                        
                        <TD class="page-url" ref="{$page_number}">               
                        </TD>
                        
                    </xsl:for-each>
                </xsl:if>
                
            </tr>
        </table>
    </xsl:template>

</xsl:stylesheet>
