/**********************************
Purpose : Filter magic box choices
How : The magic boxes are created using a certain format. Get the input from the user, loop on the divs we have and fetch those that match and hide the others
***********************************/
String.prototype.trim = function() { return this.replace(/^\s+|\s+$/, ''); };

function filter_me(id){
	//get the text in the text box
	var input = $("[name='filter_textBox_"+id+"']").val();
	if(input.trim().length == 0){
		//loop on all divs located inside the id_content and show them.
		smart_pagination(id,page,false);
	}
	else
	{	
		//loop on all divs that are located inside the div : id_content and hide them if does not contain the input ..
		$("#"+id+"_content > div").filter(function(index) {
			var inputCased = input.toLowerCase();
			var test = $(this).text().toLowerCase().split(inputCased);
			//to show the result even if not in the current page after applying the pagination
			if(test.length != 1 && $(this).is(':hidden'))
				$(this).show();
			return test.length == 1;
		}).hide();
		smart_pagination(id,page,true);
	}
}

function smart_pagination(id, view_page, filtered){
	/**************************
	 * id : the id of the parent div to be paginated
	 * view_page : pages 1 2 3 ..etc.
	 * update : whether to update the generated array
	 ***************************/
	/********** Settings *************/
	var itemsPerPage = 5; //how many divs to display per page
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
		if(!filtered)
		{
			$("#"+id+"_content > div").show();
			$("#"+id+"_content > div").each(function(index){
				smart_array[i] = this;
				$(this).hide();
				i++;
			});
		}
		else
		{
			//we have filtered betoo3 .. paginate them
			$("#"+id+"_content > div").filter(function(index){
				return !($(this).is(":hidden"));
			})
			.each(function(index){
				smart_array[i] = this;
				$(this).hide();
				i++;
			});
		}
		//Now calculate the number of pages
		var extraItems = !((smart_array.length % itemsPerPage) == 0); //whether there are items to be added in an extra page
		if(extraItems)
			numPages++;
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

function nextPage(id,page){
	smart_pagination(id,page,false);
}

function previousPage(id,page){
	smart_pagination(id,page,false);
}
