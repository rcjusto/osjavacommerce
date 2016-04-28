function countryState(cId, sId, stateId) {
    $('#' + cId).change(function() {
        var oldValue = (stateId != null) ? stateId : $('#' + sId).val();
        $('#' + sId).html('<option value="">'+params.texts.loading+'</option>');
        jQuery.getJSON(params.urls.country_states, {country:$(this).val()}, function(data) {
            $('#' + sId).html('');
            if (data) jQuery.each(data, function(i, c) {
                $('<option>').attr('value', c.idState).attr('selected', (oldValue == c.idState)).html(c.htmlStateName).appendTo('#' + sId);
            });
        });
    }).trigger('change');
}
function editBlock(blockId) {
    if ($('#dialogo').size()==0) {
        $('<div></div>').attr('id','dialogo').append(
            $('<div></div>').attr('id','dialogContent')
        ).hide().appendTo('body');
    }
    $('#dialogContent').append($('<div>').loading(params.texts.loading, params.texts.please_wait));
    $('#dialogContent').load(params.urls.block_edit, {code:blockId,rnd:Math.random()});
    $('#dialogo').dialog({width:800,height:500,modal:true,resizable:false,position:['center',50]});
    return false;
}
jQuery.fn.validForm = function() {
    var hayError = false;
    $(this).find('.error').hide();
    $(this).find("input:visible,select:visible,textarea:visible").each(function() {
        $(this).parent().find('span.field_error').hide();
        $(this).attr('placeholder',null);
        var errText = $(this).validField();
        if (errText != '') {
            hayError = true;
            var el = $(this).parent().find('span.field_error');
            if (el.size()>0) el.html(errText).show();
            else $(this).attr('placeholder',errText);
        }
        $(this).blur(function() { $(this).validField(); });
    });
    return !hayError;
};
jQuery.fn.validField = function() {
    var result = '';
    $(this).removeClass('invalid');
    $(this).parent().find('span.field_error').hide();
    if ($(this).hasClass('required')) {
        if ($(this).val() == null || $(this).val() == '' || $(this).val() == 'undefined' || ( $(this).is('select') && $(this).val() == '0')) {
            $(this).addClass('invalid');
            result = params.errors.required;
        }
    }
    if (!$(this).hasClass('invalid') && $(this).val() != '' && $(this).hasClass('integer')) {
        if (!/^-?\d+$/.test($(this).val())) {
            $(this).addClass('invalid');
            result = params.errors.integer;
        } else if ($(this).attr('maxValue') != null && $(this).attr('maxValue') != '') {
            var curv = eval($(this).val());
            var maxv = eval($(this).attr('maxValue'));
            if (curv > maxv) {
                $(this).addClass('invalid');
                result = params.errors.gt + ' ' + maxv;
            }
        }
    }
    if (!$(this).hasClass('invalid') && $(this).val() != '' && $(this).hasClass('digits')) {
        if (!/^\d+$/.test($(this).val())) {
            $(this).addClass('invalid');
            result = params.errors.integer;
        }
    }
    if (!$(this).hasClass('invalid') && $(this).val() != '' && $(this).hasClass('decimal')) {
        if (!/^-?(?:\d+|\d{1,3}(?:,\d{3})+)(?:\.\d+)?$/.test($(this).val())) {
            $(this).addClass('invalid');
            result = params.errors.decimal;
        }
    }
    if (!$(this).hasClass('invalid') && $(this).val() != '' && $(this).hasClass('email')) {
        if (!/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i.test($(this).val())) {
            $(this).addClass('invalid');
            result = params.errors.email;
        }
    }
    if (result != '') {
        var fld = $(this);
        $(this).parent().css('white-space','nowrap');
        $(this).parent().find('.field_error').html(result).show();
    }
    return result;
};
jQuery.fn.loadingSmall = function(text) {
    if (text==null || text=='') text = params.texts.loading;
    return $(this).each(function() {
        $(this).empty();
        $('<span>').addClass('loading-min').html(text).appendTo(this);
    });
};
jQuery.fn.loading = function(title, text) {
    return $(this).each(function() {
        var div = $(this).html('').removeClass('info').removeClass('error').addClass('loading');
        $('<h2>').html(title).appendTo(div);
        $('<p>').html(text).appendTo(div);
        div.show();
    });
};
jQuery.fn.loadingOverlay =  function(title, text, opacity) {
    return $(this).each(function() {
        if (opacity==null) opacity = 0.9;
        var overlay = $('<div></div>').addClass('loading-overlay').css({position:'absolute',opacity: opacity}).appendTo(this);
        var div = $('<div>').addClass('loading').css('position','absolute').appendTo(overlay);
        $('<h2>').html(title).appendTo(div);
        $('<p>').html(text).appendTo(div);
        overlay.width($(this).outerWidth()).height($(this).outerHeight()).position({my:'left top',at:'left top', of: this});
        div.show().css({'position':'absolute'}).position({my:'center center',at:'center center', of: overlay});
    });

};
jQuery.fn.info = function(title, text) {
    return $(this).each(function() {
        var div = $(this).html('').removeClass('error').removeClass('loading').addClass('info');
        $('<h2>').html(title).appendTo(div);
        $('<p>').html(text).appendTo(div);
        div.show();
    });
};
jQuery.fn.error = function(title, text) {
    return $(this).each(function() {
        var div = $(this).html('').removeClass('info').removeClass('loading').addClass('error');
        $('<h2>').html(title).appendTo(div);
        $('<p>').html(text).appendTo(div);
        div.show();
    });
};
$(document).ready(function() {

	/* Drop Down Menu */
	$("header li.dropdown").click(function () {
      	$(this).toggleClass("expanded");
    });
	
	/* Featured Product Price Overlay */
	$("#featured .regular .product").mouseover(function () {
		$(this).find(".overlay").css("display","block");
	}).mouseout(function () {
		$(this).find(".overlay").css("display","none");
	});
	
	$("#featured .responsive .product").click(function () {
		$(this).find(".overlay").toggle('fast');
	});
	
	/* Sale Product Price Overlay */
	$(".sale .regular .product").mouseover(function () {
		$(this).find(".overlay").css("display","block");
	}).mouseout(function () {
		$(this).find(".overlay").css("display","none");
	});
	
	$(".sale .responsive .product").click(function () {
		$(this).find(".overlay").toggle('fast');
	});
	
	/* Expanding Search Input Field */
	$(".search-input").focus(function() {
		$(this).attr("value","").css("width","201px");
	}).focusout(function () {
		$(this).css("width","85px");
	});
	
	/* Remove Contents fron Newsletter Input Field */
	$(".newsletter-input").focus(function() {
		$(this).attr("value","");
	});
	
	/* Slideshow Control */
	$(".slider.regular").simplecarousel({
		width: 705,
		height: 400,
		visible: 1,
		auto: 8000,
		next: $('.slider-next'),
		prev: $('.slider-prev'),
		pagination: false,
		layout: "regular",
		fade: true
	});
	
	$(".slider.responsive").simplecarousel({
		width: 300,
		height: 194,
		visible: 1,
		auto: 8000,
		next: $('.slider-next'),
		prev: $('.slider-prev'),
		pagination: false,
		layout: "responsive",
		fade: true
	});
	
	/* Slideshow Control */
	$(".product-slider.regular").simplecarousel({
		width: 705,
		height: 705,
		visible: 1,
		auto: 8000,
		next: $('.slider-next'),
		prev: $('.slider-prev'),
		pagination: false,
		layout: "regular",
		fade: true
	});

	$(".product-slider.responsive").simplecarousel({
		width: 300,
		height: 300,
		visible: 1,
		auto: 8000,
		next: $('.slider-next'),
		prev: $('.slider-prev'),
		pagination: false,
		layout: "responsive",
		fade: true
	});

	/* Featured Products Control */
	$(".featured.regular").simplecarousel({
		width: 705,
		height: 240,
		visible: 1,
		auto: 8000,
		next: $('.featured-next'),
		prev: $('.featured-prev'),
		pagination: false,
		vertical: true,
		layout: "regular"
	});
	$(".featured.responsive").simplecarousel({
		width: 300,
		height: 231,
		visible: 1,
		auto: 8000,
		next: $('.featured-next'),
		prev: $('.featured-prev'),
		pagination: false,
		vertical: true,
		layout: "responsive"
	});

    // PAGINA DE LISTING

        $('a.filter-category').click(function() {
            $('#categoryCode').val($(this).attr('data'));
            if (!$(this).hasClass('child')) {
                $('#filterManufacturer').val('');
                $('#filterLabel').val('');
            }
            $('#formProducts').submit();
            return false;
        });
        $('a.filter-manufacturer').click(function() {
            $('#filterManufacturer').val($(this).attr('data'));
            $('#formProducts').submit();
            return false;
        });
        $('a.filter-price').click(function() {
            $('#filterPriceMin').val($(this).attr('data-min'));
            $('#filterPriceMax').val($(this).attr('data-max'));
            $('#formProducts').submit();
            return false;
        });
        $('a.filter-label').click(function() {
            $('#filterLabel').val($(this).attr('data'));
            $('#formProducts').submit();
            return false;
        });
        $('a.filter-attribute').click(function(){
            $('#filterAttributesNew').val($(this).attr('attr')+'='+$(this).attr('data'));
            $('#formProducts').submit();
            return false;
        });
        $('a.closefilter-manufacturer').click(function() {
            $('#filterManufacturer').val('');
            $('#formProducts').submit();
            return false;
        });
        $('a.closefilter-price').click(function() {
            $('#filterPriceMin').val('');
            $('#filterPriceMax').val('');
            $('#formProducts').submit();
            return false;
        });
        $('a.closefilter-label').click(function() {
            $('#filterLabel').val('');
            $('#formProducts').submit();
            return false;
        });
        $('a.closefilter-attribute').click(function(){
            $('#filterAttributes'+$(this).attr('data')).remove();
            $('#formProducts').submit();
            return false;
        });
        $('a.gotopage').live('click',function() {
            $('.nav-page').val($(this).attr('data'));
            $('#formProducts').submit();
            return false;
        });

        $('.more-manufacturers').click(function() {
            $(this).closest('ul').find('li').show();
            $(this).closest('li').hide();
            return false;
        });
        $('.sort-navigator,.items-navigator').hover(function(){$(this).find('div').show();},function(){$(this).find('div').hide();});
    $('a.filter-sort').click(function(){
        $('#filterSort').val($(this).attr('data'));
        $('#formProducts').submit();
        return false;
    });
    $('a.filter-items').click(function(){
        $('#filterItems').val($(this).attr('data'));
        $('#formProducts').submit();
        return false;
    });
    // FIN PAGINA DE LISTING
    $('.editable-block').live('mouseenter',function(){
        var b = $(this);
        b.addClass('editable-block-hover');
        $(this).find('.editable-block-link').css('left', 0).css('top', 0).width(b.width()).height(b.height()).show().position({my:'left top', at:'left top', of: b})
            .find('a').position({my:'left top', at:'left top', of: b, offset:'0 0'});
    }).live('mouseleave',function(){
            $(this).removeClass('editable-block-hover');
            $(this).find('.editable-block-link').hide();
    });
    $('#emailSubscribe').submit(function() {
        if ($('#subscribeEmailAddress').validField() == '') {
            jQuery.getJSON(params.urls.reg_newsletter, {email:$('#subscribeEmailAddress').val(),rnd:Math.random()}, function(data) {
                if (data == 'ok') alert('Registration Successfully');
                else alert(data);
                $('#subscribeEmailAddress').val('');
            })
        } else {
            $('#emailSubscribe').find('div.field_error').css('visibility','visible')
        }
        return false;
    });
    $('#subscribeEmailAddress').keypress(function(){$('#emailSubscribe').find('div.field_error').css('visibility','hidden')});

    $('li.item-product .image').hover(
        function(){ $(this).find('a.quick-view').show();},
        function(){$(this).find('a.quick-view').hide();}
    );

    $('a.quick-view').click(function(){
        var url = $(this).attr('href');
        $('#quickView').html('<div class="loading-quickview"></div>').dialog({
            width: 900,
            position: 'top',
            modal: true,
            open:function(){
                $('#quickView').load(url, function(){
                    $(".product-slider.regular").simplecarousel({
                        width: 600,
                        height: 600,
                        visible: 1,
                        auto: 8000,
                        next: $('.slider-next'),
                        prev: $('.slider-prev'),
                        pagination: false,
                        layout: "regular",
                        fade: true
                    });
                    $('#quickView').dialog('option', 'position', 'top');
                });
            }
        });

        return false;
    });

});