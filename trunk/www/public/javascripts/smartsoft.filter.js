/**********************************
Purpose : Filter magic box choices
How : The magic boxes are created using a certain format. Get the input from the user, loop on the divs we have and fetch those that match and hide the others
***********************************/

/********** Settings *************/
var itemsPerPage = 5; //how many divs to display per page



/******** DO NOT EDIT ANYTHING BELOW THIS LINE .. plz? ***********/

String.prototype.trim = function() { return this.replace(/^\s+|\s+$/, ''); };

var page=1;
var filter_page = 1;
var filter_smart_array = new Array();
var globalSizeOfFilteredChildren;

function filter_me(id){
	//get the text in the text box
	var input = $("[name='filter_textBox_"+id+"']").val();
	if(input.trim().length == 0){
		//loop on all divs located inside the id_content and show them.
		smart_pagination(id,page);
		showNormalLinks();
		hideFilterLinks();
	}
	else
	{	
		hideNormalLinks();
		showFilterLinks();
		//loop on all divs that are located inside the div : id_content and hide them if does not contain the input ..
		$("#"+id+"_content > div").filter(function(index) {
			var inputCased = input.toLowerCase();
			var test = $(this).text().toLowerCase().split(inputCased);
			//to show the result even if not in the current page after applying the pagination
			if(test.length != 1) $(this).show();
			return test.length == 1;
		}).hide();
		filter_smart_pagination(id,filter_page,false);
	}
}

function smart_pagination(id, view_page){
	/**************************
	 * id : the id of the parent div to be paginated
	 * view_page : pages 1 2 3 ..etc.
	 ***************************/
	/********** Do not edit anything below this line ******/
	//CHECKS
	var sizeOfChildren = $("#"+id+"_content > div").size();
	var numPages = Math.floor(sizeOfChildren/ itemsPerPage);
	var extraItems = !((sizeOfChildren % itemsPerPage) == 0); //whether there are items to be added in an extra page
	if(extraItems)
		numPages++;
	if(view_page<1)
	{
		page++;
	}
	else if(view_page>numPages){
		page--;
	}
	else
	{
		//First of all get all "shown" divs and store them in the smart_array
		var smart_array = new Array();
		var i = 0;
			$("#"+id+"_content > div").show();
			$("#"+id+"_content > div").each(function(index){
				smart_array[i] = this;
				$(this).hide();
				i++;
			});
		
		//Now display only those in the current page
		view_page--;//if page 1, the starting index should be zero (array ba2a)
		var starting_index = view_page * itemsPerPage;
		var j = 1;//counter
		while(j<= itemsPerPage){
			$(smart_array[starting_index]).show();
			starting_index++;
			j++;
		}
	}
}

function filter_smart_pagination(id,view_page, nextPrevious){
	if(!nextPrevious)
	{
		var sizeOfFilteredChildren = $("#"+id+"_content > div").filter(":visible").size();
		globalSizeOfFilteredChildren = sizeOfFilteredChildren;
	}
	var numPages = Math.floor(globalSizeOfFilteredChildren/ itemsPerPage);
	var extraItems = !((globalSizeOfFilteredChildren % itemsPerPage) == 0); //whether there are items to be added in an extra page
	if(extraItems)
		numPages++;
	if(view_page<1)
	{
		filter_page++;
	}
	else if(view_page>numPages){
		filter_page--;
	}
	else
	{
		if(!nextPrevious)
		{
			var i = 0;
			filter_page=1;
			filter_smart_array = new Array();
			$("#"+id+"_content > div").filter(":visible").each(function(index){
				filter_smart_array[i] = this;
				$(this).hide();
				i++;
			});
		}
		
		//Now display only those in the current page
		view_page--;//if page 1, the starting index should be zero (array ba2a)
		var starting_index = view_page * itemsPerPage;
		var j = 1;//counter
		$(filter_smart_array).each(function(index){ $(this).hide(); });
		while(j<= itemsPerPage){
			$(filter_smart_array[starting_index]).show();
			starting_index++;
			j++;
		}
	}
}

function nextPage(id,page){
	smart_pagination(id,page);
}

function previousPage(id,page){
	smart_pagination(id,page);
}

function nextFilterPage(id){
	filter_smart_pagination(id,filter_page,true);
}
function previousFilterPage(id){
	filter_smart_pagination(id,filter_page, true);
}

function hideNormalLinks(){
	$("[name='normal_links']").hide();
}

function showNormalLinks(){
	$("[name='normal_links']").show();
}

function hideFilterLinks(){
	$("[name='filter_links']").hide();
}

function showFilterLinks(){
	$("[name='filter_links']").show();
}