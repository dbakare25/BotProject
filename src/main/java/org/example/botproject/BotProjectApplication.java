package org.example.botproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

@SpringBootApplication
public class BotProjectApplication {

	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
		SpringApplication.run(BotProjectApplication.class, args);
		Scanner scanner = new Scanner(System.in);
		boolean continueBetting = true;

		String baseUrl = "https://identitysso.nxt.com.betfair";
		String applicationKey = "npo67wopV4oKVu5g";
		BettingAPI bettingAPI = new BettingAPI(baseUrl, applicationKey);

		// Login process
		handleLogin(scanner, bettingAPI);

		while (continueBetting) {
			// Starting point for event type selection
			startEventTypeSelection:
			while (true) {
				// Displaying Event Types
				String eventTypesResponse = bettingAPI.listEventTypes();
				JsonObject jsonResponse = JsonParser.parseString(eventTypesResponse).getAsJsonObject();
				JsonArray eventTypes = jsonResponse.getAsJsonArray("result");

				System.out.println("\n----------Available Event Types-----------");
				System.out.println();
				for (JsonElement eventTypeElement : eventTypes) {
					JsonObject eventType = eventTypeElement.getAsJsonObject().getAsJsonObject("eventType");
					String id = eventType.get("id").getAsString();
					String name = eventType.get("name").getAsString();
					System.out.printf("%s - ID: %s\n", name, id);
				}

				// First event type selection - no back option
				System.out.println("\nPlease enter the Event Type ID that you would like to place a bet on: ");
				String eventTypeId = scanner.nextLine().trim();

				// Starting point for event selection
				startEventSelection:
				while (true) {
					String eventResponse = bettingAPI.listEvents(eventTypeId);
					JsonObject eventJsonResponse = JsonParser.parseString(eventResponse).getAsJsonObject();
					JsonArray events = eventJsonResponse.getAsJsonArray("result");

					System.out.println("\n----------Available Events----------\n");
					for (JsonElement eventElement : events) {
						JsonObject eventObject = eventElement.getAsJsonObject().getAsJsonObject("event");
						String id = eventObject.get("id").getAsString();
						String name = eventObject.get("name").getAsString();
						System.out.printf("%s - ID: %s%n ", name, id);
					}

					String eventId = getInputWithBackOption(scanner, "\nPlease enter the Event ID for the game you'd like to place a bet on: ");
					if (eventId == null) continue startEventTypeSelection;

					// Starting point for market selection
					startMarketSelection:
					while (true) {
						// Listing Market Catalogue
						String marketCatalogueResponse = bettingAPI.listMarketCatalogue(eventTypeId, eventId);

						String marketId = getInputWithBackOption(scanner, "\nPlease enter the Market ID you would like to bet on: ");
						if (marketId == null) continue startEventSelection;

						// List Market Book
						String marketBookResponse = bettingAPI.listMarketBook(marketId);
						JsonObject marketBookJsonResponse = JsonParser.parseString(marketBookResponse).getAsJsonObject();
						JsonArray marketbooks = marketBookJsonResponse.getAsJsonArray("result");
						System.out.println("\n---------Market Book-----------\n ");

						if (marketbooks == null || marketbooks.size() == 0) {
							System.out.println("No marketbooks found.");
							System.out.println("\nWould you like to:");
							System.out.println("1) Place a bet on a different event type (e.g., basketball, football)");
							System.out.println("2) Place a bet on the same event type but different event");
							System.out.println("3) Place a bet on the same event but different Market ID");
							System.out.println("c) Close the application");

							String choice = scanner.nextLine().toLowerCase().trim();
							switch (choice) {
								case "1":
									continue startEventTypeSelection;
								case "2":
									continue startEventSelection;
								case "3":
									continue startMarketSelection;
								case "c":
									continueBetting = false;
									System.out.println("Thank you for using BETBOT3000 — Goodbye!");
									scanner.close();
									return;
								default:
									System.out.println("Invalid choice. Returning to event type selection.");
									continue startEventTypeSelection;
							}
						}

						// Display runners and handle bet placement
						for (JsonElement marketbookElement : marketbooks) {
							JsonObject marketBookObject = marketbookElement.getAsJsonObject();
							JsonArray runners = marketBookObject.getAsJsonArray("runners");

							for (JsonElement runnerElement : runners) {
								JsonObject runnerObject = runnerElement.getAsJsonObject();
								String id = runnerObject.get("selectionId").getAsString();
								System.out.printf("ID: %s%n", id);
							}

							// PLACING A BET LOGIC:
							String placeBetChoice = getYesNoInput(scanner, "\nWould you like to place a bet?");

							if (placeBetChoice.equals("y")) {
								handleBetPlacement(scanner, bettingAPI, marketId);

								// List Current Orders
								String showOrdersChoice = getYesNoInput(scanner, "\nWould you like to see your current orders?");

								if (showOrdersChoice.equals("y")) {
									String currentOrders = bettingAPI.listCurrentOrders(marketId);
									JsonObject ordersResponseObj = JsonParser.parseString(currentOrders).getAsJsonObject();
									JsonArray currentOrdersArray = ordersResponseObj.getAsJsonObject("result").getAsJsonArray("currentOrders");

									for (JsonElement orderElement : currentOrdersArray) {
										JsonObject order = orderElement.getAsJsonObject();
										String betId = order.get("betId").getAsString();
										String side = order.get("side").getAsString();
										JsonObject priceSize = order.getAsJsonObject("priceSize");
										double price = priceSize.get("price").getAsDouble();
										double size = priceSize.get("size").getAsDouble();

										System.out.printf("Bet ID: %s, Side: %s, Price: %.2f, Size: %.2f%n",
												betId, side, price, size);
									}
								} else {
									System.out.println("Thank you for placing a bet!");
								}

								// Cancel Bet
								String cancelBetChoice = getYesNoInput(scanner, "\nWould you like to cancel your bet?");

								if (cancelBetChoice.equals("y")) {
									String betId = bettingAPI.getLastBetId();
									if (betId != null) {
										System.out.println("Cancelling bet with ID: " + betId);
										String cancelResponse = bettingAPI.cancelBet(marketId, betId);
										JsonObject responseObj = JsonParser.parseString(cancelResponse).getAsJsonObject();
										JsonObject result = responseObj.getAsJsonObject("result");
										String status = result.get("status").getAsString();

										if ("SUCCESS".equals(status)) {
											System.out.println("Success, you've cancelled your bet.");
										} else {
											System.out.println("Bet cancellation failed.");
										}
									} else {
										System.out.println("No bet ID available to cancel.");
									}
								}

								String anotherBet = getYesNoInput(scanner, "\nWould you like to place another bet?");
								if (anotherBet.equals("n")) {
									continueBetting = false;
									System.out.println("Thank you for using BETBOT3000 — Goodbye!");
									scanner.close();
									return; // Exit the main method entirely
								} else {
									continue startEventTypeSelection;
								}
							}
						}
					}
				}
			}
		}
		scanner.close();
	}

	private static void handleLogin(Scanner scanner, BettingAPI bettingAPI) throws IOException, InterruptedException, URISyntaxException {
		String username, password;
		LoginResponse loginResponse;

		// do/while MUST return a boolean value. Will continue loop until whilst condition is TRUE
		do {
			// Username input
			do {
				System.out.println("Enter your username: ");
				username = scanner.nextLine().trim();
				if (username.isEmpty()) {
					System.out.println("Username cannot be empty.");
				}
			} while (username.isEmpty());

			// Password input
			do {
				System.out.println("Enter your password: ");
				password = scanner.nextLine().trim();
				if (password.isEmpty()) {
					System.out.println("Password cannot be empty.");
				}
			} while (password.isEmpty());

			// Attempt login
			String loginResponseBody = bettingAPI.login(username, password);
			loginResponse = new Gson().fromJson(loginResponseBody, LoginResponse.class);

			// Validate login
			if (loginResponse.getToken() == null) {
				System.out.println("Error: " + loginResponse.getError());
			}
			if ("FAIL".equals(loginResponse.getStatus())) {
				System.err.println("Login Failed: " + loginResponse.getError());
			} else if ("SUCCESS".equals(loginResponse.getStatus())) {
				System.out.println("Login Successful!");
				System.out.println("Welcome " + username + " - you are using currently using BETBOT3000!");
				break;
			}
		} while (true);

	}
	private static String getYesNoInput(Scanner scanner, String prompt) {
		String input;
		do {
			System.out.println(prompt + " (y/n)");
			input = scanner.nextLine().trim().toLowerCase();
			if (!input.equals("y") && !input.equals("n")) {
				System.out.println("Please enter either 'y' for yes or 'n' for no.");
			}
		} while (!input.equals("y") && !input.equals("n"));
		return input;
	}

	private static void handleBetPlacement(Scanner scanner, BettingAPI bettingAPI, String marketId) throws IOException, InterruptedException, URISyntaxException {
		while (true) {
			String selectionId = getInputWithBackOption(scanner, "Enter the Selection ID you want to bet on: ");
			if (selectionId == null) return;

			String side;
			while (true) {
				System.out.println("\nChoose bet type:");
				System.out.println("\nb - Back bet");
				System.out.println("l - Lay bet");
				side = scanner.nextLine().trim().toLowerCase();
				if (side.equals("b") || side.equals("l")) {
					side = side.equals("b") ? "BACK" : "LAY";
					break;
				}
				System.out.println("Invalid input. Please enter 'b' for back or 'l' for lay.");
			}

			String size = getInputWithBackOption(scanner, "\nEnter bet size (as whole numbers): ");
			if (size == null) {
				// Go back to selection ID input
				continue;
			}

			String price = getInputWithBackOption(scanner, "\nEnter bet price (up to 2 d.p): ");
			if (price == null) {
				// Go back to size input
				continue;
			}

			String placedBet = bettingAPI.placeBet(marketId, selectionId, side, size, price);
			JsonObject responseObj = JsonParser.parseString(placedBet).getAsJsonObject();
			JsonObject result = responseObj.getAsJsonObject("result");
			String status = result.get("status").getAsString();
			JsonObject instructionReport = result.getAsJsonArray("instructionReports").get(0).getAsJsonObject();
			String betId = instructionReport.get("betId").getAsString();

			System.out.println("\nBet Status: " + status);
			System.out.println("Bet ID: " + betId);
			return;
		}
	}

	private static String getInputWithBackOption(Scanner scanner, String prompt) {
		while (true) {
			System.out.println(prompt);
			System.out.println("(Enter 'b' to go back or 'c' to continue with your input)");
			String input = scanner.nextLine().trim();

			if (input.equalsIgnoreCase("b")) {
				return null;
			} else if (input.equalsIgnoreCase("c")) {
				System.out.println("Please enter your input:");
				return scanner.nextLine().trim();
			} else {
				System.out.println("Continuing with input: " + input);
				return input;
			}
		}
	}
}