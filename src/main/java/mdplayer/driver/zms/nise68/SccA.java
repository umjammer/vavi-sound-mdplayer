package mdplayer.driver.zms.nise68;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.System.getLogger;


/**
 * @see "Outside X68000"
 */
public class SccA {

    private static final Logger logger = getLogger(SccA.class.getName());

    private final BiFunction<Integer, Byte, Integer> scc;
    private byte group = 0;
    private byte interrupt = 0;
    private byte vect = 0;
    private byte intMask = (byte) 0xff;

    private byte[] reg = new byte[0x100];
    private Consumer<Byte>[] cmdw = null;
    private Supplier<Byte>[] cmdr = null;
    private short generalTimerValue = 0;
    private int renderingFreq;
    // private double clkM = 4_915_200.0 / 8.0;// 1_000_000.0;
    private final double clkM = 1_000_000.0;
    private double stepM;
    private double generalTimerValueWrk = 0.0;
    private double clickcounter = 1.0;

    private byte currentReg = 0;

    public SccA(int renderingFreq, BiFunction<Integer, Byte, Integer> scc) {
        this.scc = scc;

        setClock(renderingFreq);
    }

    public void setClock(int renderingFreq) {
        this.renderingFreq = renderingFreq;
        stepM = clkM / (double) renderingFreq;
    }

    public byte read(int ptr) {
        int c = ptr & 0xf;
        byte dat = 0;
        if (c == 0x5) {
            dat = switch (currentReg) {
                case 0 -> 4; // 4 TxBufferEmpty
                case 1 -> 1; // 1 AllSent completed
                case 2 -> vect;
                case 3 -> 0;
                case 8 -> throw new UnsupportedOperationException();
                case 10 -> throw new UnsupportedOperationException();
                case 12 -> throw new UnsupportedOperationException();
                case 13 -> throw new UnsupportedOperationException();
                case 15 -> throw new UnsupportedOperationException();
                default -> dat;
            };
            currentReg = 0; // Anything you do resets the register to 0.
        } else {
            return dat;
//            throw new UnsupportedOperationException();
        }
        logger.log(Level.TRACE, "Read SCC_A Adr:$00e9_80%02x Dat:$%02x".formatted(c, dat & 0xff));
        return dat;
    }

    public boolean write(int ptr, byte dat) {
        int c = ptr & 0xf;

        if (c == 0x5) {
            currentReg = dat;
        } else if (c == 0x7) {
            switch (currentReg) {
                case 0:
                    byte crcResetCommand = (byte) (dat & 0xe0);
                    byte commandCode = (byte) (dat & 0x1c);
                    byte registerSelect = (byte) (dat & 0x03);
                    logger.log(Level.TRACE, "Write SCC_A WR00:%02x".formatted(dat & 0xff));
                    divSendSCC(dat);
                    return false;
                case 1:
                    throw new UnsupportedOperationException();
                case 2:
                    throw new UnsupportedOperationException();
                case 3:
                    throw new UnsupportedOperationException();
                case 8:
                    throw new UnsupportedOperationException();
                case 10:
                    throw new UnsupportedOperationException();
                case 12:
                    throw new UnsupportedOperationException();
                case 13:
                    throw new UnsupportedOperationException();
                case 15:
                    throw new UnsupportedOperationException();
            }
            currentReg = 0;
        } else {
            throw new UnsupportedOperationException();
        }
        return false;
    }

    int currentMIDI = 0;
    boolean changeMIDI = false;

    private void divSendSCC(byte dat) {
        switch (dat & 0xff) {
            case 0xf5:
                changeMIDI = true;
                return;
            default:
                if (changeMIDI) {
                    currentMIDI = dat == 1 ? 0 : 1;
                    changeMIDI = false;
                    return;
                }
                break;
        }

        scc.apply(currentMIDI, dat);
    }
}
