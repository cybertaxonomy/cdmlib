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

$(document).ready(function () {
	$("dialog-message").hide(); 
	$("#csvExportOptions").hide();
	var o = new Option("", "");
	var classification = getUrlVars()["classification"];
	$(o).html("");
	$("#combobox").append(o);
	$.getJSON(classification, function(data) {//'../classification.json'
		var count = data.count;
		for(i=0;i < count ; i++){
			var o = new Option(data.records[i].titleCache, data.records[i].uuid);
			$(o).html(this.titleCache);
			$("#combobox").append(o);

		}

	});
	var checkbox;
	var featureTree = getUrlVars()["featureTree"];
	$.getJSON(featureTree, function(data) {
		for(j = 0; j < data.length; j++){
			for(i=0; i < data[j].root.children.length ; i++){
				checkbox = document.createElement('input');
				checkbox.id = "checkbox"+i;
				checkbox.type = "checkbox";
				checkbox.name = "features";
				checkbox.value = data[j].root.children[i].feature.uuid;
				var description = data[j].root.children[i].feature.titleCache; 
				$("#csvExportOptions").append(checkbox);
				$("#csvExportOptions").append(document.createTextNode(description));
				$("#csvExportOptions").append("<p>");
			}
		}
		$("#csvExportOptions").show(); 
	});
});


function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}


function validateForm()
{
	var x=document.forms["exportForm"]["combobox"].selectedIndex;
	if (x==null || x==-1 || x == "")
	{
		 $( "#dialog-message" ).show();
	        $( "#dialog-message" ).dialog({
	            modal: true,
	            buttons: {
	                Ok: function() {
	                    $( this ).dialog( "close" );
	                    $( "#dialog-message" ).hide();
	                }
	            }
	        });
		$('#comboboxWidget').addClass("error");
		return false;
	}
	blockUIForDownload();
}

var fileDownloadCheckTimer;
function blockUIForDownload() {
  var token = '1234'; //use the current timestamp as the token value
  $('#downloadTokenValueId').val(token);
  $.blockUI( { message:'<h1><img src="../css/jquery-ui/images/ajax-loader.png">Please wait...</h1>'});
  fileDownloadCheckTimer = window.setInterval(function () {
    var cookieValue = $.cookie('fileDownloadToken');
    if (cookieValue == token)
     finishDownload();
  }, 1000);
}

function finishDownload() {
	 window.clearInterval(fileDownloadCheckTimer);
	 $.cookie('fileDownloadToken', null); //clears this cookie value
	 $.unblockUI();
}

