function round(value, decimals) {
  return Number(Math.round(value+'e'+decimals)+'e-'+decimals);
}


var minWeek = 1;
var maxWeek = 12;

var app = angular.module("App", [ 'ui.bootstrap', 'ngRoute', 'ui.toggle' ])
//hack because sometimes we want the $location not to reload the view https://github.com/angular/angular.js/issues/1699
.run(['$route', '$rootScope', '$location', function ($route, $rootScope, $location) {
    var original = $location.path;
    $location.path = function (path, reload) {
        if (reload === false) {
        	 if (typeof (history.pushState) != "undefined") {
                 var obj = { Page: path, Url: baseUrl + path };
                 history.pushState(obj, obj.Page, obj.Url);
             } 
        }else {
        	return original.apply($location, [path]);
        }
        
    };
}])


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

app.controller("chestChartsController", function($scope, $http, $timeout) {
    function loadData() {
        $scope.loading = true;
        $http.get(baseUrl + "/rest/clan/score").then(function(response) {
            $scope.loading = false;
            $scope.clanWeeklyStats = response.data;
            var score = [];
            var crowns=[];
            var deviations=[];
            var weeks= [];

            //we want to produce newsest to latest graphs
            response.data.reverse();

            response.data.forEach(function(value){
                //unshift to produce oldest to newest progress graph
                score.unshift(value.clanChestScore * 100)
                crowns.unshift(value.crownScore * 100)
                deviations.unshift(value.playerDeviationScore * 100 )
                weeks.unshift(value.endDate[2] + "/" + value.endDate[1] + "/" +  value.endDate[0])
            })

            $timeout(function() {
                scoreProgressChart(score, deviations, crowns, weeks)
                response.data.forEach(function(value, index){
                    crownChart(value, index)
                })
            })


        }, function(response) {
            $scope.loading = false;
            alert ("Error!! Please try again later")
        })
    }



    loadData();
});


app.controller("newPlayersController", function($scope, $http, $routeParams) {
	
	$scope.submitDisabled = function(){
		return !($routeParams.week == null || $routeParams.week == 0)  
	}
	
	function loadData(week) {
		$scope.loading = true;
		
		
		if ($routeParams.week == null){
			$routeParams.week = 0;
		}
		
		$http.get(baseUrl + "/rest/newPlayers/" + $routeParams.week).then(function(response) {
			$scope.loading = false;
				
			$scope.stats = response.data;
			
			$scope.stats.forEach(function(value, index) {
				value.chestToggleValue = true;
				value.cardToggleValue = true;
			})
		}, function(response) {
			$scope.loading = false;
		})
	}
	
	$scope.submit = function() {
		$scope.loading = true;
		var request = [];
		$scope.stats.forEach(function(value, index) {
			if (!value.chestToggleValue || !value.cardToggleValue) {
				request.push({
					tag: value.tag,
					deleteChest: !value.chestToggleValue,
					deleteCard: !value.cardToggleValue
				})
			}
		})
		console.log(request);
		$http.post(baseUrl+"/rest/newPlayers/update/"+ $routeParams.week, request).then(function(response){
			loadData($routeParams.week);
		}, function (response) {
			$scope.loading = false;
			alert("something went wrong!")
		})
	}
	
	loadData();
})

app.controller("playerStatsController", function($scope, $http, $routeParams, colorfy, history) {

	$scope.player;
	
	$scope.colorfy = colorfy.colorfy
	
	$scope.maxChestContribution
	
	$scope.maxCardDonation
	
	$scope.maxChestContributionWeek
	
	$scope.maxCardDontationWeek
	
	$scope.dataLoading = true;
	
	$scope.back = function() {
		history.back()
	}
	
	$scope.hasBack = function() {
		return history.hasBack();
	}
	
	function loadData() {
		$scope.dataLoading = true;

		var currentWeek = -1;

		function emptyStats() {
            return {
                chestContribution: "-",
                cardDonation: "-"
            }
        };
		
		$http.get(baseUrl + "/rest/info/week").then(function(response){
			currentWeek = response.data;
			return $http.get(baseUrl + "/rest/player/" + $routeParams.playerTag)
		}).then(
			function(response) {
				var maxChestContribution = 0;
				var maxCardDonation = 0;
				var maxCardDonationWeek = 0;

				// if there are weeks missing due to player not be part of the clan, 
				// fill them with N/A data in order to show these missing weeks
				var completeStats = [];
				
				response.data.statsDto.forEach(function(value, index) {
					while (currentWeek > value.week) {
						completeStats.push(new emptyStats())
						currentWeek = currentWeek - 1;
					}
					completeStats.push(value)
					currentWeek = currentWeek - 1
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
			} 
		).finally(function() {
			$scope.dataLoading = false;
		}, null)
	}
	
	loadData();

})

app.factory('clanStatsState',  function(roleComparator) {
	var state = {
		selectableColumns: { 
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
		},
		bulkSelects: {
			"rakings": false,
			"values" : true,
			"avgs": true
		},
		showPercentage: false,
		filter : {
			orderBy : "-role",
			comparator : roleComparator
		}
	}
	return state;

});

app.controller("weeksDropdownController", function($scope, $http, $timeout, $filter, $routeParams, $location, colorfy, roleComparator, history, clanStatsState) {
	
	$scope.selectedItem = (function() {
		var week =  $routeParams.week -1 + 1;
		if (week >= maxWeek) {
			return  maxWeek;
		}else if (week <= minWeek) {
			return  minWeek;
		}else  {
			return week;
		}
	})();
	
	$scope.next = function () {
		history.store();
	}
	
	$scope.state = clanStatsState;
	
	$scope.bulkSelect = function() {
		var r = $scope.state.bulkSelects.rankings;
		var v = $scope.state.bulkSelects.values;
		var a = $scope.state.bulkSelects.avgs
		$scope.state.selectableColumns.ccRank.show = r;
		$scope.state.selectableColumns.cardDonationRank.show = r;
		$scope.state.selectableColumns.overallRank.show = r;
		$scope.state.selectableColumns.avgCcRank.show = (a && !v && !r) || (r && a);
		$scope.state.selectableColumns.avgDonationRank.show = (a && !v && !r) || (r && a) 
		$scope.state.selectableColumns.avgOverallRank.show = (a && !v && !r) || (r && a);
		$scope.state.selectableColumns.cc.show = v;
		$scope.state.selectableColumns.cardDonation.show = v;
		$scope.state.selectableColumns.avgCc.show = (a && !v && !r) || (v && a);
		$scope.state.selectableColumns.avgDonation.show = (a && !v && !r) || (v && a);
		$scope.averageColSpan = colSpan()
	}
	
	$scope.averageColSpan = colSpan();

	$scope.totalDonations;
	
	$scope.chestLevel;

	$scope.stats = []

	$scope.roleOrder = roleComparator
	
	$scope.avgContrColor = colorfy.colorfy

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
		$location.path("/" + newValue, false)
		getData(newValue);
	})

	$scope.triggerOrderDirective = function(event) {
		$timeout(function() {
			$(event.target).find("i").trigger('click');
		}, 0, false)

	}

	$scope.togglePercentage = function() {
		if ($scope.state.showPercentage) {
			$scope.state.showPercentage = false;
		} else {
			$scope.state.showPercentage = true;
		}
		
		if ($scope.state.showPercentage) {
			$scope.percentageButtonLbl = "View Absolute Values"
		}else {
			$scope.percentageButtonLbl = "View Percentage (%)"
		}
	}

	
	
	function colSpan() {
		var result = 0;
		if ($scope.state.selectableColumns.avgCc.show == true) {
			result++;
		}
		if ($scope.state.selectableColumns.avgCcRank.show == true) {
			result++;
		}
		if ($scope.state.selectableColumns.avgDonation.show == true) {
			result++;
		}
		if ($scope.state.selectableColumns.avgDonationRank.show == true) {
			result++;
		}
		if ($scope.state.selectableColumns.avgOverallRank.show == true) {
			result++;
		}
		return result;
	}
	
	function getData(week) {
		$scope.loading = true;
		
		$http.get(baseUrl + "/rest/" + week).then(function(response) {
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
		}, function(response) {
			$scope.loading = false;
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

