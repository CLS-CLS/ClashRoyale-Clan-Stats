
(function(){
    Highcharts.setOptions({
    chart: {
        backgroundColor: {
           linearGradient: {
               x1: 0,
               y1: 0,
               x2: 1,
               y2: 1
           },
           stops: [
               [0, '#323d4c'],
               [1, '#2e3948']
           ]
       }
    }
  })
})()

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