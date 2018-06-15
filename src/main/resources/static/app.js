var globalData = {};

var app = angular.module('murexApp', ['ngMaterial','md.data.table','chart.js'])
    .config(function($mdThemingProvider) {
        $mdThemingProvider.definePalette('amazingPaletteName', {
            '50': '80ff00',
            '100': '80ff00',
            '200': '80ff00',
            '300': '80ff00',
            '400': 'ffc766',
            '500': 'ffb433',
            '600': 'ffa300',
            '700': 'd32f2f',
            '800': 'c62828',
            '900': 'b71c1c',
            'A100': 'ff8a80',
            'A200': 'ff5252',
            'A400': 'ff1744',
            'A700': 'd50000',
            'contrastDefaultColor': 'light',    // whether, by default, text (contrast)
                                                // on this palette should be dark or light

            'contrastDarkColors': ['50', '100', //hues which contrast should be 'dark' by default
             '200', '300', '400', 'A100'],
            'contrastLightColors': undefined    // could also specify this if default was 'dark'
          });

      $mdThemingProvider.theme('default')
        .primaryPalette('amazingPaletteName');
        //.accentPalette('orange');
    });

app.controller("murexRootController", function($scope, $http){
    $scope.tradePerformed = false;
    $scope.account = {};

    $scope.backTestData = function(){
        tradeWithBackTestData();
    };

    $scope.realData = function(){
        tradeWithRealData();
    };

    $scope.strategyEvaluation = function(){
        evaluateStrategies();
    };

    var loadAccount = function(){
        $http({
            method: 'GET',
            url: 'http://localhost:8080/algo/account'

        }).then(function successCallback(response) {
            $scope.account = response.data;
            console.log($scope.account);
            $scope.tradePerformed = true;
        }, function errorCallback(response) {
            console.log("Error when loading accounts");
            console.log(response);
        });
    };

    var tradeWithBackTestData = function(){
        $http({
            method: 'GET',
            url: 'http://localhost:8080/algo/backTest'
        }).then(function successCallback(response) {
            loadAccount();
        }, function errorCallback(response) {
            console.log("Error when trading with backtest data");
            console.log(response);
        });
    };

    var tradeWithRealData = function(){
        $http({
            method: 'GET',
            url: 'http://localhost:8080/algo/real'
        }).then(function successCallback(response) {
            loadAccount();
        }, function errorCallback(response) {
            console.log("Error when trading with real data");
            console.log(response);
        });
    };

    var evaluateStrategies = function(){
        $http({
            method: 'GET',
            url: 'http://localhost:8080/algo/strategyEval'
        }).then(function successCallback(response) {
            loadAccount();
        }, function errorCallback(response) {
            console.log("Error when evaluating all strategies");
            console.log(response);
        });
    };
});
