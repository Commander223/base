"use strict";

angular.module("subutai.settings-updates.controller", [])
    .controller("SettingsUpdatesCtrl", SettingsUpdatesCtrl);


SettingsUpdatesCtrl.$inject = ["$scope", "SettingsUpdatesSrv", "SweetAlert"];
function SettingsUpdatesCtrl($scope, SettingsUpdatesSrv, SweetAlert) {
    var vm = this;
    vm.config = {isUpdatesAvailable: "waiting"};

    function getConfig() {
        LOADING_SCREEN();
        SettingsUpdatesSrv.getConfig().success(function (data) {
            LOADING_SCREEN('none');
            vm.config = data;
        });
    }

    getConfig();


    vm.update = update;
    function update() {
        LOADING_SCREEN();
        SettingsUpdatesSrv.update(vm.config).success(function (data) {
            LOADING_SCREEN('none');
            SweetAlert.swal("Success!", "Your settings were saved.", "success");
        }).error(function (error) {
            LOADING_SCREEN('none');
            SweetAlert.swal("ERROR!", "Save config error: " + error.replace(/\\n/g, " "), "error");
        });
    }
}