angular.module("App").controller("playerStatsController", function($scope, $http, $routeParams, $timeout, colorfy, history, playerTabCommonState) {
    $scope.commonState = playerTabCommonState;

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

	$scope.next = function() {
        history.store()
    }

	$scope.hasBack = function() {
		return history.hasBack();
	}

	function loadData() {
		$scope.commonState.name = '';
        $scope.commonState.tag = '';
        $scope.commonState.joinedAt = '';
		$scope.loading = true;

		var currentWeek = -1;

		function emptyStats() {
            return {
                cardsReceived: "-",
                cardDonation: "-"
            }
        };

		$http.get(baseUrl() + "/rest/info/week").then(function(response){
			currentWeek = response.data - 1 ;
			return $http.get(baseUrl() + "/rest/player/" + $routeParams.playertag)
		}).then(
			function(response) {
				var maxCardsReceived = 0;
				var maxCardDonation = 0;
				var maxCardDonationWeek = 0;
				var maxCardsReceivedWeek = 0;

				// if there are weeks missing due to player not be part of the clan,
				// fill them with N/A data in order to show these missing weeks
				var completeStats = [];
                if (!response.data || !response.data.statsDto){
                    $scope.loading = false; //for some reason loading = false in final block does not work
                    return
                }
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
					if (value.cardsReceived > maxCardsReceived) {
						maxCardsReceived = value.cardsReceived;
						$scope.maxCardsReceivedWeek = index + 1;
					}

					$scope.maxCardsReceived = maxCardsReceived
					$scope.maxCardDonation = maxCardDonation;
				})

				$scope.player = response.data

				//common tab data
				$scope.commonState.name = response.data.name;
				$scope.commonState.tag = response.data.tag;
				$scope.commonState.joinedAt = response.data.joinedAt;

				$timeout(function() {
                    playerProgressChart($scope.player.statsDto)
                })
			}
		).finally(function() {
			$scope.loading = false;
		}, function(){
		    $scope.loading = false;
		})
	}

	loadData();

})