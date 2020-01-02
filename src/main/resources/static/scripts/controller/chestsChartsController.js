
app.controller("chestChartsController", function($scope, $http, $timeout) {

    $scope.maxPieChartsToDisplay = 12;

    function loadData() {
        $scope.loading = true;
        $http.get(baseUrl() + "/rest/clan/score").then(function(response) {
            $scope.loading = false;
            $scope.clanWeeklyStats = response.data;
            var score = [];
            var crowns=[];
            var deviations=[];
            var weeks= [];

            //we want to produce newsest to latest graphs
            response.data.reverse();

            response.data.forEach(function(value){
                //unshift to produce oldest to newest progress graph
                score.unshift(value.clanChestScore * 100)
                crowns.unshift(value.crownScore * 100)
                deviations.unshift(value.playerDeviationScore * 100 )
                weeks.unshift(value.endDate[2] + "/" + value.endDate[1] + "/" +  value.endDate[0])
            })

            $timeout(function() {
                scoreProgressChart(score, deviations, crowns, weeks)
                response.data.forEach(function(value, index){
                    if (index > $scope.maxPieChartsToDisplay) {
                        return;
                    }
                    crownPieChart(value, index)
                })
            })


        }, function(response) {
            $scope.loading = false;
            alert ("Error!! Please try again later")
        })
    }

    loadData();
});