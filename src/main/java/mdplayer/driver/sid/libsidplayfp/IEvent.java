
package mdplayer.driver.sid.libsidplayfp;

public interface IEvent {
    void setNext(IEvent val);
    IEvent getNext();
    void setTriggerTime(long val);
    long getTriggerTime();
    void setName(String val);
    String getName();
    void event();
}
