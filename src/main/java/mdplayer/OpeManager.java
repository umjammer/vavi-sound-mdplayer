package mdplayer;

import java.util.ArrayList;
import java.util.List;


// for gui
public class OpeManager {

    private static final List<Request> reqToAudio = new ArrayList<>();

    private static final Object reqLock = new Object();

    public static AudioStatus getAudioStatus() {
        return AudioStatus.Unknown;
    }

    public static void requestToAudio(Request req) {
        synchronized (reqLock) {
            reqToAudio.add(req);
        }
    }

    /**
     * Audio receives the request
     */
    public static Request getRequestToAudio() {
        synchronized (reqLock) {
            if (reqToAudio.isEmpty())
                return null;

            Request req = reqToAudio.get(reqToAudio.size() - 1);
            reqToAudio.remove(req); // .clear();

            return req;
        }
    }

    /**
     * Called when Audio has finished processing a request.
     */
    public static void completeRequestToAudio(Request req) {
        synchronized (reqLock) {
            TrdCallback cb = new TrdCallback(req);
            Thread trd = new Thread(cb::callBack);
            trd.setPriority(Thread.MIN_PRIORITY);
            trd.start();
        }
    }

    static class TrdCallback {
        private final Request request;

        public TrdCallback(Request req) {
            request = req;
        }

        public void callBack() {
            request.callBack.accept(request.results);
        }
    }

    public enum AudioStatus {
        Unknown,
        Stop,
        FadeOut,
        Play,
        Pause
    }
}
