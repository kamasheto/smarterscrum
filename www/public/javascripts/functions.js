		var getNotifications = function() {
				$.getJSON('/notificationtasks/getlatestnews',
					function(data) {
						$(data).each(function(){
							$.gritter.add({
								title: this.madeBySysAdmin? '[SysAdmin] '+this.header : this.header,
								text: this.body,
								image: this.importance > 0 ? '/public/images/tick.png' : this.importance < 0 ? '/public/images/cross.png' : '/public/images/error.png',
								sticky: false,
								time: ''
							});
							return true;
						});
					setTimeout('getNotifications();', 1000);
					});
				}
var ping = function() {
				$.getJSON('/sessions/ping',
					function(data) {
						str = '';
						$(data).each(function() {
							// users?
							if (this.isAdmin) {
								this.name = '<span class="isAdmin">' + this.name + '</span>';
							}
							str += '<a href="/show/user?id='+this.id+'">' + this.name + '</a>, ';
						});
						
						$('#onlineUsers').html(str.substring(0,str.length-2));
						setTimeout('ping()', 1000*30);
					});
				}

			 $.extend($.gritter.options, { 
				fade_in_speed: 50, // how fast notifications fade in (string or int)
				fade_out_speed: 300, // how fast the notices fade out
				time: 5000 // hang on the screen for...
			});
function delete_meeting(id, pId)
{
	var confirmation= confirm("Are you sure you want to delete this meeting ?");
	
	if (confirmation) {
		$.post('/Meetings/deleteMeeting', {
			id:id
		}, function(){
		removeMe(pId);	
		});
	};		
}

function set_confirmed(id)
{
	if(confirm("Change your status to attending?"))
	$.post('/MeetingAttendances/confirmAttendance',{meetingId: id}
	,function()
	{
		$('#accept_'+id).hide();
		$('#decline_'+id).hide();
		$('#confirmed_'+id).show();
		$('#inv_'+id).hide();
	})
}


function set_declined(id)
{
	
	
		var reno=prompt("Please enter the reason :","");
		while(reno.length==0)
		{
			reno=prompt("Please enter the reason :","");
		}
		$.post('/MeetingAttendances/declineAttendance',{meetingId:id , reason:reno},
		function()
		{
			$('#declined_'+id).show();
			$('#accept_'+id).hide();
			$('#decline_'+id).hide();
			$('#inv_'+id).hide();
			
		}
		);
	
}

function join_meeting(id)
{
	if(confirm("Join meeting?"))
	$.post('/Meetings/joinMeeting',{meetingID: id}
	,function()
	{
		$('#accepts_'+id).hide();
		$('#confirmedd_'+id).show();
	})
}

function confirm_me(id)
{
	if(confirm("Change your status to attending?"))
	$.post('/MeetingAttendances/confirmAttendance',{meetingId: id}
	,function()
	{
		$.bar({
			message : 'You accepted the invitation.'
		});
		$('#inv_'+id).hide();
		$('#accept_'+id).hide();
		$('#decline_'+id).hide();
		$('#confirmed_'+id).show();
		$('#inv_'+id).hide();

	})
}

function decline_me(id)
{
	var reno=prompt("Please enter the reason :","");
	while(reno.length==0)
	{
		reno=prompt("Please enter the reason :","");
	}
	$.post('/MeetingAttendances/declineAttendance',{meetingId:id , reason:reno},
	function()
	{
		$.bar({
			message : 'You Declined the invitation.'
		});
		$('#inv_'+id).hide();
		$('#declined_'+id).show();
		$('#accept_'+id).hide();
		$('#decline_'+id).hide();
	
	}
	);
}


function deleteStory(sId,box,d){
	message = "Are you sure you want to delete this story?";
	if(d)
		message+= " Some stories depend on it.";
	if((confirm(message)))
	{
		$.post('/Storys/delete', {id:sId}, function(data){
			$.bar({message:data});
			removeMe(box);
		});
	}
}





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
																	load($(this).attr('name') + ' .actual', $(this).attr('id'));
								

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
				if($(this).parent().next().html()=='')
					load2($(this).parent().parent().attr('name') + ' .actual',$(this).parent().parent().attr('id'));

				

				$(this).parent().next().slideToggle(400);

			});

	$('.revertFrom').live('click', function() {
		var url = $(this).parent().parent().attr('name');
		myDivs = $.grep(myDivs, function(value) {
	
		    return value != url;
		});
		var url2 = $(this).parent().parent().attr('name')+' .actual';
		myDivs = $.grep(myDivs, function(value) {
	
		    return value != url2;
		});
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
	load2(url,el)
		myDivs.push(url);


}}function load2(url, el) {

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
		

	});
}

function removeMe(me)
{
	$(me).closest('.ui-widget-content').slideUp(function() {
		$(this).remove()
	});
}


function loadBox(url, el) {
	
if($.inArray(url,myDivs)==-1){
	$('#' + el).append('<div style="position:absolute;z-index:0"id="myTemp"></div>');
	
	$('#' + el + ' #myTemp').load(url, function() {
		$('#' + el + ' #myTemp').children().attr('name',url);
		$('#' + el + ' #myTemp').children().css('position','absolute!important');

		$('#' + el + ' #myTemp').children().css('z-index','4');
		$('#' + el + ' #myTemp').replaceWith($('#' + el + ' #myTemp').html());
		myDivs.push(url);
	});
	}
}
var num =1;
function magic(id) {

	$("#" + id + "_content div[name]")
			.each(
					function() {
						if($(this).attr('class')=='overlay')
						{
							var id2 = "ui" +num;
							num++;
							var head = '<div id="'+id2+'_header" onclick="overlayOpen(\''+$(this).attr('name')+'\')" class="ui-widget-header">' + $(this).html() + '</div>';
							$(this).html(head);
							$(this).attr('id', id2);
							$(this).addClass('ui-widget-content');
						}
						else{

						var url = $(this).attr('name');
						var url2 = url+' .actual';
						if($.inArray(url,myDivs)==-1 && $.inArray(url2,myDivs)==-1){
							
							var id2 = "ui" +num++;
						var head = '<div id="'+id2+'_header" class="ui-widget-header mainH"><span class="revertFrom"><span class="ui-icon ui-icon-circle-close"></span></span><span class="min"onclick="$(this).children().toggle();"><span class="ui-icon ui-icon-circle-triangle-n" style="display: none;"></span><span class="ui-icon ui-icon-circle-triangle-s"></span></span>' + $(this).html() + '</div>';
						$(this).html(head);
						$(this).addClass('ui-widget-content draggableChild');

						$(this).attr('id', id2);
						$(this)
								.append(
										'<div id="' + id2 + '_content" class="ui-widget-content" ></div>');
						}
						else
						{	//alert($($(this).closest('.workspaceContainer').find('div[name='+$(this).attr('name')+']')).first().attr('id'));
							var id2= $($(this).closest('.workspaceContainer').find('div[name='+$(this).attr('name')+']')).first().attr('id')+'_2';
							var head = '<div id="'+id2+'_header" class="ui-widget-header mainH">' + $(this).html() + '</div>';
							$(this).html(head);
							$(this).addClass('clone');

							$(this).attr('id', id2);	
						}
						
						}
					});

}
var myDivs = new Array();
function show(id) {
	$('#normal').hide();
	$('#myHeaders').show();
	$('#myWorkspaces').show();
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

function deleteTheTask(tId, box){
	
	if((confirm('Are you sure you want to delete the Task ?')))
	{
	
	$.post('/tasks/delete', {id:tId}, function(data){ $.bar({message:data}
	);
	removeMe(box);
	;});
	}
}

function set_attended(id)
{
	if(confirm("change status to attended??"))
	$.post('/meetingAttendances/setConfirmed',{id: id}
	,function()
	{
		$('#reason_span_'+id).hide();
		$('#attendance_of_user_'+id).html("attended");
	})
}

function set_didnot_attend(id)
{
	var reno=prompt("Please enter the reason :","");
	while(reno.length==0)
	{
		reno=prompt("Please enter the reason :","");
	}
	
		$.post('/meetingAttendances/setDeclined',{id:id , reason:reno},
		function()
		{
			$('#reason_span_'+id).show();
			$('#attendance_of_user_'+id).html("did not attend");
			$('#reason_of_user_'+id).html(reno);
				
	})
	
}
