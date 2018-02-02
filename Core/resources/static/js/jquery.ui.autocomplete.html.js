/*
 * jQuery UI Autocomplete HTML Extension
 *
 * Copyright 2010, Scott González (http://scottgonzalez.com)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 *
 * http://github.com/scottgonzalez/jquery-ui-extensions
 */
(function( $ ) {

var proto = $.ui.autocomplete.prototype,
	initSource = proto._initSource;

function filter( array, term ) {
	var matcher = new RegExp( $.ui.autocomplete.escapeRegex(term), "i" );
	return $.grep( array, function(value) {
		return matcher.test( $( "<div>" ).html( value.label || value.value || value ).text() );
	});
}

$.extend( proto, {
	_initSource: function() {
		if ( this.options.html && $.isArray(this.options.source) ) {
			this.source = function( request, response ) {
				response( filter( this.options.source, request.term ) );
			};
		} else {
			initSource.call( this );
		}
	},

	_renderItem: function( ul, item) {
		return $( "<li></li>" )
			.data( "item.autocomplete", item )
			.append( $( "<a></a>" )[ this.options.html ? "html" : "text" ]( item.label ) )
			.appendTo( ul );
	}

});

})( jQuery );

(function( $ ) {

$( ".ui-autocomplete-input" ).live( "blur", function(event) {
	var autocomplete = $( this ).data( "autocomplete" );

    var hiddenField = (autocomplete.options.hiddenField) ? $(autocomplete.options.hiddenField) : null;

	if ( !autocomplete.options.comboBox ) {
		return;
	}

    var matcher = new RegExp("^" + jQuery.ui.autocomplete.escapeRegex($(this).val()) + "$", "i");
    autocomplete.widget().children(".ui-menu-item").each(function() {
        var item = $(this).data("item.autocomplete");
        if (matcher.test(item.label || item.value || item)) {
            autocomplete.selectedItem = item;
            return;
        }
    });
    if (autocomplete.selectedItem) {
        autocomplete._trigger("select", event, {
            item: autocomplete.selectedItem
        });
    } else {
        $(this).val('');
        if (hiddenField!=null) hiddenField.val('');
    }

});

}( jQuery ));
