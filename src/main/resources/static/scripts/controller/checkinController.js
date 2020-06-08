app.controller("checkinController", function($scope, $http, $timeout, $filter, $routeParams, $location, colorfy, history) {

    $scope.loading = true;

    $scope.hidePlayersNotInClan =  {
        enabled: false
    }

    $scope.filter =  {
        orderBy : "name"
    }

    $scope.dismissModal = function(){
        $("#dismissButton").trigger('click')
        $timeout(function(){
            $location.url("/view/player/" + $scope.player.tag + "/war?tab=playerWarStats")
        }, 1000, true)
    }

    $scope.updateModalStats = function(checkouts, name, tag) {
        $scope.modalStats = checkouts;
        $scope.player = {
            name: name,
            tag: tag
        }
    }

    getData();

    $scope.triggerOrderDirective = function(event) {
        $timeout(function() {
            $(event.target).find("i").trigger('click');
        }, 0, false)

    }

    function getData() {
        $scope.loading = true;

        $http.get(baseUrl() + "/rest/roster").then(function(response) {
            $scope.loading = false;
            $scope.stats = response.data;
        }, function(errorResponse) {
            alert(errorResponse)
            $scope.loading = false;
        })
    }
})
