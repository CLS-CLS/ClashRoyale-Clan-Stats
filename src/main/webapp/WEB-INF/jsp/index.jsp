<html lang="en">
<head>
	<base href="/">
	<!-- Global Site Tag (gtag.js) - Google Analytics -->
	<script async src="https://www.googletagmanager.com/gtag/js?id=UA-106574321-1"></script>
	<script>
	  window.dataLayer = window.dataLayer || [];
	  function gtag(){dataLayer.push(arguments)};
	  gtag('js', new Date());

	  gtag('config', 'UA-106574321-1');
	</script>
	
	<script type="text/javascript">
		var baseUrl = "${baseUrl}";
	</script>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=768"/>
    <meta name="description" content="">
    <meta name="author" content="">


    <title>Clash Royal Clan Stats</title>

    <!-- Bootstrap core CSS -->
    <link href="lib/bootstrap/css/bootstrap.css" rel="stylesheet">
    <link href="lib/bootstrap-toggle/angular-bootstrap-toggle.min.css" rel="stylesheet">


    <!-- Custom styles for this template -->
    <link href="css/jumbotron.css" rel="stylesheet">
    <link href="//maxcdn.bootstrapcdn.com/font-awesome/4.6.0/css/font-awesome.min.css" rel="stylesheet">


    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
    <script src="lib/bootstrap/js/bootstrap.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.6.4/angular.min.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.6.4/angular-route.min.js"></script>
    <script src="lib/ui-bootstrap-tpls-2.5.0.min.js"></script>
    <script src="scripts/app.js"></script>
    <script src="scripts/services.js"></script>
    <script src="scripts/configuration.js"></script>
    <script src="scripts/directive.js"></script>
    <script src="lib/bootstrap-toggle/angular-bootstrap-toggle.min.js"></script>
    <script src="https://code.highcharts.com/highcharts.js"></script>
    <script src="https://code.highcharts.com/modules/series-label.js"></script>
    <script src="https://code.highcharts.com/modules/exporting.js"></script>
    <script src="scripts/highcharts.js"></script>


</head>

<body>

<div class="navbar-wrapper">
	<div class="container">

		<nav class="navbar navbar-default navbar-fixed-top main-navbar">
			<div class="navbar-header">
				<button type="button" class="navbar-toggle collapsed"
					data-toggle="collapse" data-target="#navbar"
					aria-expanded="false" aria-controls="navbar">
					<span class="sr-only">Toggle navigation</span> <span
						class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>
			</div>
			<div id="navbar" class="navbar-collapse collapse">
				<ul class="nav navbar-nav">
					<li><a href="#"><h3><i class='fa fa-fw fa-home'></i></h3></a></li>
					<li><a href="/upload"><h3> Upload</h3></a></li>
					<li><a href="/clan/score"><h3><i class='fa fa-fw fa-line-chart'><sup style="color:red; text-shadow: 0px 0px 9px yellow">new</sup></i></h3></a></li>
				</ul>
			</div>
		</nav>

	</div>
</div>

	<!-- Main jumbotron for a primary marketing message or call to action -->
<div class="jumbotron">
    <div class="container">
        <h1 class="clan-header"><img src="images/clanlogo.png">
        The Greek Noobs</h1>
        <p style="text-align:center">Donations and Clan Chest Contribution Statistics </p>

    </div>
</div>



<div ng-app="App" class="container">
	<div ng-view autoscroll="true"></div>
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