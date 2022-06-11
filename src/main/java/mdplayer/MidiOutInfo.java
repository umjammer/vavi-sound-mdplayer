package mdplayer;

import java.io.Serializable;


public class MidiOutInfo implements Serializable {

    public int id = 0;
    public int manufacturer = -1;
    public String name = "";
    public int type = 0;//GM / XG / GS / LA / GS(SC - 55_1) / GS(SC - 55_2)

    public int beforeSendType = 0;//None / GM Reset / XG Reset / GS Reset / Custom
    public Boolean isVST = false;
    public String fileName = "";
    public String vendor = "";
}
