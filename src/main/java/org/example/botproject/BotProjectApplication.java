package org.example.botproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.SortedMap;
import com.google.gson.Gson;



@SpringBootApplication
public class BotProjectApplication {

	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
		// Run the Spring Boot application
		SpringApplication.run(BotProjectApplication.class, args);
		Scanner scanner = new Scanner(System.in);

		System.out.println("Enter your username: ");
		String username = scanner.nextLine();

		System.out.println("Enter your password: ");
		String password = scanner.nextLine();

		String baseUrl = "https://identitysso.nxt.com.betfair";
		String applicationKey = "npo67wopV4oKVu5g";
		BettingAPI bettingAPI = new BettingAPI(baseUrl, applicationKey);
		String loginResponse = bettingAPI.login(username, password);
		System.out.println(loginResponse);

		System.out.println("Hello " + username);
		// Displaying Event Types:
		String eventTypesResponse = bettingAPI.listEventTypes();
		System.out.println("Event Types: " + eventTypesResponse);

		// Displaying placed bet:
		System.out.println("Enter the Event Type ID from the list please: ");
		String eventTypeId = scanner.nextLine();

		System.out.println("Please enter the Market ID: ");
		String marketId = scanner.nextLine();

		System.out.println("Please enter the Selection ID: ");
		String selectionId = scanner.nextLine();

		String placedBet = bettingAPI.placeBet(marketId, selectionId);
		System.out.println("Place bet: " + placedBet);


		scanner.close();




	}
}



/*
		TESTS:

			// Check if the token is available
			if (loginResponse.getToken() != null) {
				System.out.println("Token: " + loginResponse.getToken());
			} else {
				System.out.println("Error: " + loginResponse.getError());
			}
		} else {
			System.out.println("Failed to login. Status code: " + response.statusCode());

			// Optionally, you can also parse the body if it contains an error message
			Gson gson = new Gson();
			LoginResponse loginResponse = gson.fromJson(response.body(), LoginResponse.class);
			System.out.println("Error: " + loginResponse.getError());

*/