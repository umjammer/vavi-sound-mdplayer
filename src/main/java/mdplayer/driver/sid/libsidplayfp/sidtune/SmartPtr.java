/* Simple smart pointer class. */

package mdplayer.driver.sid.libsidplayfp.sidtune;

import java.nio.ByteBuffer;


class SmartPtrBase<T> {

    public long ulint_smartpt;

    public SmartPtrBase(byte[] buffer,
                        long bufferLen,
                        boolean bufOwner/* = false */) {
        bufBegin = null;
        bufEnd = null;
        pBufCurrent = null;
        bufLen = 0;
        status = false;
        doFree = bufOwner;
        dummy = null;

        if ((buffer != null) && (bufferLen != 0)) {
            bufBegin = ByteBuffer.wrap(buffer);
            pBufCurrent = ByteBuffer.wrap(buffer);
            bufEnd = bufBegin; bufEnd.position((int) bufferLen);
            bufLen = bufferLen;
            status = true;
        }
    }

    // public member functions

    public ByteBuffer tellBegin() {
        return bufBegin;
    }

    public long tellLength() {
        return bufLen;
    }

    public long tellPos() {
        return pBufCurrent.position() - bufBegin.position();
    }

    public boolean checkIndex(long index) {
        return (pBufCurrent.position() + (int) index) < bufEnd.position();
    }

    public boolean reset() {
        if (bufLen != 0) {
            pBufCurrent = bufBegin;
            status = true;
        } else {
            status = false;
        }
        return status;
    }

    public boolean good() {
        return pBufCurrent.position() < bufEnd.position();
    }

    public boolean fail() {
        return pBufCurrent == bufEnd;
    }

    public void opePlusPlus() {
        if (good()) {
            pBufCurrent.position(pBufCurrent.position()+1);
        } else {
            status = false;
        }
    }

    public void opePlusPlus(int m) {
        if (good()) {
            pBufCurrent.position(pBufCurrent.position()+1);
        } else {
            status = false;
        }
    }

    public void opeMinusMinus() {
        if (!fail()) {
            pBufCurrent.position(pBufCurrent.position()-1);
        } else {
            status = false;
        }
    }

    public void opeMinusMinus(int m) {
        if (!fail()) {
            pBufCurrent.position(pBufCurrent.position()-1);
        } else {
            status = false;
        }
    }

    public void opePlusEquel(long offset) {
        if (checkIndex(offset)) {
            pBufCurrent.position(pBufCurrent.position()+(int) offset);
        } else {
            status = false;
        }
    }

    public void opeMinusEquel(long offset) {
        if ((pBufCurrent.position() - (int) offset) >= bufBegin.position()) {
            pBufCurrent.position(pBufCurrent.position() - (int) offset);
        } else {
            status = false;
        }
    }

    public ByteBuffer opePtr() {
        if (good()) {
            return pBufCurrent;
        } else {
            status = false;
            return dummy;
        }
    }

    public ByteBuffer opeDKakko(long index) {
        if (checkIndex(index)) {
            return ByteBuffer.wrap(pBufCurrent.array(), pBufCurrent.position() + (int) index, pBufCurrent.capacity() - (pBufCurrent.position() + (int) index));
        } else {
            status = false;
            return dummy;
        }
    }

    public boolean opeBool() {
        return status;
    }

    protected ByteBuffer bufBegin;
    protected ByteBuffer bufEnd;
    protected ByteBuffer pBufCurrent;
    protected long bufLen;
    protected boolean status;
    protected boolean doFree;
    protected ByteBuffer dummy;

    public static class SmartPtr extends SmartPtrBase {
        public SmartPtr(byte[] buffer,
                        long bufferLen,
                        boolean bufOwner /* = false */) {
            super(buffer, bufferLen, bufOwner);
        }

        SmartPtr() {
            super(null, 0, false);
        }

        public void setBuffer(byte[] b, long bufferLen) {
            ByteBuffer buffer = ByteBuffer.wrap(b);
            if (bufferLen != 0) {
                this.bufBegin = buffer;
                this.pBufCurrent = buffer;
                this.bufEnd = buffer; bufEnd.position((int) bufferLen);
                this.bufLen = bufferLen;
                this.status = true;
            } else {
                this.bufBegin = null;
                this.pBufCurrent = null;
                this.bufEnd = null;
                this.bufLen = 0;
                this.status = false;
            }
        }
    }
}
