package nbattle;

import com.sun.istack.internal.Nullable;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static nbattle.GameLogic.gameOver;
import static nbattle.Main.controller;
import static nbattle.MainController.alertShow;
import static nbattle.MainController.lastScene;


public class JsonUtils {
    private static final String HASH = "BN45CJLI149XCdp434xhzzzLKxCKnBP1432lkU";

    @Nullable
    public static String parseUrl(String sUrl, String sParams) {
        // Создаем URL из строки.
        URL url = JsonUtils.createUrl(sUrl);
        if (url == null)
            return "";

        StringBuilder stringBuilder = new StringBuilder();

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            String params = "hash=" + HASH + sParams;

            // Send post request
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(params);
            wr.flush();
            wr.close();
            // done

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            // Построчно считываем результат в объект StringBuilder.
            while ((inputLine = in.readLine()) != null) {
                stringBuilder.append(inputLine);
                System.out.println(inputLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }


    // Парсим некоторые данные об играх.
    @Nullable
    public static JSONArray parseListJson(String resultJson) {
        try {
            // Конвертируем строку с Json в JSONObject для дальнейшего его парсинга.
            JSONObject gamesJsonObject = (JSONObject) JSONValue.parseWithException(resultJson);

            // Получаем состояние текущих игр.
            System.out.println("Состояние сервера: " + gamesJsonObject.get("msg"));

            // Получаем массив элементов для поля.
            /* {"msg":"ok","games":[{"id":"1","host":"vasya"},{"id":"2","host":"loh"},{"id":"3","host":"123"},{"id":"4","host":"1"}]} */
            if (gamesJsonObject.get("info") == null) {
                return (JSONArray) gamesJsonObject.get("games");
            }

            System.out.println("Сообщение: " + gamesJsonObject.get("info"));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Парсим некоторые данные создании игры.
    @Nullable
    public static String parseCreateJson(String resultJson) {
        try {
            // Конвертируем строку с Json в JSONObject для дальнейшего его парсинга.
            JSONObject gamesJsonObject = (JSONObject) JSONValue.parseWithException(resultJson);

            // Получаем состояние текущих игр.
            System.out.println("Состояние сервера: " + gamesJsonObject.get("msg"));

            if (gamesJsonObject.get("info") == null) {
                System.out.println("ID игры: " + gamesJsonObject.get("id"));
                return gamesJsonObject.get("id").toString();
            }

            System.out.println("Сообщение: " + gamesJsonObject.get("info"));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public static ArrayList<String> parseStatusJson(String resultJson) {
        ArrayList<String> list = new ArrayList<>();
        try {
            JSONObject gamesJsonObject = (JSONObject) JSONValue.parseWithException(resultJson);

            System.out.println("Состояние сервера: " + gamesJsonObject.get("msg"));

            if (gamesJsonObject.get("info") == null) {
                System.out.println("Кто таков? " + gamesJsonObject.get("player"));
                System.out.println("Кто ходит? " + gamesJsonObject.get("mover"));
                list.add(gamesJsonObject.get("player").toString());
                list.add(gamesJsonObject.get("mover").toString());
                return list;
            } else if (gamesJsonObject.get("info") != "waiting") {
                System.out.println("Ошибка: " + gamesJsonObject.get("info"));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean parseNoParamJson(String resultJson) {
        try {
            JSONObject gamesJsonObject = (JSONObject) JSONValue.parseWithException(resultJson);

            System.out.println("Состояние сервера: " + gamesJsonObject.get("msg"));

            if (gamesJsonObject.get("info") == null) {
                System.out.println("Всё ОК!");
                return true;
            }

            System.out.println("Ошибка: " + gamesJsonObject.get("info"));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Создаем объект URL из указанной в параметре строки.
    @Nullable
    public static URL createUrl(String link) {
        try {
            return new URL(link);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String createDuringConnecting(int[][][] matrix) {
        String result = "";

        for (int i = 0; i < 10; i++) {
            result += "[\"" + matrix[i][0][0] + "\", \"" + matrix[i][1][0] + "\", \"" + matrix[i][2][0] + "\"]";
            if (i < 9)
                result += ", ";
        }
        System.out.println(result);

        return "[" + result + "]";
    }

    @Nullable
    public static int[][][] parseMatrices(String resultJson, int idMap) {
        int[][][] matrix = new int[10][3][1];
        try {
            JSONObject gamesJsonObject = (JSONObject) JSONValue.parseWithException(resultJson);

            System.out.println("Состояние сервера: " + gamesJsonObject.get("msg"));
            JSONArray ship = (JSONArray) gamesJsonObject.get("map" + idMap);
            for (int i = 0; i < ship.size(); i++) {
                JSONArray gamesData = (JSONArray) ship.get(i);
                matrix[i][0][0] = Integer.parseInt(gamesData.get(0).toString());
                matrix[i][1][0] = Integer.parseInt(gamesData.get(1).toString());
                matrix[i][2][0] = Integer.parseInt(gamesData.get(2).toString());
            }
            return matrix;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean parseUpdateJson(String resultJson) {
        try {
            JSONObject gamesJsonObject = (JSONObject) JSONValue.parseWithException(resultJson);
            System.out.println("Состояние сервера: " + gamesJsonObject.get("msg"));

            if (gamesJsonObject.get("info") == null) {
                return true;
            }

            System.out.println("Ошибка: " + gamesJsonObject.get("info"));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean parseMoverJson(String resultJson) {
        try {
            JSONObject gamesJsonObject = (JSONObject) JSONValue.parseWithException(resultJson);
            System.out.println("Состояние сервера: " + gamesJsonObject.get("msg"));

            if (gamesJsonObject.get("info") == null) {
                System.out.println("Кто ходит? " + gamesJsonObject.get("mover"));
                return gamesJsonObject.get("mover").equals("1");
            }

            System.out.println("Ошибка: " + gamesJsonObject.get("info"));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Nullable
    public static Point parseCoordJson(String resultJson) {
        Point point = new Point();

        try {
            JSONObject gamesJsonObject = (JSONObject) JSONValue.parseWithException(resultJson);
            System.out.println("Состояние сервера: " + gamesJsonObject.get("msg"));
            if (gamesJsonObject.get("info") == null) {
                String coord = gamesJsonObject.get("coord").toString(); //"5 , 6"
                coord = coord.trim();
                String[] coordNew = coord.split(",");
                point.x = Integer.parseInt(coordNew[0]);
                point.y = Integer.parseInt(coordNew[1]);

                return point;
            } else {
                if (!gamesJsonObject.get("info").toString().equals("empty coordinates") && Main.isOnline) {
                    Main.isOnline = false;
                    gameOver = true;

                    Platform.runLater(() -> {
                        if (gamesJsonObject.get("info").toString().equals("disconnected")) {
                            alertShow("The game has interrupted", "Your enemy has disconnected from the server!", Alert.AlertType.ERROR);
                        } else {
                            alertShow("Unexpected error", "You have been disconnected from the server!", Alert.AlertType.ERROR);
                        }

                        try {
                            Stage stage = (Stage) lastScene.getWindow();
                            Parent root = FXMLLoader.load(controller.getClass().getResource("main.fxml"));

                            lastScene = new Scene(root);
                            stage.setScene(lastScene);
                            stage.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }

            System.out.println("Ошибка: " + gamesJsonObject.get("info"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}