package mdplayer.driver.hes;

import dotnet4j.util.compat.TriConsumer;


/**
 * KMxxx event timer header.
 *
 * @author Mamiya
 */
public class KmEvent {

    public static final int KMEVENT_ITEM_MAX = 31; /**  MAX 255 */

    public interface dlgProc extends TriConsumer<Event, Integer, M_Hes.HESHES> {
    }

    public static class Item {
        // Direct access to members prohibited
        //public Object user;
        public M_Hes.HESHES user;
        public dlgProc proc;
        /** Event occurrence time */
        public int count;
        /** Bidirectional Linked List */
        public int prev;
        /** Bidirectional Linked List */
        public int next;
        /** Internal State Flags */
        public int sysflag;
        /** Unused */
        public int flag2;
    }

    public static class Event {
        // Direct access to members prohibited
        public Item[] item = new Item[KMEVENT_ITEM_MAX + 1];
    }

    private enum Flag {
        BREAKED(0x01),
        DISPATCHED(0x02),
        ALLOCED(0x80);
        final int v;

        Flag(int v) {
            this.v = v;
        }
    }

    private void reset(Event kme) {
        kme.item[0].count = 0;
        for (int id = 0; id <= KMEVENT_ITEM_MAX; id++) {
            kme.item[id].sysflag &= ~Flag.ALLOCED.v;
            kme.item[id].count = 0;
            kme.item[id].next = id;
            kme.item[id].prev = id;
        }
    }

    public void init(Event kme) {
        for (int id = 0; id <= KMEVENT_ITEM_MAX; id++) {
            kme.item[id] = new Item();
            kme.item[id].sysflag = 0;
        }
        reset(kme);
    }

    public int alloc(Event kme) {
        int id;
        for (id = 1; id <= KMEVENT_ITEM_MAX; id++) {
            if (kme.item[id].sysflag == 0) {
                kme.item[id].sysflag = Flag.ALLOCED.v;
                return id;
            }
        }
        return 0;
    }

    /** Remove from list */
    private void unlistItem(Event kme, int curid) {
        Item cur, next, prev;
        cur = kme.item[curid];
        next = kme.item[cur.next];
        prev = kme.item[cur.prev];
        next.prev = cur.prev;
        prev.next = cur.next;
    }

    /** Insert just before the specified position (baseid) in the list */
    private void listItem(Event kme, int curid, int baseid) {
        Item cur, next, prev;
        cur = kme.item[curid];
        next = kme.item[baseid];
        prev = kme.item[next.prev];
        cur.next = baseid;
        cur.prev = next.prev;
        prev.next = curid;
        next.prev = curid;
    }

    /** Insert into sorted list */
    private void insertItem(Event kme, int curid) {
        int baseid;
        for (baseid = kme.item[0].next; baseid != 0; baseid = kme.item[baseid].next) {
            if (kme.item[baseid].count != 0) {
                if (kme.item[baseid].count > kme.item[curid].count) break;
            }
        }
        listItem(kme, curid, baseid);
    }

    public void free(Event kme, int curid) {
        unlistItem(kme, curid);
        kme.item[curid].sysflag = 0;
    }

    public void setTimer(Event kme, int curid, int time) {
        unlistItem(kme, curid); // removal
        kme.item[curid].count = time != 0 ? kme.item[0].count + time : 0;
        if (kme.item[curid].count != 0) insertItem(kme, curid); // sort
    }

    public int getTimer(Event kme, int curid, /* ref */ int[] time) {
        int nextcount;
        nextcount = kme.item[curid != 0 ? curid : kme.item[0].next].count;
        if (nextcount == 0) return 0;
        nextcount -= kme.item[0].count;
        if (time[0] != 0) time[0] = nextcount;
        return 1;
    }

    public void setEvent(Event kme, int curid, dlgProc proc, M_Hes.HESHES user) {
        kme.item[curid].proc = proc;
        kme.item[curid].user = user;
    }

    /** Execute the specified number of cycles */
    public void process(Event kme, int cycles) {
        int id;
        int nextCount;
        kme.item[0].count += cycles;
        if (kme.item[0].next == 0) {
            // If the list is empty, then stop.
            kme.item[0].count = 0;
            return;
        }
        nextCount = kme.item[kme.item[0].next].count;
        while (nextCount != 0 && kme.item[0].count >= nextCount) {
            // Resetting the event occurrence flag
            for (id = kme.item[0].next; id != 0; id = kme.item[id].next) {
                kme.item[id].sysflag &= 0xfc; // ~((byte) Flag.KMEVENT_FLAG_BREAKED + (byte) Flag.KMEVENT_FLAG_DISPATCHED);
            }
            // Progress by nextCount
            kme.item[0].count -= nextCount;
            for (id = kme.item[0].next; id != 0; id = kme.item[id].next) {
                if (kme.item[id].count == 0) continue;
                kme.item[id].count -= nextCount;
                if (kme.item[id].count != 0) continue;
                // Set the event occurrence flag
                kme.item[id].sysflag |= Flag.BREAKED.v;
            }
            for (id = kme.item[0].next; id != 0; id = kme.item[id].next) {
                // Checking the event done flag
                if ((kme.item[id].sysflag & Flag.DISPATCHED.v) != 0) continue;
                kme.item[id].sysflag |= Flag.DISPATCHED.v;
                // Checking the event occurrence flag
                if ((kme.item[id].sysflag & Flag.BREAKED.v) == 0) continue;
                // Target event start
                kme.item[id].proc.accept(kme, id, kme.item[id].user);
                // Rescan from the beginning
                id = 0;
            }
            nextCount = kme.item[kme.item[0].next].count;
        }
    }
}
