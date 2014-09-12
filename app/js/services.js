'use strict';

/* Services */


// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('buzzMeWhenApp.services', []).
    factory('alertAPIService', function($http) {

        var alertAPI = {};

        alertAPI.getAlerts = function() {
            return $http({
                method: 'GET',
                url: 'http://svc.swtradewindsllc.com/buzz/alert',
                headers:{
                    'Access-Control-Allow-Origin': '*',
                    'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
                    'Access-Control-Allow-Headers': 'Origin, X-Requested-With, Content-Type, Accept'
                }})
                .success(function(d){ console.log( "yay" ); })
                .error(function(d){ console.log( "nope" ); });
            }

        return alertAPI;
    });
