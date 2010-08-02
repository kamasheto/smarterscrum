﻿		var getNotifications = function() {
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

var CURRENT_PROJECT = 0;

function request_accept( id, hash )
{
	$.post('/requests/requestAccept' ,
		    {hash:hash, id:id} ,
		    function(){
				reload('reviewer-requests')
		    })
}
function request_accept2( id, hash )
{
	$.post('/requests/deletionRequestAccept' ,
		    {hash:hash} ,
		    function(){
				reload('reviewer-requests')
		    })
		}
function request_ignore( id, hash)
{
	$.post('/requests/requestIgnore' ,
		    {hash:hash} ,
		    function(){
				reload('reviewer-requests')
		    })
		}
function show_comment(id){

	$('#comment_'+id).show();
	}
function do_ignore(id, hash){
	var textValue = $('#text_'+id).val();
	if(textValue == '')
		$.bar({message:'You must Enter a reason why you declined this request!'})
	else
	{
		$.post('/requests/requestIgnore' ,
		    {hash:hash, body:textValue} ,
		    function(){
		    	$('#req_'+id).remove();
				$('#comment_'+id).remove();
		    })
	}
}
function doOnLoad() {
	
	$('.dim').live('mouseover', function() {
		$(this).click(function() {	
		})
	})
		$('.formatDate').each(function(){
			if (!$(this).data('processed')) {
				$(this).data('processed', true)
				$(this).html( formatDate( new Date(getDateFromFormat($(this).html(),'yyyy-MM-dd HH:mm:ss')), 'd MMM, yyyy') );	
			}
		});
		$('.formatTime').each(function(){
			if (!$(this).data('processed')) {
				$(this).data('processed', true)
				$(this).html( formatDate( new Date(Number($(this).html())), 'd MMM, yyyy hh:mma') );	
			}
		});
	
	    $("a").tipTip({delay:0});
	    $("td").tipTip({delay:0});
	    $("span").tipTip({delay:0});
	 $("div").tipTip({delay:0});
	 $("img").tipTip({delay:0});
	    $('div.crudField').each(function(){
				if ($(this).html().trim() == '') {
					$(this).remove();
				}
		    });
}
function delete_meeting(id, pId)
{
	var confirmation= confirm("Are you sure you want to delete this meeting ?");
	
	if (confirmation) {
		$.post('/Meetings/deleteMeeting', {
			id:id
		}, function(){
		// removeMe(pId);	
		});
	};		
}

function join_meeting(id)
{
	if(confirm("Join meeting?"))
	$.post('/Meetings/joinMeeting',{meetingID: id}
	,function(flag)
	{
		if(flag)
			{
			$.bar({ message:'You have successfully joined the meeting.' })
		$('#accepts_'+id).hide();
		$('#confirmedd_'+id).show();
			}
		else
			{
			$.bar({
				message : 'Meeting has already ended.'
			});
			$('#accepts_'+id).hide();
			}
	})
}

function confirm_me(id)
{
	if(confirm("Change your status to attending?"))
	$.post('/MeetingAttendances/confirmAttendance',{meetingId: id}
	,function(flag)
	{
		if(flag)
			{
		$.bar({
			message : 'You accepted the invitation.'
		});
		$('#inv_'+id).hide();
		$('#accept_'+id).hide();
		$('#decline_'+id).hide();
		$('#confirmed_'+id).show();
		$('#inv_'+id).hide();
			}
		else
			{
			$('#inv_'+id).hide();
			$('#accept_'+id).hide();
			$('#decline_'+id).hide();
			
				$.bar({
					message : 'Meeting has already ended.'
				});
			}

	})
}

function requestRole(roleIdd){
	$.post('/projecttasks/requestRole', {id:roleIdd}, function(msg){
		$.bar({message:msg});
		reload('roles')
	});
}

function revokeRole(roleIdd){
	var confirmation= confirm("Are you sure you want to revoke this role ?");
	if (confirmation) {
		$.post('/projecttasks/revokeRole', {id:roleIdd}, function(msg){
		$.bar({message:msg});
	});
	};
}

function deleteRequest(roleIdd){
	if(confirm("Are you sure you want to cancel your request?")){
		$.post('/requests/removeRequestRoleInProject', {roleId:roleIdd}, function(msg){
			$.bar({message:msg});
			reload('roles')
		});
	}
}

function requestToBeReviewer(taskTypeId){
	$.post('/requestreviewers/requestToBeReviewer', {ID:taskTypeId}, function(msg){
		$.bar({message:msg});
		reload('reviewer-requests')
	});
}

function removeRequestToBeReviewer(id){
	$.post('/requestreviewers/removerequest', {taskTypeId:id}, function(msg){
		$.bar({message:msg});
		reload('reviewer-requests')
	});
}

function decline_me(id)
{
	var reno=prompt("Please enter the reason :","");
	while(reno.length==0)
	{
		reno=prompt("Please enter the reason :","");
	}
	$.post('/MeetingAttendances/declineAttendance',{meetingId:id , reason:reno},
	function(flag)
	{
		if(flag)
			{
		$.bar({
			message : 'You Declined the invitation.'
		});
		$('#inv_'+id).hide();
		$('#declined_'+id).show();
		$('#accept_'+id).hide();
		$('#decline_'+id).hide();
			}
		else
			{
			$.bar({
				message : 'Meeting has already ended.'
			});
			$('#inv_'+id).hide();
			$('#accept_'+id).hide();
			$('#decline_'+id).hide();
			}
	
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
			// removeMe(box);
		});
	}
}


$(function() {
	$('.draggable').live('mouseover', function() {
		var con = $(this).closest('.workspaceDraggables').attr('id');
		$(this).data('init', 1);
		$(this).draggable( {
			handle : '.ui-widget-header',
			cancel : 'img',
			stack  : '.draggable',
			containment: '#'+con
		});
		var h = $(this).height();
		var w = $(this).width();
		$(this).resizable( {
			containment: '#'+con,
			autoHide : true,
			disable: false,
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

									if ($(this).hasClass('draggableChild'))
										is = true;
		
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
									$(this).closest('.workspaceDraggables')
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
								load($(this).attr('name') + ' .actual', $(this).attr('id'),1);
								$(this).resizable({disabled: true});

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

	$('.draggableChild .ui-widget-header').live(
			'click',
			function() {
				if($(this).next().html()=='')
					load($(this).parent().attr('name') + ' .actual',$(this).parent().attr('id'),3);

				$(this).next().slideToggle(400);

			});

	$('.min').live(
			'click',
			function() {
				if($(this).parent().next().html()=='')
					load($(this).parent().parent().attr('name') + ' .actual',$(this).parent().parent().attr('id'),2);



				$(this).parent().next().slideToggle(400);

			});

	$('.revertFrom').live('click', function() {
		var url = $(this).parent().parent().attr('name');
		removeFromDiv(url);
		var theId = $(this).parent().parent().attr('id');
		var theSecondId = theId + "_2";
		if ($('#' + theSecondId).attr('id') == null)
			$('#' + theId).remove();
		else {
			var elHtml = $('#' + theSecondId).html();
			$('#' + theId).removeAttr('style');
			$('#' + theId).removeClass('draggable');
			$('#' + theId).addClass('draggableChild');
			$('#' + theSecondId).replaceWith($('#' + theId));
			$('#' + theId).find('.mainH').first().html(elHtml);
		}
	
		});
	});

		
function removeFromDiv(url)
{
	myDivs = $.grep(myDivs, function(value) {

	    return value != url;
	});
	var url2 = url+' .actual';
	myDivs = $.grep(myDivs, function(value) {
		
	    return value != url2;
	});	
}
		
function load(url, el, n) {
	if($.inArray(url,myDivs)==-1 || n==2){
		var pUrl = $('#'+el).attr('name');
		if(n!=3)
		$('#'+el+'_header').load(pUrl+' .mainH', function(){
			$('#'+el+'_header').html($('#'+el+'_header').find('.mainH').first().html());						
		});
		$('#' + el + '_content').load(url, function() {
			$('#' + el + ' .min').first().show();
			$('#' + el + '_content').slideDown(400);
			magic(el);
		});
	}
	if(n==1)
		myDivs.push(url);
}


function loadBox(url, el) 
{
	if($.inArray(url,myDivs)==-1)
	{
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

function magic(id) {
	doOnLoad();
	$("#" + id + "_content div[name]").each(
	function() 
	{
						if($(this).attr('class')=='overlay')
						{
							var id2 = "ui" +num;
							num++;
							var head = '<div id="'+id2+'_header" class="ui-widget-header"><a href="#" onclick="overlayOpen(\''+$(this).attr('name')+'\')"><span class="ui-icon ui-icon-extlink"></span></a>' + $(this).html()+ '</div>';
							$(this).html(head);
							$(this).attr('id', id2);
							$(this).addClass('ui-widget-content');
						}
		else
		{
						var url = $(this).attr('name');
						var url2 = url+' .actual';
			if($.inArray(url,myDivs)==-1 && $.inArray(url2,myDivs)==-1)
			{
						var id2 = "ui" +num++;
						var head = '<div id="'+id2+'_header" class="ui-widget-header mainH"><span class="revertFrom"><span class="ui-icon ui-icon-circle-close"></span></span>' + $(this).html() + '</div>';
						$(this).html(head);
						$(this).addClass('ui-widget-content draggableChild');

						$(this).attr('id', id2);
				$(this).append('<div id="' + id2 + '_content" class="ui-widget-content" ></div>');
						}
						else
			{	var id2= $($(this).closest('.workspaceDraggables').find('div[name='+$(this).attr('name')+']')).first().attr('id')+'_2';
							var head = '<div id="'+id2+'_header" class="ui-widget-header mainH">' + $(this).html() + '</div>';
							$(this).html(head);
							$(this).addClass('clone');

							$(this).attr('id', id2);	
						}
						
						}
					});

}
var myDivs = new Array();
var num =1;
function deleteTheTask(tId, box){
	
	if((confirm('Are you sure you want to delete the Task ?')))
	{
	
	$.post('/tasks/delete', {id:tId}, function(data){ $.bar({message:data}
	);
	// removeMe(box);
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
searching_projects = null
function search_projects() {
	if ($('#project_search_text').val() == '') {
		$('#projects_search_results').html('')
		return
	}
	$('#projects_search_results').html('<img src="/public/images/loadingMagic.gif">')
	if (searching_projects) {
		searching_projects.abort()
	}
	searching_projects = $.post('/ajax/projects', {query: $('#project_search_text').val()}, function(projects) {
		results = ''
		$(projects).each(function() {
			results += '<div id="project-search-result-'+this.id+'"><a onclick="showProjectWorkspace('+this.id+')">'+this.name+'</a></div>'
		})
		$('#projects_search_results').html(results)
	})
}

function show(id) {
	if (!id) {
		$('#workspaces').hide()
		$('#normal').show()
		$('.project-button').removeClass('selectedADiv')
	} else if ($('.workspace-'+id).length) {
		CURRENT_PROJECT = id;
		// make sure we have that thingie first
		// DO NOT load workspace here, it might have been removed on purpose!
		$('#normal').hide();
		$('#workspaces').show()
		$('.workspace').hide()
		$('.workspace-' + id).show()
		$('.project-button').removeClass('selectedADiv')
		$('#project-button-'+id).addClass('selectedADiv')	
	}
}
function closeWindows(el)
{
	$('#workspace-'+el+' div[name]').each(function(){
		removeFromDiv($(this).attr('name'));
		$(this).remove();
	});
	$('#chatContainer_'+el).hide();
}

function close_workspace(project_id) {
	$('#project-button-'+project_id).fadeOut(function() {
		$(this).remove()
	})
	closeWindows(project_id)
	$('.workspace-'+project_id).remove()
	show(0)
}


function showProjectWorkspace(project_id) {
	if ($('.workspace-'+project_id).length) {
		// workspace already loaded, just show it instead
		$('#top_header_projects_pane').slideUp()
		show(project_id)
		return
	}
	$('#workspaces').append('<div class="workspace workspace-'+project_id+'"><div style="width:32px;margin:auto;margin-top:200px;"><img src="/public/images/loadingMagic.gif"></div></div>')
	show(project_id)
	// return
	$.ajax({
		async: false,
		url: '/show/workspace',
		data: {id: project_id},
		success: function(data) {
			$('#project-tabs').append('<a class="aDIV topCornersRounded right project-button selectedADiv" id="project-button-'+project_id+'" href="#" onclick="show('+project_id+')" style="width: 120px !important" title="">'+$(data).find('.project_name_in_header').html()+' <span class="right ui-icon ui-icon-circle-close" onclick="close_workspace('+project_id+')"> </span></a>');
			$('.workspace-' + project_id).html(data)
			$('#top_header_projects_pane').slideUp()
		}
	})
}

function reload() {
	for (i in arguments) {
		sel = '.reload-' + arguments[i]
		div = $(sel, '#workspace-' + CURRENT_PROJECT)
		div.each(function() {
			url = div.attr('name')
			div.find('.actual:first').html('<div class="bar center"><img src="/public/images/loadingMagic.gif"></div>')
			load(url + ' .actual', div.attr('id'), 2)	
		})
	}
}
function deleteTheComponent(cId, box){
	if((confirm('Are you sure you want to delete the component?')))
	{
		$.post('/components/delete', {id:cId}, function(data){ 
				$.bar({message:data});
				// removeMe(box);
				reload('components')
			});
	}
}