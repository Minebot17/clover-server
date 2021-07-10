package ru.minebot.cloverserver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CloverBridge {

    private static final String pathToCloverStart = "C:\\Users\\serpi\\Desktop\\repos\\Clover-Edition\\play.bat";
    private static final String pathToCloverDirectory = "C:\\Users\\serpi\\Desktop\\repos\\Clover-Edition";

    private static Thread lastInitThread;

    private final Object inputSyncObject = new Object();
    private final Object nextInputSyncObject = new Object();

    private boolean autoStart = false;

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private int connectTries = 21;
    private boolean isAwaiting;
    private boolean inGame;
    private String lastInput;
    private String lastMessage;
    private List<String> savedGames;

    public void initialize() {
        lastInitThread = Thread.currentThread();
        while (true) {
            try {
                connectToClover();
                savedGames = new ArrayList<>();

                while (true) {
                    String[] received = reader.readLine().split(";");
                    writer.write(handleMessage(received[0], Arrays.stream(received).skip(1).collect(Collectors.toList()).toArray(new String[1])));
                    writer.flush();
                }
            }
            catch (IOException e) {
                try {
                    Thread.sleep(5000);

                    if (autoStart) {
                        if (connectTries <= 20)
                            connectTries++;
                        else {
                            launch();
                            connectTries = 0;
                        }
                    }
                }
                catch (InterruptedException ee) {
                    ee.printStackTrace();
                    connectTries = 21;
                    isAwaiting = false;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                connectTries = 21;
                isAwaiting = false;
            }
        }
    }

    public void launch() {
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "start " + pathToCloverStart);
            builder.directory(new File(pathToCloverDirectory));
            builder.start();
            isAwaiting = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        ProcessHandle.allProcesses().forEach(ph -> {
            if (ph.info().command().map(Object::toString).orElse("-").equals("C:\\Users\\serpi\\Desktop\\repos\\Clover-Edition\\venv\\python.exe")) {
                try {
                    Runtime.getRuntime().exec("taskkill /F /PID " + ph.pid());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        lastInitThread.interrupt();
        new Thread(this::initialize).start();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        launch();
        savedGames.clear();
    }

    // Only for call from outer threads
    public void startNewCustomGame(String context, String prompt, String saveName) {
        isAwaiting = true;
        enterInput("1");
        waitForNextInputWait();
        enterInput(context);
        waitForNextInputWait();
        enterInput(prompt);
        waitForNextInputWait();
        enterInput(saveName);
        waitForNextInputWait();
        isAwaiting = false;
        inGame = true;
    }

    // Only for call from outer threads
    public void startNewGame(int folderIndex, int subFolderIndex, String saveName) {
        isAwaiting = true;
        enterInput("0");
        waitForNextInputWait();
        enterInput(folderIndex + 1 + "");
        waitForNextInputWait();
        enterInput(subFolderIndex + 1 + "");
        waitForNextInputWait();
        enterInput(saveName);
        waitForNextInputWait();
        isAwaiting = false;
        inGame = true;
    }

    // Only for call from outer threads
    public void loadGame(String saveName) {
        isAwaiting = true;
        enterInput("2");
        waitForNextInputWait();
        enterInput(savedGames.indexOf(saveName) + 1 + "");
        waitForNextInputWait();
        isAwaiting = false;
        inGame = true;
    }

    // Only for call from outer threads
    public void sendResponse(String message) {
        isAwaiting = true;
        enterInput(message);
        waitForNextInputWait();
        isAwaiting = false;
    }

    // Only for call from outer threads
    public void waitForNextInputWait(){
        synchronized (nextInputSyncObject) {
            try {
                nextInputSyncObject.wait();
                Thread.sleep(10);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Only for call from outer threads
    public void enterInput(String value) {
        synchronized (inputSyncObject){
            lastInput = value;
            inputSyncObject.notify();
        }
    }

    public boolean inGame() {
        return inGame;
    }

    public boolean isConnected() {
        return socket.isConnected() && !socket.isClosed();
    }

    public boolean isAwaiting() {
        return isAwaiting;
    }

    public List<String> getSavedGames() {
        return savedGames;
    }

    public List<Map<String, Object>> getPresets() {
        return Presets.maps;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    private String handleMessage(String message, String[] args) throws Exception {
        switch (message){
            case "menu":
                inGame = false;

                if (savedGames.size() == 0)
                    collectMenuData();

                return waitForInput();
            case "save_select":
                savedGames.clear();
                for (int i = 0; i < args.length; i++){
                    int closeBracketIndex = args[i].indexOf(')');

                    if (i != 0 && i != args.length - 1)
                        savedGames.add(args[i].substring(closeBracketIndex + 2));
                }
                return waitForInput();
            case "preset_select":
                return waitForInput();
            case "action":
                lastMessage = args[0];
                return waitForInput();
            default:
                throw new Exception("Undefined message " + message);
        }
    }

    private void collectMenuData(){
        new Thread(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            enterInput("2");
            waitForNextInputWait();
            enterInput(savedGames.size() + 1 + "");
            waitForNextInputWait();
            isAwaiting = false;
        }).start();
    }

    // Only for call from inner thread
    private String waitForInput(){
        synchronized (nextInputSyncObject){
            nextInputSyncObject.notify();
        }
        synchronized (inputSyncObject) {
            try {
                inputSyncObject.wait();
                return lastInput;
            }
            catch (InterruptedException e){
                e.printStackTrace();
                return null;
            }
        }
    }

    private void connectToClover() throws IOException {
        if (socket != null) {
            socket.close();
            socket = null;
        }

        socket = new Socket ("127.0.0.1",65432);
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
    }
}
