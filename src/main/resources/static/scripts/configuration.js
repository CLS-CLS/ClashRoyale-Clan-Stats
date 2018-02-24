app.config([ "$locationProvider", "$routeProvider",
	function config($locationProvider, $routeProvider) {

		$routeProvider.when("/upload" , {
			templateUrl: "views/upload.htm", 
			controller: "uploadController"
		}).when("/newplayers/:week?", {
			templateUrl: "views/newPlayers.htm"
		}).when("/clan/score", {
        	templateUrl: "views/chestCharts.htm"
       	}).when("/player/:playerTag", {
			templateUrl : "views/playerStats.htm",
		}).when("/clanrules", {
            templateUrl : "views/clanRules.htm"
        }).when("/:week", {
			templateUrl : "views/clanStats.htm"
		}).otherwise("/1")

		$locationProvider.html5Mode(true);

	} 
]);
