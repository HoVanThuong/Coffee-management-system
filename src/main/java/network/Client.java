package network;

import entity.Application;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        try(Socket socket = new Socket("H31M50", 9090);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in);
        ){

            int choice = 0;

            while (true){
                System.out.println("==Main menu==");
                System.out.println("1. Find by company's id");
                System.out.println("2. List companies");
                System.out.println("3. Find by application's id");
                System.out.println("4. Find by job's skill");
                choice = scanner.nextInt();
                Request request = null;
                switch (choice){
                    case 1 -> {
                        CommandType commandType = CommandType.COMPANY_FIND_BY_ID;
                        String companyId = "CP2";
                        request = Request.builder().commandType(commandType)
                                .data(companyId)
                                .build();
                    }
                    case 2 -> {
                        CommandType commandType = CommandType.COMPANY_LOAD_ALL;
                        request = Request.builder().commandType(commandType)
                                .build();
                    }
                    case 3 -> {
                        CommandType commandType = CommandType.APPLICATION_FIND_BY_ID;
                        Application.ApplicationId applicationId = new Application.ApplicationId("C2", "J2");
                        request = Request.builder().commandType(commandType)
                                .data(applicationId)
                                .build();
                    }
                    case 4 -> {
                        CommandType commandType = CommandType.APPLICATION_FIND_BY_SKILL;
                        String skill = "Java";
                        request = Request.builder().commandType(commandType)
                                .data(skill)
                                .build();
                    }
                }
                out.writeObject(request);
                out.flush();

                Response response = (Response) in.readObject();
                System.out.println(response);
            }


        }catch (Exception ex){
            throw  new RuntimeException(ex);
        }
    }
}
