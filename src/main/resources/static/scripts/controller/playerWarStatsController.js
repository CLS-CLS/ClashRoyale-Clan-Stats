angular.module("App").controller("playerWarStatsController", function($scope, $http, $routeParams, $timeout, colorfy, history, playerTabCommonState) {

    $scope.commonState = playerTabCommonState;

	$scope.player;

	$scope.colorfy = colorfy.colorfy

	$scope.dataLoading = true;

	$scope.back = function() {
		history.back()
	}

    $scope.next = function(){
        history.store();
    }

	$scope.hasBack = function() {
		return history.hasBack();
	}

	function loadData() {
		$scope.loading = true;

		$http.get(baseUrl() + "/rest/player/" + $routeParams.playertag +"/war").then(
			function(response) {
				$scope.player = response.data
				$scope.player.stats.forEach(function(element, index){
				    element.fightStatuses = [];
				    for (var i = 0; i < element.gamesWon; i++) {
				        element.fightStatuses.push("win");
				    }
                    for (var i = 0; i < element.gamesLost; i++) {
                        element.fightStatuses.push("loose");
                    }
                    for (var i = 0; i < element.gamesNotPlayed; i++) {
                        element.fightStatuses.push("forfeit");
                    }
				})

				$scope.commonState.role = response.data.role;

				$timeout(function() {
                    playerWarProgressChart(response.data)
                })
			}
		).finally(function() {
			$scope.loading = false;
		}, null)
	}

	loadData();

})