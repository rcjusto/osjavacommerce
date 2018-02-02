(function($) {
    $.fn.mymenu = function(options) {
        options = options || {};
        var defaults = {
            menuPosition: 'right'
            , offsetTop: 0
            , offsetLeft: 0
            , avoidCollision: false
        };

        jQuery.fn.showMenu = function(li, options) {
            var defPos = options.menuPosition;
            var offLeft = options.offsetLeft;
            var offTop = options.offsetTop;
            if(jQuery.browser.msie) {
                offLeft = offLeft-1;
                if ($(this).width()<$(li).width()+12) $(this).width($(li).width()+14);
            }
            $(this).attr('cancelHide', 'yes');
            $(this).css('left',0).css('top',0).css('min-width',$(li).width()+'px')
                    .position({my: defPos+" top",at: defPos+" bottom",of: li,collision: "fit flip", offset: offLeft+" "+offTop, using:function(pos){
                        if (options.avoidCollision && defPos=='left' && pos.left+$(this).width() > $(li).parent().parent().offset().left+$(li).parent().parent().width()) {
                            $(this).css('left',0);
                        }
                    }})
                    .hover(function(){$(this).data('hide','n');},function(){$(this).data('hide','');})
                    .slideDown('fast')
                    .position({my: defPos+" top",at: defPos+" bottom",of: li,collision: "fit flip", offset: offLeft+" "+offTop, using:function(pos){
                        if (options.avoidCollision && defPos=='left' && pos.left+$(this).width() > $(li).parent().parent().offset().left+$(li).parent().parent().width()) {
                            var nl = $(li).parent().offset().left+$(li).parent().width() - $(this).width()-18;
                            $(this).css('left',nl+"px").css('top',pos.top+"px");                            
                        } else {
                            $(this).css('left',pos.left+"px").css('top',pos.top+"px");
                        }
                    }});
        };
        jQuery.fn.hideMenu = function(li) {
            $(this).attr('cancelHide', 'no');
            var $this = this;
            setTimeout(function() {
                if ($($this).attr('cancelHide')!='yes') {
                    var dId = $('> a',li).attr('rel');
                    $(dId).slideUp('fast');
                    $(li).removeClass('hover');
                }
            }, 100);
        };

        options = $.extend(defaults, options);
        return this.each(function() {
                $(this).find('li').each(function(){
                    var li = this, dId = $('> a',this).attr('rel');
                    if ($(li).parent().hasClass('avoid-collision')) options.avoidCollision = true;
                    if ($(dId).size()>0) $(li).addClass('haschild');
                    $(dId).css('position','absolute').hover(
                        function(e) { $(this).attr('cancelHide', 'yes');},
                        function(e) { $(this).hideMenu(li);}
                    ).hide();
                    $(this).hover(
                        function(e){
                            e.stopImmediatePropagation();
                            $(this).addClass('hover');
                            var dId = $('> a',this).attr('rel');
                            $(dId).showMenu(this, options);
                        },
                        function(e){
                            e.stopImmediatePropagation();
                            var dId = $('> a',this).attr('rel');
                            if (dId!=null && dId!='') $(dId).hideMenu(this);
                            else $(this).removeClass('hover');
                        }
                    );
                });
        });
    };
})(jQuery);