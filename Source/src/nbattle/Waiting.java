package nbattle;


import javafx.application.Platform;

import java.io.IOException;
import java.util.ArrayList;

import static nbattle.JsonUtils.parseMatrices;
import static nbattle.GameLogic.*;
import static nbattle.Main.*;
import static nbattle.MainController.*;


public class Waiting implements Runnable {
    private final int DELAY = 750;
    public boolean isConnected = false;

    Waiting() {
        super();
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while (!isConnected && isRun) {
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String resultJsonWait = JsonUtils.parseUrl(MAIN_URL + "status.php", "&id=" + sNetId);
            ArrayList<String> list = JsonUtils.parseStatusJson(resultJsonWait);
            System.out.println("Current List = " + list);
            if (list != null) {
                connection(list, resultJsonWait);
                isConnected = true;
            }
        }
    }

    public void connection(ArrayList<String> list, String resultJsonWait) {
        Platform.runLater(() -> {
            try {
                Main.sNetEnemy = list.get(0);

                createStage();
                isHost = true;
                isOnline = true;

                coordinatesFriend = parseMatrices(resultJsonWait, 1);
                coordinatesEnemy = parseMatrices(resultJsonWait, 2);

                int k = 4;
                for (int i = 0; i < 10; i++) {
                    if (i == 1)
                        k--;
                    else if (i == 3)
                        k--;
                    else if (i == 6)
                        k--;
                    setShipNet(k, coordinatesFriend[i][2][0] == 1, coordinatesFriend[i][0][0], coordinatesFriend[i][1][0], true);
                    setShipNet(k, coordinatesEnemy[i][2][0] == 1, coordinatesEnemy[i][0][0], coordinatesEnemy[i][1][0], false);
                }

                step = (list.get(1).equals("1")) == isHost;
                changeStepDesign();
                changeCountShip(true);
                changeCountShip(false);
                new Processing();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }
}
