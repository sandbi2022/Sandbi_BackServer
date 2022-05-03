/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbibackserver;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Ray peng Sun
 */
public class SandbiBackServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        Timer timer = new Timer();

        timer.schedule(new MyTimerTask(), 0, 1000);

    }

}

class MyTimerTask extends TimerTask {

    @Override
    public void run() {

        CheckTrans.mapAddress();

    }

}
