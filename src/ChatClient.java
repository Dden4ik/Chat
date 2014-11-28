
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import javax.swing.JTextArea;
import javax.swing.JTextField;

//192.168.64.198
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Igor
 */
public class ChatClient implements Runnable {
    private String fromS;                               // Буфер чтения
    private Socket sConn;                               // Сокет
    private BufferedReader readS;                       // Буфер чтения
    private BufferedWriter writeSb;                     // Буфер записи
    public boolean isExec;                              // Признак соединения
    private final int socket;                           // Порт подключения
    private final String address;                       // Адрес подключения
    private final JTextArea jt;                         // Поле чата
    private final JTextField jf;                        // Поле вовда
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
    
    ChatClient(int socket, String address, JTextArea jt, JTextField jf) {
        super();
        fromS = null;
        sConn = null;
        readS = null;
        writeSb = null;
        isExec = false;
        this.socket = socket;
        this.address = address;
        this.jt = jt;
        this.jf = jf;
    }
    
    private void connect() throws IOException {
        addMessage(Prompt.Server, "Подключение...");
        sConn = new Socket(address, socket);    
        addMessage(Prompt.Server, "Пользователь "
                + sConn.getRemoteSocketAddress().toString() + " подключен");
        isExec = true;
        readS = new BufferedReader(
                new InputStreamReader(sConn.getInputStream(),"UTF-8"));
        writeSb = new BufferedWriter(
                new OutputStreamWriter(sConn.getOutputStream(), "UTF-8"));
    }
    
    private void disconnect() throws IOException {
        isExec = false;
        writeSb.close();
        readS.close();
        sConn.close();
        addMessage(Prompt.Server, "Соединение разорвано");
    }
    
    @Override
    public void run() {   
        try {
            connect();
            while (isExec) {
                if (readS.ready()) {
                    fromS = readString();
                    addMessage(Prompt.Admin, fromS.trim());
                }
            }
            disconnect();
        } catch (IOException ex) {
            addMessage(Prompt.Error, ex.getLocalizedMessage());
        }
    }
    
    private String readString() throws IOException {
        StringBuilder sb = new StringBuilder();
        char buf;
        while (readS.ready()) {
            buf = (char)readS.read();
            sb.append(buf);
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

    }
}
