angular.module("App").controller("playerStatsTabController", function($scope, playerTabCommonState, history) {

    $scope.activateTab = function (event, tabId) {
        event.preventDefault();
    }

    $scope.back = function() {
        history.back()
    }

    $scope.hasBack = function() {
        return history.hasBack()
    }

    $scope.player = playerTabCommonState;

})