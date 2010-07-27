$(function() {
	$('.draggable').live('mouseover', function() {
		var con = $(this).closest('.workspaceContainer').attr('id');
		$(this).data('init', 1);
		$(this).draggable( {
			handle : '.ui-widget-header',
			cancel : 'img',
			stack  : '.draggable',
			containment: '#'+con,
		});
		var h = $(this).height();
		var w = $(this).width();
		$(this).resizable( {
			containment: '#'+con,
			minHeight : h,
			minWidth : w,
			autoHide : true
		});

	});

	$(".draggableChild").live(
			'mouseover',
			function() {

				$(this).draggable(
						{
							handle : '.ui-widget-header',
							cancel : 'img',
							stop : function(event, ui) {
								var x = $(this).attr('class').split(" ");
								var is = false;
								for ( var i = 0; i < x.length; i++) {
									if (x[i] == 'draggableChild')
										is = true;
								}
								if (is) {
									var el = $(this);
									var pos = el.position();
									var pos2 = el.closest('.draggable')
											.position();
									el.css( {
										position : "absolute",
										marginLeft : 0,
										marginTop : 0,
										top : pos2.top + pos.top,
										left : pos2.left + pos.left
									});
									$(this).closest('.workspaceContainer')
											.append($(this));
								}
								$(this).draggable( {
									start : function(event, ui) {
									},
									stop : function(event, ui) {
									}
								});
								$(this).children().show();
								$(this).removeClass('draggableChild');
								$(this).addClass('draggable');
								if (!$(this).data('loaded')
										&& $(this).find('.ui-widget-content')
												.first().html() == '') {
									load($(this).attr('name') + ' .actual', $(
											this).attr('id'));
									$(this).data('loaded', 9);
								}

							},
							start : function(event, ui) {
								var id = $(this).attr('id');
								var t = $('#' + id + ' .ui-widget-header')
										.first().clone();
								$(t).removeClass('ui-widget-header');
								$(t).addClass('clone');
								$(t).insertBefore($(this));
								$(t).attr('id', $(this).attr('id') + "_2");
							}
						});

			});

	$('.min').live(
			'click',
			function() {

				if (!$(this).parent().parent().data('loaded')
						&& $(this).parent().next().html() == '') {
					load($(this).parent().parent().attr('name') + ' .actual',
							$(this).parent().parent().attr('id'));
					$(this).parent().parent().data('loaded', 9);

				}

				$(this).parent().next().slideToggle(400);

			});

	$('.revertFrom').live('click', function() {
		var theId = $(this).parent().parent().attr('id');
		var theSecondId = theId + "_2";
		if ($('#' + theSecondId).attr('id') == null)
			$('#' + theId).remove();
		else {
			$('#' + theId).removeAttr('style');
			$('#' + theId).removeClass('draggable');
			$('#' + theId).addClass('draggableChild');
			$('#' + theSecondId).replaceWith($('#' + theId));
		}
	});
});

function load(url, el) {

	if($.inArray(url,myDivs)==-1){
	var pUrl = $('#'+el).attr('name');
	$('#'+el+'_header').load(pUrl+' .mainH', function(){
		
		$('#'+el+'_header').html($('#'+el+'_header').find('.mainH').first().html());
	});
	$('#' + el + '_content').load(url, function() {
		$('#' + el + ' .min').first().show();
		$('#' + el + '_content').children().show();
		//$('#' + el + '_content').find('ui-widget-header').first().load
		$('#' + el + ' .loading').first().hide();
		$('#' + el + '_content').slideDown(400);
		magic(el);
		myDivs.push(url);

	});
}}

function removeMe(me)
{
	$(me).closest('.ui-widget-content').remove();
}


function loadBox(url, el) {
	alert(url);
	if($.inArray(url,myDivs)==-1){
	$('#' + el).append('<div style="position:absolute"id="myTemp"></div>');
	
	$('#' + el + ' #myTemp').load(url, function() {

		$('#' + el + ' #myTemp').attr('id', '');
		myDivs.push(url);
	});
	}
}

function magic(id) {

	$("#" + id + "_content div[name]")
			.each(
					function() {

						var id2 = "ui" + Math.ceil(Math.random() * 100);
						var head = '<div id="'+id2+'_header" class="ui-widget-header mainH">' + $(this)
								.html() + '<span class="revertFrom"><span class="ui-icon ui-icon-circle-close"></span></span><span class="min"onclick="$(this).children().toggle();"><span class="ui-icon ui-icon-circle-triangle-s" style="display: none;"></span><span class="ui-icon ui-icon-circle-triangle-n"></span></span></div>';
						$(this).html(head);
						$(this).addClass('ui-widget-content draggableChild');

						$(this).attr('id', id2);
						$(this)
								.append(
										'<div id="' + id2 + '_content" class="ui-widget-content" ></div>');

					});

}
var myDivs = new Array();
function show(id) {
	$('#normal').hide();
	$('#myHeaders').show();
	$('#myWrkspaces').show();
	$('#myWorkspaces').children().each(function() {
		if ($(this).attr('id') == ('workspace_' + id)) {
			$(this).show();
		} else {
			$(this).hide();
		}

	});
	$('#myHeaders').children().each(function() {
		if ($(this).attr('id') == ('header_' + id)) {
			$(this).show();
		} else {
			$(this).hide();
		}

	});

}
