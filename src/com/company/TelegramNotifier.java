package com.company;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TelegramNotifier {

    public TelegramNotifier(String token, String chat_id, String message){
        System.out.println("Telegram notifier created.");


        String full_url = "https://api.telegram.org/bot"+ token +"/sendMessage?chat_id="+ chat_id +"&text="+ message;


        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_2)
                .build();

        /*
        UriBuilder builder = UriBuilder
                .fromUri("https://api.telegram.org")
                .path("/{token}/sendMessage")
                .queryParam("chat_id", CHAT_ID)
                .queryParam("text", message);
         */

        try {
            URI oURL = new URI(full_url);

            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(oURL)
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = client
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if(response.statusCode() != 200){
                System.out.println("Error from telegram:");
                System.out.println(response.body());

                System.out.println("");
                System.out.println("MESSAGE: "+ message);
                System.out.println("TOKEN: "+ token);
                System.out.println("CHAT_ID: "+ chat_id);
                System.out.println(full_url);
            }


        } catch (URISyntaxException e) {
            System.out.println("Error building Telegram message string");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error sending Telegram message");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Interrupt sending Telegram message");
            e.printStackTrace();
        }

    }

}