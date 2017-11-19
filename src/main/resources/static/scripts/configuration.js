app.config([ "$locationProvider", "$routeProvider",
	function config($locationProvider, $routeProvider) {

		$routeProvider.when("/upload" , {
			templateUrl: "views/upload.htm", 
			controller: "uploadController"
		}).when("/:week", {
			templateUrl : "views/clanStats.htm"
		}).when("/player/:playerTag", {
			templateUrl : "views/playerStats.htm",
		}).otherwise("/1")

		$locationProvider.html5Mode(true);

	} 
]);
