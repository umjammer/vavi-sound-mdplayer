package mdplayer.driver.hes;

import mdsound.Common.TriConsumer;


/*
 KMxxx event timer header
 by Mamiya
*/
public class KmEvent {

    public static final int KMEVENT_ITEM_MAX = 31; /* MAX 255 */

    public interface dlgProc extends TriConsumer<KMEVENT, Integer, M_Hes.HESHES> {
    }

    public static class KMEVENT_ITEM {
        /* メンバ直接アクセス禁止 */
        //public Object user;
        public M_Hes.HESHES user;
        public dlgProc proc;
        public int count;   /* イベント発生時間 */
        public byte prev;     /* 双方向リンクリスト */
        public byte next;     /* 双方向リンクリスト */
        public byte sysflag;  /* 内部状態フラグ */
        public byte flag2;    /* 未使用 */
    }

    public static class KMEVENT {
        /* メンバ直接アクセス禁止 */
        public KMEVENT_ITEM[] item = new KMEVENT_ITEM[KMEVENT_ITEM_MAX + 1];
    }

    private enum KMEVENT_FLAG {
        KMEVENT_FLAG_BREAKED((byte) 0x01),//(1 << 0),
        KMEVENT_FLAG_DISPATCHED((byte) 0x02),//(1 << 1),
        KMEVENT_FLAG_ALLOCED((byte) 0x80);//(1 << 7)
        byte v;

        KMEVENT_FLAG(byte v) {
            this.v = v;
        }
    }

    private void kmevent_reset(KMEVENT kme) {
        int id;
        kme.item[0].count = 0;
        for (id = 0; id <= KMEVENT_ITEM_MAX; id++) {
            kme.item[id].sysflag &= (byte) ~KMEVENT_FLAG.KMEVENT_FLAG_ALLOCED.v;
            kme.item[id].count = 0;
            kme.item[id].next = (byte) id;
            kme.item[id].prev = (byte) id;
        }
    }

    public void kmevent_init(KMEVENT kme) {
        int id;
        for (id = 0; id <= KMEVENT_ITEM_MAX; id++) {
            kme.item[id] = new KMEVENT_ITEM();
            kme.item[id].sysflag = 0;
        }
        kmevent_reset(kme);
    }

    public int kmevent_alloc(KMEVENT kme) {
        int id;
        for (id = 1; id <= KMEVENT_ITEM_MAX; id++) {
            if (kme.item[id].sysflag == 0) {
                kme.item[id].sysflag = (byte) KMEVENT_FLAG.KMEVENT_FLAG_ALLOCED.v;
                return id;
            }
        }
        return 0;
    }

    /** リストから取り外す */
    private void kmevent_itemunlist(KMEVENT kme, int curid) {
        KMEVENT_ITEM cur, next, prev;
        cur = kme.item[curid];
        next = kme.item[cur.next];
        prev = kme.item[cur.prev];
        next.prev = cur.prev;
        prev.next = cur.next;
    }

    /** リストの指定位置(baseid)の直前に挿入 */
    private void kmevent_itemlist(KMEVENT kme, int curid, int baseid) {
        KMEVENT_ITEM cur, next, prev;
        cur = kme.item[curid];
        next = kme.item[baseid];
        prev = kme.item[next.prev];
        cur.next = (byte) baseid;
        cur.prev = next.prev;
        prev.next = (byte) curid;
        next.prev = (byte) curid;
    }

    /** ソート済リストに挿入 */
    private void kmevent_iteminsert(KMEVENT kme, int curid) {
        int baseid;
        for (baseid = kme.item[0].next; baseid != 0; baseid = kme.item[baseid].next) {
            if (kme.item[baseid].count != 0) {
                if (kme.item[baseid].count > kme.item[curid].count) break;
            }
        }
        kmevent_itemlist(kme, curid, baseid);
    }

    public void kmevent_free(KMEVENT kme, int curid) {
        kmevent_itemunlist(kme, curid);
        kme.item[curid].sysflag = 0;
    }

    public void kmevent_settimer(KMEVENT kme, int curid, int time) {
        kmevent_itemunlist(kme, curid); /* 取り外し */
        kme.item[curid].count = time != 0 ? kme.item[0].count + time : 0;
        if (kme.item[curid].count != 0) kmevent_iteminsert(kme, curid); /* ソート */
    }

    public int kmevent_gettimer(KMEVENT kme, int curid, int time) {
        int nextcount;
        nextcount = kme.item[curid != 0 ? curid : kme.item[0].next].count;
        if (nextcount == 0) return 0;
        nextcount -= kme.item[0].count;
        if (time != 0) time = nextcount;
        return 1;
    }

    public void kmevent_setevent(KMEVENT kme, int curid, dlgProc proc, M_Hes.HESHES user) {
        kme.item[curid].proc = proc;
        kme.item[curid].user = user;
    }

    /** 指定サイクル分実行 */
    public void kmevent_process(KMEVENT kme, int cycles) {
        int id;
        int nextcount;
        kme.item[0].count += cycles;
        if (kme.item[0].next == 0) {
            /* リストが空なら終わり */
            kme.item[0].count = 0;
            return;
        }
        nextcount = kme.item[kme.item[0].next].count;
        while (nextcount != 0 && kme.item[0].count >= nextcount) {
            /* イベント発生済フラグのリセット */
            for (id = kme.item[0].next; id != 0; id = kme.item[id].next) {
                kme.item[id].sysflag &= 0xfc;// ~((byte)KMEVENT_FLAG.KMEVENT_FLAG_BREAKED + (byte)KMEVENT_FLAG.KMEVENT_FLAG_DISPATCHED);
            }
            /* nextcount分進行 */
            kme.item[0].count -= nextcount;
            for (id = kme.item[0].next; id != 0; id = kme.item[id].next) {
                if (kme.item[id].count == 0) continue;
                kme.item[id].count -= nextcount;
                if (kme.item[id].count != 0) continue;
                /* イベント発生フラグのセット */
                kme.item[id].sysflag |= (byte) KMEVENT_FLAG.KMEVENT_FLAG_BREAKED.v;
            }
            for (id = kme.item[0].next; id != 0; id = kme.item[id].next) {
                /* イベント発生済フラグの確認 */
                if ((kme.item[id].sysflag & (byte) KMEVENT_FLAG.KMEVENT_FLAG_DISPATCHED.v) != 0) continue;
                kme.item[id].sysflag |= (byte) KMEVENT_FLAG.KMEVENT_FLAG_DISPATCHED.v;
                /* イベント発生フラグの確認 */
                if ((kme.item[id].sysflag & (byte) KMEVENT_FLAG.KMEVENT_FLAG_BREAKED.v) == 0) continue;
                /* 対象イベント起動 */
                kme.item[id].proc.accept(kme, id, kme.item[id].user);
                /* 先頭から再走査 */
                id = 0;
            }
            nextcount = kme.item[kme.item[0].next].count;
        }
    }
}
