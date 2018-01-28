<html lang="en">
<head>
	<base href="/">

	<script type="text/javascript">
		//var baseUrl = "${baseUrl}";
		var clanStats =${stats};
	</script>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=768"/>
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Clash Royal Clan Stats</title>

    <!-- Bootstrap core CSS -->
    <link href="lib/bootstrap/css/bootstrap.css" rel="stylesheet">


    <!-- Custom styles for this template -->
    <link href="css/jumbotron.css" rel="stylesheet">
    <link href="//maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css" rel="stylesheet">

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
    <script src="lib/bootstrap/js/bootstrap.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.6.4/angular.min.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.6.4/angular-route.min.js"></script>
    <script src="lib/ui-bootstrap-tpls-2.5.0.min.js"></script>
    <script src="scripts/app.js"></script>

</head>

<body>

<!-- Main jumbotron for a primary marketing message or call to action -->
<div class="jumbotron">
    <div class="container">
    	
        <h1 class="clan-header"><img src="images/clanlogo.png">
        The Greek Noobs</h1>
        <p style="text-align:center">Donations and Clan Chest Contribution Statistics </p>

    </div>
</div>

<div ng-app="App" class="container">
	<div ng-cloak>
    <div class="inner-container col-xs-12">
        <div ng-controller="adminController">
			<form class="form-horizontal">
				<div class="form-group bubble">
					<label class="col-xs-3 control-label align-left" >Total Weekly Donations:</label>
					<label class="col-xs-3 control-label" style="text-align:left">
						<h3 style="display:inline">{{totalDonations}}</h3>
					</label>
					<label class="col-xs-3 control-label" style="float:right;text-align:left">
						<img style="display:inline" src="images/clanChest.png">
						<h3 style="display:inline">{{chectLevel}} <small>/10</small></h3>
					</label>
				</div>
			</form>
            <div style="position:relative">
	            <table class="table table-striped">
	                <thead>
		                <tr>
		                    <th><span ng-click=triggerOrderDirective($event)>
		                    	Player <order-directive bind-to="name" filter-by="filter"/>
		                        </span>
		                    </th>
		                    <th><span ng-click=triggerOrderDirective($event)>
		                    	Role <order-directive bind-to="role" filter-by="filter" comparator="roleOrder"/>
		                    	</span>
		                    </th>
		                    <th><span ng-click=triggerOrderDirective($event)>Chest contribution
		                        <order-directive bind-to="chestContribution" filter-by="filter"/></span>
		                    </th>
		                    <th><span ng-click=triggerOrderDirective($event)>Donations
		                        <order-directive bind-to="cardDonation" filter-by="filter"/></span>
		                    </th>
		                   
		                </tr>
	                </thead>
	                <tbody>
		                <tr ng-if="stats.length > 0 " ng-repeat="s in stats | orderBy: filter.orderBy: false : filter.comparator">
		                    <td style="font-family: 'Comic Sans MS', cursive, sans-serif;">{{s.player.name}}</td>
		                    <td>{{s.player.role}}</td>
		
		                    <td ng-show="!showPercentage">{{s.chestContribution }}</td>
		                    <td ng-show="showPercentage">{{s.chestContributionPerc | percentage:1}}</td>
		                    <td style="border-right: 1.5px solid #ddd; " ng-show="!showPercentage">{{s.cardDonation}}</td>
		                    <td style="border-right: 1.5px solid #ddd; " ng-show="showPercentage">{{s.cardDonationPerc | percentage:1}}</td>
		                </tr>
	                </tbody>
	            </table>
            </div>
        </div>
    </div>
</div>
</div> <!-- /container -->

<footer>
    <div class="jumbotron" style="margin-bottom:0px">
        <div class="container">
       	  	<div class="clan-header col-xs-12"><h2>Powered by lytsiware productions</h2></div>
       	    <div class="col-xs-12"> <h4 style="color:yellow; display: inline">mail: </h4> lytsiware@gmail.com </div>
        	<div class="col-xs-6"><h4>The stats have been provided by
                     <a style="color:yellow" href="http://www.clashstat.com" target="_black">www.clanstat.com</a></h4>
            </div>
            <div class="col-xs-6"><h4>This content is not affiliated with, endorsed, sponsored, or specifically approved by Supercell
                    and Supercell is not responsible for it.</h4>
            </div>
            <div class="col-xs-6">Backgrounds by <a href="https://www.toptal.com/designers/subtlepatterns">toptal.com </a></div>
        </div>
    </div>
</footer>




</body>
</html>