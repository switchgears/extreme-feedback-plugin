var xfModule = angular.module('xfApp', []);

xfModule.directive('ngEnter', function() {
    return function(scope, element, attrs) {
        element.bind("keydown keypress", function(event) {
            if(event.which === 13) {
                scope.$apply(function(){
                    scope.$eval(attrs.ngEnter);
                });

                event.preventDefault();
            }
        });
    };
});

xfModule.directive('inverted', function() {
    return {
        require: 'ngModel',
        link: function(scope, element, attrs, ngModel) {
            ngModel.$parsers.push(function(val) { return !val; });
            ngModel.$formatters.push(function(val) { return !val; });
        }
    };
});

xfModule.directive('ngBlur', function() {
    return function( scope, elem, attrs ) {
        elem.bind('blur', function() {
            scope.$apply(attrs.ngBlur);
        });
    };
});

xfModule.directive('typeahead', function () {
    return {
        restrict: "E",
        replace: true,
        scope: {
            items: "=",
            btntxt: "@",
            action: "&",
            context: "="
        },
        template: '<div><input type="text" ng-model="query" ng-blur="blur"><button ng-click="action({arg1: query, arg2: context})">{{btntxt}}</button><ul class="typeahead" ng-show="query.length && showList"><li ng-repeat="item in items | filter:query" ng-class="{selected: $index==selectedIndex}" index="{{$index}}">{{item}}</li></ul></div>',
        controller: ["$scope", function($scope) {
            $scope.query = "";
            $scope.selectedIndex = 0;
            $scope.currentLength = 0;
            $scope.showList = false;
            $scope.blur = false;

            $scope.selectNextItem = function() {
                $scope.selectedIndex = ($scope.selectedIndex + 1) % $scope.currentLength;
                $scope.$apply();
            };

            $scope.selectPreviousItem = function() {
                $scope.selectedIndex = ($scope.selectedIndex + $scope.currentLength - 1) % $scope.currentLength;
                $scope.$apply();
            };

            $scope.resetIndex = function() {
                $scope.selectedIndex = 0;
            };
        }],
        link: function(scope, element, attrs, controller) {
            var $input = element.find('input');
            var $ul = element.find('ul');

            $ul.bind('mouseover', function(e) {
                console.log(e);
                var $element = e.target;
                var $i = $element.getAttribute("index");
                scope.$apply(function() {
                    scope.selectedIndex = $i;
                });
            });

            $input.bind('focus', function() {
                scope.$apply(function() {
                    scope.resetIndex();
                    scope.showList = true;
                });
            });

            $input.bind('blur', function() {
                scope.$apply(function() {
                    scope.showList = false;
                    if ($input[0].value.length) {
                        scope.query = $ul.children()[parseInt(scope.selectedIndex)].innerHTML;
                    }
                    scope.resetIndex();
                });
            });

            $input.bind('keydown', function(e) {
                var $list = element.find('ul');
                scope.$apply(function() {
                    scope.currentLength = $list.children().length;
                });

                if (e.keyCode === 40) {
                    e.preventDefault();
                    scope.selectNextItem();
                }

                if (e.keyCode === 38) {
                    e.preventDefault();
                    scope.selectPreviousItem();
                }

                if (e.keyCode === 13) {
                    e.preventDefault();
                    var $query = $list.children()[parseInt(scope.selectedIndex)].innerHTML;
                    scope.$apply(function() {
                        scope.query = $query;
                        scope.showList = false;
                    });
                }
            });
        }
    }
});

xfModule.controller('xfController', [ '$scope', function($scope) {
    $scope.lamps = [];

    it.getLamps(function(t) {
        $scope.lamps = t.responseObject();
        $scope.$apply();
    });

    it.getProjects(function(t) {
        $scope.projects = t.responseObject();
        $scope.$apply();
    });

    $scope.findlampsToggle = true;
    $scope.ipToggle = true;
    $scope.ipAddress = "IP Address";

    $scope.updateIpContent = function() {
        if (!$scope.ipAddress.match(/\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b/)) {
            $scope.ipAddress = "";
            $scope.$apply();
        }
    };

    $scope.changeLamp = function(lamp) {
        it.updateLamp(lamp, function(result) {
            if (result) {
                notificationBar.show('Lamp updated', notificationBar.OK);
            } else {
                notificationBar.show('Lamp not updated', notificationBar.WARNING);
            }
        })
    };

    $scope.suggestProjects = function(number) {
        new Suggest.Local("job-"+number, "suggest-"+number, $scope.projects, {dispAllKey: true});
    };

    $scope.addProjectToLamp = function(job, lamp) {
        it.addProjectToLamp(job, lamp.macAddress, function(t) {
            var newLamps = t.responseObject();
            if (!newLamps.length) {
                notificationBar.show('There was a problem updating the lamp', notificationBar.ERROR);
            } else if (angular.equals($scope.lamps, newLamps)) {
                notificationBar.show('Could not add the job to the lamp', notificationBar.WARNING);
            } else {
                notificationBar.show('Job added to lamp', notificationBar.OK);
                $scope.lamps = newLamps;
                $scope.$apply();
            }
        });
    };

    $scope.removeProjectFromLamp = function(job, lamp) {
        it.removeProjectFromLamp(job, lamp.macAddress, function(t) {
            var newLamps = t.responseObject();
            if (!newLamps.length) {
                notificationBar.show('There was a problem updating the lamp', notificationBar.ERROR);
            } else if (angular.equals($scope.lamps, newLamps)) {
                notificationBar.show('The job was already removed from the job', notificationBar.WARNING);
            } else {
                notificationBar.show('Job removed from lamp', notificationBar.OK);
                $scope.lamps = newLamps;
                $scope.$apply();
            }
        });
    };

    $scope.findlamps = function() {
        $scope.findlampsToggle = false;
        it.findLamps(function(t) {
            var l = t.responseObject();
            if (!l.length) {
                notificationBar.show('No lamps have been found',notificationBar.WARNING);
            }
            $scope.lamps = l;
            $scope.findlampsToggle = true;
            $scope.$apply();
        });
    }

    $scope.addLamp = function(ip) {
        $scope.ipToggle = false;
        $scope.$apply();
        it.addLampByIpAddress(ip, function(t) {
            var l = t.responseObject();

            if (l != null) {
                $scope.lamps = l;
            } else {
                notificationBar.show('Lamp not found', notificationBar.WARNING);
            }
            $scope.ipToggle = true;
            $scope.$apply();
        });
    };

    $scope.removeLamp = function(lamp) {
        var result = confirm("Are you sure you want to remove this lamp?");
        if (result) {
            it.removeLamp(lamp.macAddress, function(t) {
                var l = t.responseObject();
                if (angular.equals(l, $scope.lamps)) {
                    notificationBar.show('Cannot remove the lamp', notificationBar.WARNING);
                } else {
                    notificationBar.show('Lamp removed', notificationBar.OK);
                    $scope.lamps = l;
                    $scope.$apply();
                }
            });
        }
    };

    $scope.write = function(text) {
        console.log(text);
    };
}]);