
//(function(){
//    Highcharts.setOptions({
//    chart: {
//        backgroundColor: {
//           linearGradient: {
//               x1: 0,
//               y1: 0,
//               x2: 1,
//               y2: 1
//           },
//           stops: [
//               [0, '#323d4c'],
//               [1, '#2e3948']
//           ]
//       }
//    }
//  })
//})()

function crownPieChart(value, index) {
    var stops = [-1, 7, 16, 25, 38, 51, 90, 1600];
    var colors = ["#790000", "#bf0000","#ffc800","#00FF00","#41c941", "#4b9c4b","#2a402a"];
    var y = new Array(colors.length);
    y.fill(0, 0, y.length)

    var name = (function(){
        var name = [];
        stops.forEach(function(value, index){
            if (index != 0) {
                name.push((stops[index - 1] +1) + "-" + stops[index])
            }
        })
        return name;
    })()

    value.data.forEach(function(value, idx){
        var index = (function(){
            var index = 0;
            while(value < stops[index] || value > stops[index+1]){
                index++;
            }
            return index;
        })()
        y[index] = y[index] + 1;
    })

    var seriesData = [];
    for (var i =0; i < y.length; i++){
        seriesData.push( {
            color: colors[i],
            name: name[i],
            y: y[i]
        })
    }

    Highcharts.chart('chart'+ index, {
        chart: {
            type: 'pie'

        },
        title: {
            text: 'Player Crown Distribution'
        },
        subtitle: {
            text: 'Sunday: ' + value.endDate[2] +'/' +value.endDate[1] +'/' +value.endDate[0]
        },
        plotOptions: {
            pie: {
                allowPointSelected: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: false
                },
                showInLegend: true,
            },
            series: {
                dataLabels: {
                    enabled: true,
                    formatter: function() {
                        return Math.round(this.percentage*100)/100 + ' %';
                    },
                    distance: 5,
                }
            }
        },

        series: [{
            name: 'Number Of Players',
            colorByPoint: true,
            data: seriesData
        }],
        responsive: {
            rules: [{
                condition: {
                    maxWidth: 500
                },
                chartOptions: {
                    legend: {
                        layout: 'horizontal',
                        align: 'center',
                        verticalAlign: 'bottom'
                    }
                }
            }]
        }
    });
}



function crownChart(value, index) {
    Highcharts.chart('chart'+ index, {
        title: {
            text: 'Player Crown Distribution'
        },
        subtitle: {
            text: 'Sunday: ' + value.endDate[2] +'/' +value.endDate[1] +'/' +value.endDate[0]
        },

        yAxis: {
            title: {
                text: 'Crowns'
            }
        },
        xAxis: {
            title: {
                text: 'players'
            }
        },
        plotOptions: {
            series: {
                label: {
                    connectorAllowed: false
                },
                pointStart: 0
            }
        },

        series: [{
            name: 'Week ' + value.endDate[2] +'/' +value.endDate[1] +'/' +value.endDate[0],
            data: value.data
        }],

        responsive: {
            rules: [{
                condition: {
                    maxWidth: 500
                },
                chartOptions: {
                    legend: {
                        layout: 'horizontal',
                        align: 'center',
                        verticalAlign: 'bottom'
                    }
                }
            }]
        }

    });
}

function playerProgressChart(data) {

    var cardsReceived = [];
    var cardDonation = [];

    data.forEach(function(value, index) {

        if (value.cardsReceived =="-") {
         cardsReceived.unshift(null)
        }else {
            cardsReceived.unshift(value.cardsReceived)
        }
        if (value.cardDonation == "-") {
         cardDonation.unshift(null);
        } else {
            cardDonation.unshift(value.cardDonation);
        }
    })


    Highcharts.chart('player_chart', {
        title: {
            text: 'Donations / Requests'
        },
        yAxis: [{
            title: {
                text: 'Donation/Request Points'
            }
        },
        ],
        xAxis: {
            title: {
                text: 'Week'
            },
            tickInterval: 1
        },
        plotOptions: {
            series: {
                label: {
                    connectorAllowed: false
                },
                pointStart: 0
            }
        },
        series: [{
            name: 'Received Cards (Requests)',
            data: cardsReceived
        },
        {
            name: 'Donated Cards (Donations)',
            data: cardDonation
        }],
        responsive: {
            rules: [{
                condition: {
                    maxWidth: 500
                },
                chartOptions: {
                    legend: {
                        layout: 'horizontal',
                        align: 'center',
                        verticalAlign: 'bottom'
                    }
                }
            }]
        }

    });
}


function playerWarProgressChart(data) {

    var winRatio =[];
    var score = [];
    var leagueDates = []

    data.stats.forEach(function(value, index) {
        winRatio.unshift(value.avgWins);
        score.unshift(value.avgScore);
        leagueDates.unshift(value.leagueDate);
    })


    Highcharts.chart('player_war_chart', {
        title: {
            text: 'Player Win Ratio Progress'
        },
        chart: {
            height: 600
        },
        tooltip: {
            valueDecimals: 2
        },
        yAxis: [{
            title: {
                text: 'Win ratio'
            },
            alignTicks: false,
            tickInterval: 0.25,
            max: 1.0,
            min: 0
        },
        {
            title: {
                text: 'Score'
            },
            tickInterval: 200,
            tickLength: 0,
            opposite: true,
            gridLineWidth :0,
            min: 0
        }],
        xAxis: {
            title: {
                text: 'War League'
            },
            tickInterval: 2,
            type: 'category',
            categories: leagueDates,
            labels: {
                rotation: 70
            }
        },
        plotOptions: {
            series: {
                label: {
                    connectorAllowed: false
                },
                pointStart: 0
            }
        },
        series: [{
            name: 'Win Ratio',
            data: winRatio,
            yAxis: 0
        },
        {
            name: 'Score',
            data: score,
            yAxis: 1
        }],
        responsive: {
            rules: [{
                condition: {
                    maxWidth: 500
                },
                chartOptions: {
                    legend: {
                        layout: 'horizontal',
                        align: 'center',
                        verticalAlign: 'bottom'
                    }
                }
            }]
        }
    });
}


function clanWarProgressChart(data) {
    var leagueDates = []
    var teamCardAvgs = [];
    var totalCards = [];
    var totalTrophies = [];

    data.forEach(function(value, index) {
        leagueDates.unshift(value.startDate);
        teamCardAvgs.unshift(value.teamCardAvg);
        totalCards.unshift(value.teamTotalCards);
        totalTrophies.unshift(value.totalTrophies );
    })


    Highcharts.chart('clan_war_chart', {
        title: {
            text: 'Clan War Progress'
        },
        chart: {
            height: 600
        },
        tooltip: {
            valueDecimals: 2
        },
        yAxis: [{
            title: {
                text: 'Average Collected Cards'
            },
            tickInterval: 400,
            gridLineWidth :0,
            min: 0
        },
        {
            title: {
                text: 'Total Trophies'
            },
            tickInterval: 400,
            tickLength: 0,
            min: 0
        },
        {
            title: {
                text: 'Total Cards'
            },
            tickInterval: 5000,
            tickLength: 0,
            opposite: true,
            min: 0
        }
        ],
        xAxis: {
            title: {
                text: 'War League'
            },
            tickInterval: 10,
            type: 'category',
            categories: leagueDates,
            labels: {
                rotation: 70
            }
        },
        plotOptions: {
            series: {
                marker: {
                    enabled: false
                },
                pointStart: 0
            }
        },
        series: [{
            name: 'Average Collected Cards',
            data: teamCardAvgs,
            visible:false,
            yAxis: 0
        },
        {
            name: 'Total trophies',
            data: totalTrophies,
            yAxis: 1
        },
        {
            name: 'Total Cards',
            data: totalCards,
            yAxis: 2
        }
        ],
        responsive: {
            rules: [{
                condition: {
                    maxWidth: 500
                },
                chartOptions: {
                    legend: {
                        layout: 'horizontal',
                        align: 'center',
                        verticalAlign: 'bottom'
                    }
                }
            }]
        }
    });
}

function scoreProgressChart(score, deviations, crowns, weeks) {

    Highcharts.chart('progress_chart', {
        title: {
            text: 'Score Progress over the last weeks'
        },
        yAxis: {
            title: {
                text: 'Score'
            }
        },
        xAxis: {
            title: {
                text: 'week'
            },
            categories: weeks

        },
        plotOptions: {
            series: {
                label: {
                    connectorAllowed: false
                },
                pointStart: 0
            }
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle'
        },
        series: [{
            name: 'Total Score',
            data: score
        },
        {
            name: 'Crown score',
            data: crowns,
            visible: false
        },
        {
            name: "Player's Crowns Deviation",
            data: deviations,
            visible: false
        }],

        responsive: {
            rules: [{
                condition: {
                    maxWidth: 500
                },
                chartOptions: {
                    legend: {
                        layout: 'horizontal',
                        align: 'center',
                        verticalAlign: 'bottom'
                    }
                }
            }]
        }
    });
}