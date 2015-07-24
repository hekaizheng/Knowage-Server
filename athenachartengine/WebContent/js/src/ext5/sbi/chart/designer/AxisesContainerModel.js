Ext.define('Sbi.chart.designer.AxisesContainerModel',{
    extend: 'Ext.data.Model',
    fields: [
		'id', 'axisName', 'axisType', 
		
		'categoryColumn', 'categoryGroupby', 'categoryStacked', 
		'categoryStackedType', 'categoryOrderColumn', 'categoryOrderType', 
		'categoryDataType',
		
		'serieAxis', 'serieGroupingFunction', 'serieType', 'serieOrderType', 
		'serieColumn', 'serieColor', 'serieShowValue', 'seriePrecision',
		'seriePrefixChar', 'seriePostfixChar', 
		'serieDataType',
		
		'serieTooltipTemplateHtml', 'serieTooltipBackgroundColor', 'serieTooltipAlign', 
		'serieTooltipColor', 'serieTooltipFont', 'serieTooltipFontWeight', 'serieTooltipFontSize'
		
	],
});