package cognitionmodel.models.inverted.index;

import org.roaringbitmap.RoaringBitmap;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class LongRoaringBitmap extends RoaringBitmap {

    private HashMap<Integer, RoaringBitmap> bitmaps = new HashMap<>();

    private static final long maxLength = 0xffffffff;

    public LongRoaringBitmap(){
        bitmaps.put(0, new RoaringBitmap());
    }

    private RoaringBitmap getMap(long x){
        return bitmaps.get((int) (x / maxLength));
    }

    public void add(long x){
        RoaringBitmap rm = getMap(x);
        if (rm == null) bitmaps.put((int) (x / maxLength), rm = new RoaringBitmap());
    }

    public boolean runOptimize(){
        AtomicBoolean r = new AtomicBoolean(true);
        bitmaps.values().forEach(a->{
            r.set(r.get() && a.runOptimize());
        });

        return r.get();
    }


}
