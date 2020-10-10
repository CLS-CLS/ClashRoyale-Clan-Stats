app.factory('promotionState',  function(roleComparator) {
	var state = {
		filter : {
			orderBy : "-performancePoints",
		},
		hideNotInClanPlayers:{
		    enabled: true
		}
	}
	return state;

});

app.controller("promotionController", function($scope, $http, $routeParams, $location, $timeout, history, roleComparator, promotionState, colorfy){

    $scope.stats = [];

    $scope.filter = promotionState.filter;

    $scope.state = promotionState;

    $scope.roleOrder = roleComparator

    $scope.next = function(){
        history.store();
    }

    $scope.min = function(i) {
        return Math.min(10, i);
    }

    function init() {
        getData($scope.selectedItem);
    }

    init();

    $scope.triggerOrderDirective = function(event) {
        $timeout(function() {
            $(event.target).find("i").trigger('click');
        }, 0, false)
    }

    function getData(deltaWeek) {
        $scope.loading = true;
        $http.get(baseUrl() + "/rest/riverrace/promotions").then(function(response) {
            $scope.loading = false;
            $scope.stats = response.data;
        }, function(response) {
            $scope.loading = false;
        })
    }
})
