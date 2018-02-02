(function($) {
    var methods = {
        layoutV : function( obj, size, options ) {
            var l = parseFloat($(obj).css('padding-left'));
            var w = (obj.tagName.toLowerCase()=='body') ? $(window).width() : $(obj).width();
            size -= ($(obj).children('.'+options.fixedInLayout+':visible,.'+options.modificable+':visible').size() - 1) * options.padding;
            $(obj).children('.'+options.fixedInLayout+':visible').each(function(){ size -= $(this).outerHeight(); });
            size = size / $(obj).children('.'+options.modificable+':visible').size();
            var t = parseFloat($(obj).css('padding-top'));
            $(obj).children(':visible').each(function(){
                if ($(this).hasClass(options.fixedInLayout)) {
                    var d1 = $(this).outerWidth()-$(this).width();
                    $(this).css('position','absolute').css({left : l, top: t +'px'}).width(w - d1);
                    t += $(this).outerHeight() + options.padding;
                } else if ($(this).hasClass(options.modificable)) {
                    var d2 = $(this).outerWidth()-$(this).width();
                    $(this).css('position','absolute').css({left : l, top: t +'px'}).width(w - d2);
                    var dif = $(this).outerHeight()-$(this).height();
                    $(this).height(size - dif);
                    t += $(this).outerHeight() + options.padding;
                }
            });
        },
        layoutH : function( obj, size, options ) {
            var t = parseFloat($(obj).css('padding-top'));
            var h = (obj.tagName.toLowerCase()=='body') ? $(window).height() : $(obj).height();
            size -= ($(obj).children('.'+options.fixedInLayout+':visible,.'+options.modificable+':visible').size() - 1) * options.padding;
            $(obj).children('.'+options.fixedInLayout+':visible').each(function(){ size -= $(this).outerWidth(); });
            size = size / $(obj).children('.'+options.modificable+':visible').size();
            var l = parseFloat($(obj).css('padding-left'));
            $(obj).children(':visible').each(function(){
                if ($(this).hasClass(options.fixedInLayout)) {
                    var d1 = $(this).outerHeight()-$(this).height();
                    $(this).css('position','absolute').css({left : l + 'px', top: t }).height(h - d1);
                    l += $(this).outerWidth() + options.padding;
                } else if ($(this).hasClass(options.modificable)) {
                    var d2 = $(this).outerHeight()-$(this).height();
                    $(this).css('position','absolute').css({left : l + 'px', top: t }).height(h - d2);
                    var dif2 = $(this).outerWidth()-$(this).width();
                    $(this).width(size - dif2);
                    l += $(this).outerWidth() + options.padding;
                }
            });
        }
    };

    $.fn.simpleLayout = function( options ) {
        options = $.extend({
            direction: 'vertical',
            padding: 1,
            modificable: 'layout-resize',
            fixedInLayout: 'layout-fixed'
        }, (options || {}));

        return this.each(function() {
            if (options.position=='horizontal') methods.layoutH(this, this.tagName=='BODY' ? $(window).width() : $(this).width(), options );
            else methods.layoutV(this, this.tagName=='BODY' ? $(window).height() : $(this).height(), options );
        });
    };

})(jQuery);
