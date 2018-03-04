
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

    var chestContribution = [];
    var cardDonation = [];

    data.forEach(function(value, index) {
        chestContribution.unshift(value.chestContribution)
        cardDonation.unshift(value.cardDonation);
    })


    Highcharts.chart('player_chart', {
        title: {
            text: 'Player Progress'
        },
        yAxis: [{
            title: {
                text: 'Crowns'
            }
        },
        {
            title: {
                text: 'Donation Points'
            },
            opposite: true
        }],
        xAxis: {
            title: {
                text: 'Week'
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
            name: 'CC Crowns',
            data: chestContribution,
            yAxis: 0
        },
        {
            name: 'Donation Points',
            data: cardDonation,
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