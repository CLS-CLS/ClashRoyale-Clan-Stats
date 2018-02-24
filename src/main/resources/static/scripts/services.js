app.service("colorfy", function() {
	this.colorfy = function(number, type) {
		var boundaryChest = 15;
		var boundaryCard = 200;
		var wowChest = 60;
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

app.factory('history', ["$location", function($location) {
	var history = {};

	var data = []

	history.store = function() {
		data.push($location.path())
	}

	history.back = function back() {
		var url = data.pop()
		$location.path(url);
	}
	
	history.hasBack = function(){
		return data.length > 0;
	}

	return history;

} ]);



app.factory("generalComparator", [ function() {
	return function(object1, object2) {
		if (object1.value == undefined || object1.value == null
				|| object1.value == "null") {
			return -1;
		} else if (object2.value == undefined || object2.value == null
				|| object2.value == "null") {
			return 1
		} else {
		    if (object1.type == "string") {
		        return object1.value.toLowerCase().localeCompare(object2.value.toLowerCase());
		    }
			return (object1.value > object2.value ? 1 : -1);
		}
		return 1;
	}
} ])

app.factory("roleComparator", [ function() {
	return function(item1, item2) {
		var item1Order;
		var item2Order;

		var findOrder = function(item) {
			var value = item.value.toLowerCase();
			switch (value) {
			case 'leader':
				return 4;
			case 'co-leader':
				return 3;

			case 'elder':
				return 2;
			default:
				return 1;
			}
		}
		item1Order = findOrder(item1);
		item2Order = findOrder(item2);
		return item1Order > item2Order ? 1 : -1
	};
} ]);

app.filter('percentage', [ '$filter', function($filter) {
	return function(input, decimals) {
		if (input == 0) {
			decimals = 0;
		}
		return $filter('number')(input * 100, decimals) + '%';
	};
} ]);