app.service("colorfy", function() {
	this.colorfy = function(number, type) {
		var boundaryChest = 20;
		var boundaryCard = 150;
		var wowChest = 100;
		var wowCard = 600;

		var boundary = 0;
		var wow = 100000;

		var style = {};

		if (type == "chest") {
			boundary = boundaryChest;
			wow = wowChest;
		}
		if (type == "card") {
			boundary = boundaryCard;
			wow = wowCard;
		}

		if (boundary == 0) {
			style.color = "black"
			return style;
		}

		if (number >= boundary) {
			style.color = 'green'
		} else if (number < boundary) {
			style.color = 'red'
		}

		if (number >= wow) {
			style["font-weight"] = 'bold'
		}

		return style;
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
				elem.removeClass("fa-sort-up fa-sort-down fa-sort")
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
					scope.filterBy.orderBy = (state == "up" ? scope.bindTo
							: "-" + scope.bindTo);
				})

			})
			scope.$watch('filterBy.orderBy',
				function(newValue) {
					if (newValue != scope.bindTo
							&& newValue != "-" + scope.bindTo) {
						resetState();
						applyClass(elem);
					}
				})
		}
	}
}])

app.factory("numberComparator", [function() {
	return function(number1, number2){
		if (number1.value == undefined || number1.value == null || number1.value =="null") {
			return -1;
		}else if (number2.value == undefined || number2.value == null || number2.value == "null") {
			return 1
		}else {
			return (number1.value > number2.value ? 1: -1);
		} 
		return 1;
	}
}]) 

app.factory("roleComparator", [function() {
	return function(item1, item2) {
		var item1Order;
		var item2Order;

		var findOrder = function(item) {
			var value = item.value;
			switch (value) {
			case 'Leader':
				return 4;

			case 'Co-Leader':
				return 3;

			case 'Elder':
				return 2;
			default:
				return 1;
			}
		}
		item1Order = findOrder(item1);
		item2Order = findOrder(item2);
		return item1Order > item2Order ? 1 : -1
	};
}]);

app.filter('percentage', [ '$filter', function($filter) {
	return function(input, decimals) {
		if (input == 0) {
			decimals = 0;
		}
		return $filter('number')(input * 100, decimals) + '%';
	};
} ]);