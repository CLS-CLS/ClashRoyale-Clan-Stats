app.factory('riverRaceState',  function(roleComparator) {
	var state = {
//        selectableColumns: {
//            "averageCardsWon": {name: "Average Cards Won", show: true},
//            "warsParticipated": {name: "Wars Participated", show: true},
//            "winRatio": {name: "Win ratio", show: true},
//            "score": {name: "Score", show: true},
//            "crownsWon": {name: "Games Won", show: true},
//            "crownsLost": {name: "Games Lost", show: false},
//            "gamesNotPlayed": {name: "Games not played", show: true},
//            "collectionGamesMissed": {name: "Collection Games Missed", show: true},
//            "totalGamesMissed": {name: "Total Games Missed", show: true}
//        },
		filter : {
			orderBy : "-score",
		},
		hideNotInClanPlayers:{
		    enabled: false
		}
	}
	return state;

});

app.controller("riverRaceController", function($scope, $http, $routeParams, $location, $timeout, history, roleComparator, riverRaceState, colorfy){

    $scope.stats = [];

    $scope.filter = riverRaceState.filter;

    $scope.state = riverRaceState;

    $scope.roleOrder = roleComparator

    $scope.colorfy = colorfy.colorfy;

    $scope.next = function(){
        history.store();
    }

    $scope.hideNotInClanPlayers = riverRaceState.hideNotInClanPlayers;


   	$scope.selectedItem = (function() {
   	    if ($routeParams.deltaWeek == null) {
   	        $routeParams.deltaWeek = 0;
   	    }
   		var deltaWeek =  $routeParams.deltaWeek -1 + 1;
   		if (deltaWeek < 0) {
   			return 0;
   		}else  {
   			return deltaWeek;
   		}
   	})();



    $scope.onItemSelectedBlur = function(item) {
        $scope.selectedItem = item;
        $location.path("/view/riverRace/" + $scope.selectedItem)
    }

    $scope.previousWeek = function() {
        $scope.selectedItem = $scope.selectedItem - 1
         if ($scope.selectedItem < 0) {
            $scope.selectedItem = 0;
            return;
        }
        $location.path("/view/riverRace/" + $scope.selectedItem)
    }

    $scope.nextWeek = function() {
        //sometimes 1 is considered a string and "+" is considered as string concatenator
        //subtracting 1 first makes the selectedItem a number
        $scope.selectedItem = ($scope.selectedItem - 1) + 2;
        $location.path("/view/riverRace/" + $scope.selectedItem)
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

        $http.get(baseUrl() + "/rest/riverrace/" + deltaWeek).then(function(response) {
            $scope.loading = false;
            $scope.stats = response.data;
        }, function(response) {
            $scope.loading = false;
        })

    }
})
