'use strict';

angular.module('ice.entry.service', [])
    .factory('Selection', function ($rootScope) {
        var selectedSearchResults = {};
        var selectedSearchResultsCount = 0;
        var selectedSearchNotificationSent = false;  // send notification when at least one is selected and then none
        var canEdit = false;

        return {
            selectSearchEntry:function (entry) {
                canEdit = entry.canEdit;

                if (selectedSearchResults[entry.id]) {
                    // remove entry
                    selectedSearchResults[entry.id] = undefined;
                    selectedSearchResultsCount -= 1;
                } else {
                    selectedSearchResults[entry.id] = entry;
                    selectedSearchResultsCount += 1;
                }

                // determine when to send a notification
                if (selectedSearchResultsCount == 0) {
                    if (selectedSearchNotificationSent) {
                        // notify again that count is not 0
                        $rootScope.$emit("EntrySelected", selectedSearchResultsCount);
                        selectedSearchNotificationSent = false;
                    } else {
                        // no need
                    }
                } else {
                    // if count > 0
                    if (selectedSearchNotificationSent) {
                        // no need. count is at least one
                    } else {
                        $rootScope.$emit("EntrySelected", selectedSearchResultsCount);
                        selectedSearchNotificationSent = true;
                    }
                }
            },

            searchEntrySelected:function (entry) {
                return selectedSearchResults[entry.id] != undefined;
            },

            getSelectedEntries:function () {
                var selected = [];
                for (var k in selectedSearchResults) {
                    if (selectedSearchResults.hasOwnProperty(k) && selectedSearchResults[k]) {
                        selected.push({id:k});
                    }
                }
                return selected;
            },

            canEdit:function () {
                return canEdit && selectedSearchResultsCount > 0;
            },

            // resets all selected and send notifications
            reset:function () {
                selectedSearchResults = {};
                selectedSearchResultsCount = 0;
                selectedSearchNotificationSent = false;
                canEdit = false;
                $rootScope.$emit("EntrySelected", selectedSearchResultsCount);
            }
        }
    })
    .factory('EntryService', function () {
        var toStringArray = function (objArray) {
            var result = [];
            angular.forEach(objArray, function (object) {
                if (!object || !object.value || object.value === "")
                    return;
                result.push(object.value);
            });
            return result;
        };

        // commons fields to all the different types of parts supported by the system
        var partFields = [
            {label:"Name", required:true, schema:'name', placeHolder:'e.g. JBEI-0001', inputType:'short'},
            {label:"Alias", schema:'alias', inputType:'short'},
            {label:"Principal Investigator", required:true, schema:'principalInvestigator', inputType:'withEmail', bothRequired:'false'},
            {label:"Funding Source", schema:'fundingSource', inputType:'short'},
            {label:"Status", schema:'status', options:[
                {value:"Complete", text:"Complete"},
                {value:"In Progress", text:"In Progress"},
                {value:"Abandoned", text:"Abandoned"},
                {value:"Planned", text:"Planned"}
            ]},
            {label:"Bio Safety Level", schema:'bioSafetyLevel', options:[
                {value:"1", text:"Level 1"},
                {value:"2", text:"Level 2"}
            ]},
            {label:"Creator", required:true, schema:'creator', inputType:'withEmail', bothRequired:'true'},
            {label:"Keywords", schema:'keywords', inputType:'medium'},
            {label:"Links", schema:'links', inputType:'add'},
            {label:"Summary", required:true, schema:'shortDescription', inputType:'long'},
            {label:"References", schema:'references', inputType:'long'},
            {label:"Intellectual Property", schema:'intellectualProperty', inputType:'long'}
        ];

        // fields peculiar to plasmids
        var plasmidFields = [
            {label:"Backbone", schema:'backbone', subSchema:'plasmidData', inputType:'medium'},
            {label:"Circular", schema:'circular', inputType:'bool', subSchema:'plasmidData'},
            {label:"Origin of Replication", schema:'originOfReplication', inputType:'autoComplete',
                autoCompleteField:'ORIGIN_OF_REPLICATION', subSchema:'plasmidData'},
            {label:"Selection Markers", required:true, schema:'selectionMarkers', inputType:'autoCompleteAdd',
                autoCompleteField:'SELECTION_MARKERS'},
            {label:"Promoters", schema:'promoters', subSchema:'plasmidData', inputType:'autoComplete', autoCompleteField:'PROMOTERS'},
            {label:"Replicates In", schema:'replicatesIn', subSchema:'plasmidData', inputType:'autoComplete', autoCompleteField:'REPLICATES_IN'}
        ];

        // fields peculiar to arabidopsis seeds
        var seedFields = [
            {label:"Sent To ABRC", schema:'sentToABRC', help:"Help Text", inputType:'bool', subSchema:'arabidopsisSeedData'},
            {label:"Plant Type", schema:'plantType', subSchema:'arabidopsisSeedData', options:[
                {value:"EMS", text:"EMS"},
                {value:"OVER_EXPRESSION", text:"OVER_EXPRESSION"},
                {value:"RNAI", text:"RNAi"},
                {value:"REPORTER", text:"Reporter"},
                {value:"T_DNA", text:"T-DNA"},
                {value:"OTHER", text:"Other"}
            ]},
            {label:"Generation", schema:'generation', subSchema:'arabidopsisSeedData', options:[
                {value:"UNKNOWN", text:"UNKNOWN"},
                {value:"F1", text:"F1"},
                {value:"F2", text:"F2"},
                {value:"F3", text:"F3"},
                {value:"M0", text:"M0"},
                {value:"M1", text:"M1"},
                {value:"M2", text:"M2"},
                {value:"T0", text:"T0"},
                {value:"T1", text:"T1"},
                {value:"T2", text:"T2"},
                {value:"T3", text:"T3"},
                {value:"T4", text:"T4"},
                {value:"T5", text:"T5"}
            ]},
            {label:"Harvest Date", schema:'harvestDate', subSchema:'arabidopsisSeedData', inputType:'date'},
            {label:"Homozygosity", schema:'homozygosity', subSchema:'arabidopsisSeedData', inputType:'medium'},
            {label:"Ecotype", schema:'ecotype', subSchema:'arabidopsisSeedData', inputType:'medium'},
            {label:"Selection Markers", required:true, schema:'selectionMarkers', inputType:'autoCompleteAdd',
                autoCompleteField:'SELECTION_MARKERS'}
        ];

        // fields peculiar to seeds
        var strainFields = [
            {label:"Selection Markers", required:true, schema:'selectionMarkers',
                inputType:'autoCompleteAdd', autoCompleteField:'SELECTION_MARKERS'},
            {label:"Genotype/Phenotype", schema:'genotypePhenotype', inputType:'long', subSchema:'strainData'},
            {label:"Plasmids", schema:'plasmids', inputType:'autoComplete', autoCompleteField:'PLASMID_PART_NUMBER', subSchema:'strainData'},
            {label:"Host", schema:'host', inputType:'short', subSchema:'strainData'}
        ];

        var generateLinkOptions = function (type) {
            switch (type.toLowerCase()) {
                case 'plasmid':
                    return [
                        {type:'part', display:'Part'},
                        {type:'plasmid', display:'Plasmid'}
                    ];

                case 'part':
                    return [
                        {type:'part', display:'Part'}
                    ];

                case 'strain':
                    return [
                        {type:'part', display:'Part'},
                        {type:'plasmid', display:'Plasmid'},
                        {type:'strain', display:'Strain'}
                    ];

                case 'arabidopsis':
                    return [
                        {type:'part', display:'Part'},
                        {type:'arabidopsis', display:'Arabidopsis Seed'}
                    ];
            }
        };

        var validateFields = function (part, fields) {
            var canSubmit = true;

            // main type
            angular.forEach(fields, function (field) {
                if (!field.required)
                    return;

                if (field.inputType === 'add' || field.inputType === 'autoCompleteAdd') {
                    if (part[field.schema].length == 0) {
                        field.invalid = true;
                    }
                    else {
                        for (var i = 0; i < part[field.schema].length; i += 1) {
                            var fieldValue = part[field.schema][i].value;
                            field.invalid = !fieldValue || fieldValue === '';
                        }
                    }
                } else {
                    field.invalid = (part[field.schema] === undefined || part[field.schema] === '');
                }

                if (canSubmit) {
                    canSubmit = !field.invalid;
                }
            });
            return canSubmit;
        };

        var getFieldsForType = function (type) {
            var fields = angular.copy(partFields);
            type = type.toLowerCase();
            switch (type) {
                case 'strain':
                    fields.splice.apply(fields, [7, 0].concat(strainFields));
                    return fields;

                case 'arabidopsis':
                    fields.splice.apply(fields, [7, 0].concat(seedFields));
                    return fields;

                case 'plasmid':
                    fields.splice.apply(fields, [7, 0].concat(plasmidFields));
                    return fields;

                case 'part':
                default:
                    return fields;
            }
        };

        return {
            toStringArray:function (obj) {
                return toStringArray(obj);
            },

            linkOptions:function (type) {
                return generateLinkOptions(type);
            },

            getFieldsForType:function (type) {
                return getFieldsForType(type);
            },

            // converts to a form that the backend can work with
            getTypeData:function (entry) {
                var type = entry.type.toLowerCase();
                var fields = getFieldsForType(type);
                angular.forEach(fields, function (field) {
                    if (field.subSchema) {
                        if (entry[field.subSchema] === undefined)
                            entry[field.subSchema] = {};
                        entry[field.subSchema][field.schema] = entry[field.schema];
                    }
                });

                return entry;
            },

            // inverse of the above. converts to form ui can work with
            convertToUIForm:function (entry) {
                var type = entry.type.toLowerCase();
                var fields = getFieldsForType(type);

                angular.forEach(fields, function (field) {
                    if (field.subSchema && entry[field.subSchema]) {
                        entry[field.schema] = entry[field.subSchema][field.schema];
                    }
                });

                return entry;
            },

            validateFields:function (part, fields) {
                return validateFields(part, fields);
            },

            // converts autocomplete fields from an array string to an array of objects in order to be
            // able to use ng-model on the ui
            // also converts entry to form that UI can work with
            setNewEntryFields:function (entry) {
                var type = entry.type.toLowerCase();
                var fields = getFieldsForType(type);

                angular.forEach(fields, function (field) {
                    if (field.inputType === 'autoCompleteAdd' || field.inputType === 'add') {
                        entry[field.schema] = [
                            {value:''}
                        ];
                    }

                    if (field.subSchema && entry[field.subSchema]) {
                        entry[field.schema] = entry[field.subSchema][field.schema];
                    }
                });

                entry.bioSafetyLevel = '1';
                entry.status = 'Complete';
                entry.parameters = [];
                return entry;
            }
        }
    });