package network;

import dto.ApplicationDto;
import dto.CompanyDto;
import entity.Application;
import service.ApplicationService;
import service.CompanyService;
import service.impl.ApplicationServiceImpl;
import service.impl.CompanyServiceImpl;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) {

        ExecutorService pool = Executors.newFixedThreadPool(10);
        try (ServerSocket serverSocket = new ServerSocket(9090);) {
            System.out.println("Server is ready...");

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                pool.submit(clientHandler);
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private CompanyService companyService;
    private ApplicationService applicationService;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        companyService = new CompanyServiceImpl();
        applicationService = new ApplicationServiceImpl();
    }

    @Override
    public void run() {

        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        ) {

            while (true) {
                Request request = (Request) in.readObject();
                CommandType commandType = request.getCommandType();
                Response response = null;
                switch (commandType) {
                    case COMPANY_FIND_BY_ID -> {
                        String companyId = (String) request.getData();
                        CompanyDto companyDto = companyService.findById(companyId);
                        response = Response.builder()
                                .success(true)
                                .data(companyDto)
                                .message(companyDto != null ? "Found" : "Not found")
                                .build();
                    }
                    case COMPANY_LOAD_ALL -> {
                        List<CompanyDto> companyDtos = companyService.loadAll();
                        response = Response.builder()
                                .success(true)
                                .data(companyDtos)
                                .message(companyDtos.size() + " companies found")
                                .build();
                    }
                    case APPLICATION_FIND_BY_ID -> {
                        Application.ApplicationId applicationId = (Application.ApplicationId) request.getData();
                        ApplicationDto applicationDto = applicationService.findById(applicationId);
                        response = Response.builder()
                                .success(true)
                                .data(applicationDto)
                                .message(applicationDto != null ? "Found" : "Not found")
                                .build();
                    }
                    case APPLICATION_FIND_BY_SKILL -> {
                        String skill = (String) request.getData();
                        List<ApplicationDto> applicationDtos = applicationService.findBySkillInOpenJobs(skill);
                        response = Response.builder()
                                .success(true)
                                .data(applicationDtos)
                                .message(applicationDtos.size() + " applications found")
                                .build();
                    }
                }

                out.writeObject(response);
                out.flush();
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
}
