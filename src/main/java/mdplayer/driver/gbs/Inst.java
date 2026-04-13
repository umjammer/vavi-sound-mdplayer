package mdplayer.driver.gbs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class Inst {

    public Supplier<Integer> meth;
    public int length;
    public String flags;
    public int[] cycle;

    public Inst(Supplier<Integer> meth, int length, String cycle, String flags) {
        this.meth = meth;
        this.length = length;
        String sCycle = cycle;
        this.flags = flags;

        String[] c = sCycle.split("/");
        // Filter out empty strings (equivalent to RemoveEmptyEntries)
        List<String> filtered = new ArrayList<>();
        for (String part : c) {
            if (!part.trim().isEmpty()) {
                filtered.add(part.trim());
            }
        }

        this.cycle = new int[filtered.size()];
        for (int i = 0; i < filtered.size(); i++) {
            try {
                this.cycle[i] = Integer.parseInt(filtered.get(i));
            } catch (NumberFormatException e) {
                this.cycle[i] = 0;
            }
        }
    }
}