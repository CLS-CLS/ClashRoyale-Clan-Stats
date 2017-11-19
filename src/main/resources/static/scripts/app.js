var minWeek = 1;
var maxWeek = 12;

var app = angular.module("App", [ 'ui.bootstrap', 'ngRoute' ]);




app.controller("uploadController", function($scope, $http){
	$scope.downloadTemplate = function() {
		$http.get(baseUrl + "/rest/generateTemplate")
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
				// fill them with N/A data in order to show these missing weeks
				var week = -1;
				
				function emptyStats() {
				    return {
                        chestContribution: "-",
                        cardDonation: "-"
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

	$scope.selectableColumns = {
		"role": {name: "role", show: true},
	 	"cc": {name: "Chest Contribution", show: true },
	 	"ccRank": {name: "Chest Contribution Rank", show: false},
	 	"cardDonation": {name: "Card Donations", show: true},
	 	"cardDonationRank": {name: "Card Donation Rank", show: false},
	 	"overallRank": {name: "Overall Rank", show: false},
	 	"avgCc": {name: "Average Chest Contribution", show: true},
	 	"avgCcRank": {name: "Average Chest Contribution Rank", show: false},
	 	"avgDonation": {name: "Average Card Donations", show: true},
	 	"avgDonationRank": {name: "Average Donation Donations Rank", show: false},
	 	"avgOverallRank": {name: "Average Final Rank", show: false}
	}
	
	$scope.bulkSelects = {
		"rakings": false,
		"values" : true,
		"avgs": true
	}
	
	$scope.bulkSelect = function() {
		$scope.selectableColumns.ccRank.show = $scope.bulkSelects.rankings;
		$scope.selectableColumns.cardDonationRank.show = $scope.bulkSelects.rankings;
		$scope.selectableColumns.overallRank.show = $scope.bulkSelects.rankings;
		$scope.selectableColumns.avgCcRank.show = $scope.bulkSelects.rankings && $scope.bulkSelects.avgs;
		$scope.selectableColumns.avgDonationRank.show = $scope.bulkSelects.rankings && $scope.bulkSelects.avgs;
		$scope.selectableColumns.avgOverallRank.show = $scope.bulkSelects.rankings && $scope.bulkSelects.avgs;
		$scope.selectableColumns.cc.show = $scope.bulkSelects.values;
		$scope.selectableColumns.cardDonation.show = $scope.bulkSelects.values;
		$scope.selectableColumns.avgCc.show = $scope.bulkSelects.values && $scope.bulkSelects.avgs;
		$scope.selectableColumns.avgDonation.show = $scope.bulkSelects.values &&  $scope.bulkSelects.avgs;
		$scope.averageColSpan = colSpan()
	}
	
	$scope.averageColSpan = colSpan();

	$scope.totalDonations;
	
	$scope.chestLevel;

	$scope.stats = []

	$scope.showPercentage = false;
	
	$scope.showRanking = false;

	$scope.filter = {
		orderBy : "-role",
		comparator : roleComparator
	}

	$scope.percentageButtonLbl = "View Percentages (%)"
		
	$scope.dataLoading = true;

	$scope.setItemSelected = function(event) {
		if (event) {
			event.stopPropagation();
		}
		$scope.averageColSpan = colSpan()
	}
		
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
		//sometimes 1 is considered a string and "+" is considered as string concatenator
		//subtracting 1 first makes the selectedItem a number
		$scope.selectedItem = ($scope.selectedItem  -1) + 2;
	}

	$scope.$watch('selectedItem', function(newValue) {
		getData(newValue);
	})

	$scope.triggerOrderDirective = function(event) {
		$timeout(function() {
					$(event.target).find("i").trigger('click');
		}, 0, false)

	}

	$scope.togglePercentage = function() {
		if ($scope.showRanking) {
			$scope.showRanking = false;
			$scope.showPercentage = false;
		} else if ($scope.showPercentage) {
			$scope.showPercentage = false;
			$scope.showRanking = true;
			
		} else {
			$scope.showPercentage = true;
			$scope.showRanking = false;
		}
		
		if ($scope.showRanking) {
			$scope.percentageButtonLbl = "View Absolute Values"
		} else if ($scope.showPercentage) {
			$scope.percentageButtonLbl = "View Ranking"
		}else {
			$scope.percentageButtonLbl = "View Percentage (%)"
		}
	}

	$scope.roleOrder = roleComparator
	

	$scope.avgContrColor = colorfy.colorfy
	
	
	function colSpan() {
		var result = 0;
		if ($scope.selectableColumns.avgCc.show == true) {
			result++;
		}
		if ($scope.selectableColumns.avgCcRank.show == true) {
			result++;
		}
		if ($scope.selectableColumns.avgDonation.show == true) {
			result++;
		}
		if ($scope.selectableColumns.avgDonationRank.show == true) {
			result++;
		}
		if ($scope.selectableColumns.avgOverallRank.show == true) {
			result++;
		}
		return result;
	}
	
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
			calculateRankingsAndUpdateData(response.data);
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
	
	function calculateRankingAndUpdateData (data, valueColumn, rankColumn, sortCallback ) {
		
		if (sortCallback == null || sortCallback == undefined) {
			sortCallback = function(a, b) {
				return b[valueColumn] - a[valueColumn];
			}
		}
		
		data.sort(sortCallback);
		
		var currentRanking = 1;
		data.forEach(function (value, index){
			if (index -1 >=0 && value[valueColumn] == data[index-1][valueColumn]) {
				value[rankColumn] = data[index-1][rankColumn]
			}else {
				value[rankColumn] = currentRanking;
			}
			currentRanking++;
		})
	}
	
	
	
	function calculateRankingsAndUpdateData(data) {
		
		calculateRankingAndUpdateData(data, "chestContribution", "ccRank");
		
		calculateRankingAndUpdateData(data, "cardDonation", "donationRank");
		
		var sortCallback = function(a,b) {
			return a.donationRank + a.ccRank - b.donationRank - b.ccRank
		}
		
		calculateRankingAndUpdateData(data, "overallRank", "overallRank", sortCallback);
		
		calculateRankingAndUpdateData(data, "avgChestContribution", "avgCcRank");
		
		calculateRankingAndUpdateData(data, "avgChestContribution", "avgCcRank");
		calculateRankingAndUpdateData(data, "avgCardDonation", "avgDonationRank");
		
		sortCallback = function(a,b) {
			return a.avgDonationRank + a.avgCcRank - b.avgDonationRank - b.avgCcRank
		}
		
		calculateRankingAndUpdateData(data, "avgOverallRank", "avgOverallRank", sortCallback);
		
		
	
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

