'use strict';

var pipelineApp = angular.module('cApp.pipeline', ['ngRoute']);

pipelineApp.factory('PipelineDataService', ['$http', function PipelineDataService($http) {
    var getPipelines = function () {
        return $http.get('/pipeline/getAll')
            .then(function (response) {
                return response.data;
            });
    };
    var getPipeline = function (name) {
        return $http.get('/pipeline/get?name=' + name)
            .then(function (response) {
                return response.data;
            });
    };
    var getLibraries = function () {
        return $http.get('/clustering/libraries')
            .then(function (response) {
                return response.data.libraries;
            })
    };
    return {
        getPipelines: getPipelines,
        getPipeline: getPipeline,
        getLibraries: getLibraries
    };
}]);

pipelineApp.config(['$routeProvider', function ($routeProvider) {
    $routeProvider
        .when('/cluster/pipelines', {
            templateUrl: '/assets/components/pipeline/cluster-pipeline-create.html',
            controller: 'ClusterPipelineCtrl',
            controllerAs: 'vm1',
            resolve: {
                pipelines: function (PipelineDataService) {
                    return PipelineDataService.getPipelines();
                },
                libraries: function (PipelineDataService) {
                    return PipelineDataService.getLibraries();
                }
            }
        });
}]);

pipelineApp.controller('ClusterPipelineCtrl', ['scAuth', 'scData', 'scModel', 'pipelines', 'libraries', '$http', '$location', function (scAuth, scData, scModel, pipelines, libraries, $http, $location) {
    var self = this;

    self.pipelines = pipelines;
    self.libraries = libraries;
    self.showRest = false;
    self.transformers = undefined;
    self.algorithms = undefined;
    self.workspace = undefined;
    self.selectedType = undefined;
    self.dataset = "";
    self.mongoProjectKey = "";

    self.onLibrarySelect = function (id) {
        self.selectedLibrary = self.libraries[id - 1];
        self.selectedAlgorithm = undefined;
        self.algoOptions = undefined;
        self.showRest = true;
    };
    var AnimateRotate = function (angle, repeat) {
        var duration = 1000;
        self.rotateNumber = setTimeout(function () {
            if (repeat && repeat === "infinite") {
                AnimateRotate(angle, repeat);
            } else if (repeat && repeat > 1) {
                AnimateRotate(angle, repeat - 1);
            }
        }, duration);
        var $elem = $('#upload-icon');

        $({deg: 0}).animate({deg: angle}, {
            duration: duration,
            step: function (now) {
                $elem.css({
                    'transform': 'rotate(' + now + 'deg)'
                });
            }
        });
    };

    self.onSelectTransformer = function (id) {
        self.transformer = {
            id: id
        };
    };

    self.onSelectAlgorithm = function (id) {
        self.algorithm = self.selectedLibrary.options.algorithms.filter(function (algorithm) {
            return algorithm.id === id;
        })[0];
        self.algoOptions = self.algorithm.options !== undefined ? self.algorithm.options : undefined;
    };

    self.onAlgoOptionsChange = function (algoOptionName) {
        self.algoOptions.filter(function (algoOptions) {
            return algoOptions.name === algoOptionName;
        })[0].set("value", $('#' + algoOptionName).value());
        self.algorithm.options = self.algoOptions;
    };

    self.onFileChange = function (ele) {
        var files = ele.files;
        self.file = files[0];
    };

    self.uploadDataSetFile = function () {
        AnimateRotate(360, "infinite");
        var fd = new FormData();
        fd.append("file", self.file);
        $http.post("/clustering/dataset/upload", fd, {
            headers: {'Content-Type': undefined}
        }).then(function (response) {
            clearTimeout(self.rotateNumber);
            Materialize.toast('File Uploaded!', 5000, "bottom");
            self.dataset = response.data.results.path;
        });
    };

    self.linkSC = function () {
        scData.Workspace.query(function (workspaces) {
            self.workspaces = workspaces;
        });
    };

    self.linkMongo = function() {
        self.mongo = true;
    };

    self.getPages = function () {
        self.pages = [];
        self.attributeType = "Page";
        scData.Workspace.get({id: self.workspace}, function (workspace) {
            scData.Entity.get({id: workspace.rootEntity.id}, function (entity) {
                entity.children.forEach(function (subpage) {
                    if (subpage.name.indexOf(".") < 0) {
                        self.pages.push(subpage);
                    }
                });
            });
        });
        self.types = [];
        scData.Workspace.getEntityTypes({id: self.workspace}, function (types) {
            self.types = types;
        });
    };

    self.getAttributes = function (type) {
        self.attributes = [];
        self.selectedType = type;
        scModel.EntityType.getAttributeDefinitions({id: type.id}, function (attributes) {
            self.attributes = attributes;
        });
    };

    self.updateSelection = function (position, entities) {
        angular.forEach(entities, function (subscription, index) {
            if (position !== index)
                subscription.checked = false;
        });
    };

    self.resetSCLink = function () {
        self.selectedAttributesForMining = undefined;
        self.selectedType = undefined;
        self.typeCheckBox = undefined;
        self.types = undefined;
        self.workspaces = undefined;
        self.workspace = undefined;
        self.scFileName = undefined;
        self.mongoProjectKey = undefined;
    };

    self.createClusteringPipeline = function () {
        var request_url = "";
        var pipelineName = self.pipelineName;
        switch (self.selectedLibrary.id) {
            case 1:
                request_url = "/spark/train/pipeline/" + pipelineName;
                break;
            case 2:
                request_url = "/weka/train/pipeline/" + pipelineName;
                break;
        }
        Materialize.toast('Creating Pipeline, Please Wait!', 5000);
        $("#progress").css({
            "visibility": "visible"
        });
        if (self.scFileName) {
            self.dataset = self.scFileName;
        } else {
            self.dataset = pipelineName;
        }
        self.algorithm.options = self.algoOptions;
        var miningAttr = [];
        if (self.selectedAttributesForMining)
            self.selectedAttributesForMining.forEach(function (attr) {
                miningAttr.push(attr.name);
            });

        var data = {
            pipeline: {
                href: request_url,
                name: pipelineName,
                library: {
                    name: self.selectedLibrary.name,
                    id: self.selectedLibrary.id
                },
                scLink: !!self.selectedType,
                scData: {
                    filename: self.scFileName,
                    type: self.selectedType,
                    miningAttributes: miningAttr
                },
                dataset: self.dataset,
                algorithm: self.algorithm,
                preprocessors: self.preprocessors,
                transformer: self.transformer,
                mongoProjectKey: self.mongoProjectKey
            }
        };
        $http.post("/clustering/pipeline/create", data)
            .then(function (response) {
                Materialize.toast('Pipeline Create!', 4000);
                $("#progress").css({
                    "visibility": "hidden"
                });
                Materialize.toast('You will be redirect to visualize results', 5000); // 4000 is the duration of the toast
                if (response.data.results) {
                    $location.path("/clustering/clusters/" + pipelineName);
                }
            });
    };
}]);