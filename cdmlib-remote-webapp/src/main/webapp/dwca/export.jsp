<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script type="text/javascript" src="../js/jquery-1.7.2.min.js"></script>
<script type="text/javascript"
	src="../js/jquery-ui-1.8.23.custom.min.js"></script>

<script src="http://code.jquery.com/jquery-1.8.2.js"></script>
<script src="http://code.jquery.com/ui/1.9.0/jquery-ui.js"></script>
<link rel="stylesheet"
	href="http://code.jquery.com/ui/1.9.0/themes/base/jquery-ui.css" />

<link type="text/css" href="../css/jquery-ui-1.8.23.custom.css"
	rel="stylesheet" />

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>CDM Remote API</title>
<script>
$('.showall').click(function() {
	$(this).prev().focus().autocomplete("search", "", { delay: 0 });
});

(function( $ ) {
    $.widget( "ui.combobox", {
        _create: function() {
            var input,
                that = this,
                select = this.element.hide(),
                selected = select.children( ":selected" ),
                value = selected.val() ? selected.text() : "",
                wrapper = this.wrapper = $( "<span>" )
                    .addClass( "ui-combobox" )
                    .insertAfter( select );

            function removeIfInvalid(element) {
                var value = $( element ).val(),
                    matcher = new RegExp( "^" + $.ui.autocomplete.escapeRegex( value ) + "$", "i" ),
                    valid = false;
                select.children( "option" ).each(function() {
                    if ( $( this ).text().match( matcher ) ) {
                        this.selected = valid = true;
                        return false;
                    }
                });
                if ( !valid ) {
                    // remove invalid value, as it didn't match anything
                    $( element )
                        .val( "" )
                        .attr( "title", value + " didn't match any item" )
                        .tooltip( "open" );
                    select.val( "" );
                    setTimeout(function() {
                        input.tooltip( "close" ).attr( "title", "" );
                    }, 2500 );
                    input.data( "autocomplete" ).term = "";
                    return false;
                }
            }

            input = $( "<input>" )
                .appendTo( wrapper )
                .val( value )
                .attr( "title", "" )
                .addClass( "ui-state-default ui-combobox-input" )
                .autocomplete({
                    delay: 0,
                    minLength: 0,
                    source: function( request, response ) {
                        var matcher = new RegExp( $.ui.autocomplete.escapeRegex(request.term), "i" );
                        response( select.children( "option" ).map(function() {
                            var text = $( this ).text();
                            if ( this.value && ( !request.term || matcher.test(text) ) )
                                return {
                                    label: text.replace(
                                        new RegExp(
                                            "(?![^&;]+;)(?!<[^<>]*)(" +
                                            $.ui.autocomplete.escapeRegex(request.term) +
                                            ")(?![^<>]*>)(?![^&;]+;)", "gi"
                                        ), "<strong>$1</strong>" ),
                                    value: text,
                                    option: this
                                };
                        }) );
                    },
                    select: function( event, ui ) {
                        ui.item.option.selected = true;
                        that._trigger( "selected", event, {
                            item: ui.item.option
                        });
                    },
                    change: function( event, ui ) {
                        if ( !ui.item )
                            return removeIfInvalid( this );
                    }
                })
                .addClass( "ui-widget ui-widget-content ui-corner-left" );

            input.data( "autocomplete" )._renderItem = function( ul, item ) {
                return $( "<li>" )
                    .data( "item.autocomplete", item )
                    .append( "<a>" + item.label + "</a>" )
                    .appendTo( ul );
            };

            $( "<a>" )
                .attr( "tabIndex", -1 )
                .attr( "title", "Show All Items" )
                .tooltip()
                .appendTo( wrapper )
                .button({
                    icons: {
                        primary: "ui-icon-triangle-1-s"
                    },
                    text: false
                })
                .removeClass( "ui-corner-all" )
                .addClass( "ui-corner-right ui-combobox-toggle" )
                .click(function() {
                    // close if already visible
                    if ( input.autocomplete( "widget" ).is( ":visible" ) ) {
                        input.autocomplete( "close" );
                        removeIfInvalid( input );
                        return;
                    }

                    // work around a bug (likely same cause as #5265)
                    $( this ).blur();

                    // pass empty string as value to search for, displaying all results
                    input.autocomplete( "search", "" );
                    input.focus();
                });

                input
                    .tooltip({
                        position: {
                            of: this.button
                        },
                        tooltipClass: "ui-state-highlight"
                    });
        },

        destroy: function() {
            this.wrapper.remove();
            this.element.show();
            $.Widget.prototype.destroy.call( this );
        }
    });
})( jQuery );

$(function() {
    $( "#combobox" ).combobox();
    $( "#toggle" ).click(function() {
        $( "#combobox" ).toggle();
    });
});
</script>

<script>
  $(document).ready(function () {
  
  
  var o = new Option("", "");
    $(o).html("");
    $("#combobox").append(o);
	$.getJSON('../classification.json', function(data) {
		var count = data.count;
		for(i=0;i < count ; i++){
			var o = new Option(data.records[i].titleCache, data.records[i].uuid);
			$(o).html(this.titleCache);
			$("#combobox").append(o);
			
		}
	});
});


  </script>
<style type="text/css">
/*demo page css*/
body {
	font: 62.5% "Trebuchet MS", sans-serif;
	margin: 50px;
}

.demoHeaders {
	margin-top: 2em;
}

#dialog_link {
	padding: .4em 1em .4em 20px;
	text-decoration: none;
	position: relative;
}

#dialog_link span.ui-icon {
	margin: 0 5px 0 0;
	position: absolute;
	left: .2em;
	top: 50%;
	margin-top: -8px;
}

ul#icons {
	margin: 0;
	padding: 0;
}

ul#icons li {
	margin: 2px;
	position: relative;
	padding: 4px 0;
	cursor: pointer;
	float: left;
	list-style: none;
}

ul#icons span.ui-icon {
	float: left;
	margin: 0 4px;
}

.ui-combobox {
	position: relative;
	display: inline-block;
}

.ui-combobox-toggle {
	position: absolute;
	top: 0;
	bottom: 0;
	margin-left: -1px;
	padding: 0;
	/* adjust styles for IE 6/7 */
	*height: 1.7em;
	*top: 0.1em;
}

.ui-combobox-input {
	margin: 0;
	padding: 0.3em;
}
</style>

</head>
<body>
	<h1>Export TSV Page</h1>
	<form method="post" action="getDB">
		<div class="ui-widget">
			<label for="combobox">Classification</label> <select id="combobox" name="combobox" value=""
				title="type &quot;*&quot; to retrieve all entries"></select>
			<!--  <span class="showall ui-icon ui-icon-triangle-1-s" title="Show all">Show all </span> -->
		</div>
		<table id="dwcaOptions">
			<tr>
				<td>Download Options:</td>

			<tr>
				<td><input type="checkbox" name="dlOptions"
					value="setRl1996"> Rote Liste Status 1996</td>
			</tr>
			<tr>
				<td><input type="checkbox" name="dlOptions"
					value="setRl2013"> Rote Liste Status 2013</td>
			</tr>
		 	<tr>
				<td colspan="2" align="center"><input type="submit"
					value="Submit" onclick=""></td>
			</tr> 
		</table>
	</form>
</body>
</html>