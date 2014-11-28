
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
 * @author Igor
 */
public class ChatServer implements Runnable {
    private String fromS;                               // Буфер чтения
    private ServerSocket sSocket;                       // Сокет сервера
    private Socket sConn;                               // Сокет
    private BufferedReader readS;                       // Буфер чтения
    private BufferedWriter writeSb;                     // Буфер записи
    public boolean isExec;                              // Признак соединения
    private final String code;                          // Кодировка    
    private final int socket;                           // Порт подключения
    private final JTextArea jt;                         // Поле чата
    public enum Prompt {User, Admin, Server, Error};    // Варианты приглашений
    
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
    
    ChatServer(int socket, JTextArea jt, JTextField jf, String code) {
        super();
        fromS = null;
        sSocket = null;
        sConn = null;
        readS = null;
        writeSb = null;
        isExec = false;
        this.socket = socket;
        this.jt = jt;
        this.code = code;
    }
    
    private void connect() throws IOException {
        sSocket = new ServerSocket(socket);
        addMessage(Prompt.Server, "Сервер запущен");
        addMessage(Prompt.Server, "Ожидание подключения...");
        sConn = sSocket.accept();
        addMessage(Prompt.Server, "Пользователь " 
                + sConn.getRemoteSocketAddress().toString() + " подключен");
        isExec = true;
        readS = new BufferedReader(
                new InputStreamReader(sConn.getInputStream(), code));
        writeSb = new BufferedWriter(
                new OutputStreamWriter(sConn.getOutputStream(), code));
    }
    
    private void disconnect() throws IOException {
        isExec = false;
        writeSb.close();
        readS.close();
        sConn.close();
        sSocket.close();
        addMessage(Prompt.Server, "Сервер остановлен");
    }
    
    @Override
    public void run() {   
        try {
            connect();
            while (isExec) {
                if (readS.ready()) {
                    if (code.equals("UTF-8")) {
                        fromS = readString();
                    } else {
                        fromS = readS.readLine();
                        
                    }
                    addMessage(Prompt.User, fromS.trim());
                }
            }
            disconnect();
        } catch (IOException ex) {
            addMessage(Prompt.Error, ex.getLocalizedMessage());
        }
    }
    
    private String readString() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (readS.ready()) {
            sb.append((char)readS.read());
        }
        return sb.toString();
    } 
    
    public void addMessage(Prompt prompt, String message) {
        jt.setText(jt.getText() + getPrompt(prompt) + message 
                + (char)0xD + (char)0xA);
        jt.select(jt.getText().length(), jt.getText().length());
    }
    
    public void sendMessage(String message) {
        try {
            writeSb.write(message + (char)0xD + (char)0xA);
            writeSb.flush();
        } catch (IOException ex) {
            addMessage(Prompt.Error, ex.getLocalizedMessage());
        }
    }
    
    public void stop() throws IOException {   
        isExec = false;
        sSocket.close();
    }
}
