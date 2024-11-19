//package org.example.Model;
//
//import com.azure.core.credential.TokenCredential;
//import com.azure.identity.*;
//import com.azure.storage.blob.*;
//import com.azure.storage.blob.models.BlobContainerItem;
//
////255134902477-vm2vgrohddg3l52kl3etkfsc856qcpfi.apps.googleusercontent.com
//
//import java.util.Scanner;
//
//public class AzureConnection {
//
//    private static String clientId = "733b4657-8943-4734-b057-83f1d39d84a9";
//    private static String tenantId = "030bd8c2-ab13-4f4a-8d17-8d594cabb3b8";
//
//    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//
//        System.out.println("Choose authentication method:");
//        System.out.println("1. Username and Password");
//        System.out.println("2. Interactive Browser Login");
//        System.out.print("Enter your choice (1/2): ");
//        int choice = scanner.nextInt();
//        scanner.nextLine(); // Consume the newline character
//
//        TokenCredential credential = null;
//
//        try {
//            if (choice == 1) {
//                // Username and Password Authentication
//                System.out.print("Enter your Azure username: ");
//                String username = scanner.nextLine();
//                System.out.print("Enter your Azure password: ");
//                String password = scanner.nextLine();
//
//                credential = new UsernamePasswordCredentialBuilder()
//                        .clientId(clientId)
//                        .tenantId(tenantId)
//                        .username(username)
//                        .password(password)
//                        .build();
//            } else if (choice == 2) {
//                // Interactive Browser Authentication
//                credential = new InteractiveBrowserCredentialBuilder()
//                        .clientId(clientId)
//                        .redirectUrl("http://localhost:11142")
//                        .build();
//            } else {
//                System.out.println("Invalid choice.");
//                return;
//            }
//
//            // Access Azure Blob Storage
//            String accountUrl = "https://atlas231.blob.core.windows.net";
//            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
//                    .endpoint(accountUrl)
//                    .credential(credential)
//                    .buildClient();
//
//            // List Blob Containers
//            System.out.println("Listing blob containers:");
//            for (BlobContainerItem container : blobServiceClient.listBlobContainers()) {
//                System.out.println(" - " + container.getName());
//            }
//        } catch (Exception e) {
//            System.err.println("An error occurred: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}
