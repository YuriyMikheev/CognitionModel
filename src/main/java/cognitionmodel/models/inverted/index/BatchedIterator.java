package cognitionmodel.models.inverted.index;

import org.roaringbitmap.RoaringBatchIterator;
import org.roaringbitmap.RoaringBitmap;

import java.util.Iterator;

public class BatchedIterator implements Iterator<Long> {

    private RoaringBitmap roaringBitmap;
    private RoaringBatchIterator batchIterator;
    private int pointer = 0, batchsize = 0;
    private int[] buf = new int[256];
    private long last;
    public BatchedIterator(RoaringBitmap roaringBitmap){
        this.roaringBitmap = roaringBitmap;
        batchIterator = roaringBitmap.getBatchIterator();
        batchsize = batchIterator.nextBatch(buf);
        last = roaringBitmap.last();
    }

    @Override
    public boolean hasNext() {
        return batchIterator.hasNext() || pointer < batchsize;
    }

    @Override
    public Long next() {

        if (pointer == batchsize) {
            if (!batchIterator.hasNext()) return null;
            batchsize = batchIterator.nextBatch(buf); pointer = 0;
        }

        return Integer.toUnsignedLong(buf[pointer++]);
    }

    private long getX(){
        return Integer.toUnsignedLong(buf[pointer]);
    }

    public Long nextAfter(long x){
        if (getX() > x) return getX();
        if (last < x) return -1l;

        while (Integer.toUnsignedLong(buf[batchsize - 1]) < x && batchIterator.hasNext()){
            batchsize = batchIterator.nextBatch(buf);
            pointer = 0;
        }

        while (getX() < x & pointer < batchsize)
            pointer++;

        if (getX() < x) pointer++;

        return getX();
    }

}

