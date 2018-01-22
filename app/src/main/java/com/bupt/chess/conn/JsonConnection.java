package com.bupt.chess.conn;

import android.content.Context;

import com.bupt.chess.R;
import com.bupt.chess.msg.Message;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.net.Socket;


/**
 * Created by xusong on 2017/11/26.
 * Email:xusong@bupt.edu.cn
 * Email:xusongnice@gmail.com
 */
public class JsonConnection {
    private Socket socket;

    private OutputStreamWriter writer;
    private InputStreamReader reader;
    JsonReader jsonReader;

    private JsonParser parser = new JsonParser();

    public static JsonConnection createConnection(Socket socket) {
        try {
            return new JsonConnection(socket);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JsonConnection createDemoConn(Context context) throws IOException {
        JsonConnection connection = new JsonConnection();
        connection.writer = new FileWriter(new File("output"));
        connection.reader = new InputStreamReader(context.getResources().openRawResource(R.raw.input));
        connection.jsonReader = new JsonReader(connection.reader);
        connection.jsonReader.setLenient(true);
        return connection;
    }

    private JsonConnection(Socket socket) throws IOException {
        this.socket = socket;
        writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
        reader = new InputStreamReader(socket.getInputStream(), "UTF-8");
        jsonReader = new JsonReader(reader);
        jsonReader.setLenient(true);
    }

    private JsonConnection() {

        jsonReader = new JsonReader(reader);
        jsonReader.setLenient(true);
    }


    public JsonElement readJson() throws JsonIOException {
        for (; ; ) {
            JsonElement element = parser.parse(jsonReader);
            if (element != null)
                return element;
        }
    }

    public Message readMessage() throws JsonIOException {
        JsonElement element = readJson();
        return Message.getActualMessage(element);
    }

    // TODO: 2018/1/2 写入正确的数据
    public void writeJson(String msg) throws IOException {
        writer.write(msg);
        writer.flush();
    }

    public void close() {
        try {
            reader.close();
            writer.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
