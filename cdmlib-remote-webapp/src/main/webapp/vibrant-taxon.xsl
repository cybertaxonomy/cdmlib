<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <!-- pass this in as a parameter? -->

    <xsl:variable name="url_webservice">http://localhost:8080/taxon/findTaxaAndNames</xsl:variable>
    <xsl:variable name="url_start">"http://localhost:8080/taxon/</xsl:variable>
    <xsl:variable name="url_end">/extensions.json"</xsl:variable>


    <xsl:template match="/DefaultPagerImpl">
        <HTML>

            <HEAD>
                <script type="text/javascript" src="http://code.jquery.com/jquery-latest.js"/>
                <link type="text/css" rel="stylesheet" media="all" href="/vibrant.css"/>
            </HEAD>
            <BODY>


                <script type="text/javascript" language="javascript">               

        //function theCall(jsonurl) {
        $(document).ready(function() {
        
$('.source-url').each(function(index) {
    //alert( "SOURCE URL " + $('.source-url').attr('ref'));
            
            var fullurl = 'http://localhost:8080/taxon/' + $('.source-url').attr('ref') + '/extensions.json';
            $.ajax({
                url: fullurl,
                dataType: "jsonp",
                async: false, // only for debugging, let's you see more error messages
                cache: false, // browser will not cache
                success: function(data) {
                    
                    var outString = '';
                    var dataRecords = data.records;
            
                    $.each(data.records, function(i,record){
                    
                    var source_url = record.value;
                    var ch = 'http';
                    if ( source_url.indexOf(ch) === 0 ) {
                        //alert(source_url);
                        $('#content').append(source_url);
                        $("<a/>").attr("href", source_url).appendTo('#linktosource');
                
                        }
                    });
                    },
                error: function(XMLHttpRequest, statusText, errorThrown){
                                     $("#content").html("ERROR: " + statusText + " - " + errorThrown);
                             }
            });

  });           
 });
         
            </script>



                <a href="/vibrant_names.html" title="Home">
                    <img src="http://localhost:8080/acquia_prosper_logo.png" alt="Home"/>
                </a>
                <br/>
                <table cellpadding="1" width="100%">
                    <tr>
                        <td width="5%"/>
                        <td valign="top">

                            <h2>ViBRANT Common Data Model names search</h2>
                            <br/>
                            <BR/>
                            <H4>Source(s) using this name: </H4>
                            <br/>


                            <xsl:for-each select="records/e">
                                <table width="35%" border="1" cellspacing="4" cellpadding="0"
                                    bordercolor="#666666" style="border-collapse: collapse">

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
                                                  <xsl:variable name="taxonclass">
                                                  <xsl:value-of select="class"/>
                                                  </xsl:variable>
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
                                            <TR>
                                                <TD>
                                                  <strong>UUID: </strong>
                                                </TD>
                                                <TD>
                                                  <xsl:apply-templates select="uuid"/>
                                                </TD>
                                            </TR>
                                            <TR>
                                                <TD> </TD>
                                                <xsl:variable name="uuid">
                                                    <xsl:value-of select="uuid"/>
                                                </xsl:variable>
                                                <TD class="source-url" ref="{uuid}">

                                                    <xsl:apply-templates select="uuid"/>
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
                                </table>
                                <br/>
                            </xsl:for-each>
                        </td>
                    </tr>
                </table>
            </BODY>
        </HTML>
    </xsl:template>


    <!--xsl:template match=".">
        <xsl:value-of select="text()"/>
    </xsl:template-->

</xsl:stylesheet>
