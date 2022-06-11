
package mdplayer;

import java.util.ArrayList;
import java.util.List;

import dotnet4j.Tuple;
import mdplayer.form.sys.frmMain;


enum Ope {
    END,
    PLAY,
    STOP,
    PAUSE,
    RELEASE
}

public class Operation {
    private Object lockObj = new Object();

    private List<Tuple<Ope, Object[]>> cmdBuf = new ArrayList<Tuple<Ope, Object[]>>();

    private frmMain parent;

    public void SendCommand(Ope cmd, Object... option) {
        synchronized (lockObj) {
            cmdBuf.add(new Tuple<Ope, Object[]>(cmd, option));
        }
    }

    public Operation(frmMain parent) {
        this.parent = parent;

        Thread trd = new Thread(this::start);
        trd.start();
    }

    private void start() {
        while (true) {
            Thread.sleep(10);
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
                    parent.opePlay();
                    break;
                case STOP:
                    parent.opeStop();
                    break;
                case PAUSE:
                    parent.pause();
                    break;
                }
            }

            // RELEASEを受け取るまで待ち状態
            while (true) {
                Thread.sleep(10);
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
