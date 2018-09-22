app.config([ "$locationProvider", "$routeProvider",
	function config($locationProvider, $routeProvider) {

		$routeProvider
		    .when("/warstats/input", {
		        templateUrl: "/views/warstatsInput.htm"
		    })
		    .when("/warstats/:week?", {
                templateUrl: "views/warstats.htm"
            })
            .when("/upload" , {
                templateUrl: "views/upload.htm",
                controller: "uploadController"
            })
            .when("/newplayers/:deltaWeek?", {
                templateUrl: "views/newPlayers.htm"
            })
            .when("/clan/score", {
                templateUrl: "views/chestCharts.htm"
            })
            .when("/player/:playertag/war", {
                templateUrl: "views/playerwarstats.htm"
            })
            .when("/player/:playertag", {
                templateUrl : "views/playerStats.htm"
            })
            .when("/clanrules", {
                templateUrl : "views/clanRules.htm"
            })
            .when("/:week", {
                templateUrl : "views/clanStats.htm"
            })
            .otherwise("/1")

		$locationProvider.html5Mode(true);

	} 
]);
