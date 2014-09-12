'use strict';

/* Controllers */

angular.module('buzzMeWhenApp.controllers', [])
  .controller('alertsController', function($scope, alertAPIService) {
        $scope.symbolFilter = null;
        $scope.alertList = [];

        alertAPIService.getAlerts().success(function (response) {
            $scope.alertList = response;
        });        
//        $scope.alertList = [
//            {
//                symbol: "IBM",
//                price: 322,
//                direction: "L"
//            },
//            {
//                symbol: "CSCO",
//                price: 15.5,
//                direction: "S"
//            }
//        ];
  });
