jQuery.fn.validForm = function() {
    var hayError = false;
    $(this).find("input:visible,select:visible,textarea:visible").each(function() {
        $(this).parent().find('span.field_error').hide();
        var errText = $(this).validField();
        if (errText != '') {
            hayError = true;
            var errEl = $(this).parent().find('span.field_error');
            var dataMY = errEl.attr('data-my'); if (typeof dataMY === 'undefined' || dataMY === false || dataMY=='') dataMY = 'left top';
            var dataAT = errEl.attr('data-at'); if (typeof dataAT === 'undefined' || dataAT === false || dataAT=='') dataAT = 'right top';
            var dataOS = errEl.attr('data-os'); if (typeof dataOS === 'undefined' || dataOS === false || dataOS=='') dataOS = '0 0';
            errEl.html(errText).css('position', 'absolute')
                .show()
                .position({my:dataMY,at:dataAT,of:this,offset:dataOS, collision: "fit flip"});
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
        var errDiv = $(this).parent().find('.field_error');
        var dataMY = errDiv.attr('data-my'); if (typeof dataMY === 'undefined' || dataMY === false || dataMY=='') dataMY = 'left top';
        var dataAT = errDiv.attr('data-at'); if (typeof dataAT === 'undefined' || dataAT === false || dataAT=='') dataAT = 'right top';
        var dataOS = errDiv.attr('data-os'); if (typeof dataOS === 'undefined' || dataOS === false || dataOS=='') dataOS = '0 0';
        errDiv.html(result).css('position','absolute').show().position({my:dataMY,at:dataAT,of:fld,offset:dataOS});

    }
    return result;
};
jQuery.fn.equalHeight = function(h) {
    if (!h) {
        h = 0;
        $(this).each(function() {
            if (h<$(this).height()) h = $(this).height();
        });
    }
    return $(this).each(function() {
        $(this).height(h);
    });
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

$(function() {
    $('ul.menu-left').mymenu({menuPosition:'left'});
    $('ul.menu-right').mymenu({menuPosition:'right'});
    $('.field').live('focusin', function() {$(this).addClass('focused');}).live('focusout', function() {$(this).removeClass('focused');});
    $('button').live('mouseenter', function() {$(this).addClass('hoverBtn')}).live('mouseleave', function() {$(this).removeClass('hoverBtn')});
    jQuery.datepicker.setDefaults(jQuery.datepicker.regional[params.language]);
    $('button').button();
    //$('.label-change').tooltip();
    $('.label-change').live('click', function(e) {
        if (e.ctrlKey && $(this).attr('key') != '') {
            e.stopImmediatePropagation();
            editLabel($(this).attr('key'));
        }
    });
    $('.statictext-change')
        .live('mouseenter',function(e){
            if (e.ctrlKey) {
                var el = $(this);
                $(this).addClass('focused').find('div.statictext-change-title').each(function() {
                    $(this).width(el.width()).height(el.height());
                });
            }
        })
        .live('mouseleave',function(e){
            $(this).removeClass('focused')
        });
    $('button').live('click', function(e) {
        var href = $(this).attr('href');
        if (e.ctrlKey && $(this).find('.label-change') && $(this).find('.label-change').attr('key') != '') {
            e.preventDefault();
            editLabel($(this).find('.label-change').attr('key'));
        } else if (href != null && href != '') {
            document.location.href = href;
        }
    });
    $('#emailSubscribe').submit(function() {
        if ($('#subscribeEmailAddress').validField() == '') {
            jQuery.getJSON(params.urls.reg_newsletter, {email:$('#subscribeEmailAddress').val(),rnd:Math.random()}, function(data) {
                if (data == 'ok') alert('Registration Successfully');
                else alert(data);
            })
        }
        return false;
    });
    $('.editable-block').live('mouseenter',function(){
        var b = $(this);
        b.addClass('editable-block-hover');
        $(this).find('.editable-block-link').css('left', 0).css('top', 0).width(b.width()).height(b.height()).show().position({my:'left top', at:'left top', of: b})
            .find('a').position({my:'left top', at:'left top', of: b, offset:'0 0'});
    }).live('mouseleave',function(){
            $(this).removeClass('editable-block-hover');
            $(this).find('.editable-block-link').hide();
        });
    $('.hoverable').live('mouseenter',function(){$(this).addClass('hover');}).live('mouseleave',function(){$(this).removeClass('hover');});
    $('.has-children').click(function() {
        var li = $(this).closest('li');
        var ul = li.children('ul');
        if ($(this).hasClass('expanded')) {
            $(this).removeClass('expanded');
            ul.removeClass('expanded');
        } else {
            $(this).addClass('expanded');
            ul.addClass('expanded');
            if (ul.hasClass('use-ajax') && ul.hasClass('unloaded')) {
                var idCat = ul.attr('data');
                var level = ul.attr('level');
                if (level==null || level=='') level = '1';
                if (idCat!=null && idCat!='') {
                    ul.append($("<li class=\"submenu-loading\">"+params.texts.loading+"</li>"));
                    ul.load(params.urls.menu_subcategories, {idCategory: idCat, idOrder: level}, function(){
                        ul.removeClass('unloaded');
                    });
                }
            }
        }
        return false;
    });
    $('.ajax-cart').live('click', function(){
        var id = $(this).attr('data');
        if (id!=null && id!='') {
            jQuery.post(params.urls.add_cart,{idProduct:id,output:'ajax-cart'}, function(data){
                $('.ajax-shopcart').html(data);
                $('.ajax-shopcart').find('.top-cart-message').show();
                $('.ajax-shopcart').addClass('hover');
                $('.ajax-shopcart').find('.top-cart-wrapper').addClass('hover');
                if ($('.top-cart-header').is(':visible')) jQuery.scrollTo('.top-cart-header',800);
                if (ajaxShopCartTimeout!=null) clearTimeout(ajaxShopCartTimeout);
                ajaxShopCartTimeout = setTimeout(hideAjaxShopCart, 5000);
            });
        }
        return false;
    });
    $('a.product-compare').live('click', function(){
        $('#productComparator').load(params.urls.compare_product,{idProduct:$(this).attr('data')}, function(){
            $('#productComparator').show();
            jQuery.scrollTo('#productComparator',800);
        });
        return false;
    });
    $('a.remove-compare').live('click', function(){
        $('#productComparator').load(params.urls.compare_remove,{idProduct:$(this).attr('data')}, function(){
            if ($('#productComparator').find('li').size()<1) $('#productComparator').hide();
        });
        return false;
    });
});
var ajaxShopCartTimeout;
function hideAjaxShopCart() {
    if (ajaxShopCartTimeout!=null) clearTimeout(ajaxShopCartTimeout);
    $('.ajax-shopcart').find('.top-cart-message').hide();
    $('.ajax-shopcart').removeClass('hover');
    $('.ajax-shopcart').find('.top-cart-wrapper').removeClass('hover');
}
function editLabel(lId) {
    $('#dialogContent').append($('<div>').loading(params.texts.loading, params.texts.please_wait));
    $('#dialogContent').load(params.urls.label_edit, {modal:true,staticId:lId,rnd:Math.random()});
    $('#dialogo').dialog({width:600,modal:true,resizable:false,position:['center',50]});
    return false;
}
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
function countryStateEx(cId, sId, stateId, stateName) {
    $('#' + cId).change(function() {
        var oldValue = (stateId != null) ? stateId : $('#' + sId).val();
        var dest = $('#' + sId);
        dest.html('<span class="loading">'+params.texts.loading+'</span>')
            .load(params.urls.country_states_ex, {country:$(this).val(), addressState: oldValue, fieldId:dest.attr('field-id'), fieldName:dest.attr('field-name'), fieldNameEx:dest.attr('field-name-ex'), stateName: stateName});
    }).trigger('change');
}

function emailSubscribe() {
}
function editBlock(blockId) {
    $('#dialogContent').append($('<div>').loading(params.texts.loading, params.texts.please_wait));
    $('#dialogContent').load(params.urls.block_edit, {code:blockId,rnd:Math.random()});
    $('#dialogo').dialog({width:800,height:500,modal:true,resizable:false,position:['center',50]});
    return false;
}
function debug(c) {
    $('#debug').html(c + '\n' + $('#debug').text());
}