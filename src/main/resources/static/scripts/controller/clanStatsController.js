app.factory('clanStatsState',  function(roleComparator) {
	var state = {
		selectableColumns: {
			"role": {name: "role", show: true},
		 	"cardDonation": {name: "Card Donations", show: true},
		 	"donationRequestScore": {name: "Donation/Request Score", show: true},
		 	"overallRank": {name: "Overall Rank", show: false},
		 	"avgDonation": {name: "Average Card Donations", show: true},
		 	"avgDonationRank": {name: "Average Donations Rank", show: false},
		 	"avgOverallRank": {name: "Average Final Rank", show: false},
		 	"avgRequestDonationDiff": {name: "Average Request/Donation difference", show: true},
		 	"avgDonationRequestScore": {name: "Average Request/Donation Score", show: false}
		},
		bulkSelects: {
			"rakings": false,
			"values" : false,
			"avgs": false
		},
		showPercentage: {
		    enabled: false
		},
		filter : {
			orderBy : "-role",
			comparator : roleComparator
		},
		hideNotInClanPlayers : {
		    enabled: true
		}

	}
	return state;

});


app.controller("clanStatsController", function($scope, $http, $timeout, $filter, $routeParams, $location, colorfy, roleComparator, history, clanStatsState) {

	$scope.selectedItem = (function() {
		var week =  $routeParams.week -1 + 1;
		if (isNaN(week)) {
			week = 1;
		}
		if (week > maxWeek) {
			return  maxWeek;
		}else if (week < minWeek) {
			return defaultWeek;
		}else  {
			return week;
		}
	})();


	$scope.next = function () {
		history.store();
	}

	$scope.availableWeeks = (function(){
        var array = [];
        for (var i = minWeek; i <= maxWeek; i++) {
           array.push(i);
        }
        return array;
    })()

	$scope.state = clanStatsState;

	$scope.bulkSelect = function() {
		var r = $scope.state.bulkSelects.rankings;
		var v = $scope.state.bulkSelects.values;
		var a = $scope.state.bulkSelects.avgs
		$scope.state.selectableColumns.overallRank.show = r;
		$scope.state.selectableColumns.avgDonationRank.show = (a && !v && !r) || (r && a)
		$scope.state.selectableColumns.avgOverallRank.show = (a && !v && !r) || (r && a);
		$scope.state.selectableColumns.cardDonation.show = v;
		$scope.state.selectableColumns.avgDonation.show = (a && !v && !r) || (v && a);
		$scope.averageColSpan = colSpan()
	}

	$scope.averageColSpan = colSpan();

	$scope.totalDonations;

	$scope.chestLevel;

	$scope.stats = []

	$scope.roleOrder = roleComparator

	$scope.avgContrColor = colorfy.colorfy

	$scope.avgRequestDonationDiffColor = colorfy.avgRequestDonationDiffColor

	$scope.percentageButtonLbl = "View Percentages (%)"

	$scope.loading = true;

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
		$scope.selectedItem = ($scope.selectedItem - 1) + 2;
	}

	$scope.$watch('selectedItem', function(newValue, oldValue) {
		$location.path("/view/" + newValue, false)

	})

    function init() {
        getData($scope.selectedItem);
    }

    init();

	$scope.triggerOrderDirective = function(event) {
		$timeout(function() {
			$(event.target).find("i").trigger('click');
		}, 0, false)

	}

	$scope.togglePercentage = function() {
		$scope.state.showPercentage.enabled = !$scope.state.showPercentage.enabled;
	}



	function colSpan() {
		var result = 0;
		if ($scope.state.selectableColumns.avgDonation.show == true) {
			result++;
		}
		if ($scope.state.selectableColumns.avgDonationRank.show == true) {
			result++;
		}
		if ($scope.state.selectableColumns.avgOverallRank.show == true) {
			result++;
		}
		if ($scope.state.selectableColumns.avgRequestDonationDiff.show == true) {
            result++;
        }
        if ($scope.state.selectableColumns.avgDonationRequestScore.show == true) {
                    result++;
                }
		return result;
	}

	function getData(week) {
		$scope.loading = true;

		$http.get(baseUrl() + "/rest/" + week).then(function(response) {
			$scope.loading = false;

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
			calculateRequestDonationDiffAndUpdateData(response.data);
			calculateDonationRequestScoreAndUpdateData(response.data);

		}, function(response) {
			$scope.loading = false;
		})

	}


	function calculateDonationRequestScoreAndUpdateData(data) {
		data.forEach(function(item) {
			item.donationRequestScore = Math.round((item.cardDonation + item.cardsReceived) / 4 + Math.min(item.cardDonation, item.cardsReceived) / 2);
			item.avgDonationRequestScore = Math.round((item.avgCardDonation + item.avgCardsReceived) / 4 + Math.min(item.avgCardDonation, item.avgCardsReceived) / 2);
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

	function calculateRequestDonationDiffAndUpdateData(data){
	    data.forEach(function(item) {
	        item.avgRequestDonationDiff = Math.round(item.avgCardDonation - item.avgCardsReceived);
	    })
	}

	function calculatePercentageAndUpdateData(data) {
		var avgSumChest  = 0;
		var avgSumDonation = 0;
		var avgSumRequest = 0;
		var sumChest = 0;
		var sumDonation = 0;
		var sumRequest = 0;
		data.forEach(function(item) {
			sumChest += item.chestContribution;
			sumDonation += item.cardDonation;
			sumRequest += item.cardsReceived;
			avgSumChest += item.avgChestContribution;
			avgSumDonation += item.avgCardDonation;
			avgSumRequest += item.avgCardsReceived;
		})
		data.forEach(function(item) {
			item.chestContributionPerc = item.chestContribution / sumChest;
			item.cardDonationPerc = item.cardDonation / sumDonation;
			item.cardRequestPerc = item.cardsReceived / sumRequest;
			item.avgChestContributionPerc = item.avgChestContribution / avgSumChest;
			item.avgCardDonationPerc = item.avgCardDonation / avgSumDonation;
			item.avgCardRequestPerc = item.avgCardsReceived / avgSumRequest;
			if (item.avgCardsReceived == 0 || item.avgCardDonation == 0){
			    item.avgRequestDonationDiffPerc = (Math.round(item.avgCardDonation  * 100) / 100) + ":" + (Math.round(item.avgCardsReceived * 100) / 100)
			}else {
			    var denominator = Math.min(item.avgCardDonation, item.avgCardsReceived);
			    item.avgRequestDonationDiffPerc = (Math.round((item.avgCardDonation / denominator) * 100) / 100) + ":" + (Math.round((item.avgCardsReceived / denominator) * 100) / 100);
            }
		})
		$scope.totalDonations = sumDonation;
		$scope.chectLevel = calculateChestLvl(sumChest);
	}
})