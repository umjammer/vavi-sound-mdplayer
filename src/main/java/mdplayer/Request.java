
package mdplayer;

import java.util.function.Consumer;


public class Request {
    public final enmRequest request;

    public final Object[] args;

    public Object[] results;

    public final Consumer<Object> callBack;

    private final Object objlock = new Object();

    private boolean _end = false;

    public boolean getEnd() {
        synchronized (objlock) {
            return _end;
        }
    }

    public void setEnd(boolean value) {
        synchronized (objlock) {
            _end = value;
        }
    }

    public Request(enmRequest req,
            Object[] args/* = null */,
            Consumer<Object> callBack/* = null */) {
        request = req;
        this.args = args;
        this.callBack = callBack;
    }

    public enum enmRequest {
        Die,
        GetStatus,
        Stop,
        Play,
        Pause,
        Fadeout
    }
}
