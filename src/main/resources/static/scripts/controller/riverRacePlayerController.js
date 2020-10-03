app.factory('riverRacePlayerState',  function(roleComparator) {
	var state = {
		filter : {
			orderBy : "-score",
		}
	}
	return state;

});


angular.module("App").controller("riverRacePlayerController", function($scope, $http, $routeParams, $timeout, colorfy, history, riverRacePlayerState) {
    $scope.stats = [];

	$scope.player ={};

	$scope.colorfy = colorfy.colorfy

	$scope.filter = riverRacePlayerState.filter;

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
//				if ($scope.stats && $scope.stats.length && $scope.stats.length > 0) {
				    $scope.player.name = $scope.stats[0].name;
				    $scope.player.tag = $scope.stats[0].tag;
//				}
			}
		).finally(function() {
			$scope.loading = false;
		}, null)
	}

	loadData();

})
