/**
 * This function generates a graph.
 * 
 * @author Hadeer Younis
 * @category C4S8
 * @param data
 *            This is a 2-dimensional integer array containing the points
 *            coordinates.
 * @param xTicks
 *            This is an integer array containing the x-axis ticks.
 * @param yTicks
 *            This is an integer array containing the y-axis ticks.
 * @param xLabel
 *            This is a string containing the name of the x-axis.
 * @param yLabel
 *            This is a string containing the name of the y-axis.
 * @param title
 *            This is a string containing the name of the chart.
 * @param xMax
 *            This is an integer containing the maximum value of the y-axis.
 * @param yMax
 *            This is an integer containing the maximum value of the y-axis.
 * @see A Broken line graph.
 */
function GenerateGraph(data,xTicks,yTicks,xLabel,yLabel,title,xMax,yMax,divName)
{
	var interval=5;
	if(yMax>100)
		interval=10;
	if(data=='NONE')
	{
		alert('THERE IS NO DATA TO DISPLAY!!!');
		return;
	}
	var estimated = []; 
	var i=0;
	var y=yMax/(xMax-1);
	
	for (var z=(xMax-1); z>=0; z--) 
	{ 
		estimated.push([z,i]); 
		i=i+y;
	}
	
	plot1 = $.jqplot(divName, [data,estimated], 
	{
		title: 
		{
			text: title, 
			show: true,
		},
		axes:
		{
			xaxis:
			{
				label:xLabel, 
				labelOptions:{ fontFamily:'Helvetica',fontSize: '13pt'},
				labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
				//ticks: xTicks
				max:xMax,
				tickInterval:1,
			}, 
			yaxis:
			{
				labelOptions:{fontFamily:'Helvetica',fontSize: '13pt'},
				label:yLabel,
				labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
				//ticks:yTicks
				tickInterval:interval,
				max:yMax+5,
			}
		},
		axesDefaults: 
		{
			renderer: $.jqplot.LinearAxisRenderer,  
			showTicks: true,
			min:0,
		},
		legend: 
		{
			show: true,
			location: 'ne',
			xoffset: 12,       
			yoffset: 12,       
		},				
		grid: 
		{
			drawGridLines: true,       
			gridLineColor: '#777777',    
			background: '#F2f2f2',      
			borderColor: '#999999',     
			borderWidth: 2.0,           
			shadow: true,               
			shadowAngle: 45,            
			shadowOffset: 1.5,          
			shadowWidth: 3,             
			shadowDepth: 3,             
			shadowAlpha: 0.07,          
			renderer: $.jqplot.CanvasGridRenderer,     
		},cursor: {tooltipLocation:'sw', followMouse:true,showVerticalLine:true,showHorizontalLine:true,zoom:true, clickReset:true,showTooltipDataPosition:true}, 
		series: 
		[{
			show: true,     
			xaxis: 'xaxis',
			yaxis: 'yaxis',
			label: 'Actual Remaining Points per day',  
			color: '#777777',    
			lineWidth: 5,
			shadow: true,  
			shadowAngle: 45,  
			shadowOffset: 1.25, 
			shadowDepth: 3,     
			shadowAlpha: 0.1,  
			showLine: true,    
			showMarker: true,  
			renderer: $.jqplot.LineRenderer,    
			markerRenderer: $.jqplot.MarkerRenderer,
			markerOptions: 
			{ 
				show: true, 
				style: 'square', 
				lineWidth: 3,  
				size: 15,        
				color: '#222222',
				shadow: true,  
				shadowAngle: 45,    
				shadowOffset: 1,
				shadowDepth: 3,
				shadowAlpha: 0.07 
			}
		},
		{
			show: true,     
			xaxis: 'xaxis',
			yaxis: 'yaxis',
			label: 'Estimated Remaining Points per day',  
			color: 'red',    
			lineWidth: 5,
			shadow: true,  
			shadowAngle: 45,  
			shadowOffset: 1.25, 
			shadowDepth: 3,     
			shadowAlpha: 0.1,  
			showLine: true,    
			showMarker: false,
			renderer: $.jqplot.LineRenderer,    
			markerRenderer: $.jqplot.MarkerRenderer,			
		}]
		
	});
}
/**
 * This function generates a graph but with diffrent inputs.
 * 
 * @author Hadeer Younis
 * @category C4S9
 * @param data
 *            This is a 2-dimensional integer array containing the points
 *            coordinates.
 * @param names
 *            This ia an array of strings containing the graph names.
 * @see A Broken line graph.
 */
function GenerateFullGraph(data,names)
{

	var bla = data;
	var plot1 = $.jqplot('c_FULL', data, 
	{
		series:names,
		seriesDefaults:
		{
			markerOptions:{style:'square'}
		},
		title: 
		{
			text: 'Project Progress', 
			show: true,
		},
		axes:
		{
			xaxis:
			{
				label:'Days', 
				labelOptions:{ fontFamily:'Helvetica',fontSize: '13pt'},
				labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
				// ticks: xTicks
				tickInterval:1
			}, 
			yaxis:
			{
				labelOptions:{fontFamily:'Helvetica',fontSize: '13pt'},
				label:'Remainging Points',
				labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
			  // ticks:yTicks
				tickInterval:2
			}
		},
		axesDefaults: 
		{
			renderer: $.jqplot.LinearAxisRenderer,  
			showTicks: true,   
			min:0,
		},
		legend: 
		{
			show: true,
			location: 'ne',
			xoffset: 12,       
			yoffset: 12,       
		},				
		grid: 
		{
			drawGridLines: true,       
			gridLineColor: '#333333',    
			background: '#f2f2f2',      
			borderColor: '#999999',     
			borderWidth: 2.0,           
			shadow: true,               
			shadowAngle: 45,            
			shadowOffset: 1.5,          
			shadowWidth: 3,             
			shadowDepth: 3,             
			shadowAlpha: 0.07,          
			renderer: $.jqplot.CanvasGridRenderer,     
		},	cursor: {tooltipLocation:'sw', zoom:true, clickReset:true}, 	
	});
}
