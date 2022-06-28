
package mdplayer;

import java.util.ArrayList;
import java.util.List;

import dotnet4j.Tuple;
import mdplayer.form.sys.frmMain;


public class Operation {

    private enum Ope {
        END,
        PLAY,
        STOP,
        PAUSE,
        RELEASE
    }

    private final Object lockObj = new Object();

    private List<Tuple<Ope, Object[]>> cmdBuf = new ArrayList<>();

    private frmMain parent;

    public void SendCommand(Ope cmd, Object... option) {
        synchronized (lockObj) {
            cmdBuf.add(new Tuple<>(cmd, option));
        }
    }

    public Operation(frmMain parent) {
        this.parent = parent;

        Thread trd = new Thread(this::start);
        trd.start();
    }

    private void start() {
        while (true) {
            try { Thread.sleep(10); } catch (InterruptedException e) {}
            if (cmdBuf.size() < 1)
                continue;

            Tuple<Ope, Object[]> cmd;
            synchronized (lockObj) {
                cmd = cmdBuf.get(0);
                if (cmd == null)
                    continue;
                if (cmd.Item1 == Ope.END)
                    return;
                cmdBuf.clear();

                switch (cmd.Item1) {
                case PLAY:
                    parent.play();
                    break;
                case STOP:
                    parent.stop();
                    break;
                case PAUSE:
                    parent.pause();
                    break;
                }
            }

            // RELEASEを受け取るまで待ち状態
            while (true) {
                try { Thread.sleep(10); } catch (InterruptedException e) {}
                if (cmdBuf.size() < 1)
                    continue;

                synchronized (lockObj) {
                    cmd = cmdBuf.get(0);
                    if (cmd == null)
                        continue;
                    if (cmd.Item1 == Ope.RELEASE) {
                        cmdBuf.clear();
                        break;
                    }
                    cmdBuf.remove(0);
                }
            }
        }
    }
}
