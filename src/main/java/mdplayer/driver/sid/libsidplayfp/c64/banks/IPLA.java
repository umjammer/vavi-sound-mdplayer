package mdplayer.driver.sid.libsidplayfp.c64.banks;

public interface IPLA
    {
        void setCpuPort(byte state);
        byte getLastReadByte();
        long getPhi2Time();
    }
