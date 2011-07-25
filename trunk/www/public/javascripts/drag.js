"use strict";var REDIPS={};REDIPS.drag=(function(){var init,img_onmousemove,handler_onmousedown,handler_onmouseup,handler_onmousemove,handler_onresize,set_trc,set_color,box_offset,calculate_cells,getScrollPosition,autoscrollX,autoscrollY,clone_object,clone_limit,elementControl,trash_delete,enable_drag,save_content,obj_margin=null,mouseButton=0,mouseX=null,mouseY=null,window_width=0,window_height=0,scroll_width=null,scroll_height=null,edgeX=0,edgeY=0,bgcolor_old=null,tables=[],autoscrollX_flag=0,autoscrollY_flag=0,moved_flag=0,cloned_flag=0,cloned_id=[],currentCell=[],div_drag=null,div_box=null,table=null,table_old=null,table_source=null,row=null,row_old=null,row_source=null,cell=null,cell_old=null,cell_source=null,obj=false,obj_old=false,hover_color='#CCFFCC',hover_color1='#E7AB83';var bound=25,speed=20,mark_cname='mark',marked_cell='deny',marked_exception=[],trash='trash',trash_ask=true,drop_option='multiple',delete_cloned=true,target_cell=null,source_cell=null,clone_ctrlKey=false;init=function(){var self=this,i,j,imgs,evt,element,table_nested,tables_nodeList;div_drag=document.getElementById('drag');tables_nodeList=div_drag.getElementsByTagName('table');for(i=0,j=0;i<tables_nodeList.length;i++){table_nested=true;element=tables_nodeList[i].parentNode;do{if(element.id==='drag'){table_nested=false;break;}
if(element.nodeName==='TABLE'){break;}
element=element.parentNode;}while(element);if(!table_nested){tables[j]=tables_nodeList[i];j++;}}
handler_onresize();window.onresize=handler_onresize;enable_drag(true);imgs=div_drag.getElementsByTagName('img');for(i=0;i<imgs.length;i++){imgs[i].onmousemove=img_onmousemove;}
div_drag.onselectstart=function(e){evt=e||window.event;if(!elementControl(evt)){return false;}};window.onscroll=calculate_cells;};img_onmousemove=function(){return false;};handler_onmousedown=function(e){var evt=e||window.event,offset;if(elementControl(evt)){return true;}
REDIPS.drag.obj_old=obj_old=obj||this;REDIPS.drag.obj=obj=this;if(obj.className.indexOf('clone')===-1){obj.style.zIndex=999;}
mouseX=evt.clientX;mouseY=evt.clientY;set_trc(evt);table_source=table;row_source=row;cell_source=cell;REDIPS.drag.source_cell=source_cell=tables[table_source].rows[row_source].cells[cell_source];if(evt.which){mouseButton=evt.which;}
else{mouseButton=evt.button;}
if(mouseButton===1){moved_flag=0;cloned_flag=0;document.onmousemove=handler_onmousemove;document.onmouseup=handler_onmouseup;REDIPS.drag.myhandler_clicked();}
if(table!==null||row!==null||cell!==null){bgcolor_old=tables[table].rows[row].cells[cell].style.backgroundColor;}
offset=box_offset(obj);obj_margin=[mouseY-offset[0],offset[1]-mouseX,offset[2]-mouseY,mouseX-offset[3]];return false;};handler_onmouseup=function(e){var evt=e||window.event,target_table,i,target_elements,target_elements_length;mouseButton=0;obj.style.left=0;obj.style.top=0;obj.style.zIndex=10;document.onmousemove=null;document.onmouseup=null;scroll_width=document.documentElement.scrollWidth;scroll_height=document.documentElement.scrollHeight;autoscrollX_flag=autoscrollY_flag=0;if(cloned_flag===1&&(table===null||row===null||cell===null)){obj.parentNode.removeChild(obj);cloned_id[obj_old.id]-=1;REDIPS.drag.myhandler_notcloned();}
else if(table===null||row===null||cell===null){REDIPS.drag.myhandler_notmoved();}
else{if(row_source!=row&&gup('componentID')==0)
{target_table=tables[table];REDIPS.drag.target_cell=target_cell=target_table.rows[row].cells[cell];target_cell.style.backgroundColor=bgcolor_old;return;}
else if(table<tables.length){target_table=tables[table];REDIPS.drag.target_cell=target_cell=target_table.rows[row].cells[cell];}
else if(table_old===null||row_old===null||cell_old===null){target_table=tables[table_source];REDIPS.drag.target_cell=target_cell=target_table.rows[row_source].cells[cell_source];}
else{target_table=tables[table_old];REDIPS.drag.target_cell=target_cell=target_table.rows[row_old].cells[cell_old];}
target_cell.style.backgroundColor=bgcolor_old;if(moved_flag===0){REDIPS.drag.myhandler_notmoved();target_cell.appendChild(obj);}
else if(cloned_flag===1&&table_source===table&&row_source===row&&cell_source===cell){obj.parentNode.removeChild(obj);cloned_id[obj_old.id]-=1;REDIPS.drag.myhandler_notcloned();}
else if(cloned_flag===1&&REDIPS.drag.delete_cloned===true&&(evt.clientX<target_table.offset[3]||evt.clientX>target_table.offset[1]||evt.clientY<target_table.offset[0]||evt.clientY>target_table.offset[2])){obj.parentNode.removeChild(obj);cloned_id[obj_old.id]-=1;REDIPS.drag.myhandler_notcloned();}
else if(target_cell.className.indexOf(REDIPS.drag.trash)>-1){obj.parentNode.removeChild(obj);if(REDIPS.drag.trash_ask){setTimeout(trash_delete,10);}
else{REDIPS.drag.myhandler_deleted();if(cloned_flag===1){clone_limit();}}}
else if(REDIPS.drag.drop_option==='switch'){obj.parentNode.removeChild(obj);target_elements=target_cell.getElementsByTagName('DIV');target_elements_length=target_elements.length;for(i=0;i<target_elements_length;i++){source_cell.appendChild(target_elements[0]);}
target_cell.appendChild(obj);if(target_elements_length){REDIPS.drag.myhandler_switched();REDIPS.drag.myhandler_dropped(target_cell);if(cloned_flag===1){clone_limit();}}
else{REDIPS.drag.myhandler_dropped(target_cell);if(cloned_flag===1){clone_limit();}}}
else{target_cell.appendChild(obj);REDIPS.drag.myhandler_dropped(target_cell);if(cloned_flag===1){clone_limit();}}
if(table_source!==null&&row_source!==null){tables[table_source].rows[row_source].className=tables[table_source].rows[row_source].className;}
target_cell.parentNode.className=target_cell.parentNode.className;calculate_cells();}
var oldPos=cell_source-1;table_old=row_old=cell_old=null;if(row_source==row&&cell_source!=cell){$.post('/Tasks/changeTaskStatusHelper',{id:gup('sprintID'),columnSequence:cell-1,taskString:obj.id,user_id:selectedUser,cid:gup('componentID')});}
else if(row_source!=row&&gup('componentID')==0)
{alert("you can't change the task from component to component");}
else if(row_source!=row&&cell_source!=cell&&gup('componentID')!=0)
{$.post('/Tasks/changeTaskStatusHelper',{id:gup('sprintID'),columnSequence:cell-1,taskString:obj.id,user_id:selectedUser,cid:gup('componentID')});$.post('/Tasks/changeTaskAssigneeHelper',{id:gup('componentID'),taskString:obj.id,user_id:selectedUser,row:(row/2)-1});}
else if(row_source!=row&&gup('componentID')!=0)
{$.post('/Tasks/changeTaskAssigneeHelper',{id:gup('componentID'),taskString:obj.id,user_id:selectedUser,row:(row/2)-1});}};handler_onmousemove=function(e){var evt=e||window.event,bound=REDIPS.drag.bound;if(moved_flag===0&&(obj.className.indexOf('clone')>-1||(REDIPS.drag.clone_ctrlKey===true&&evt.ctrlKey))){clone_object();cloned_flag=1;REDIPS.drag.myhandler_cloned();set_color();}
else if(moved_flag===0){REDIPS.drag.myhandler_moved();set_color();}
moved_flag=1;if(evt.clientX>obj_margin[3]&&evt.clientX<window_width-obj_margin[1]){obj.style.left=(evt.clientX-mouseX)+"px";}
if(evt.clientY>obj_margin[0]&&evt.clientY<window_height-obj_margin[2]){obj.style.top=(evt.clientY-mouseY)+"px";}
if(evt.clientX<div_box[1]&&evt.clientX>div_box[3]&&evt.clientY<div_box[2]&&evt.clientY>div_box[0]&&(evt.clientX<currentCell[3]||evt.clientX>currentCell[1]||evt.clientY<currentCell[0]||evt.clientY>currentCell[2])){set_trc(evt);if(table<tables.length&&(table!==table_old||row!==row_old||cell!==cell_old)){if(table_old!==null&&row_old!==null&&cell_old!==null){tables[table_old].rows[row_old].cells[cell_old].style.backgroundColor=bgcolor_old;}
set_color();}}
if(evt.which){mouseButton=evt.which;}
else{mouseButton=evt.button;}
if(mouseButton!==1){handler_onmouseup(evt);return;}
edgeX=bound-(window_width/2>evt.clientX?evt.clientX-obj_margin[3]:window_width-evt.clientX-obj_margin[1]);if(edgeX>0){if(edgeX>bound){edgeX=bound;}
edgeX*=evt.clientX<window_width/2?-1:1;if(autoscrollX_flag++===0){window.onscroll=null;autoscrollX('start');}}
else{edgeX=0;}
edgeY=bound-(window_height/2>evt.clientY?evt.clientY-obj_margin[0]:window_height-evt.clientY-obj_margin[2]);if(edgeY>0){if(edgeY>bound){edgeY=bound;}
edgeY*=evt.clientY<window_height/2?-1:1;if(autoscrollY_flag++===0){window.onscroll=null;autoscrollY('start');}}
else{edgeY=0;}
evt.cancelBubble=true;if(evt.stopPropagation){evt.stopPropagation();}};handler_onresize=function(){if(typeof(window.innerWidth)==='number'){window_width=window.innerWidth;window_height=window.innerHeight;}
else if(document.documentElement&&(document.documentElement.clientWidth||document.documentElement.clientHeight)){window_width=document.documentElement.clientWidth;window_height=document.documentElement.clientHeight;}
else if(document.body&&(document.body.clientWidth||document.body.clientHeight)){window_width=document.body.clientWidth;window_height=document.body.clientHeight;}
scroll_width=document.documentElement.scrollWidth;scroll_height=document.documentElement.scrollHeight;calculate_cells();};set_trc=function(evt){var cell_current,row_offset,cells,has_content,mark_found,single_cell,i;for(table=0;table<tables.length;table++){if(tables[table].offset[3]<evt.clientX&&evt.clientX<tables[table].offset[1]&&tables[table].offset[0]<evt.clientY&&evt.clientY<tables[table].offset[2]){row_offset=tables[table].row_offset;for(row=0;row<row_offset.length-1&&row_offset[row][0]<evt.clientY;row++){currentCell[0]=row_offset[row][0];currentCell[2]=row_offset[row+1][0];if(evt.clientY<=currentCell[2]){break;}}
if(row===row_offset.length-1){currentCell[0]=row_offset[row][0];currentCell[2]=tables[table].offset[2];}
do{cells=tables[table].rows[row].cells.length-1;for(cell=cells;cell>=0;cell--){currentCell[3]=row_offset[row][3]+tables[table].rows[row].cells[cell].offsetLeft;currentCell[1]=currentCell[3]+tables[table].rows[row].cells[cell].offsetWidth;if(currentCell[3]<=evt.clientX&&evt.clientX<currentCell[1]){break;}}}
while(cell===-1&&row-->0);if(row<1||cell<1){table=table_old;row=row_old;cell=cell_old;}
cell_current=tables[table].rows[row].cells[cell];if(cell_current.className.indexOf(REDIPS.drag.trash)===-1){mark_found=cell_current.className.indexOf(REDIPS.drag.mark_cname)>-1?true:false;if((mark_found===true&&marked_cell==='deny')||(mark_found===false&&marked_cell==='allow')){if(cell_current.className.indexOf(marked_exception[obj.id])===-1){if((table_old!==null&&row_old!==null&&cell_old!==null)){table=table_old;row=row_old;cell=cell_old;}
break;}}}
single_cell=cell_current.className.indexOf('single')>-1?true:false;if((REDIPS.drag.drop_option==='single'||single_cell)&&cell_current.childNodes.length>0){if(cell_current.childNodes.length===1&&cell_current.firstChild.nodeType===3){break;}
has_content=false;for(i=cell_current.childNodes.length-1;i>=0;i--){if(cell_current.childNodes[i].className&&cell_current.childNodes[i].className.indexOf('drag')>-1){has_content=true;break;}}
if(has_content&&table_old!==null&&row_old!==null&&cell_old!==null){if(table_source!==table||row_source!==row||cell_source!==cell){table=table_old;row=row_old;cell=cell_old;break;}}}
break;}}};set_color=function(){if(table!==null&&row!==null&&cell!==null){bgcolor_old=tables[table].rows[row].cells[cell].style.backgroundColor;if(row_source==row&&cell_source!=cell){tables[table].rows[row].cells[cell].style.backgroundColor=REDIPS.drag.hover_color;}
else if(row_source!=row&&gup('componentID')==0)
{tables[table].rows[row].cells[cell].style.backgroundColor=REDIPS.drag.hover_color1;}
else if(row_source!=row&&cell_source!=cell&&gup('componentID')!=0)
{tables[table].rows[row].cells[cell].style.backgroundColor=REDIPS.drag.hover_color;}
else if(row_source!=row&&gup('componentID')!=0)
{tables[table].rows[row].cells[cell].style.backgroundColor=REDIPS.drag.hover_color;}
else
{tables[table].rows[row].cells[cell].style.backgroundColor=REDIPS.drag.hover_color;}
table_old=table;row_old=row;cell_old=cell;}};box_offset=function(box){var scrollPosition=getScrollPosition(),oLeft=0-scrollPosition[0],oTop=0-scrollPosition[1],box_old=box;do{oLeft+=box.offsetLeft;oTop+=box.offsetTop;box=box.offsetParent;}
while(box);return[oTop,oLeft+box_old.offsetWidth,oTop+box_old.offsetHeight,oLeft];};calculate_cells=function(){var i,j,row_offset;for(i=0;i<tables.length;i++){row_offset=[];for(j=tables[i].rows.length-1;j>=0;j--){row_offset[j]=box_offset(tables[i].rows[j]);}
tables[i].offset=box_offset(tables[i]);tables[i].row_offset=row_offset;}
div_box=box_offset(div_drag);};getScrollPosition=function(){var scrollX,scrollY;if(typeof(window.pageYOffset)==='number'){scrollX=window.pageXOffset;scrollY=window.pageYOffset;}
else if(document.body&&(document.body.scrollLeft||document.body.scrollTop)){scrollX=document.body.scrollLeft;scrollY=document.body.scrollTop;}
else if(document.documentElement&&(document.documentElement.scrollLeft||document.documentElement.scrollTop)){scrollX=document.documentElement.scrollLeft;scrollY=document.documentElement.scrollTop;}
else{scrollX=scrollY=0;}
return[scrollX,scrollY];};autoscrollX=function(call){var old=0,scrollPosition=getScrollPosition()[0];if(mouseButton===1&&((edgeX<0&&scrollPosition>0)||(edgeX>0&&scrollPosition<(scroll_width-window_width)))){window.scrollBy(edgeX,0);old=scrollPosition;scrollPosition=getScrollPosition()[0];obj.style.left=(parseInt(obj.style.left,10)+scrollPosition-old)+"px";mouseX-=scrollPosition-old;setTimeout(REDIPS.drag.autoscrollX,REDIPS.drag.speed);}
else{if(call!==null){calculate_cells();}
window.onscroll=calculate_cells;autoscrollX_flag=0;}};autoscrollY=function(call){var top,old=0,scrollPosition=getScrollPosition()[1];if(mouseButton===1&&((edgeY<0&&scrollPosition>0)||(edgeY>0&&scrollPosition<(scroll_height-window_height)))){window.scrollBy(0,edgeY);old=scrollPosition;scrollPosition=getScrollPosition()[1];top=(isNaN(parseInt(obj.style.top,10))?0:parseInt(obj.style.top,10));obj.style.top=(top+scrollPosition-old)+"px";mouseY-=scrollPosition-old;setTimeout(REDIPS.drag.autoscrollY,REDIPS.drag.speed);}
else{if(call!==null){calculate_cells();}
window.onscroll=calculate_cells;autoscrollY_flag=0;}};clone_object=function(){var obj_new=obj.cloneNode(true),offset,offset_dragged;document.getElementById('obj_new').appendChild(obj_new);offset=box_offset(obj);offset_dragged=box_offset(obj_new);obj_new.style.top=(offset[0]-offset_dragged[0])+"px";obj_new.style.left=(offset[3]-offset_dragged[3])+"px";obj_new.onmousedown=handler_onmousedown;obj_new.className=obj_new.className.replace('clone','');if(cloned_id[obj.id]===undefined){cloned_id[obj.id]=0;}
obj_new.id=obj.id+'c'+cloned_id[obj.id];cloned_id[obj.id]+=1;mouseX-=parseInt(obj_new.style.left,10);mouseY-=parseInt(obj_new.style.top,10);REDIPS.drag.obj_old=obj_old=obj;REDIPS.drag.obj=obj=obj_new;};clone_limit=function(){var match_arr,limit_type,limit,classes;classes=obj_old.className;match_arr=classes.match(/climit(\d)_(\d+)/);if(match_arr!==null){limit_type=parseInt(match_arr[1],10);limit=parseInt(match_arr[2],10);limit-=1;classes=classes.replace(/climit\d_\d+/g,'');if(limit<=0){classes=classes.replace('clone','');if(limit_type===2){classes=classes.replace('drag','');obj_old.onmousedown=null;obj_old.style.cursor='auto';REDIPS.drag.myhandler_clonedend2();}
else{REDIPS.drag.myhandler_clonedend1();}}
else{classes=classes+' climit'+limit_type+'_'+limit;}
classes=classes.replace(/^\s+|\s+$/g,'').replace(/\s{2,}/g,' ');obj_old.className=classes;}};elementControl=function(evt){var formElement,srcName;if(evt.srcElement){srcName=evt.srcElement.tagName;}
else{srcName=evt.target.tagName;}
switch(srcName){case'A':case'INPUT':case'SELECT':case'OPTION':formElement=true;break;default:formElement=false;}
return formElement;};trash_delete=function(){var div_text='element';border;if(obj.className.indexOf('t1')>0){border='green';}
else if(obj.className.indexOf('t2')>0){border='blue';}
else{border='orange';}
if(obj.getElementsByTagName('INPUT').length||obj.getElementsByTagName('SELECT').length){div_text='form element';}
else if(obj.innerText||obj.textContent){div_text='"'+(obj.innerText||obj.textContent)+'"';}
if(confirm('Delete '+div_text+' ('+border+') from\n table '+table_source+', row '+row_source+' and column '+cell_source+'?')){REDIPS.drag.myhandler_deleted();if(cloned_flag===1){clone_limit();}}
else{if(cloned_flag!==1){tables[table_source].rows[row_source].cells[cell_source].appendChild(obj);calculate_cells();}
REDIPS.drag.myhandler_undeleted();}};enable_drag=function(enable,div_id){var i,divs=[],borderStyle,cursor,handler;if(enable===true){handler=handler_onmousedown;cursor='move';}
else{handler=null;cursor='auto';}
if(div_id===undefined){divs=document.getElementById('drag').getElementsByTagName('div');}
else{divs[0]=document.getElementById(div_id);}
for(i=0;i<divs.length;i=i+1){if(divs[i].className.indexOf('drag')>-1){divs[i].onmousedown=handler;divs[i].style.borderStyle=borderStyle;divs[i].style.cursor=cursor;}}};save_content=function(tbl){var query='',tbl_rows,cells,tbl_cell,t,r,c,d;if(tbl===undefined||tbl<0||tbl>tables.length-1){tbl=0;}
tbl_rows=tables[tbl].rows.length;for(r=0;r<tbl_rows;r++){cells=tables[tbl].rows[r].cells.length;for(c=0;c<cells;c++){tbl_cell=tables[tbl].rows[r].cells[c];if(tbl_cell.childNodes.length>0){for(d=0;d<tbl_cell.childNodes.length;d++){if(tbl_cell.childNodes[d].tagName==='DIV'){query+='p[]='+tbl_cell.childNodes[d].id+'_'+r+'_'+c+'&';}}}}}
query=query.substring(0,query.length-1);return query;};return{obj:obj,obj_old:obj_old,target_cell:target_cell,source_cell:source_cell,hover_color:hover_color,hover_color1:hover_color1,bound:bound,speed:speed,mark_cname:mark_cname,marked_cell:marked_cell,marked_exception:marked_exception,trash:trash,trash_ask:trash_ask,drop_option:drop_option,delete_cloned:delete_cloned,cloned_id:cloned_id,clone_ctrlKey:clone_ctrlKey,init:init,enable_drag:enable_drag,save_content:save_content,autoscrollX:autoscrollX,autoscrollY:autoscrollY,handler_onmousedown:handler_onmousedown,myhandler_clicked:function(){},myhandler_moved:function(){},myhandler_notmoved:function(){},myhandler_dropped:function(){},myhandler_switched:function(){},myhandler_cloned:function(){},myhandler_clonedend1:function(){},myhandler_clonedend2:function(){},myhandler_notcloned:function(){},myhandler_deleted:function(){},myhandler_undeleted:function(){}};}());