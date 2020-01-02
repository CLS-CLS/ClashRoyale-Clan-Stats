angular.module("App").controller("newPlayersController", function($scope, $http, $routeParams) {

	$scope.submitDisabled = function(){
		return !($routeParams.deltaWeek == null || $routeParams.deltaWeek == 0)
	}

	function loadData() {
		$scope.loading = true;

		var url = baseUrl() + "/rest/newPlayers"

		if ($routeParams.deltaWeek == null && $routeParams.deltaFrom == null){
			$routeParams.deltaWeek = 0;
			url += "/" + $routeParams.deltaWeek;
		}
		else if ($routeParams.deltaWeek != null){
		    url += "/" + $routeParams.deltaWeek;
		}else {
		    if ($routeParams.deltaTo == null) {
		        $routeParams.deltaTo = "";
		    }
		    url +="?deltaFrom=" + $routeParams.deltaFrom + "&deltaTo=" + $routeParams.deltaTo
		}


		$http.get(url).then(function(response) {
			$scope.loading = false;

			$scope.stats = response.data;

			$scope.stats.newPlayers.forEach(function(value, index) {
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
		$scope.stats.newPlayers.forEach(function(value, index) {
			if (!value.chestToggleValue || !value.cardToggleValue) {
				request.push({
					tag: value.tag,
					deleteChest: !value.chestToggleValue,
					deleteCard: !value.cardToggleValue
				})
			}
		})
		console.log(request);
		$http.post(baseUrl()+"/rest/newPlayers/update/"+ $routeParams.deltaWeek, request).then(function(response){
			loadData($routeParams.deltaWeek);
		}, function (response) {
			$scope.loading = false;
			alert("something went wrong!")
		})
	}

	loadData();
})