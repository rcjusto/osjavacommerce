(function($) {
    $.fn.flashPromo = function(options) {

        if (options!=null && !jQuery.isArray(options)) {
            if (options=='close') {
                hidePromo();
                return;
            }
        }

        options = options || {};
        var defaults = {
            position: 'right bottom',
            showAfter: 0,
            hideAfter: 0,
            opacity: 0.8,
            showClose: true,
            modal: true,
            width: 250,
            verticalMargin: 15,
            horizontalMargin: 15,
            cookie : ''
        };

        options = $.extend(defaults, options);

        var overlay = $('<div></div>').addClass('flash-overlay').css({opacity: options.opacity}).hide();
        var dialog = $('<div></div>').width(options.width).addClass('flash-dialog').hide();
        if (options.showClose) $('<a></a>').attr('href','#').addClass('flash-close').click(function(){return hidePromo()}).hide().appendTo(dialog);
        var dialogContent = $('<div></div>').addClass('flash-content').appendTo(dialog);

        hidePromo = function() {
            dialog.fadeOut( 'fast', function(){
                overlay.fadeOut('fast', function(){
                    dialog.remove();
                    overlay.remove();
                });
            });
            return false;
        };

        return this.each(function() {
            $(this).hide();
            dialogContent.html($(this).html());
            $(this).html('');
            setTimeout(function(){

                if (options.modal) overlay.appendTo('body').height($('body').height()).show();
                dialog.appendTo('body');
                var arrPos = options.position.split(' ');
                var slideDirection = '';
                if (arrPos.length>1) {
                    if (arrPos[1]=='top') {dialog.css('top',options.verticalMargin + 'px');slideDirection='up';}
                    else if (arrPos[1]=='bottom') {dialog.css('bottom',options.verticalMargin + 'px');slideDirection = 'down'}
                    else if (arrPos[1]=='center') dialog.css('top', (($(window).height()-dialog.height()) /2) + 'px' );
                    else if (!isNaN(eval(arrPos[1]))) {
                        var y = eval(arrPos[1]);
                        if (y>=0) {dialog.css('top', y+'px');slideDirection='up';}
                        else {dialog.css('bottom', (-y)+'px');slideDirection = 'down';}
                    }
                    if (arrPos[0]=='left') dialog.css('left',options.horizontalMargin + 'px');
                    else if (arrPos[0]=='right') dialog.css('right',options.horizontalMargin + 'px');
                    else if (arrPos[0]=='center') dialog.css('left', (($(window).width()-dialog.width()) /2) + 'px' );
                    else if (!isNaN(eval(arrPos[0]))) {
                        var x = eval(arrPos[0]);
                        if (x>=0) dialog.css('left', x+'px');
                        else dialog.css('right', (-x)+'px');
                    }
                }
                if (slideDirection!='') {
                    dialog.show('slide',{direction:slideDirection},1000,function(){$('.flash-close').fadeIn('fast');});
                } else {
                    dialog.fadeIn(1000,function(){$('.flash-close').fadeIn('fast');});
                }

                if (options.hideAfter>0) setTimeout(hidePromo, options.hideAfter * 1000);
                if (options.cookie!=null && options.cookie!='') $.cookie(options.cookie, 'Y', {expires:100});
            }, options.showAfter * 1000);

        });
    };
})(jQuery);