var dialog;
var validationErrorTitle = 'Error in values';
var validationErrorText = 'Please check the values of the fields marked in red color';
var loadingText = 'Loading. Please wait ...';
function setLanguage(lang) {
    $('a.lang-selector').removeClass('selected');
    if (lang != thisLanguage) $('.multilang').trigger('blur').removeClass('lang-' + thisLanguage);
    thisLanguage = lang;
    $('.multilang').trigger('loadContent').addClass('lang-' + thisLanguage);
    $("a[rel='" + thisLanguage + "'].lang-selector").addClass('selected');
    $('.multilang:first').focus();
}
jQuery.fn.validField = function() {
    $(this).removeClass('invalid');
    if ($(this).hasClass('required')) {
        if ($(this).val() == null || $(this).val() == '' || $(this).val() == 'undefined' || ( $(this).is('select') && $(this).val() == '0')) {
            $(this).addClass('invalid');
        }
    }
    if (!$(this).hasClass('invalid') && $(this).val() != '' && $(this).hasClass('integer')) {
        if (!/^-?\d+$/.test($(this).val())) $(this).addClass('invalid');
    }
    if (!$(this).hasClass('invalid') && $(this).val() != '' && $(this).hasClass('digits')) {
        if (!/^\d+$/.test($(this).val())) $(this).addClass('invalid');
    }
    if (!$(this).hasClass('invalid') && $(this).val() != '' && $(this).hasClass('decimal')) {
        if (!/^-?(?:\d+|\d{1,3}(?:,\d{3})+)(?:\.\d+)?$/.test($(this).val())) $(this).addClass('invalid');
    }
    if (!$(this).hasClass('invalid') && $(this).val() != '' && $(this).hasClass('email')) {
        if (!/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i.test($(this).val())) $(this).addClass('invalid');
    }
    if (!$(this).hasClass('invalid') && $(this).hasClass('customval')) {
        if (window.customVal && !customVal(this)) $(this).addClass('invalid');
    }
    return !$(this).hasClass('invalid');
};
jQuery.fn.validForm = function() {
    var hayError = false;
    $(this).find("input,select").each(function() {
        if (!$(this).validField()) {hayError = true;}
        $(this).blur(function() { $(this).validField(); });
    });
    if (hayError) $(this).find('.validation-error').error(validationErrorTitle,validationErrorText);
    else {
        $(this).find('.validation-error').hide();
        $(this).find('.multilang').each(function(){
            var nField = $(this).attr('name').substr('4');
            if ($('#langfields_fillempty').is(':checked')) {
                var aValue;
                $("input[name='"+nField+"']").each(function(){if ($(this).val()!='') aValue = $(this).val()});
                if (aValue!='') $("input[name='"+nField+"']").each(function(){if ($(this).val()=='') $(this).val(aValue)});
            }
        });
    }
    return !hayError;
};
jQuery.fn.treeSelect = function(w) {
    if (!w) w = 20;
    return $(this).each(function() {
        $(this).find('option').each(function(){
                var l = eval($(this).attr('level'))*w+4;
                if (!isNaN(l)) $(this).css('padding-left',l+'px')
            });
    });
};
jQuery.fn.sortedTable = function(fField, fDir , f) {
    $(this).find('th.sort').each(function(){
        if ($(this).attr('sortby')==$('#'+fField).val()) {
            $(this).addClass($('#'+fDir).val()=='desc' ? 'sort-desc' : 'sort-asc')
        }
        $(this).click(function() {
            $('#'+fDir).val($(this).hasClass('sort-asc') ? 'desc' : 'asc');
            $('#'+fField).val($(this).attr('sortby'));
            if (jQuery.isFunction(f)) f();
        });
    });

};
jQuery.fn.loading = function(title, text) {
    return $(this).each(function() {
        var div = $('<div>').removeClass('info').removeClass('error').addClass('loading');
        $('<h2>').html(title).appendTo(div);
        $('<p>').html(text).appendTo(div);
        $(this).html(div);
        div.show();
    });
};
jQuery.fn.info = function(title, text) {
    return $(this).each(function() {
        var div = $('<div>').removeClass('error').removeClass('loading').addClass('info');
        $('<h2>').html(title).appendTo(div);
        $('<p>').html(text).appendTo(div);
        $(this).html(div);
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
function initPage() {
    $('ul.menu-left').mymenu({menuPosition:'left'});
    $('input.date').each(function(){
        if (!$(this).hasClass('readonly')) {
            $(this).datepicker({ dateFormat: params.dateFormat });
        }
    });
    $('h2.block-title').find('a').live('click',function(){
        var blockId = $(this).attr('rel');
        if (blockId!=null && blockId!='') {
            if ($(this).hasClass('hidden')) {
                $(blockId).show();
                $(this).removeClass('hidden')
            } else {
                $(blockId).hide();
                $(this).addClass('hidden')
            }
        }
        return false;
    });
    //$('.label-change').tooltip();
    $('.label-change').live('click',function(e){
        if (e.ctrlKey && $(this).attr('key')!=''){e.preventDefault(); editLabel($(this).attr('key')); }
    });
    $('button').live('click',function(e){
        var href = $(this).attr('href');
        if (e.ctrlKey && $(this).find('.label-change') && $(this).find('.label-change').attr('key')!='') {
            e.preventDefault();
            editLabel($(this).find('.label-change').attr('key'));
        } else if (href!=null && href!='') {
            document.location.href = href;
        }
    });
    if (window.onLoadPage) onLoadPage();
    $('table.listado').find("tr:nth-child(odd)").addClass("odd");
    $('table.listado').find("tr:not(.nohover)").live('mouseenter',function() {$(this).addClass('hover');}).live('mouseleave', function() {$(this).removeClass('hover')});
    $('.hoverable').live('mouseenter',function() {$(this).addClass('hover');}).live('mouseleave', function() {$(this).removeClass('hover')});
    $('.field').live('focusin', function() {$(this).addClass('focused');}).live('focusout', function() {$(this).removeClass('focused');});
    $('button').button().live('mouseenter',function(){$(this).addClass('hoverBtn')}).live('mouseleave',function(){$(this).removeClass('hoverBtn')});
    // Campos multi-idioma
    $('.multilang').blur(function() {
        var nField = $(this).attr('name').substr('4');
        $(this).siblings("[name='" + nField + "']." + thisLanguage).val($(this).val());
    });
    $('.multilang').change(function() {
        var nField = $(this).attr('name').substr('4');
        $(this).siblings("[name='" + nField + "']." + thisLanguage).val($(this).val());
    });
    $('.multilang').bind('loadContent', function() {
        var nField = $(this).attr('name').substr('4');
        $(this).val($(this).siblings("[name='" + nField + "']." + thisLanguage).val());
    });
    $('.multilang').each(function(){
        var $m = $(this);
        $(this).parents('form').submit(function(){$m.trigger('blur');});
    });
    $('a.lang-selector').click(function() {
        setLanguage($(this).attr('rel'));
        return false;
    });
    $('#langfields_fillempty').attr('checked', jQuery.cookie('languages.fillempty')!='false' ).click(function() {
        jQuery.cookie('languages.fillempty', ($(this).is(':checked')) ? 'true' : 'false');
    });
    // Formularios validables
    $('form.validable').submit(function() {
        return $(this).validForm();
    });
    $('form.validable').find("input:text,select,textarea").each(function() {
        $(this).attr('oldvalue', $(this).val()).change(function() {if ($(this).attr('oldvalue') != $(this).val()) $(this).addClass('modified'); else $(this).removeClass('modified'); });
    });
//    $('form.validable').find("input:date").each(function() {
//        $(this).attr('oldvalue', $(this).val()).change(function(e, nv) {if ($(this).attr('oldvalue') != nv) $(this).addClass('modified'); else $(this).removeClass('modified'); });
//    });
    $('.textarea-ckeditor').live('click', function() {
        $(this).parent().find('textarea').ckeditor({
            contentsCss : 'assets/output_xhtml.css',
            language : thisLanguage,
            filebrowserBrowseUrl : params.urlImgExplorer,
            filebrowserImageBrowseUrl : params.urlImgExplorer + '?filterType=img',
            filebrowserFlashBrowseUrl : params.urlImgExplorer + '?filterType=swf'
        });
        $(this).hide();
        return false;
    });
    // Crear dialogo
 //   dialog = $('#dialogo').overlay({api:true,closeOnClick:false,closeOnEsc:false,top:'5%',mask:{color: '#bbbbbb',loadSpeed: 200,opacity: 0.8}});
    // inicializar campos multidioma
    setLanguage(thisLanguage);
}
