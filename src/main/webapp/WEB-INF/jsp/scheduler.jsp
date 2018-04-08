<html lang="en">
<%@include file="head.jsp" %>

<body>

<%@include file="header.jsp" %>
<div ng-app="App" class="container">
	<div ng-cloak>
        <div class="inner-container col-xs-12">
            <div ng-controller="scheduler">
                <div class="row" ng-repeat="scheduler in schedulers">
                    <div>{{$index + 1}}:</div>
                     <div ng-repeat="(key, value) in scheduler">
                         <div class="col-xs-offset-1"></div>
                         <div class="col-xs-2" style="color:blue">{{key + ":"}} </div>
                         <div class="col-xs-9">{{value}}</div>
                     </div>
                </div>
            </div>
        </div>
    </div>
</div> <!-- /container -->
<%@include file="footer.jsp" %>

</body>
</html>