﻿$(document).ready(function () {

    //to prevent back
    // window.onload  = window.history.forward();
    var app = new ViewModel();
    ko.applyBindings(app);

    var tokenKey = 'accessToken';
    self.surveysDataTable = $("#surveysTable").DataTable(
        {
            data: self.surveys,
            dom: 'Bfrtip',
            buttons: [
                'print'
            ],
            columns: [{ data: "SurveyId" }, { data: "SurveyName" }, { data: "StudyGroupName" }]
        });

    LoadStudyGroups();
    LoadQuestions();
    LoadSurveys();
    LoadAllSurveys();
    
    
    function LoadQuestions() {
        var headers = {};
        var token = sessionStorage.getItem(tokenKey);
        if (token) {
            headers.Authorization = 'Bearer ' + token;
        }
        console.log(token);
        $.ajax({
            type: 'GET',
            url: '/api/Questions',
            headers: headers,
            contentType: 'application/json; charset=utf-8'
        }).done(function (data) {
            console.log(data);
            for (var i = 0; i < data.length; i++) {
                self.questions.push(data[i]);
            }
        }).fail(showError);
    }

    function LoadStudyGroups() {
        var headers = {};
        var token = sessionStorage.getItem(tokenKey);
        var userId = sessionStorage.getItem("userId");
        if (token) {
            headers.Authorization = 'Bearer ' + token;
        }
        console.log(token);
        $.ajax({
            type: 'GET',
            url: '/api/StudyGroups/Coordinator?coordinatorId=' + userId,
            headers: headers,
            contentType: 'application/json; charset=utf-8'
        }).done(function (data) {
            for (var i = 0; i < data.length; i++) {
                self.studyGroups.push(data[i]);
            }
        }).fail(showError);
    }

    function LoadSurveys() {
        var headers = {};
        var token = sessionStorage.getItem(tokenKey);
        var coordinatorId = sessionStorage.getItem("userId");
        if (token) {
            headers.Authorization = 'Bearer ' + token;
        }
        console.log(token);
        $.ajax({
            type: 'GET',
            url: '/api/Surveys/CoordinatorSurveys?coordinatorId=' + coordinatorId,
            headers: headers,
            contentType: 'application/json; charset=utf-8'
        }).done(function (data) {
            console.log(data);
            self.surveys = data;
            BindSurveysToDatatable();
        }).fail(showError);
    }

    function LoadAllSurveys() {
        var headers = {};
        var token = sessionStorage.getItem(tokenKey);
        if (token) {
            headers.Authorization = 'Bearer ' + token;
        }
        console.log(token);
        $.ajax({
            type: 'GET',
            url: '/api/Surveys',
            headers: headers,
            contentType: 'application/json; charset=utf-8'
        }).done(function (data) {
            console.log(data);
            for (var i = 0; i < data.length; i++) {
                self.AllSurveys.push(data[i]);
            }
        }).fail(showError);
    }

    function BindSurveysToDatatable(data) {
        self.surveysDataTable.clear();
        self.surveysDataTable.destroy();
        self.surveysDataTable = $("#surveysTable").DataTable(
            {
                data: self.surveys,
                dom: 'Bfrtip',
                buttons: [
                    'print'
                ],
                columns: [{ data: "SurveyId" }, { data: "SurveyName" }, { data: "StudyGroupName" }]
            });
    }


    $('input[name=frequency]').change(function () {
        var value = $('input[name=frequency]:checked').val();
        if (value == "0") {
            $('input[name=time1]').css('visibility', 'visible');
            $('input[name=time2]').css('visibility', 'hidden');
        }
        else if (value == "2") {
            $('input[name=time1]').css('visibility', 'visible');
            $('input[name=time2]').css('visibility', 'visible');
        }
        else {
            $('input[name=time1]').css('visibility', 'hidden');
            $('input[name=time2]').css('visibility', 'hidden');
        }
    });

    $("#btnPublishQuestion").click(function () {
        var frequencyOfNotifications = $('input[name=frequency]:checked').val();

        var questionText = $('input[name=txtQuestion]').val();

       // if (questionText == undefined || questionText == "") {
        //    alert("Please Enter question Text");
         //   return;
        //}
        
        if (frequencyOfNotifications == undefined) {
            alert("Please select frequency of Notification");
            return;
        }
        var time1, time2;
        if (frequencyOfNotifications == "0") {
            time1 = $('input[name=time1]').val();

            if (time1 == undefined || time1 == "") {
                alert("Please Enter time slot of Notification");
                return;
            }
            time2 = "";
        }
        else if (frequencyOfNotifications == "1") {
            time1 = "";
            time2 = "";
        }
        else if (frequencyOfNotifications == "2") {
            time1 = $('input[name=time1]').val();
            time2 = $('input[name=time2]').val();

            if (time1 == undefined || time1 == "" || time2 == undefined || time2 == "") {
                alert("Please Enter time slot of Notification");
                return;
            }
        }
        var tokenKey = 'accessToken';
        var headers = {};
        var token = sessionStorage.getItem(tokenKey);
        if (token) {
            headers.Authorization = 'Bearer ' + token;
        }
        var date = new Date();
        var surveyData = {
            SurveyId: self.selectedQuestionId(),            
            StudyGroupId: self.selectedStudyGroupForQuestion(),
            SurveyCreatedTime: date.toString(),
            FrequencyOfNotifications: frequencyOfNotifications,
            Time1: time1.toString(),
            Time2: time2.toString()
        }

        console.log(JSON.stringify(surveyData));
        $.ajax({
            type: 'POST',
            url: '/api/Surveys/PublishQuestion',
            headers: headers,
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(surveyData)

        }).done(function (data) {
            alert("Published!");
            self.selectedQuestionId('');
            self.selectedStudyGroupForSurvey('');
            $('input[name=time1]').val("");
            $('input[name=time2]').val("");
            $('input[name=frequency]:checked').prop('checked', false);

        }).fail(showError);



    })


    $("#btnPublishSurvey").click(function () {

        var tokenKey = 'accessToken';
        var headers = {};
        var token = sessionStorage.getItem(tokenKey);
        if (token) {
            headers.Authorization = 'Bearer ' + token;
        }
        var date = new Date();
        var surveyData = {
            SurveyId: self.selectedSurveyId(),
            StudyGroupId: self.selectedStudyGroupForSurvey(),
            SurveyCreatedTime: date.toString()
        }

        console.log(JSON.stringify(surveyData));
        $.ajax({
            type: 'POST',
            url: '/api/Surveys/Publish',
            headers: headers,
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(surveyData)

        }).done(function (data) {
            alert("Published!");
            LoadSurveys();
            $('input[name=time1]').val("");
            $('input[name=time2]').val("");
            $('input[name=frequency]:checked').prop('checked', false);

        }).fail(showError);



    })


    function guid() {
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    }
    $('#questionType').on('change', function () {
        console.log("in change");
        //alert(this.value);
        //alert($(this).find(":selected").val());
        if (this.value == 0) {
            $('#options').css('visibility', 'hidden');
        }
        if (this.value == 1) {
            $('#options').css('visibility', 'visible');
        }
        if (this.value == 2) {
            $('#options').css('visibility', 'hidden');
        }
    })
    function ViewModel() {
        
        self.studyGroups = ko.observableArray([]);
        self.questions = ko.observableArray([]);
        self.AllSurveys = ko.observableArray([]);
        self.surveys = {}
        self.userEmail = ko.observable();
        self.selectedStudyGroup = ko.observable();
        self.selectedStudyGroupForQuestion = ko.observable();
        self.selectedStudyGroupForSurvey = ko.observable();
        self.selectedQuestionId = ko.observable();
        self.selectedSurveyId = ko.observable();

        self.questionText = ko.observable();
        self.options = ko.observable();

        self.result = ko.observable();
        self.errors = ko.observableArray([]);

    }

    $("#presentSurvey").click(function () {

        console.log("indied button click");
        var tokenKey = 'accessToken';
        var headers = {};
        var token = sessionStorage.getItem(tokenKey);
        if (token) {
            headers.Authorization = 'Bearer ' + token;
        }
        console.log(token);
        var surveyData = {
            StudyGroupId: self.selectedStudyGroupForSurvey()
        }
        $.ajax({
            type: 'POST',
            url: '/api/GenerateSurvey',
            headers: headers,
            data: surveyData,

        }).done(function (data) {
            console.log("data is received");

        }).fail(showError);
    })


    $('#navigateToSurveyManager').click(function () {
        // Response.Redirect("~/Views/Survey/Manage.cshtml");

        window.location.href = yourApp.Urls.surveyManageUrl;
        //replace("~/Views/Survey/Manage");

    })

   
    function showError(jqXHR) {
        console.log(jqXHR);
        self.result(jqXHR.status + ': ' + jqXHR.statusText);

        var response = jqXHR.responseJSON;
        if (response) {
            if (response.Message) self.errors.push(response.Message);
            if (response.ModelState) {
                var modelState = response.ModelState;
                for (var prop in modelState) {
                    if (modelState.hasOwnProperty(prop)) {
                        var msgArr = modelState[prop]; // expect array here
                        if (msgArr.length) {
                            for (var i = 0; i < msgArr.length; ++i) self.errors.push(msgArr[i]);
                        }
                    }
                }
            }
            if (response.error) self.errors.push(response.error);
            if (response.error_description) {
                self.errors.push(response.error_description);
                console.log(response.error_description);
            }
        }
    }



})

