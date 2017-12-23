app.directive("loadingDirective", function() {
	return {
		template: "<div class='loading' ng-show='loading'><i class='fa fa-spinner fa-spin fa-5x'></i></div>",
		scope: {
			loading :'='
		}
	}
})

app.directive("orderDirective", ["numberComparator", function(numberComparator) {
	return {
		template : "<i class='order fa fa-fw fa-sort'></i>",
		replace : true,
		scope : {
			bindTo : '@',
			filterBy : '=',
			comparator : '=?'
		},
		link : function(scope, elem, attrs) {
			
			var states = {
					unselected: {
						clazz:"fa-sort",
						next: "down"
					},
					down: {
						clazz:"fa-sort-down",
						next: "up"
					},
					up: {
						clazz:"fa-sort-up",
						next: "down"
					}
			}
			
			var state = states.unselected;
			
			function applyClass() {
				elem.removeClass("fa-sort-up fa-sort-down fa-sort")
				elem.addClass(state.clazz)
			}

		
			elem.bind('mouseover', function() {
				elem.css('cursor', 'pointer')
			})

			elem.bind('click', function() {
				state = states[state.next]
				applyClass()
				scope.$apply(function() {
					if (scope.comparator == null) {
						scope.comparator = numberComparator;
					}
					scope.filterBy.comparator = scope.comparator;
					scope.filterBy.orderBy = (state == states.up ? scope.bindTo: "-" + scope.bindTo);
				})
			})
			
			scope.$watch('filterBy.orderBy', function(newValue) {
				if (newValue != scope.bindTo && newValue != "-"	+ scope.bindTo) {
					state = states.unselected
					applyClass(elem);
				}
			});
			
			//init
			(function() {
				if (scope.filterBy.orderBy == scope.bindTo) {
					state = states.up
					applyClass()	
				}else if (scope.filterBy.orderBy == "-" + scope.bindTo) {
					state= states.down
					applyClass()	
				}
			})();
		}
	}
} ])