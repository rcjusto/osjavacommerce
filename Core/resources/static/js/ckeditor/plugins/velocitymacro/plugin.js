(function()
{
	var velocitymacroReplaceRegex = /\[\[[^\]]+\]\]/g;
	CKEDITOR.plugins.add( 'velocitymacro',
	{
		init : function( editor )
		{

			editor.addCommand( 'createvelocitymacro', {exec:function(e){if (window.editVelocityMacro) editVelocityMacro(e);}} );
			editor.addCommand( 'editvelocitymacro', {exec:function(e){if (window.editVelocityMacro) editVelocityMacro(e);}} );

			editor.ui.addButton( 'CreateVelocityMacro',
			{
				label : 'Add Token',
				command :'createvelocitymacro',
				icon : this.path + 'velocitymacro.png'
			});

			editor.on( 'doubleclick', function( evt )
				{
					if ( CKEDITOR.plugins.velocitymacro.getSelectedToken( editor ) )
						editor.execCommand('editvelocitymacro');
				});

			editor.addCss(
				'.cke_velocitymacro' +
				'{' +
					'background-color: #ffff00;' +
					( CKEDITOR.env.gecko ? 'cursor: default;' : '' ) +
				'}'
			);

			editor.on( 'contentDom', function()
				{
					editor.document.getBody().on( 'resizestart', function( evt )
						{
							if ( editor.getSelection().getSelectedElement().data( 'cke-velocitymacro' ) )
								evt.data.preventDefault();
						});
				});

		},
		afterInit : function( editor )
		{
			var dataProcessor = editor.dataProcessor,
				dataFilter = dataProcessor && dataProcessor.dataFilter,
				htmlFilter = dataProcessor && dataProcessor.htmlFilter;

			if ( dataFilter )
			{
				dataFilter.addRules(
				{
					text : function( text )
					{
						return text.replace( velocitymacroReplaceRegex, function( match )
							{
								return CKEDITOR.plugins.velocitymacro.createToken( editor, null, match, 1 );
							});
					}
				});
			}

			if ( htmlFilter )
			{
				htmlFilter.addRules(
				{
					elements :
					{
						'span' : function( element )
						{
							if ( element.attributes && element.attributes[ 'data-cke-velocitymacro' ] )
								delete element.name;
						}
					}
				});
			}
		}
	});
})();

CKEDITOR.plugins.velocitymacro =
{
	createToken : function( editor, oldElement, text, isGet )
	{
		var element = new CKEDITOR.dom.element( 'span', editor.document );
		element.setAttributes(
			{
				contentEditable		: 'false',
				'data-cke-velocitymacro'	: 1,
				'class'			: 'cke_velocitymacro'
			}
		);

		text && element.setText( text );

		if ( isGet )
			return element.getOuterHtml();

		if ( oldElement )
		{
			if ( CKEDITOR.env.ie )
			{
				element.insertAfter( oldElement );
				// Some time is required for IE before the element is removed.
				setTimeout( function()
					{
						oldElement.remove();
						element.focus();
					}, 10 );
			}
			else
				element.replace( oldElement );
		}
		else
			editor.insertElement( element );

		return null;
	},

	getSelectedToken : function( editor )
	{
		var range = editor.getSelection().getRanges()[ 0 ];
		range.shrink( CKEDITOR.SHRINK_TEXT );
		var node = range.startContainer;
		while( node && !( node.type == CKEDITOR.NODE_ELEMENT && node.data!=undefined && node.data( 'cke-velocitymacro' ) ) )
			node = node.getParent();
		return node;
	}
};
