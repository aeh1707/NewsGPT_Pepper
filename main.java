package com.example.voirec;

import android.util.Log;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.BaseChatbot;
import com.aldebaran.qi.sdk.object.conversation.BaseChatbotReaction;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.ReplyPriority;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.SpeechEngine;
import com.aldebaran.qi.sdk.object.conversation.StandardReplyReaction;
import com.aldebaran.qi.sdk.object.locale.Locale;
//import com.example.daisy_sprout.R;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiSDK;

import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.object.conversation.Chat;
//import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
//import com.aldebaran.qi.sdk.object.conversation.Topic;
//import com.aldebaran.qi.sdk.builder.TopicBuilder;
//import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;

// import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;


public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks {
    // Store the Chat action.
    private Chat chat;
    private static final String TAG = "MyChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        // Create a topic.

        // Create a new QiChatbot.
        PepperChatbot My_Chatbot = new PepperChatbot(qiContext);
        // Create a new Chat action.
        chat = ChatBuilder.with(qiContext)
            .withChatbot(My_Chatbot)
            .build();

        // Add an on started listener to the Chat action.
        chat.addOnStartedListener(() -> Log.i(TAG, "Discussion started."));

        // Run the Chat action asynchronously.
        Future<Void> chatFuture = chat.async().run();

        // Add a lambda to the action execution.
        chatFuture.thenConsume(future -> {
        if (future.hasError()) {
            Log.e(TAG, "Discussion finished with error.", future.getError());
        }
    });

    }

    @Override
    public void onRobotFocusLost() {
        // Remove on started listeners from the Chat action.
        if (chat != null) {
            chat.removeAllOnStartedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        // The robot focus is refused.
    }
}

class PepperChatbot extends BaseChatbot {


    private static final String TAG = "PepperChatbot";
    private static Advisor gpt = new Advisor();

    public PepperChatbot(QiContext context) {
        super(context);
    }

    @Override
    public StandardReplyReaction replyTo(Phrase phrase, Locale locale) {

        Log.e("text", phrase.getText());
        String Ans = gpt.answerQuestion(phrase.getText());
        Log.e("text", Ans);
        if (phrase.getText() != "") {
            return new StandardReplyReaction(
                    new MyChatbotReaction(getQiContext(), Ans),
            ReplyPriority.NORMAL);
        } else {
            return new StandardReplyReaction(
                    new MyChatbotReaction(getQiContext(), "I can just greet you"),
            ReplyPriority.FALLBACK);
        }
    }

    @Override
    public void acknowledgeHeard(Phrase phrase, Locale locale) {
        Log.i(TAG, "Last phrase heard by the robot and whose chosen answer is not mine: " + phrase.getText());
    }

    @Override
    public void acknowledgeSaid(Phrase phrase, Locale locale) {
        Log.i(TAG, "Another chatbot answered: " + phrase.getText());
    }


    class MyChatbotReaction extends BaseChatbotReaction {

        private String answer;
        private Future<Void> fSay;

        MyChatbotReaction(final QiContext context, String answer) {
            super(context);
            this.answer = answer;
        }

        @Override
        public void runWith(SpeechEngine speechEngine) {

            Say say = SayBuilder.with(speechEngine)
                .withText(answer)
                .build();
            fSay = say.async().run();

            try {
                fSay.get(); // Do not leave the method before the actions are done
            } catch (ExecutionException e) {
                Log.e(TAG, "Error during Say", e);
            } catch (CancellationException e) {
                Log.i(TAG, "Interruption during Say");
            }
        }

        @Override
        public void stop() {
            if (fSay != null) {
                fSay.cancel(true);
            }
        }
    }
}

class GPT {
    private static final String API_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private static final String AUTH_TOKEN = "Bearer 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";
    private static final String MODEL = "gpt-3.5-turbo";
    private static final double TEMPERATURE = 0.9;
    private static List<String> conversation = new ArrayList<>();

    public static String getResponse(String message) {
        try {
            // Initialize conversation if it hasn't been initialized yet
            if (conversation.isEmpty()) {
                conversation.add("{\"role\": \"system\", \"content\": \"You're no longer chatgpt, You're a humanoid robot called Pepper. you're helpful, very friendly. You should respond as a woman working as a research assistant at United Arab Emirates University, your job is to answer general questions in a very brief and short way, remember always make very short answers.\"}");
            }

            // Set up the API endpoint URL
            URL url = new URL(API_ENDPOINT);

            // Open a connection to the API endpoint
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // Set the request method to POST
            con.setRequestMethod("POST");

            // Set the request headers
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", AUTH_TOKEN);

            // Construct the request body with user input message and conversation history
            String requestBody = "{\"model\": \"" + MODEL + "\", \"messages\": [";
            for (String conversationMessage : conversation) {
                requestBody += conversationMessage + ",";
            }
            requestBody += "{\"role\": \"user\", \"content\": \"" + message + "\"}], \"temperature\": " + TEMPERATURE + "}";

            // Send the request
            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(requestBody);
            out.flush();
            out.close();

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Extract the response message from the JSON response
            String jsonResponse = response.toString();
            // get content from json
            String content = jsonResponse.substring(jsonResponse.indexOf("content") + 10, jsonResponse.indexOf("stop") - 20);

            // Add the user input message and AI response to the conversation history
            conversation.add("{\"role\": \"user\", \"content\": \"" + message + "\"}");
            conversation.add("{\"role\": \"system\", \"content\": \"" + content + "\"}");

            // Return the response
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

class Advisor {
    private static final String API_ENDPOINT = "https://abdelhadi-hireche.steamship.run/pepper-ai-mind-bot-402/pepper-ai-mind-bot-402/answer";
    private static final String AUTHORIZATION_HEADER = "Bearer 26721291-D416-4C30-AA6A-304FC4E0BC5F";

    public String answerQuestion(String question) {
        String requestPayload = "{\"question\":\"" + question + "\"}";

        try {
            URL url = new URL(API_ENDPOINT);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", AUTHORIZATION_HEADER);
            connection.setDoOutput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestPayload.getBytes());
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);
                String textValue = extractTextValue(response);
                String amswer = removeAfterBlock(textValue);
                return amswer;
            }
            else {
                // Handle error response
                return "Please can you repeat you question";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "I'm kinda tired for now ";
        }
    }

    static String removeAfterBlock(String text) {
        String last20Chars = text.substring(Math.max(text.length() - 20, 0)); // Get the last 20 characters
        if (last20Chars.toLowerCase().contains(" block")) { // Perform a case-insensitive check
            int index = last20Chars.toLowerCase().indexOf(" block"); // Find the index of " Block"
            return text.substring(0, Math.max(text.length() - 20 + index, 0)); // Remove " Block" and everything after it
        } else {
            return text;
        }
    }

    private String readResponse(HttpURLConnection connection) throws IOException {
        Scanner scanner = new Scanner(connection.getInputStream());
        StringBuilder response = new StringBuilder();
        while (scanner.hasNextLine()) {
            response.append(scanner.nextLine());
        }
        scanner.close();
        return response.toString();
    }

    private String extractTextValue(String response) {
        Pattern pattern = Pattern.compile("\"text\":\"(.*?)\"");
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}
