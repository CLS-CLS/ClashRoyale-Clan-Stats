function round(value, decimals) {
  return Number(Math.round(value+'e'+decimals)+'e-'+decimals);
}

function baseUrl() {
    return window.location.protocol + "//"+ window.location.host
}


var minWeek = 0;
var defaultWeek = 1;
var maxWeek = 12;

var app = angular.module("App", [ 'ui.bootstrap', 'ngRoute', 'ui.toggle' ])

//hack because sometimes we want the $location not to reload the view https://github.com/angular/angular.js/issues/1699
.run(['$route', '$rootScope', '$location', function ($route, $rootScope, $location) {
    var original = $location.path;
    $location.path = function (path, reload) {
        if (reload === false) {
        	 if (typeof (history.pushState) != "undefined") {
                 var obj = { Page: path, Url: baseUrl() + path };
                 history.pushState(obj, obj.Page, obj.Url);
             } 
        }else {
        	return original.apply($location, [path]);
        }
        
    };
}])

app.controller("scheduler", function($scope, $http) {
    $http.get(baseUrl() + "/rest/scheduler").then(function(response){
        $scope.schedulers = response.data;
    })
})

app.controller("uploadController", function($scope, $http){
	$scope.downloadTemplate = function() {
		$http.get(baseUrl() + "/rest/generateTemplate")
	}
})


app.controller("clanWarStatsTabController", function($scope) {
    $scope.activateTab = function (event, tabId) {
        event.preventDefault();
    }
})

app.controller("clanWarProgressControler", function($http, $timeout){
    loadClanChartData();

    function loadClanChartData() {
        $http.get(baseUrl() + "/rest/warstats/warleague").then(
            function(response) {
                $timeout(function() {
                    clanWarProgressChart(response.data)
                })
            }
        )
    }
})








