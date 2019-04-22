app.config([ "$locationProvider", "$routeProvider",
	function config($locationProvider, $routeProvider, $httpProvider) {
		$routeProvider
			.when("/", {
				templateUrl: "/views/clanRules.htm"
			})
			.when("/scheduler", {
				templateUrl: "/views/scheduler.htm"
			})
		    .when("/warstats/input", {
		        templateUrl: "/views/warStatsInput.htm"
		    })
		    .when("/warstats/:week?", {
                templateUrl: "views/warStats.htm"
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
                templateUrl: "views/playerWarStats.htm"
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

//		$httpProvider.interceptors.push('authInterceptor');

	} 
]);

