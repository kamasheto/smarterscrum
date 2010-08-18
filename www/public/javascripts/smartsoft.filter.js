/**********************************
Purpose : Filter magic box choices
How : The magic boxes are created using a certain format. Get the input from the user, loop on the divs we have and fetch those that match and hide the others
***********************************/

/********** Settings *************/
// commented out by mahmoudsakr
// this variable definition has moved to Application/index.html
// var itemsPerPage = 5; //default number of items per page



/****************** 7ewar el array of objects *************/

var uniqueArray = new Array();
var UniqueArrayFilter = new Array();
var notificationPage=1;

/**********************************************************/


/******** DO NOT EDIT ANYTHING BELOW THIS LINE .. plz? ***********/

String.prototype.trim = function() { return this.replace(/^\s+|\s+$/, ''); };


function filter_me(el){
	//get id from parent container
	var id = $(el).closest('.filter').attr('id').split("_")[0];
	//get the text in the text box
	var input = $(el).val();
	if(input.trim().length == 0){
		//loop on all divs located inside the id_content and show them.
		var smartObject = getTheUnique(id);
		if(smartObject == false){
			smartObject = new Object();
			smartObject.id = id;
			smartObject.numPages = -1;
			smartObject.smart_array = new Array();
			smartObject.page = 1;
			smartObject.itemsPerPage = itemsPerPage;
			uniqueArray.push(smartObject);
		}
		smart_pagination(id,smartObject.page);
		hideFilterLinks(id);
	}
	else
	{	
		hideNormalLinks(id);
		//showFilterLinks(id);
		//loop on all divs that are located inside the div : id_content and hide them if does not contain the input ..
		$("#"+id+"_content > div").filter(function(index) {
			var inputCased = input.toLowerCase();
			var test = $(this).text().toLowerCase().split(inputCased);
			//to show the result even if not in the current page after applying the pagination
			if(test.length != 1) $(this).show();
			return test.length == 1;
		}).hide();
		
		filter_smart_pagination(el,1,false);
	}
}

function smart_pagination(el, view_page, itemsChanged){
	var id = el;
	if($(el).closest('.filter').attr('id')!=null)
		id = $(el).closest('.filter').attr('id').split('_')[0];
	if(!($("#"+id+"_content").parent().hasClass("do_not_paginate")))
	{
		/********** Do not edit anything below this line ******/
		var smartObject = getTheUnique(id);
		//CHECKS
		if(smartObject == false){
			var sizeOfChildren = $("#"+id+"_content > div").size();
			var sizeOfNotPaginated= $("#"+id+"_content > div").filter(function(){ return $(this).hasClass("do_not_paginate") }).size();
			var numPagesNotPaginated = Math.floor(sizeOfNotPaginated/itemsPerPage);
			var extraNotPaginatedItems = !((sizeOfNotPaginated % itemsPerPage) == 0);
			if(extraNotPaginatedItems)
				numPagesNotPaginated++;
			//alert(numPagesNotPaginated);
			var numPages = Math.floor(sizeOfChildren/ itemsPerPage);
			var extraItems = !((sizeOfChildren % itemsPerPage) == 0); //whether there are items to be added in an extra page
			if(extraItems)
				numPages++;
			smartObject = new Object();
			smartObject.id = id;
			smartObject.numPages = numPages;
			smartObject.smart_array = new Array();
			if(!itemsChanged)
				smartObject.page = 1;
			smartObject.numElements = sizeOfChildren;
			smartObject.sizeOfNotPaginated = sizeOfNotPaginated;
			smartObject.numPagesNotPaginated = numPagesNotPaginated;
			if(itemsChanged){
				//alert($('#'+el).closest(".galal").first().html());
				smartObject.itemsPerPage = calculateItems(el);
			}
			else
				smartObject.itemsPerPage = itemsPerPage;
			uniqueArray.push(smartObject);
		}
		else if(smartObject.numPages == -1){
			smartObject.numPages = numPages;
		}
		if(view_page<1)
		{
			smartObject.page++;
		}
		else if(view_page>(smartObject.numPages-smartObject.numPagesNotPaginated)){
			if(!view_page == 1)
				smartObject.page--;
			else
			{
				$("#"+id+" .normalLinkp").addClass('dim');
				if(view_page >= (smartObject.numPages - smartObject.numPagesNotPaginated))
					$("#"+id+" .normalLinkn").addClass('dim');
			}
		}
		else
		{
			//First of all get all "shown" divs and store them in the smart_array
			var i = 0;
				$("#"+id+"_content > div").filter(function(index) { return !($(this).hasClass("do_not_paginate")); }).show();
				$("#"+id+"_content > div").filter(function(index) { return !($(this).hasClass("do_not_paginate")); }).each(function(index){
					smartObject.smart_array[i] = this;
					$(this).hide();
					i++;
				});
			if(itemsChanged){
				smartObject.itemsPerPage = calculateItems(el);
				sizeOfChildren = $("#"+id+"_content > div").size();
				sizeOfNotPaginated= $("#"+id+"_content > div").filter(function(){ return $(this).hasClass("do_not_paginate") }).size();
				numPagesNotPaginated = Math.floor(sizeOfNotPaginated/smartObject.itemsPerPage);
				extraNotPaginatedItems = !((sizeOfNotPaginated % smartObject.itemsPerPage) == 0);
				if(extraNotPaginatedItems)
					numPagesNotPaginated++;
				numPages = Math.floor(sizeOfChildren/ smartObject.itemsPerPage);
				extraItems = !((sizeOfChildren % smartObject.itemsPerPage) == 0); //whether there are items to be added in an extra page
				if(extraItems)
					numPages++;
				smartObject.numElements = sizeOfChildren;
				smartObject.sizeOfNotPaginated = sizeOfNotPaginated;
				smartObject.numPagesNotPaginated = numPagesNotPaginated;
				smartObject.numPages = numPages;
			}
			//Now display only those in the current page
			view_page--;//if page 1, the starting index should be zero (array ba2a)
			var starting_index = view_page * smartObject.itemsPerPage;
			var j = 1;//counter
			while(j<= smartObject.itemsPerPage){
				$(smartObject.smart_array[starting_index]).show();
				starting_index++;
				j++;
			}
			view_page++;
			//alert("view page:"+view_page+" out of "+(smartObject.numPages-smartObject.numPagesNotPaginated));
			if(view_page == (smartObject.numPages-smartObject.numPagesNotPaginated)){
				$("#"+id+" .normalLinkn").addClass('dim');
			}
			else{
				if($("#"+id+" .normalLinkn").hasClass('dim'))
					$("#"+id+" .normalLinkn").removeClass('dim');
			}
			if(view_page == 1){
				$("#"+id+" .normalLinkp").addClass('dim');
			}
			else{
				if($("#"+id+" .normalLinkp").hasClass('dim'))
					$("#"+id+" .normalLinkp").removeClass('dim');
			}
		}
		updatePageNumbers(id);
	}
	else{
		$("#"+id+"_content > div").show();
		var totalItems = $("#"+id+"_content > div").filter(function(index) { return !($(this).hasClass("do_not_paginate")); }).size();
		$("#"+id+" .numPages").text(totalItems+"/"+totalItems);
		$("#"+id+" .normalLinkp").addClass('dim');
		$("#"+id+" .normalLinkn").addClass('dim');
	}
}

function filter_smart_pagination(el,view_page, nextPrevious){
	var id = el;
	if($(el).closest('.filter').attr('id')!=null)
		id = $(el).closest('.filter').attr('id').split('_')[0];
	if(!($("#"+id+"_content").parent().hasClass("do_not_paginate")))
	{
		var smartObject = getTheUniqueFilter(id);
		var original = getTheUnique(id);
		var height = original.itemsPerPage;
		if(smartObject == false){
			//alert("==false");
			smartObject = new Object();
			smartObject.id = id;
			smartObject.filter_page = 1;
			//smartObject.numPages = -1;
			smartObject.filter_smart_array = new Array();
			UniqueArrayFilter.push(smartObject);
		}
		if(!nextPrevious)
		{
			smartObject.sizeOfFilteredChildren = $("#"+id+"_content > div").filter(":visible").filter(function(index) { return !($(this).hasClass("do_not_paginate")); }).size();
		}
		var numPages = Math.floor(smartObject.sizeOfFilteredChildren/ height);
		var extraItems = !((smartObject.sizeOfFilteredChildren % height) == 0); //whether there are items to be added in an extra page
		if(extraItems)
			numPages++;
		smartObject.numPages = numPages;
		//alert("num"+numPages)
		if(view_page<1)
		{
			if(!(smartObject.numPages == 0))
				smartObject.filter_page++;
		}
		else if(view_page>smartObject.numPages){
			if(!(smartObject.numPages == 0))
				smartObject.filter_page--;
		}
		else
		{
			if(!nextPrevious)
			{
				var i = 0;
				smartObject.filter_page=1;
				smartObject.filter_smart_array = new Array();
				$("#"+id+"_content > div").filter(":visible").filter(function(index) { return !($(this).hasClass("do_not_paginate")); }).each(function(index){
					smartObject.filter_smart_array[i] = this;
					$(this).hide();
					i++;
				});
			}
			
			//Now display only those in the current page
			view_page--;//if page 1, the starting index should be zero (array ba2a)
			var starting_index = view_page * height;
			var j = 1;//counter
			$(smartObject.filter_smart_array).each(function(index){ $(this).hide(); });
			while(j<= height){
				$(smartObject.filter_smart_array[starting_index]).show();
				starting_index++;
				j++;
			}
			view_page++;
			if(view_page == smartObject.numPages){
				$("#"+id+" .filterLinkn").addClass('dim');
			}
			else{
				if($("#"+id+" .filterLinkn").hasClass('dim'))
					$("#"+id+" .filterLinkn").removeClass('dim');
			}
			if(view_page == 1){
				$("#"+id+" .filterLinkp").addClass('dim');
			}
			else{
				if($("#"+id+" .filterLinkp").hasClass('dim'))
					$("#"+id+" .filterLinkp").removeClass('dim');
			}
		}
		if(smartObject.numPages == 0)
			smartObject.filter_page=0;
		updatePageNumbersFilter(id);
	}
	else{
		var totalItems = $("#"+id+"_content > div").filter(":visible").filter(function(index) { return !($(this).hasClass("do_not_paginate")); }).size();
		$("#"+id+" .numPages").text(totalItems+"/"+totalItems);
	}
}

function notification_pagination(view_page){
	var smart_array = new Array();
	var sizeOfChildren = $("#notification_paginated > div").size();
	var numPages = Math.floor(sizeOfChildren/ itemsPerPage);
	var extraItems = !((sizeOfChildren % itemsPerPage) == 0); //whether there are items to be added in an extra page
	if(extraItems)
		numPages++;
	if(view_page<1)
	{
		notificationPage++;
	}
	else if(view_page>numPages){
		notificationPage--;
	}
	else
	{
		//First of all get all "shown" divs and store them in the smart_array
		var i = 0;
			$("#notification_paginated > div").show();
			$("#notification_paginated > div").each(function(index){
				smart_array[i] = this;
				$(this).hide();
				i++;
			});
		
		//Now display only those in the current page
		view_page--;//if page 1, the starting index should be zero (array ba2a)
		var starting_index = view_page * itemsPerPage;
		var j = 1;//counter
		$("#notification_paginated > div").hide();
		while(j<= itemsPerPage){
			$(smart_array[starting_index]).show();
			starting_index++;
			j++;
		}
		view_page++;
		if(view_page == numPages){
			$(".Notificationn").addClass('dim');
		}
		else{
			if($(".Notificationn").hasClass('dim'))
				$(".Notificationn").removeClass('dim');
		}
		if(view_page == 1){
			$(".Notificationp").addClass('dim');
		}
		else{
			if($(".Notificationp").hasClass('dim'))
				$(".Notificationp").removeClass('dim');
		}
	}
}

function nextPage(el){
	id = el;
	if($(el).closest('.filter').attr('id')!=null){
		id = $(el).closest('.filter').attr('id').split('_')[0];
	}
	var smartObject = getTheUnique(id);
	smartObject.page++;
	smart_pagination(el,smartObject.page);
}

function previousPage(el){
	if($(el).closest('.filter').attr('id')!=null){
		id = $(el).closest('.filter').attr('id').split('_')[0];
	}
	var smartObject = getTheUnique(id);
	smartObject.page--;
	smart_pagination(el,smartObject.page);
}

function nextNotificationPage(){
	notificationPage++;
	notification_pagination(notificationPage);
}

function previousNotificationPage(){
	notificationPage--;
	notification_pagination(notificationPage);
}

function nextFilterPage(el){
	if($(el).closest('.filter').attr('id')!=null){
		id = $(el).closest('.filter').attr('id').split('_')[0];
	}
	smartObject = getTheUniqueFilter(id);
	smartObject.filter_page++;
	filter_smart_pagination(el,smartObject.filter_page,true);
}

function previousFilterPage(el){
	if($(el).closest('.filter').attr('id')!=null){
		id = $(el).closest('.filter').attr('id').split('_')[0];
	}
	smartObject = getTheUniqueFilter(id);
	smartObject.filter_page--;
	filter_smart_pagination(el,smartObject.filter_page, true);
}

function hideNormalLinks(id){
	$("#"+id+" .filterLinkn").show();
	$("#"+id+" .filterLinkp").show();
	$("#"+id+" .normalLinkn").hide();
	$("#"+id+" .normalLinkp").hide();
}

function showNormalLinks(id){
	$("#"+id+" .normalLinkn").show();
	$("#"+id+" .normalLinkp").show();
	$("#"+id+" .filterLinkn").hide();
	$("#"+id+" .filterLinkp").hide();
}

function hideFilterLinks(id){
	$("#"+id+" .filterLinkn").hide();
	$("#"+id+" .filterLinkp").hide();
	$("#"+id+" .normalLinkn").show();
	$("#"+id+" .normalLinkp").show();
}

function showFilterLinks(id){
	$("#"+id+" .filterLinkn").show();
	$("#"+id+" .filterLinkp").show();
	$("#"+id+" .normalLinkn").hide();
	$("#"+id+" .normalLinkp").hide();
}

function updatePageNumbers(id){
	smartObject = getTheUnique(id);
	var items = (smartObject.page*smartObject.itemsPerPage);
	var i = $("#"+id+"_content > div").filter(function(){ return $(this).hasClass("do_not_paginate");}).size();
	items = items + i;
	if(items>smartObject.numElements)
		items = smartObject.numElements;
	else if(items<1) items = 0;
	$("#"+id+" .numPages").text(items+"/"+smartObject.numElements);
}

function updatePageNumbersFilter(id){
	smartObject = getTheUniqueFilter(id);
	original = getTheUnique(id);
	height = original.itemsPerPage;
	var items = (smartObject.filter_page*height);
	var i = $("#"+id+"_content > div").filter(":visible").filter(function(){ return $(this).hasClass("do_not_paginate"); }).size();
	items = items + i;
	if(items>smartObject.sizeOfFilteredChildren)
		items = smartObject.sizeOfFilteredChildren;
	else if(items<1) items = 0; 
	$("#"+id+" .numPages").text(items+"/"+smartObject.sizeOfFilteredChildren);
}

function getTheUnique(id){
	for(i=0;i<uniqueArray.length;i++){
		if(uniqueArray[i].id == id){
			return uniqueArray[i];
		}
	}
	return false;
}

function getTheUniqueFilter(id){
	for(i=0;i<UniqueArrayFilter.length;i++){
		if(UniqueArrayFilter[i].id == id){
			return UniqueArrayFilter[i];
		}
	}
	return false;
}

function calculateItems(el){
	var obj = $(el).parent().parent().parent().parent().parent().parent().parent();
	var height = obj.height();
	var items = Math.floor(height/40);
	$(obj).css('height','');
	return items;
}