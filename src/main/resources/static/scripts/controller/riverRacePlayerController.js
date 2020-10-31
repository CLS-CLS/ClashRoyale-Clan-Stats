app.factory('riverRacePlayerState',  function(roleComparator) {
	var state = {
		filter : {
			orderBy : "week",
		}
	}
	return state;

});


angular.module("App").controller("riverRacePlayerController", function($scope, $http, $routeParams, $timeout, colorfy,
        history, riverRacePlayerState, normalizePromotion) {
    $scope.stats = [];

	$scope.player ={};

	$scope.colorfy = colorfy.colorfy

	$scope.filter = riverRacePlayerState.filter;

	$scope.min = normalizePromotion;

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

		$http.get(baseUrl() + "/rest/riverrace/player/" + $routeParams.tag).then(
			function(response) {
				$scope.stats = response.data
			    if ($scope.stats && $scope.stats.length && $scope.stats.length > 0) {
				    $scope.player.name = $scope.stats[0].name;
				    $scope.player.tag = $scope.stats[0].tag;
				    //insert week so we can order them in ui
                    for (var i = 0; i < $scope.stats.length; i++) {
                        $scope.stats[i].week = i;
                    }
				}
			}
		).finally(function() {
			$scope.loading = false;
		}, null)
	}

	loadData();

})
