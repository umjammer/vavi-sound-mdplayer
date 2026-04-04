
package mdplayer;

import java.util.ArrayList;
import java.util.List;

import dotnet4j.util.compat.Tuple;
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

    private final List<Tuple<Ope, Object[]>> cmdBuf = new ArrayList<>();

    private final frmMain parent;

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
            try { Thread.sleep(10); } catch (InterruptedException ignore) {}
            if (cmdBuf.isEmpty())
                continue;

            Tuple<Ope, Object[]> cmd;
            synchronized (lockObj) {
                cmd = cmdBuf.getFirst();
                if (cmd == null)
                    continue;
                if (cmd.getItem1() == Ope.END)
                    return;
                cmdBuf.clear();

                switch (cmd.getItem1()) {
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

            // Waiting until "RELEASE" is received
            while (true) {
                try { Thread.sleep(10); } catch (InterruptedException ignore) {}
                if (cmdBuf.isEmpty())
                    continue;

                synchronized (lockObj) {
                    cmd = cmdBuf.getFirst();
                    if (cmd == null)
                        continue;
                    if (cmd.getItem1() == Ope.RELEASE) {
                        cmdBuf.clear();
                        break;
                    }
                    cmdBuf.removeFirst();
                }
            }
        }
    }
}
