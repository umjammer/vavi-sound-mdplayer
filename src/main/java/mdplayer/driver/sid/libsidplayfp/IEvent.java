
package mdplayer.driver.sid.libsidplayfp;

public interface IEvent {
    void SetM_next(IEvent val);
    IEvent GetM_next();
    void SetTriggerTime(long val);
    long GetTriggerTime();
    void SetM_name(String val);
    String GetM_name();
    void event_();
}
