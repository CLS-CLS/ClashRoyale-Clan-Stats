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
			var state = "unselected";
			function changeState() {
				if (state == "unselected") {
					state = "down"
				} else if (state == "up") {
					state = "down"
				} else if (state == "down") {
					state = "up"
				}
			}

			function resetState() {
				state = "unselected"
			}

			function applyClass(elem) {
				elem
						.removeClass("fa-sort-up fa-sort-down fa-sort")
				elem.addClass(getClass())
			}

			function getClass() {
				if (state == "unselected") {
					return "fa-sort"
				}
				if (state == "up") {
					return "fa-sort-up"
				}
				if (state == "down") {
					return "fa-sort-down"
				}
			}

			elem.bind('mouseover', function() {
				elem.css('cursor', 'pointer')
			})

			elem.bind('click', function() {
				changeState()
				applyClass(elem)
				scope.$apply(function() {
					if (scope.comparator == null) {
						scope.comparator = numberComparator;
					}
					scope.filterBy.comparator = scope.comparator;
					scope.filterBy.orderBy = (state == "up" ? scope.bindTo: "-" + scope.bindTo);
				})
			})
			
			scope.$watch('filterBy.orderBy', function(newValue) {
				if (newValue != scope.bindTo && newValue != "-"	+ scope.bindTo) {
					resetState();
					applyClass(elem);
				}
			})
		}
	}
} ])
