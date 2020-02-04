app.factory('globalStatsState',  function(roleComparator) {
	var state = {
		filter : {
			orderBy : "-playedWarGames",
			comparator : ""
		},
		hideNotInClanPlayers : {
		    enabled: true
		}
	}
	return state;

});


app.controller("globalStatsController", function($scope, $http, $timeout, $filter, $routeParams, $location,
    roleComparator, history, globalStatsState, colorfy) {

    $scope.colorfy = colorfy.colorfy

	$scope.next = function () {
		history.store();
	}

	$scope.state = globalStatsState;

	$scope.stats = []

	$scope.roleOrder = roleComparator

	$scope.loading = true;

    getData();

	$scope.triggerOrderDirective = function(event) {
		$timeout(function() {
			$(event.target).find("i").trigger('click');
		}, 0, false)

	}

	function getData() {
		$scope.loading = true;

		$http.get(baseUrl() + "/rest/globalStats").then(function(response) {
			$scope.loading = false;
			$scope.stats = response.data;
		}, function(response) {
			$scope.loading = false;
		})
	}

})