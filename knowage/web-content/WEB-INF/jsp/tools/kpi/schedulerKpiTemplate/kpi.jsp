<div layout-fill ng-controller = "kpiController" layout="row">
 <md-whiteframe class="md-whiteframe-4dp layout-padding " layout layout-margin >
 		<angular-table flex
		id='targetListTable' ng-model=selectedScheduler.kpis
		columns='[{"label":"Name","name":"name"},{"label":"Category","name":"valueCd"}]'
		columns-search='["name"]' show-search-bar=true
		scope-functions=tableFunction  speed-menu-option=measureMenuOption 
		> 
		
		<queue-table >
			<div layout="row"> 
				<span flex></span>
				<md-button ng-click="scopeFunctions.loadListKPI()">{{scopeFunctions.translate.load('sbi.kpi.addkpiassociation')}}</md-button>
			</div>
		</queue-table> 
		</angular-table>
		

 	
   </md-whiteframe>       


</div>