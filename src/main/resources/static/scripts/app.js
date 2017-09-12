var baseUrl = location.href

var app = angular.module("App", ['ui.bootstrap']);
app.controller("weeksDropdownController", function($scope, $http) {

    $scope.selectedItem = 1

    $scope.stats = []

    $scope.dropboxitemselected = function(item) {
        $scope.selectedItem = item;
    }

    $scope.$watch('selectedItem', function(newValue) {
        getData(newValue);
    })



    $scope.roleOrder = function (item1, item2) {
        var item1Order;
        var item2Order;

        var findOrder = function(item){
            var value = item.value;
            switch (value) {
                case 'Leader':
                    return 1;

                case 'Co-Leader':
                    return 2;

                case 'Elder':
                    return 3;
                default:
                    return 4;
            }
         }
        item1Order = findOrder(item1);
        item2Order = findOrder(item2);
        return item1Order > item2Order ? 1 : -1
    };

     $scope.filter = {
        orderBy : "-chestContribution",
        comparator: ""
    }

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

        $http.get(baseUrl + "/clan/" + week).then(function(response){
            if ($scope.stats == null) {
                $scope.stats = [];
            }
            if ($scope.stats.length < response.data.length) {
                $scope.stats.forEach(function(stat, index) {
                	$scope.stats[index] = response.data[index];
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
            filterBy: '=',
            comparator: '=?'
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
                    scope.filterBy.orderBy = (state == "up" ? scope.bindTo : "-" + scope.bindTo)
                    scope.filterBy.comparator = scope.comparator
                })

            })
            scope.$watch('filterBy.orderBy', function(newValue){
                if (newValue != scope.bindTo && newValue != "-" + scope.bindTo){
                        resetState();
                        applyClass(elem);
                }
            })
        }
    }
})