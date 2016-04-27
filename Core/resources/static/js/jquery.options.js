var helpOptionIndex = 0;
(function($) {
    $.fn.helpOptions = function(list, options) {

        options = options || {};
        var defaults = {
            readonly: false,
            width: 'auto',
            onSelect : function(o, v){
                $(o).trigger('change');
            }
        };

        options = $.extend(defaults, options);

        $.fn.helpOptions.getOptions = function(obj, list, options) {
             var id = ($(obj).data('popup')!=null && $(obj).data('popup')!='') ? $(obj).data('popup') + '' : "help_options_" + (helpOptionIndex++);
             if ($('#'+id).size()<1) {
                 var div = $('<div></div>').addClass('popup-help-options').css('position','absolute').css('z-index',9999999).attr('id', id).hide().appendTo($(obj).parent());
                 var ul = $('<ul></ul>').appendTo(div);
                 $.each(list, function(i,v){
                     $('<a></a>').attr('href','#').attr('rel',v).html(v).click(function(){ $(obj).val($(this).attr('rel')); $('#'+id).hide(); options.onSelect(obj, $(this).attr('rel')); return false;}).appendTo($('<li></li>').appendTo(ul));
                 });
             }
             $(obj).data('popup', id);
            return $('#'+id);
        };

        $.fn.helpOptions.onClick = function() {
            if (this.divOpt.is(':visible')) this.divOpt.hide();
            else this.divOpt.show().position({my:'left top',at:'left bottom',of:this}).width(('auto'==options.width) ? $(this).width()+4 : options.width );
            var v = 12;
        };

        return this.each(function() {
            $(this).addClass('popup-help-fld');
            if (options.readonly) $(this).attr('readonly',true);
            var self = this;
            self.divOpt = $.fn.helpOptions.getOptions(self, list, options);
            $(self).unbind('click', $.fn.helpOptions.onClick);
            $(self).bind('click', $.fn.helpOptions.onClick);
            $(document).bind("click", function(e) {if(e.target!=null && e.target!=undefined && e.target!=self && self.divOpt!=null ) self.divOpt.hide();});
        });
    };


})(jQuery);