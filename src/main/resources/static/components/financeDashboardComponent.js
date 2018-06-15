(function(angular) {
    'use strict';
    angular.module('murexApp').component('fincanceDashboard', {
        bindings: {
            dashboardData: '<',
            ngHide: '<'
        },
        templateUrl: 'components/financeDashboardView.html',
        controller: ("financeDashboardController", function($scope){
            var $ctrl = this;
            $ctrl.dashBoardData = [];
            $ctrl.portfolioNetAssetValue = 0;
            $ctrl.initialPortfolioValue = 250000;
            $ctrl.cashBalanceChange = 0;

            $ctrl.depthSelect = {
                selected : "Portfolio",
                entries : ["Portfolio", "Asset"]
            };

            $scope.formatNumber = function(num){
                if(num === undefined){
                    return;
                }

                var n = num.toString(), p = n.indexOf('.');
                return n.replace(/\d(?=(?:\d{3})+(?:\.|$))/g, function($0, i){
                    return p<0 || i<p ? ($0+',') : $0;
                });
            };

            $scope.calculateNetAssetValue = function(portfolio){
                var nav = portfolio.cashBalance;
                for(var i = 0; i < portfolio.products.length; i++){
                    nav += portfolio.products[i].value;
                }

                return nav;
            };

            $scope.determineEarningsColor = function(){
                if($ctrl.portfolioNetAssetValue - $ctrl.initialPortfolioValue > 0){
                    return 'green';
                } else {
                    return 'red';
                }
            };

            $scope.formatValue = function(value){
                if(value > 0){
                    return 'green';
                } else {
                    return 'red';
                }
            };

            $scope.calculateTotalEarnings = function(){
                return $ctrl.portfolioNetAssetValue - $ctrl.initialPortfolioValue;
            };

            $scope.calculateTotalEarningsInPercent = function(){
                return (($ctrl.portfolioNetAssetValue/$ctrl.initialPortfolioValue) - 1.0)*100;
            };

            $scope.calculateAssetPercentInPortfolio = function(assetValue){
                return (assetValue/$ctrl.portfolioNetAssetValue)*100;
            };

            $scope.calculateAssetEarnings = function(product){
                for(var productSnapshotIndex in $ctrl.dashBoardData.firstSnapshot.productListSnapshot){
                    var snapshot = $ctrl.dashBoardData.firstSnapshot.productListSnapshot[productSnapshotIndex];
                    if(product.ticker === snapshot.ticker){
                        return product.value - (snapshot.position * snapshot.unitValue);
                    }
                }
            };

            $scope.calculateAssetEarningsPercentage = function(product){
                for(var productSnapshotIndex in $ctrl.dashBoardData.firstSnapshot.productListSnapshot){
                    var snapshot = $ctrl.dashBoardData.firstSnapshot.productListSnapshot[productSnapshotIndex];
                    if(product.ticker === snapshot.ticker){
                        return ((product.value/(snapshot.position * snapshot.unitValue)) - 1.0)*100;
                    }
                }
            };

            $ctrl.resetDashboardData = function() {
                this.dataTable = [];

                this.currentPortfolioLabels = ['Cash'];
                this.currentPortfolioData = [];
                this.currentPortfolioOptions = {legend: {display: true}};

                this.portfolioChart = {};
                this.portfolioChart.labels = [];
                this.portfolioChart.series = ['Net Asset Value'];
                this.portfolioChart.data = [[]];
                this.portfolioChart.options = {
                    legend: {display: true},
                    scales: {
                        yAxes: [{
                            id: 'y-axis-0',
                            ticks: {
                                beginAtZero:true,
                                mirror:false,
                                suggestedMin: 0
                            }
                        }]
                    }
                };

                this.assetChart = {};
                this.assetChart.labels = [];
                this.assetChart.series = ['A', 'B', 'C', 'D'];
                this.assetChart.data = [[], [], [], []];
                this.assetChart.options = {
                    legend: {display: true},
                    scales: {
                        yAxes: [{
                            id: 'y-axis-0',
                            ticks: {
                                beginAtZero:true,
                                mirror:false,
                                suggestedMin: 0
                            }
                        }]
                    }
                }
            };

            $ctrl.populateDashboardData = function() {
                var portfolio = $ctrl.dashBoardData;
                $ctrl.resetDashboardData();

                $ctrl.portfolioNetAssetValue = $scope.calculateNetAssetValue(portfolio);
                $ctrl.cashBalanceChange = portfolio.cashBalance - portfolio.firstSnapshot.cashBalance;

                //Update current portfolio Chart
                this.currentPortfolioData.push(portfolio.cashBalance.toFixed(3));
                for(var i = 0; i < portfolio.products.length; i++){
                    this.currentPortfolioLabels.push(portfolio.products[i].ticker);
                    var productValue = portfolio.products[i].unitValue * portfolio.products[i].position;
                    this.currentPortfolioData.push(productValue.toFixed(3));
                }

                //Create data for product performance view
                if($ctrl.depthSelect.selected === "Asset"){
                    for(var snapshotProperty in portfolio.snapshotList){
                        if (!portfolio.snapshotList.hasOwnProperty(snapshotProperty)) {
                            continue;
                        }

                        var snapshot = portfolio.snapshotList[snapshotProperty];
                        var dataTableRow = $ctrl.prepareDataTableRow(snapshotProperty, snapshot);
                        this.dataTable.push(dataTableRow);

                        this.assetChart.labels.push(dataTableRow.date);
                        this.assetChart.data[0].push(dataTableRow.companyA.toFixed(3));
                        this.assetChart.data[1].push(dataTableRow.companyB.toFixed(3));
                        this.assetChart.data[2].push(dataTableRow.companyC.toFixed(3));
                        this.assetChart.data[3].push(dataTableRow.companyD.toFixed(3));
                    }

                    console.log(this.assetChart);

                //Create data for portfolio performance view
                } else if($ctrl.depthSelect.selected === "Portfolio"){
                    for(var snapshotProperty in portfolio.snapshotList){
                        if (!portfolio.snapshotList.hasOwnProperty(snapshotProperty)) {
                            continue;
                        }

                        var snapshot = portfolio.snapshotList[snapshotProperty];
                        var dataTableRow = $ctrl.prepareDataTableRow(snapshotProperty, snapshot);
                        this.dataTable.push(dataTableRow);

                        this.portfolioChart.labels.push(dataTableRow.date);
                        this.portfolioChart.data[0].push(dataTableRow.netAssetValue.toFixed(3));
                    }
                }
            };

            $ctrl.prepareDataTableRow = function(date, snapshot){
                return {
                    date: date,
                    cashBalance: snapshot.cashBalance,
                    companyA: $ctrl.calculateAssetValue(snapshot.productListSnapshot, "A"),
                    companyB: $ctrl.calculateAssetValue(snapshot.productListSnapshot, "B"),
                    companyC: $ctrl.calculateAssetValue(snapshot.productListSnapshot, "C"),
                    companyD: $ctrl.calculateAssetValue(snapshot.productListSnapshot, "D"),
                    netAssetValue: $ctrl.calculatePortfolioSnapshotNetAssetValue(snapshot)
                };
            };

            $ctrl.calculateAssetValue = function(productList, ticker) {
                var assetValue = 0;
                for(var i = 0; i < productList.length; i++){
                    if(ticker === productList[i].ticker){
                        assetValue = productList[i].unitValue * productList[i].position;
                    }
                }

                return assetValue;
            };

            $ctrl.calculatePortfolioSnapshotNetAssetValue = function(portfolioSnapshot) {
                var assetValue = portfolioSnapshot.cashBalance;
                for(var i = 0; i < portfolioSnapshot.productListSnapshot.length; i++){
                    var product = portfolioSnapshot.productListSnapshot[i];
                    assetValue += product.unitValue * product.position;
                }

                return assetValue;
            };

            /*
            *   When data changes
            */
            $ctrl.$onChanges = function (changes) {
                if (changes.dashboardData) {
                    $ctrl.dashBoardData = changes.dashboardData.currentValue;
                    //fix for when globalData is not ready
                    if((changes.dashboardData.currentValue != null)){
                       $ctrl.populateDashboardData();
                    }
                }
            }
        }),
        controllerAs: '$ctrl'
    })
})(window.angular);
