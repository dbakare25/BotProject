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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;


public class BettingAPI {
    private final HttpClient client;
    private final Gson gson;
    private final String baseUrl;
    private final String applicationKey;
    private String sessionToken;
    private String lastEventTypeId;
    private String lastEventId;
    private String lastMarketId;
    private String lastBetId;

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

        // LOGIN TESTS
        if (loginResponse.getToken() == null) {
            System.out.println("Error: " + loginResponse.getError());
        }
        if ("FAIL".equals(loginResponse.getStatus())) {
            System.err.println("Login Failed: " + loginResponse.getError());
        }
        if ("SUCCESS".equals(loginResponse.getStatus())) {
            System.out.println("Login Successful!");
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

    public String listEvents(String eventTypeId) throws IOException, InterruptedException, URISyntaxException {
        this.lastEventTypeId = eventTypeId;
        String requestBody = String.format("{\"jsonrpc\": \"2.0\", \"method\": \"SportsAPING/v1.0/listEvents\", \"params\": {\"filter\":{\"eventTypeIds\":[\"%s\"], \"textQuery\":\"\"}}, \"id\": 1}", eventTypeId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://ang.nxt.internal/exchange/betting/json-rpc/v1"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-Authentication", sessionToken)
                .header("X-Application", "npo67wopV4oKVu5g")
                .header("X-IP", "127.0.0.1")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody)) //
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();

        return responseBody;
    }

    public String listMarketCatalogue(String eventTypeId, String eventId) throws IOException, InterruptedException, URISyntaxException {
        this.lastEventTypeId = eventTypeId;
        this.lastEventId = eventId;
        String requestBody = String.format("{\"jsonrpc\": \"2.0\", \"method\": \"SportsAPING/v1.0/listMarketCatalogue\", \"params\": {\"filter\": {\"eventTypeIds\": [\"%s\"], \"eventIds\": [\"%s\"]}, \"maxResults\": 100}, \"id\": 1}", eventTypeId, eventId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://ang.nxt.internal/exchange/betting/json-rpc/v1"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("X-Authentication", sessionToken)
                .header("X-Application", applicationKey)
                .header("X-IP", "127.0.0.1")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Extract market ID from response
        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        if (jsonResponse.has("result") && jsonResponse.getAsJsonArray("result").size() > 0) {
            JsonArray markets = jsonResponse.getAsJsonArray("result");

            System.out.println("\n---------Available Markets----------\n");
            for (JsonElement marketElement : markets) {
                JsonObject market = marketElement.getAsJsonObject();
                String marketId = market.get("marketId").getAsString();
                String marketName = market.get("marketName").getAsString();
                System.out.printf("%s - Market ID: %s%n", marketName, marketId);
            }
        }

        return response.body();
    }

    public String listMarketBook(String marketId) throws IOException, InterruptedException, URISyntaxException {
        //this.lastMarketId = marketId;
        String requestBody = String.format("{ \"jsonrpc\": \"2.0\", \"method\": \"SportsAPING/v1.0/listMarketBook\", \"params\" :{\"marketIds\":[\"%s\"]}, \"id\": 1}", marketId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://ang.nxt.internal/exchange/betting/json-rpc/v1"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("X-Authentication", sessionToken)
                .header("X-Application", applicationKey)
                .header("X-IP", "127.0.0.1")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getLastMarketId() {
        return lastMarketId;
    }

    public String getLastEventId() {
        return lastEventId;
    }

    public String getLastBetId() {return lastBetId; }


    public String placeBet(String marketId, String selectionId, String side, String size, String price) throws IOException, InterruptedException, URISyntaxException {
        this.lastMarketId = marketId;
        String requestBody = String.format("{ \"jsonrpc\": \"2.0\", \"method\": \"SportsAPING/v1.0/placeOrders\", \"params\" :{\"marketId\":\"%s\",\"instructions\":[{\"orderType\":\"LIMIT\",\"selectionId\":\"%s\",\"side\":\"%s\",\"limitOrder\":{\"size\":\"%s\",\"price\":\"%s\"}}]}, \"id\": 1}", marketId, selectionId, side,size, price);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://ang.nxt.internal/exchange/betting/json-rpc/v1"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("X-Authentication", sessionToken)
                .header("X-Application", applicationKey)
                .header("X-IP", "127.0.0.1")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Extract bet ID from response
        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        if (jsonResponse.has("result") && jsonResponse.getAsJsonObject("result").has("instructionReports")) {
            JsonObject firstReport = jsonResponse.getAsJsonObject("result").getAsJsonArray("instructionReports").get(0).getAsJsonObject();
            this.lastBetId = firstReport.get("betId").getAsString();
        }

        return response.body();
    }


    public String listCurrentOrders(String marketId) throws IOException, InterruptedException, URISyntaxException {
        String requestBody = String.format("{\"jsonrpc\": \"2.0\", \"method\": \"SportsAPING/v1.0/listCurrentOrders\", \"params\": {\"marketIds\":[\"%s\"]}, \"id\": 1}", marketId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://ang.nxt.internal/exchange/betting/json-rpc/v1"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("X-Authentication", sessionToken)
                .header("X-Application", applicationKey)
                .header("X-IP", "212.58.244.20")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String cancelBet(String marketId, String betId) throws IOException, InterruptedException, URISyntaxException {
        String requestBody = String.format("{ \"jsonrpc\": \"2.0\", \"method\": \"SportsAPING/v1.0/cancelOrders\", \"params\" :{\"marketId\":\"%s\",\"instructions\":[{\"betId\":\"%s\"}]}, \"id\": 1}", marketId, betId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://ang.nxt.internal/exchange/betting/json-rpc/v1"))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("X-Authentication", sessionToken)
                .header("X-Application", applicationKey)
                .header("X-IP", "212.58.244.20")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        return response.body();

    }
}







