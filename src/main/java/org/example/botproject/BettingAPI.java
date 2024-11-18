package org.example.botproject;

import java.net.http.HttpClient;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;


public class BettingAPI {
    private final HttpClient client;
    private final Gson gson;
    private final String baseUrl;
    private final String applicationKey;
    private String sessionToken;

    public BettingAPI(String baseUrl, String applicationKey) {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.baseUrl = baseUrl;
        this.applicationKey = applicationKey;

    }

    // Creating all the POST API calls for the Bot Project. This includes:
    // Login & token storage, Listing Event Types, Listing Events, Listing Market Catalogue, and placing a bet!

    public String login(String username, String password) throws IOException, InterruptedException, URISyntaxException {
        Map<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);

        // String requestBody = gson.toJson(data);

        // POST request for login
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/api/login"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-Application", applicationKey)
                .header("X-IP", "127.0.0.1")
                // Username and password in POST body
                .POST(HttpRequest.BodyPublishers.ofString("username=" + username + "&password=" + password))
                .build();
        // receiving the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();


        Gson gson = new Gson();
        LoginResponse loginResponse = gson.fromJson(response.body(), LoginResponse.class);
        String sessionToken = loginResponse.getToken();
        this.sessionToken = sessionToken;

        // Check if the token is available
        if (loginResponse.getToken() != null) {
            System.out.println("Token: " + loginResponse.getToken());
        } else {
            System.out.println("Error: " + loginResponse.getError());

            return responseBody;
        }
        return responseBody;
    }

    public String listEventTypes() throws IOException, InterruptedException, URISyntaxException {
        String requestBody = "{\"jsonrpc\": \"2.0\", \"method\": \"SportsAPING/v1.0/listEventTypes\", \"params\": {\"filter\": {}}, \"id\": 1}";
        // String requestBody = gson.toJson(requestData);
        //HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://ang.nxt.internal/exchange/betting/json-rpc/v1")) // Correct JSON-RPC endpoint
                .header("Accept", "application/json")
                // .header("Content-Type", "application/x-www-form-urlencoded")
                .header("content-type", "application/json")
                .header("X-Authentication", sessionToken)
                .header("X-Application", "npo67wopV4oKVu5g")
                // .header("X-IP", "127.0.0.1")
                .header("X-IP", "212.58.244.20")
                //.header("token", sessionToken)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody)) // Use the JSON body
                .build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();

        return responseBody;
    }


    public String placeBet(String marketId, String selectionId) throws IOException, InterruptedException, URISyntaxException {
        Map<String, String> data = new HashMap<>();
        data.put("Market ID", marketId);
        data.put("Session ID", selectionId);

        String requestBody = "{\"jsonrpc\": \"2.0\", \"method\": \"SportsAPING/v1.0/placeOrders\", \"params\": {\"marketId\": \"1.182888386\", \"instructions\": [{\"orderType\": \"LIMIT\", \"selectionId\": \"127991\", \"side\": \"BACK\", \"limitOrder\": {\"size\": \"4\", \"price\": \"7\"}}]}, \"id\": 1}";

        // String requestBody = gson.toJson(requestData);
        //HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://ang.nxt.internal/exchange/betting/json-rpc/v1")) // Correct JSON-RPC endpoint
                .header("Accept", "application/json")
                // .header("Content-Type", "application/x-www-form-urlencoded")
                .header("content-type", "application/json")
                .header("X-Authentication", sessionToken)
                .header("X-Application", "7bc25507bcc2d80e")
                // .header("X-IP", "127.0.0.1")
                .header("X-IP", "127.0.0.1")
                //.header("token", sessionToken)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody)) // Use the JSON body
                .build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();

        return responseBody;
    }


}