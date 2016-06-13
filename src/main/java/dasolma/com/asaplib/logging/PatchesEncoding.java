package dasolma.com.asaplib.logging;

import dasolma.com.asaplib.msa.Patch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by dasolma on 14/05/15.
 */
public class PatchesEncoding implements IEncoder {

    @Override
    public Object encode(Object data) {
        List<Patch> patches = (List<Patch>) data;

        ArrayList<Integer> pos = new ArrayList<Integer>();
        for (Patch p : patches) {
            pos.add(p.getCol());
            pos.add(p.getRow());
        }

        return convertIntegers(pos);

    }

    private int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }
}
