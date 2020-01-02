angular.module("App").controller("adminController" , function ($scope, colorfy, roleComparator) {

	$scope.totalDonations;

	$scope.stats = clanStats;

	$scope.filter = {
		orderBy : "-role",
		comparator : roleComparator
	}

	$scope.triggerOrderDirective = function(elem) {
		$timeout(function() {
			$(elem.target).find("i").trigger('click');
		}, 0, false)
	}

	$scope.roleOrder = roleComparator

	$scope.avgContrColor = colorfy.colorfy

	function calculateSum(data) {
		var sumChest = 0;
		var sumDonation = 0;
		data.forEach(function(item) {
			sumChest += item.chestContribution;
			sumDonation += item.cardDonation;
		})
		$scope.totalDonations = sumDonation;
	}

	calculateSum(clanStats);

})