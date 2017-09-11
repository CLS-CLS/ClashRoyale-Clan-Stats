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
        var boundary = 0;

        if (type == "card") {
            boundary = 100;
        }else if (type == "chest") {
            boundary = 50;
        }

        if (boundary == 0) {
            return "black"
        }
        if (number >= boundary) {
            return "green"
        }else {
            return "red";
        }
    }

    $scope.today = function() {
        $scope.dt = new Date();
    };

    $scope.today();

    $scope.clear = function() {
        $scope.dt = null;
    };

    $scope.dateOptions = {
         dateDisabled: disabled,
         formatYear: 'yy',
         maxDate: new Date(),
         minDate: new Date().setDate(new Date() - 70),
         startingDay: 1,
         showWeeks: false,
    };

    function disabled(data) {
        var date = data.date
        var mode = data.mode
        return (date.getDay() != 1 )
    }

    function getData(week) {

//       $scope.stats = data

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


    $scope.open = function() {
        $scope.popup.opened = true;
    }

    $scope.setDate = function(year, month, day) {
        $scope.dt = new Date(year, month, day);
    };

    $scope.popup = {
         opened: false
    };

    function getDayClass(data) {
        var date = data.date,
        mode = data.mode;
        if (mode === 'day') {
            var dayToCheck = new Date(date).setHours(0,0,0,0);
            for (var i = 0; i < $scope.events.length; i++) {
               var currentDay = new Date($scope.events[i].date).setHours(0,0,0,0);
               if (dayToCheck === currentDay) {
                   return $scope.events[i].status;
               }
            }
        }

        return '';
     }

     $scope.sortStyle = function(elem){
        console.log(elem)
        return "fa fa-fw fa-sort"
     }
})


app.directive("orderDirective", function() {
    var state = "unselected";

    function changeState(){
        if (state == "unselected"){
            state = "up"
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

    return {
        template : "<i class='fa fa-fw fa-sort'></i>",
        replace:true,
        scope: {
            bindTo: '@',
            orderingBy: '='
        },
        link: function(scope, elem ,attrs) {
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