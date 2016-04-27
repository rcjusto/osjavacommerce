var cssEditorTagDataindex = 0;
(function($) {

    var wrapperL,wrapperT,wrapperR,wrapperB,hoverL,hoverT,hoverR,hoverB;
    var bodyElement;

    var cssEditorMethods = {
        init : function(options) {

            bodyElement = this;

            options = options || {};
            var defaults = {
                clickWidth: 2,
                clickExtra: 5,
                clickColor: '#FF0000',
                hoverWidth: 2,
                hoverExtra: 2,
                hoverColor: '#FF0000',
                editor: '#csseditor',
                canNavigate: function () {
                    return false;
                },
                onSelectModifier: function (mod, data) {
                    return false;
                },
                onSelectObject: function (obj) {
                    return false;
                },
                action : '',
                css: {}
            };

            options = $.extend(defaults, options);


            var self = this;

            $('<link>').attr('rel', 'stylesheet').attr('type', 'text/css').attr('href', '/test/css_editor.css').appendTo(this);
            wrapperL = $('<div></div>').addClass('click-border exclude-from-edition border-left').css('position', 'absolute').css('background-color', options.clickColor).width(options.clickWidth).appendTo(this).hide();
            wrapperR = $('<div></div>').addClass('click-border exclude-from-edition border-right').css('position', 'absolute').css('background-color', options.clickColor).width(options.clickWidth).appendTo(this).hide();
            wrapperT = $('<div></div>').addClass('click-border exclude-from-edition border-top').css('position', 'absolute').css('background-color', options.clickColor).height(options.clickWidth).appendTo(this).hide();
            wrapperB = $('<div></div>').addClass('click-border exclude-from-edition border-bottom').css('position', 'absolute').css('background-color', options.clickColor).height(options.clickWidth).appendTo(this).hide();
            hoverL = $('<div></div>').addClass('hover-border exclude-from-edition border-left').css('position', 'absolute').css('background-color', options.hoverColor).width(options.hoverWidth).appendTo(this).hide();
            hoverR = $('<div></div>').addClass('hover-border exclude-from-edition border-right').css('position', 'absolute').css('background-color', options.hoverColor).width(options.hoverWidth).appendTo(this).hide();
            hoverT = $('<div></div>').addClass('hover-border exclude-from-edition border-top').css('position', 'absolute').css('background-color', options.hoverColor).height(options.hoverWidth).appendTo(this).hide();
            hoverB = $('<div></div>').addClass('hover-border exclude-from-edition border-bottom').css('position', 'absolute').css('background-color', options.hoverColor).height(options.hoverWidth).appendTo(this).hide();

            cleanModifier = function(m) {
                m = m.replace(':hover', '');
                m = m.replace(':visited', '');
                m = m.replace(':link', '');
                return m;
            };

            initializeTags = function() {
                $.each(options.css, function(m) {
                    var tags = null;
                    try {
                        tags = $(self).find(cleanModifier(m));
                    } catch(e) {
                        alert(e);
                    }
                    if (tags != null) tags.each(function() {
                        if ($(this).data('csss') != null) {
                            ($(this).data('csss')).push(m);
                        } else {
                            $(this).data('csss', [m]);
                        }
                    });
                })
            };
            initializeTags();

            listSelectors = function(obj, includeTag) {
                var tag = obj.tagName.toLowerCase();
                // recorrer css
                var mod = ($(obj).data('csss') != null) ? $(obj).data('csss') : [];

                if (includeTag) {
                    mod.push(tag);
                    if (tag == 'a') mod.push(tag + ':hover');
                }
                return mod;
            };

            getTagDescription = function(elem) {
                if (elem == null) return 'Unknown';
                elem = elem.toLowerCase();
                if (elem == 'img') return 'Image';
                else if (elem == 'a') return 'Link';
                else if (elem == 'li') return 'List item';
                else if (elem == 'ul') return 'List';
                else if (elem == 'span') return 'Text';
                else if (elem == 'div') return 'Block';
                else if (elem == 'td') return 'Table cell';
                else if (elem == 'tr') return 'Table row';
                else if (elem == 'table') return 'Table';
                else return elem;
            };

            updateEditor = function(obj, includeTag) {
                if (obj != null && obj.tagName != null && obj.tagName != '') {
                    var tagName = obj.tagName.toLowerCase();
                    var selectors = listSelectors(obj, includeTag);
                    if (selectors != null && selectors.length > 0) {
                        $('<h2></h2>').html(getTagDescription(tagName)).appendTo(options.editor);
                        var ul = $('<ul></ul>').appendTo(options.editor);
                        jQuery.each(selectors, function(i, m) {
                            var li = $('<li></li>').addClass('tag-' + tagName).appendTo(ul);
                            $('<a></a>').attr('href', '#').addClass('css-selector-blink').click(
                                    function() {
                                        try {
                                            var tags = $(self).find(m);
                                            tags.effect('pulsate');
                                        } catch(e) {
                                        }
                                    }).appendTo(li);
                            $('<a></a>').html(m).attr('href', '#').addClass('css-selector').click(
                                    function() {
                                        options.onSelectModifier(m, options.css[m]);
                                        cssEditorMethods.showClick(obj);
                                        return false;
                                    }).appendTo(li);
                            if (options.css[m] != null) {
                                var data = '';
                                jQuery.each(options.css[m], function(s) {
                                    data += s + ' ';
                                });
                                $('<div></div>').addClass('css-modified').html(data).appendTo(li);
                                li.addClass('has-modifiers');
                            }
                        });
                    }
                }
            };


            $(this).data('options', options);

            $(this).find('*:not(.exclude-from-edition)')
                    .click(function(e) {
                        if (!options.canNavigate()) {
                            e.stopImmediatePropagation();
                            e.preventDefault();
                            cssEditorMethods.selectElement(this);
                            return false;
                        }
                    }).mouseover(function(e) {
                        if (!options.canNavigate()) {
                            e.stopImmediatePropagation();
                            e.preventDefault();
                            cssEditorMethods.showHover(this);
                        }
                    }).mouseleave(function(e) {
                        e.stopImmediatePropagation();
                        e.preventDefault();
                        cssEditorMethods.hideHover();
                    }).mousedown(function(e) {
                        cssEditorMethods.hideClick();
                    });
            return this;
        },
        showAllSelectors : function() {
            return this.each(function() {
                var options = $(bodyElement).data('options');

                $(options.editor).html('');
                $('<h2></h2>').html('All Modifiers').appendTo(options.editor);
                var ul = $('<ul></ul>').appendTo(options.editor);
                jQuery.each(options.css, function(m, d) {
                    var li = $('<li></li>').appendTo(ul);
                    $('<a></a>').html(m).attr('href', '#').addClass('css-selector').click(
                            function() {
                                options.onSelectModifier(m, d);
                                return false;
                            }).appendTo(li);
                    var data = '';
                    jQuery.each(d, function(s) {
                        data += s + ' ';
                    });
                    $('<div></div>').addClass('css-modified').html(data).appendTo(li);
                    li.addClass('has-modifiers');
                });
            });
        },
        showClick: function(obj) {
            var options = $(bodyElement).data('options');
            var sep2 = options.clickWidth + options.clickExtra;
            wrapperL.show().position({my:'right top',at:'left top',of: obj, offset: '-' + options.clickExtra + ' -' + sep2, collision: 'none none'}).height($(obj).height());
            wrapperR.show().position({my:'left top',at:'right top',of: obj, offset: options.clickExtra + ' -' + sep2, collision: 'none none'}).height($(obj).height());
            wrapperT.show().position({my:'left bottom',at:'left top',of: obj, offset:'-' + (sep2 ) + ' -' + options.clickExtra, collision: 'none none'}).width($(obj).width());
            wrapperB.show().position({my:'left top',at:'left bottom',of: obj, offset:'-' + (sep2 ) + ' ' + options.clickExtra, collision: 'none none'}).width($(obj).width());
            wrapperL.show().position({my:'right top',at:'left top',of: obj, offset: '-' + options.clickExtra + ' -' + sep2, collision: 'none none'}).height($(obj).height());
            wrapperR.show().position({my:'left top',at:'right top',of: obj, offset: options.clickExtra + ' -' + sep2, collision: 'none none'}).height($(obj).height());
            wrapperT.show().position({my:'left bottom',at:'left top',of: obj, offset:'-' + (sep2 ) + ' -' + options.clickExtra, collision: 'none none'}).width($(obj).width());
            wrapperB.show().position({my:'left top',at:'left bottom',of: obj, offset:'-' + (sep2 ) + ' ' + options.clickExtra, collision: 'none none'}).width($(obj).width());
            wrapperL.height(wrapperB.offset().top - wrapperT.offset().top + options.clickWidth);
            wrapperR.height(wrapperB.offset().top - wrapperT.offset().top + options.clickWidth);
            wrapperT.width(wrapperR.offset().left - wrapperL.offset().left + options.clickWidth);
            wrapperB.width(wrapperR.offset().left - wrapperL.offset().left + options.clickWidth);
            cssEditorMethods.hideHover();
        },
        showHover: function(toDraw) {
            var options = $(bodyElement).data('options');
            var sep2 = options.hoverWidth + options.hoverExtra;

            hoverL.show().position({my:'right top',at:'left top',of: toDraw, offset: '-' + options.hoverExtra + ' -' + sep2, collision: 'none none'}).height($(toDraw).height());
            hoverR.show().position({my:'left top',at:'right top',of: toDraw, offset: options.hoverExtra + ' -' + sep2, collision: 'none none'}).height($(toDraw).height());
            hoverT.show().position({my:'left bottom',at:'left top',of: toDraw, offset:'-' + sep2 + ' -' + options.hoverExtra, collision: 'none none'}).width($(toDraw).width());
            hoverB.show().position({my:'left top',at:'left bottom',of: toDraw, offset:'-' + sep2 + ' ' + options.hoverExtra, collision: 'none none'}).width($(toDraw).width());
            hoverL.show().position({my:'right top',at:'left top',of: toDraw, offset: '-' + options.hoverExtra + ' -' + sep2, collision: 'none none'}).height($(toDraw).height());
            hoverR.show().position({my:'left top',at:'right top',of: toDraw, offset: options.hoverExtra + ' -' + sep2, collision: 'none none'}).height($(toDraw).height());
            hoverT.show().position({my:'left bottom',at:'left top',of: toDraw, offset:'-' + sep2 + ' -' + options.hoverExtra, collision: 'none none'}).width($(toDraw).width());
            hoverB.show().position({my:'left top',at:'left bottom',of: toDraw, offset:'-' + sep2 + ' ' + options.hoverExtra, collision: 'none none'}).width($(toDraw).width());
            hoverL.height(hoverB.offset().top - hoverT.offset().top + options.hoverWidth);
            hoverR.height(hoverB.offset().top - hoverT.offset().top + options.hoverWidth);
            hoverT.width(hoverR.offset().left - hoverL.offset().left + options.hoverWidth);
            hoverB.width(hoverR.offset().left - hoverL.offset().left + options.hoverWidth);
        },
        hideClick: function() {
            wrapperL.hide();
            wrapperR.hide();
            wrapperT.hide();
            wrapperB.hide();
        },
        hideHover: function() {
            hoverL.hide();
            hoverR.hide();
            hoverT.hide();
            hoverB.hide();
        },
        selectElement : function(obj) {
            var options = $(bodyElement).data('options');
            if (obj != null) {
                selectedElement = obj;
                $(options.editor).html('');
                updateEditor(obj, true);

                var $par = $(obj).parent();
                while ($par.size() > 0) {
                    if ($par.attr('class') != null && $par.attr('class') != '') updateEditor($par.get(0), false);
                    $par = $par.parent();
                }

                cssEditorMethods.showClick(obj);
                options.onSelectObject(obj);
            }
        }
    };

    $.fn.cssEditor = function(method) {
        // Method calling logic
        if (cssEditorMethods[method]) {
            return cssEditorMethods[ method ].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof method === 'object' || ! method) {
            return cssEditorMethods.init.apply(this, arguments);
        } else {
            $.error('Method ' + method + ' does not exist on jQuery.tooltip');
        }
    };

})(jQuery);