package File.Node.sdk;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Example {

    public static void main(String[] args) {
        try {
            // -----------------------------
            // Use credentials from the cube
            // -----------------------------
            String username = "Anton_00";
            String apiKey = "007591a1-a9ad-43a3-86af-ac145b849f37";

            // THIS MUST BE THE RAW SECRET, NOT THE HASH!
            String rawSecret = "3544611f-9ac8-457c-84d1-96a2cc7a7f2b";

            // Initialize client with all required credentials
            CloudClient client = new CloudClient(username, apiKey, rawSecret);

            // -----------------------------
            // Upload multiple files to cubeId 2
            // -----------------------------
            List<String> uploaded = client.upload(2L,
                    new File("/Users/antonabitharshan/Desktop/x1.png"),
                    new File("/Users/antonabitharshan/Desktop/x1.png")
            );

            System.out.println("Uploaded files:");
            uploaded.forEach(System.out::println);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
