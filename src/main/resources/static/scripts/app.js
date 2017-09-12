var data = [{"tag":"tag#0","name":"Name_0","week":31,"avgChestContribution":0.0,"avgCardDonation":35.0,"chestContribution":51,"cardDonation":7},{"tag":"tag#5","name":"Name_5","week":31,"avgChestContribution":55.0,"avgCardDonation":1.0,"chestContribution":46,"cardDonation":65},{"tag":"tag#9","name":"Name_9","week":31,"avgChestContribution":99.0,"avgCardDonation":78.0,"chestContribution":32,"cardDonation":37},{"tag":"tag#4","name":"Name_4","week":31,"avgChestContribution":0.0,"avgCardDonation":43.0,"chestContribution":5,"cardDonation":29},{"tag":"tag#6","name":"Name_6","week":31,"avgChestContribution":53.0,"avgCardDonation":67.0,"chestContribution":32,"cardDonation":93},{"tag":"tag#8","name":"Name_8","week":31,"avgChestContribution":25.0,"avgCardDonation":65.0,"chestContribution":34,"cardDonation":53},{"tag":"tag#3","name":"Name_3","week":31,"avgChestContribution":42.0,"avgCardDonation":73.0,"chestContribution":27,"cardDonation":38},{"tag":"tag#7","name":"Name_7","week":31,"avgChestContribution":51.0,"avgCardDonation":17.0,"chestContribution":32,"cardDonation":54},{"tag":"tag#2","name":"Name_2","week":31,"avgChestContribution":64.0,"avgCardDonation":14.0,"chestContribution":23,"cardDonation":69},{"tag":"tag#1","name":"Name_1","week":31,"avgChestContribution":28.0,"avgCardDonation":94.0,"chestContribution":52,"cardDonation":8}]
var app = angular.module("App", ['ui.bootstrap']);
app.controller("weeksDropdownController", function($scope, $http) {

    $scope.orderingBy = "name"
    $scope.selectedItem = 1

    $scope.stats = []

    $scope.dropboxitemselected = function(item) {
        $scope.selectedItem = item;
    }

    $scope.$watch('selectedItem', function(newValue) {
        getData(newValue);
    })

    $scope.avgContrColor = function(number, type) {
        var boundaryChest = 32;
        var boundaryCard = 50;
        var wowChest = 100;
        var wowCard = 600;

        var boundary = 0;
        var wow = 100000;

        var style = {};

        if (type=="chest") {
            boundary = boundaryChest;
            wow = wowChest;
        }
        if (type=="card") {
            boundary = boundaryCard;
            wow = wowCard;
        }

        if (boundary == 0) {
            style.color = "black"
            return style;
        }

        if (number >= boundary) {
            style.color = 'green'
        }else {
             style.color  = 'red'
        }

        if (number >= wow){
             style["font-weight"] = 'bold'
        }

        return style;
    }

    function getData(week) {

        $http.get("http://localhost:8080/clan/" + week).then(function(response){
            if ($scope.stats == null) {
                $scope.stats = [];
            }
            if ($scope.stats.length < response.data.length) {
                $scope.stats.forEach(function(stat, index) {
                    stat = response.data[index];
                });
                for (i = $scope.stats.length; i < response.data.length; i++){
                    $scope.stats.push(response.data[i])
                }
            }else {
                response.data.forEach(function(stat, index){
                    $scope.stats[index] = stat
                })
                $scope.stats.splice(response.data.length)
            }
        })

    }

})


app.directive("orderDirective", function() {
   

    return {
        template : "<i class='fa fa-fw fa-sort'></i>",
        replace:true,
        scope: {
            bindTo: '@',
            orderingBy: '='
        },
        link: function(scope, elem ,attrs) {
        	var state = "unselected";

    	    function changeState(){
    	        if (state == "unselected"){
    	            state = "down"
    	        } else if (state == "up"){
    	            state = "down"
    	        } else if (state == "down"){
    	            state = "up"
    	        }
    	    }

    	    function resetState(){
    	        state = "unselected"
    	    }

    	    function applyClass(elem) {
    	         elem.removeClass("fa-sort-up fa-sort-down fa-sort")
    	         elem.addClass(getClass())
    	    }


    	    function getClass(){
    	        if (state == "unselected") {
    	            return "fa-sort"
    	        }
    	        if (state == "up") {
    	            return "fa-sort-up"
    	        }
    	        if (state == "down") {
    	            return "fa-sort-down"
    	        }
    	    }
    	    
            elem.bind('mouseover', function(){
                elem.css('cursor', 'pointer')
            })
            
            elem.bind('click', function(){
                changeState()
                applyClass(elem)
                scope.$apply(function(){
                    scope.orderingBy = (state == "up" ? scope.bindTo : "-" + scope.bindTo)
                })

            })
            scope.$watch('orderingBy', function(newValue){
                if (newValue != scope.bindTo && newValue != "-" + scope.bindTo){
                        resetState();
                        applyClass(elem);
                }
            })
        }
    }
})