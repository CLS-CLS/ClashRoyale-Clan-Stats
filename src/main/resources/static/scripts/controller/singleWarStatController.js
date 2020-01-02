app.factory('singleWarStatState',  function(roleComparator) {
	var state = {
		filter : {
			orderBy : "-score",
		},
	}
	return state;
});



app.controller("singleWarStatController", function($scope, $http, $timeout, history, singleWarStatState, colorfy){

    $scope.stats = {
        playerWarStats : []
    }

    $scope.filter = singleWarStatState.filter;

    $scope.colorfy = colorfy.colorfy;

   	$scope.selectedItem = 0;

    $scope.onItemSelectedBlur = function(item) {
        $scope.selectedItem = item;
        getData($scope.selectedItem);
    }

    $scope.previousWeek = function() {
        $scope.selectedItem = $scope.selectedItem - 1
         if ($scope.selectedItem < 0) {
            $scope.selectedItem = 0;
            return;
        }
        getData($scope.selectedItem);
    }

    $scope.nextWeek = function() {
        $scope.selectedItem = ($scope.selectedItem - 1) + 2;
        getData($scope.selectedItem);
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

    function getData(deltaWar) {
        $scope.loading = true;
        $http.get(baseUrl() + "/rest/warstats/single/" + deltaWar).then(function(response) {
            $scope.loading = false;
            $scope.stats = response.data;
        }, function(response) {
            $scope.loading = false;
        })
    }
})