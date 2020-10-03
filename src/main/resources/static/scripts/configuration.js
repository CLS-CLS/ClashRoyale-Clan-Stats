app.config([ "$locationProvider", "$routeProvider",
	function config($locationProvider, $routeProvider) {

		$routeProvider
			.when("/", {
				templateUrl: "resource/views/clanRules.htm"
			})
			.when("/view/riverRace/player/:tag", {
                templateUrl: "resource/views/riverRacePlayer.htm"
            })
			.when("/view/riverRace/:deltaWeek?", {
			    templateUrl: "resource/views/riverRace.htm"
			})
			.when("/view/globalStats", {
                templateUrl: "resource/views/globalStats.htm"
            })
			.when("/view/scheduler", {
				templateUrl: "resource/views/scheduler.htm"
			})
		    .when("/view/warstats/input", {
		        templateUrl: "resource/views/warStatsInput.htm"
		    })
		    .when("/view/warstats/:deltaWar?", {
                templateUrl: "resource/views/warStats.htm"
            })
            .when("/view/upload" , {
                templateUrl: "views/upload.htm",
                controller: "resource/uploadController"
            })
            .when("/view/newplayers/:deltaWeek?", {
                templateUrl: "resource/views/newPlayers.htm"
            })
            .when("/view/clan/score", {
                templateUrl: "resource/views/chestCharts.htm"
            })
            .when("/view/player/:playertag/war", {
                templateUrl: "resource/views/playerStats.htm"
            })
            .when("/view/player/:playertag", {
                templateUrl : "resource/views/playerStats.htm"
            })
            .when("/view/clanrules", {
                templateUrl : "resource/views/clanRules.htm"
            })
            .when("/view/roster", {
                templateUrl: "resource/views/checkins.htm"
            })
            .when("/view/:week", {
                templateUrl : "resource/views/clanStats.htm"
            })
            .otherwise("/view/1")

		$locationProvider.html5Mode(true);

	} 
]);
