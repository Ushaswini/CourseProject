package edu.uncc.homework4;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static edu.uncc.homework4.QuestionType.*;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MessagesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class MessagesFragment extends Fragment implements MessagesRecyclerAdapter.OnItemClickListener {

    MessagesRecyclerAdapter messagesRecyclerAdapter;
    ArrayList<String> messagesList;
    ArrayList<Survey> surveyQuestionArrayList;
    RecyclerView messagesView;
    Activity myActivity;
    SharedPreferences prefs;
    SharedPreferences.Editor prefsEditor;
    String UserId = "";
    String Access_Token = "";


    private OnFragmentInteractionListener mListener;

    public MessagesFragment() {
        // Required empty public constructor
    }

    public void getMyActivity(Activity activity) {
        this.myActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_messages, container, false);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    private void GetSurveysAsync() {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Constants.GET_SURVEYS_URL + UserId)
                .header("Authorization", "Bearer " + Access_Token)
                .build();


        Log.d("demo", "final" + UserId);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("demo", "failure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                Log.d("demo", "success");
                messagesList = new ArrayList<>();
                final ArrayList<Survey> surveysList = new ArrayList<Survey>();
                final String myResponse = response.body().string();
                Log.d("demo", myResponse + " hello " + messagesList);

                try {
                    JSONObject jsonObject = new JSONObject(myResponse);
                    JSONArray jsonArray = jsonObject.getJSONArray("SurveysResponded");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonO = jsonArray.getJSONObject(i);

                        Survey survey = new Survey();

                        survey.setSurveyId(jsonO.getInt("SurveyId"));
                        survey.setSurveyName(jsonO.getString("SurveyName"));
                        survey.setSurveytype(jsonO.getInt("SurveyType"));

                        ArrayList<SurveyQuestion> questions = new ArrayList<>();
                       // SurveyQuestion question;
                        JSONArray questionsJSONArray = jsonO.getJSONArray("QuestionResponses");

                        for(int p =0;p< questionsJSONArray.length();p++ ){
                            JSONObject questionJSON = questionsJSONArray.getJSONObject(p);
                            SurveyQuestion question = new SurveyQuestion();
                            question.setQuestionId(questionJSON.getInt("QuestionId"));
                            question.setQuestionText(questionJSON.getString("QuestionText"));
                            question.setQuestionType(questionJSON.getInt("QuestionType"));
                            question.setOptions(questionJSON.getString("Options"));
                            /*question.setMaximum(questionJSON.getDouble("Maximum"));
                            question.setMinimum(questionJSON.getDouble("Minimum"));
                            question.setStepsize(questionJSON.getDouble("StepSize"));*/
                            Log.d("demo2",questionJSON.toString());
                            question.setResponse(questionJSON.getString("ResponseText"));
                            questions.add(question);
                            Log.d("demo1", question.toString());
                        }

                        survey.setQuestions(questions);


                        surveysList.add(survey);
                        Log.d("demo","In responses" + surveysList.size());
                        Log.d("demo",survey.toString());

                    }
                    JSONArray jsonArray1 = jsonObject.getJSONArray("Surveys");
                    for (int i = 0; i < jsonArray1.length(); i++) {
                        JSONObject jsonO = jsonArray1.getJSONObject(i);
                        Survey survey  = new Survey();
                        survey.setSurveyId(jsonO.getInt("SurveyId"));
                        survey.setSurveyName(jsonO.getString("SurveyName"));
                        survey.setStudyCoordinatorId(jsonO.getString("StudyCoordinatorId"));
                        survey.setStudyCoordinatorName(jsonO.getString("StudyCoordinatorName"));
                        survey.setStudyGroupId(jsonO.getInt("StudyGroupId"));
                        survey.setSurveytype(jsonO.getInt("SurveyType"));

                        ArrayList<SurveyQuestion> questions = new ArrayList<>();

                        JSONArray questionsJSONArray = jsonO.getJSONArray("Questions");
                        for(int p =0;p< questionsJSONArray.length();p++ ){
                            JSONObject questionJSON = questionsJSONArray.getJSONObject(p);
                            SurveyQuestion question = new SurveyQuestion();
                            question.setQuestionId(questionJSON.getInt("Id"));
                            question.setQuestionText(questionJSON.getString("QuestionText"));
                            question.setQuestionType(questionJSON.getInt("QuestionType"));
                            question.setOptions(questionJSON.getString("Options"));
                            question.setMaximum(questionJSON.getDouble("Maximum"));
                            question.setMinimum(questionJSON.getDouble("Minimum"));
                            question.setStepsize(questionJSON.getDouble("StepSize"));
                            questions.add(question);
                        }
                        survey.setQuestions(questions);
                        /*SurveyQuestion sq = new SurveyQuestion();
                        sq.setQuestion(jsonO.getString("QuestionText"));
                        sq.setSurveyId(jsonO.getString("SurveyId"));
                        sq.setStudyGrpId(jsonO.getString("StudyGroupId"));
                        sq.setUserId(UserId);
                        sq.setQuesType(jsonO.getInt("QuestionType"));
                        sq.setSurveyTime(jsonO.getString("SurveyCreatedTime"));
                        sq.setResponse("");*/
                        surveysList.add(survey);
                    }

                    Log.d("demo",surveysList.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("demo", "success adapter");

                        surveyQuestionArrayList.clear();
                        surveyQuestionArrayList.addAll(surveysList);
                        messagesRecyclerAdapter.notifyDataSetChanged();
                    }
                });


            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prefs = getActivity().getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);

        Access_Token = prefs.getString(Constants.AUTH_HEADER, "");
        Log.d("demo",Access_Token);
        UserId = prefs.getString(Constants.USERID, "");
        surveyQuestionArrayList = new ArrayList<>();
        messagesRecyclerAdapter = new MessagesRecyclerAdapter(surveyQuestionArrayList, getContext());
        messagesRecyclerAdapter.setOnItemClickListener(MessagesFragment.this);
        messagesView = (RecyclerView) getActivity().findViewById(R.id.recyclerViewResult);

        messagesView.setAdapter(messagesRecyclerAdapter);
        messagesView.setLayoutManager(new LinearLayoutManager(getContext()));
        GetSurveysAsync();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(int position,int checkedId, String reply) {
//TODO:Send response and reload data

        SharedPreferences sharedPref = getActivity().getSharedPreferences(Constants.PREFS,Context.MODE_PRIVATE);
        String access_token = sharedPref.getString(Constants.AUTH_HEADER,"");
        Survey data = surveyQuestionArrayList.get(position);
        String replyText = "";
        //TODO Based on survey type- respond
        SurveyQuestion question = data.getQuestions().get(0);
       switch (question.getQuestionType())
        {
            case Choice:
            {
                if (checkedId == R.id.radioButtonYes) replyText = "Yes";
                else replyText = "No";

                break;
            }
            case TextEntry:
            {
                replyText = reply;
                break;
            }
        }

        Log.d("demo",replyText);

       SurveyResponse response = new SurveyResponse(data.getSurveyId(),UserId,data.getStudyGroupId(),(new Date()).toString());
       QuestionResponse quesResp = new QuestionResponse(question.getQuestionId(),replyText);
       ArrayList<QuestionResponse> list = new ArrayList<>();
       list.add(quesResp);
       response.setQuestionResponses(list);

        Gson gson = new Gson();
        String json = gson.toJson(response);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);
        Log.d("body",json);

       /*RequestBody responseBody = new FormBody.Builder()
               .add("QuestionId",question.getQuestionId()+"" )
               .add("ResponseText",replyText).build();

        RequestBody formBody = new FormBody.Builder()
                .add("UserId", UserId)
                .add("StudyGroupId", data.getStudyGroupId() + "")
                .add("SurveyId", data.getSurveyId() + "")
                .add("Responses[0]",responseBody.toString() )
                .add("ResponseReceivedTime", (new Date()).toString())
                .build();

        Log.d("demo",formBody.toString());*/
        Request request = new Request.Builder()
                .url(Constants.POST_RESPONSE_URL)
                //.header("Content-Type","application/json")
                .header("Authorization", "Bearer "+access_token)
                .post(body)
                .build();


        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("demo","response failure");
                // Toast.makeText(getContext(),"Error in sending response",Toast.LENGTH_SHORT);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Toast.makeText(getContext(),"Response sent successfully !!",Toast.LENGTH_SHORT);
                Log.d("demo","response success");
                Log.d("response", response.message());
                GetSurveysAsync();
            }
        });



    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //sorting messages
    public void sortList(){


            /*Collections.sort(surveyQuestionArrayList, new Comparator<SurveyQuestion>() {
                @Override
                public int compare(SurveyQuestion o1, SurveyQuestion o2) {
                    int returnValue = 0;
                    try {
                        if(new SimpleDateFormat("dd-MM-yyyy").parse(o1.getSurveyTime()).before(new SimpleDateFormat("dd-MM-yyyy").parse(o2.getSurveyTime())))// > 0 ? 1 : 0;
                        {returnValue = -1;}
                        else if(new SimpleDateFormat("dd-MM-yyyy").parse(o1.getSurveyTime()).after(new SimpleDateFormat("dd-MM-yyyy").parse(o2.getSurveyTime())))// > 0 ? 1 : 0;
                        {returnValue = 1; }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    return returnValue;
                }
            });*/





    }
}
