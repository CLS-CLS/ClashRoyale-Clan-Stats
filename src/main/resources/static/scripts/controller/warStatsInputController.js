angular.module("App").controller("warStatsInputController", function($scope, $http){
    $scope.activateTab = function(event, tabId) {
        event.preventDefault();
    }

    $scope.hasError = function(type, formIndex, inputIndex) {
        var errors = this['inputForm_' + formIndex][type + '_' + inputIndex].$error;
        var result =  errors.min || errors.max || errors.number || errors.required;
        return result;
    }

    $scope.saveWar = function(event, index) {
        if (angular.element(event.currentTarget).scope()[event.currentTarget.name].$valid) {
            console.log("saved")
            $http.post(baseUrl() + "/rest/warstats/inputdata", $scope.statsList[index]).then(function(response){
                console.log("TODO")
                $scope.errors = response.data;
                if ($scope.errors.length > 0) {
                    $("#errorModal").modal()
                }else {
                    alert("data saved");
                }
            },
            function(errorResponse) {
                $("#errorModal").modal()
                $scope.errors = errorResponse.data;
                console.log(errorResponse)
            })
        }
    }

    $scope.refresh = function(date, index) {
        $scope.loading = true;
        if (date instanceof Date){
            date = date.toJSON().split("T")[0];
        }
        $http.post(baseUrl() + "/rest/warStats/playersNotParticipated/" + date, $scope.statsList[index].playerWarStats).then(
            function(response) {
                $scope.statsList[index].playersNotParticipated = response.data
            },
            function(response){

            }).finally(function() {
                $scope.loading = false;
            }, null)
    }

    function loadData() {
        $scope.loading = true;
        $http.get(baseUrl() + "/rest/warstats/inputdata").then(
            function(response) {
                $scope.statsList = response.data
            },
            function(errorResponse) {
                $("#errorModal").modal()
                $scope.errors = errorResponse.data.message + "\r\n" + errorResponse.data.error
            }
        ).finally(function() {
            $scope.loading = false;
        }, null)
    }


    loadData();
})