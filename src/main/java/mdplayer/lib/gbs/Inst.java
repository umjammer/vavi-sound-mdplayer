package mdplayer.lib.gbs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


class Inst {

    final Supplier<Integer> meth;
    public final int length;
    private final String flags;
    public final int[] cycle;

    Inst(Supplier<Integer> meth, int length, String cycle, String flags) {
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
