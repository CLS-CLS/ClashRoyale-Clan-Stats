var minWeek = 1;
var maxWeek = 12;

var app = angular.module("App", [ 'ui.bootstrap', 'ngRoute' ]);

app.config([ "$locationProvider", "$routeProvider",
	function config($locationProvider, $routeProvider) {

		$routeProvider.when("/:week", {
			templateUrl : "views/clanStats.htm"
		}).when("/player/:playerTag", {
			templateUrl : "views/playerStats.htm",
		}).otherwise("/1")

		$locationProvider.html5Mode(true);

	} 
]);

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

app.controller("adminController" , function ($scope, colorfy, roleComparator) {
	
	$scope.totalDonations;
	
	$scope.stats = clanStats;

	$scope.filter = {
		orderBy : "-role",
		comparator : roleComparator
	}
			
	$scope.triggerOrderDirective = function(elem) {
		$timeout(function() {
			$(elem.target).find("i").trigger('click');
		}, 0, false)

	}

	$scope.roleOrder = roleComparator

	$scope.avgContrColor = colorfy.colorfy
	
	function calculateSum(data) {
		var sumChest = 0;
		var sumDonation = 0;
		data.forEach(function(item) {
			sumChest += item.chestContribution;
			sumDonation += item.cardDonation;
		})
		$scope.totalDonations = sumDonation;
	}
	
	calculateSum(clanStats);
	
})

app.controller("playerStatsController", function($scope, $http, $routeParams, colorfy) {

	$scope.player;
	
	$scope.colorfy = colorfy.colorfy
	
	$scope.maxChestContribution
	
	$scope.maxCardDonation
	
	$scope.maxChestContributionWeek
	
	$scope.maxCardDontationWeek
	
	$scope.dataLoading = true;
	
	
	function loadData() {
		$scope.dataLoading = true;
		
		$http.get(baseUrl + "/rest/player/" + $routeParams.playerTag).then(
			function(response) {
				$scope.dataLoading = false;
				var maxChestContribution = 0;
				var maxCardDonation = 0;
				var maxCardDonationWeek = 0;

				// if there are weeks missing due to player not be part of the clan, 
				// fill them with N/A data in order to show thes missing weeks
				var week = -1;
				
				function emptyStats() {
				    return {
                        chestContribution: "N/A",
                        cardDonation: "N/A"
                    }
				};
				
				var completeStats = [];
				
				response.data.statsDto.forEach(function(value, index) {
									
					for (var i = 0; i < week - value.week - 1; i++) {
						completeStats.push(new emptyStats())
					}
					completeStats.push(value)
					week = value.week
					
				})
				
				response.data.statsDto = completeStats;
				
				response.data.statsDto.forEach(function(value, index) {
					if (value.cardDonation > maxCardDonation){
						maxCardDonation = value.cardDonation;
						$scope.maxCardDonationWeek = index + 1;
					}
					if (value.chestContribution > maxChestContribution) {
						maxChestContribution = value.chestContribution;
						$scope.maxChestContributionWeek = index + 1;
					}
					
					$scope.maxChestContribution = maxChestContribution;
					$scope.maxCardDonation = maxCardDonation;
				})

				$scope.player = response.data
			}, 
			function(response) {
				$scope.dataLoading = false;
			}
		)
	}
	
	loadData();

})

app.controller("weeksDropdownController", function($scope, $http, $timeout, $filter, $routeParams, colorfy, roleComparator) {
	
	$scope.selectedItem = (function() {
		var week =  $routeParams.week;
		if (week >= maxWeek) {
			return  maxWeek;
		}else if (week <= minWeek) {
			return  minWeek;
		}else  {
			return week;
		}
	})();
	
	$scope.totalDonations;
	
	$scope.chestLevel;

	$scope.stats = []

	$scope.showPercentage = false;

	$scope.filter = {
		orderBy : "-role",
		comparator : roleComparator
	}

	$scope.percentageButtonLbl = "View Percentages (%)"
		
	$scope.dataLoading = true;
		
	$scope.dropboxitemselected = function(item) {
		$scope.selectedItem = item;
	}

	$scope.previousWeek = function() {
		if ($scope.selectedItem <= minWeek) {
			$scope.selectedItem = 1;
			return;
		}
		$scope.selectedItem = $scope.selectedItem - 1
	}

	$scope.nextWeek = function() {
		if ($scope.selectedItem >= maxWeek) {
			$scope.selectedItem = maxWeek;
			return;
		}
		//sometings 1 is considered a string and "+" is considered as string concatenator
		//subtracting 1 first makes the selectedItem a number
		$scope.selectedItem = ($scope.selectedItem  -1) + 2;
	}

	$scope.$watch('selectedItem', function(newValue) {
		getData(newValue);
	})

	$scope.triggerOrderDirective = function(elem) {
		$timeout(function() {
			$(elem.target).find("i").trigger('click');
		}, 0, false)

	}

	$scope.togglePercentage = function() {
		$scope.showPercentage = !$scope.showPercentage
		if ($scope.showPercentage) {
			$scope.percentageButtonLbl = "View Absolute Values"
		} else {
			$scope.percentageButtonLbl = "View Percentage (%)"
		}
	}

	$scope.roleOrder = roleComparator
	

	$scope.avgContrColor = colorfy.colorfy
	
	
	function getData(week) {
		$scope.dataLoading = true;
		
		$http.get(baseUrl + "/rest/" + week).then(function(response) {
			$scope.dataLoading = false;
			
			if ($scope.stats == null) {
				$scope.stats = [];
			}
			if ($scope.stats.length < response.data.length) {
				$scope.stats.forEach(function(stat, index) {
					$scope.stats[index] = response.data[index];
				});
				for (i = $scope.stats.length; i < response.data.length; i++) {
					$scope.stats.push(response.data[i])
				}
			} else {
				response.data.forEach(function(stat, index) {
					$scope.stats[index] = stat
				})
				$scope.stats.splice(response.data.length)
			}
			calculatePercentageAndUpdateData(response.data);
		}, function(response) {
			$scope.dataLoading = false;
		})

	}
	
	function calculateChestLvl(sumChest) {
		var lvls = [70, 160, 270, 400, 550, 720, 910, 1120, 1350, 1600];
		var lvl = 0;
		while (sumChest > lvls[lvl] - 1) {
			lvl++;
		}
		return lvl;
	}

	function calculatePercentageAndUpdateData(data) {
		var avgSumChest  = 0;
		var avgSumDonation = 0;
		var sumChest = 0;
		var sumDonation = 0;
		data.forEach(function(item) {
			sumChest += item.chestContribution;
			sumDonation += item.cardDonation;
			avgSumChest += item.avgChestContribution;
			avgSumDonation += item.avgCardDonation;
		})
		data.forEach(function(item) {
			item.chestContributionPerc = item.chestContribution / sumChest;
			item.cardDonationPerc = item.cardDonation / sumDonation;
			item.avgChestContributionPerc = item.avgChestContribution / avgSumChest;
			item.avgCardDonationPerc = item.avgCardDonation / avgSumDonation;
		})
		$scope.totalDonations = sumDonation;
		$scope.chectLevel = calculateChestLvl(sumChest);
	}
	
	

})

app.directive("orderDirective", function() {
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
					scope.filterBy.orderBy = (state == "up" ? scope.bindTo
							: "-" + scope.bindTo)
					scope.filterBy.comparator = scope.comparator
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
})

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