
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author igor
 */
public class Session {
    
    final private int idSession = this.hashCode();      // id пользователя
    private boolean isServer = false;                   // признак входящего соединения
    private boolean isClient = false;                   // признак исходящего соединения
        
    private ServerSocket serverSocket;                  // Сокет сервера
    private Socket connectSocket;                       // Сокет соединения
    private Socket clientSocket;                        // Сокет клиента
    
    
    private String sMessage;                            // Сообщение
    private BufferedReader readS;                       // Буфер чтения
    private BufferedWriter writeSb;                     // Буфер записи
    public boolean isExec;                              // Признак соединения
    private String code;                          // Кодировка    
    private int port;                           // Порт подключения
    private JTextArea jt;                         // Поле чата
    public enum Prompt {User, Admin, Server, Error};    // Варианты приглашений// Варианты приглашений// Варианты приглашений// Варианты приглашений// Варианты приглашений// Варианты приглашений// Варианты приглашений// Варианты приглашений
    
    Session(int port, JTextArea jt, JTextField jf, String code) {
        this.port = port;
        this.jt = jt;
        this.code = code;
    }
    
    public int getId() {
        return this.idSession;
    }
    
    public boolean isServer() {
        return this.isServer;
    }
    
    public boolean isClient() {
        return this.isClient;
    }
    
    private String getPrompt(Prompt pt) {
        String res = "";
        switch (pt) {
            case Admin:
                res = "Admin> ";
                break;
            case Server:
                res = "Server> ";
                break;
            case User:
                res = "User> ";
                break;
            case Error:
                res = "Error> ";
                break;
        }
        return res;
    }
    
    
    public void connectServer(int port) throws IOException {
        if (isServer) {
            addMessage(Prompt.Error, "Сервер уже запущен");
        } else {
            serverSocket = new ServerSocket(port);
            addMessage(Prompt.Server, "Сервер запущен");
            addMessage(Prompt.Server, "Ожидание подключения...");
            connectSocket = serverSocket.accept();
            addMessage(Prompt.Server, "Пользователь " 
                    + connectSocket.getRemoteSocketAddress().toString() + " подключен");
            isServer = true;
            readS = new BufferedReader(
                    new InputStreamReader(connectSocket.getInputStream(), code));
            writeSb = new BufferedWriter(
                    new OutputStreamWriter(connectSocket.getOutputStream(), code));
        }
    }
    
    public void connectClient(int port, String address) throws IOException {
        if (isClient) {
            addMessage(Prompt.Error, "Соединение уже установлено");
        } else {
            addMessage(Prompt.Server, "Подключение...");
            clientSocket = new Socket(address, port);    
            addMessage(Prompt.Server, "Пользователь "
                    + clientSocket.getRemoteSocketAddress().toString() + " подключен");
            isClient = true;
            readS = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream(),"UTF-8"));
            writeSb = new BufferedWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
        }
    }
    
    private void disconnectServer() throws IOException {
        isServer = false;
        writeSb.close();
        readS.close();
        connectSocket.close();
        serverSocket.close();
        addMessage(Prompt.Server, "Сервер остановлен");
    }
    
    private void disconnectClient() throws IOException {
        isClient = false;
        writeSb.close();
        readS.close();
        clientSocket.close();
        addMessage(Prompt.Server, "Соединение разорвано");
    }
    
    public void addMessage(Prompt prompt, String message) {
        jt.setText(jt.getText() + getPrompt(prompt) + message 
                + (char)0xD + (char)0xA);
        jt.select(jt.getText().length(), jt.getText().length());
    }
    
    private String readString() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (readS.ready()) {
            sb.append((char)readS.read());
        }
        return sb.toString();
    } 
    
    public void sendMessage(String message) {
        try {
            writeSb.write(message + (char)0xD + (char)0xA);
            writeSb.flush();
        } catch (IOException ex) {
            addMessage(Prompt.Error, ex.getLocalizedMessage());
        }
    }
    
    public void stopServer() throws IOException {   
        isServer = false;
        serverSocket.close();
    }
    
    public void stopClient() throws IOException {   
        isClient = false;
    }
}

