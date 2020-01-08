angular.module("App").controller("playerStatsTabController", function($scope, $routeParams, playerTabCommonState, history) {

    $scope.activateTab = function (event, tabId) {
        event.preventDefault();
    }

    $scope.active = function(tab) {
        return tab == $routeParams.tab;
    }

    $scope.back = function() {
        history.back()
    }

    $scope.hasBack = function() {
        return history.hasBack()
    }

    $scope.player = playerTabCommonState;

})