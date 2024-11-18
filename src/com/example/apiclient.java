package com.example;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

public class apiclient {
    public static void main(String[] args) throws Exception {
        // Creating a HttpClient instance:
        HttpClient client = HttpClient.newHttpClient();

        //Building a request to an API Login endpoint:
        //HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://identitysso.nxt.com.betfair/api/login"))
        HttpResponse<String> response = client.send(HttpRequest.newBuilder())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("X-IP", "127.0.0.1")
                .header("X-Application", "npo67wopV4oKVu5g")
                .header("Cookie", "vid=3cedfda2-8188-11ef-b4f4-fa163e911a03; wsid=3cedfda1-8188-11ef-b4f4-fa163e911a03\n")
                .build();

        // Send the request and receive a response:
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // Print the response:
        System.out.println(response.body());


    }
}
