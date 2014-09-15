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
                url: 'http://localhost:3000/alert'})
                .success(function(d){ console.log( "yay" ); })
                .error(function(d){ console.log( "nope" ); });
            }

        return alertAPI;
    });
