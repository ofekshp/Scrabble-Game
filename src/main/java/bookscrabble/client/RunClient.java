package bookscrabble.client;

import bookscrabble.client.model.ClientModel;
import bookscrabble.client.model.MyHostServer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;


public class RunClient{
    static ClientModel myClient;
    static Scanner scanner = MyLogger.getScanner();
    public static boolean exit = false;

    public RunClient() // always running
    {
        do {
            String name = getPlayerName();
            boolean isHost = chooseIfHost();

            if(isHost)
            {
                do
                {
                    hostGame(name);
                } while (!checkConnectionToHost());
                hostStartMenu();
            }
            else
            {
                do {
                    joinGame(name);
                } while (!checkConnectionToHost());
            }
        } while (!checkConnectionToHost());
    }
    
    private String getPlayerName()
    {
        System.out.println("Welcome to BookScrabble!");
        System.out.println("Please enter your name: ");
        String name;
        do {
            name = scanner.nextLine();
        } while (name.length() == 0);
        System.out.println("Welcome " + name + "!");
        return name;
    }

    private boolean chooseIfHost()
    {
        System.out.println("Are you joining a game or creating a new one? Choose 1 or 2.");
        System.out.println("1. Join a game");
        System.out.println("2. Create a new game");
        boolean invalidInput = false;
        do {
            invalidInput = false;
            String input = scanner.nextLine();
            if (input.equals("1")) { //Guest client
                return false;
            } else if (input.equals("2")) { //Host client
                return true;
            } else {
                System.out.println("Invalid input. Please try again.");
                invalidInput = true;
            }
        } while (invalidInput);
        return false;
    }

    private void hostGame(String name) //First we create MyHostServer, and then we create the client itself
    {
        boolean invalidInput;
        int hostPort, bs_port;
        String bs_IP;
        do {
            invalidInput = false;
            System.out.println("Enter the port number you want to host the game on: ");
            String input = scanner.nextLine();
            input = fixUserInput(input);
            hostPort = Integer.parseInt(input);
            if(hostPort <= 0 || hostPort > 65535)
            {
                System.out.println("Invalid port number. Please try again.");
                invalidInput = true;
            }
        } while (invalidInput);

        do {
            invalidInput = false;
            System.out.println("You need to connect to the BookScrabble server.");
            System.out.println("Please enter the BookScrabble Server IP: ");
            bs_IP = scanner.nextLine();
            if(bs_IP.length() == 0)
            {
                System.out.println("Invalid IP. Please try again.");
                invalidInput = true;
            }
        } while (invalidInput);

        do {
            invalidInput = false;
            System.out.println("Please enter the server's port: ");
            String input = scanner.nextLine();
            input = fixUserInput(input);
            bs_port = Integer.parseInt(input);
            if(bs_port <= 0 || bs_port > 65535)
            {
                System.out.println("Invalid port number. Please try again.");
                invalidInput = true;
            }
        } while (invalidInput);

        myClient = new ClientModel();
        MyHostServer.getHostServer().start(hostPort, bs_port, bs_IP);
        //Now we create the host itself
        myClient.createClient(true, "localhost", hostPort, name);
    }

    private void joinGame(String name)
    {
        String host_ip;
        int host_port;
        boolean invalidInput;

        do {
            invalidInput = false;
            System.out.println("Enter the host's IP address:");
            host_ip = scanner.nextLine();
            if(host_ip.length() == 0)
            {
                System.out.println("Invalid IP. Please try again.");
                invalidInput = true;
            }
        } while (invalidInput);

        do {
            invalidInput = false;
            System.out.println("Enter the host's port number:");
            String input = scanner.nextLine();
            input = fixUserInput(input);
            host_port = Integer.parseInt(input);
            if(host_port <= 0 || host_port > 65535)
            {
                System.out.println("Invalid port number. Please try again.");
                invalidInput = true;
            }
        } while (invalidInput);

        System.out.println("Connecting to host...");
        myClient = new ClientModel();
        myClient.createClient(false, host_ip, host_port , name); //Guest client
    }

    private boolean checkConnectionToHost() //Returns true if connected to host
    {
        if(!myClient.isConnectedToHost)
        {
            System.out.println("Could not connect to host.");
            System.out.println("Trying again...");
            myClient = null;
            return false;
        }
        return true;
    }

    private void hostStartMenu()
    {
        InetAddress ipAddress = null;
        try {
            ipAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        System.out.println("You have created a game lobby! Waiting for other players to join...");
        System.out.println("Your IP is: " + ipAddress.getHostAddress() + " you can give this to your friends to join your game.");
        System.out.println("Type '!start' to begin the game, '!exit' to close or '!who' to see who's connected.");
        System.out.println("Remember, a game is played with 2-4 players.");

        do {
            String input = scanner.nextLine();
            switch (input) {
                case "!start":
                    exit = MyHostServer.getHostServer().startGame();
                    break;
                case "!exit":
                    System.out.println("Exiting...");
                    myClient.close();
                    MyLogger.close();
                    exit = true;
                    break;
                case "!who":
                    String[] players = MyHostServer.getHostServer().getConnectedClients();
                    for (String player : players)
                        MyLogger.println(player + " is connected.");
                    break;
                default:
                    System.out.println("Invalid input. Please try again.");
                    break;
            }
        } while (!exit); 
    }

    public static void disconnectedFromHost()
    {
        if(myClient != null)
            myClient.close();
        myClient = null;
        scanner = MyLogger.getScanner();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}
        MyLogger.println("Disconnected from game lobby.");
        MyLogger.println("Choose 1 to play again or 2 to exit the game.");

        do {
            exit = false;
            String input = scanner.nextLine();
            if (input.equals("1")) {
                MyLogger.println("Restarting game...");
                exit = true;
            } else if (input.equals("2")) {
                MyLogger.println("Exiting...");
                MyLogger.close();
                return; //Exit the game
            } else
                MyLogger.println("Invalid input. Please try again."); 
        } while (!exit);
        new RunClient(); //Restart the game
    }

    private String fixUserInput(String input)
    {
        input = input.toLowerCase();
        input = input.replaceAll("[^0-9]", "");
        if(input.length() == 0)
            input = "0";
        return input;
    }
}