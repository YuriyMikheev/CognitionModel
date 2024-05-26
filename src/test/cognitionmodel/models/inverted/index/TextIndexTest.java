package cognitionmodel.models.inverted.index;

import org.junit.Test;
import org.roaringbitmap.RoaringBitmap;

import java.util.Random;

import static java.lang.Math.random;
import static java.lang.Math.round;
import static org.junit.Assert.*;

public class TextIndexTest {

    @Test
    public void and() {

        RoaringBitmap r = TextIndex.and(RoaringBitmap.bitmapOfRange(1000000250, 1000000260), RoaringBitmap.bitmapOfRange(1000000255, 1000000280), 2);
        System.out.println(r);

        r = TextIndex.and(RoaringBitmap.bitmapOfRange(1000000015, 1000000030), RoaringBitmap.bitmapOfRange(1000000010, 1000000020), 2);
        System.out.println(r);

        RoaringBitmap r1 = new RoaringBitmap(), r2 = new RoaringBitmap();
        for (int i = 0; i < 100000000; i++){
            if (random()>0.5) r1.add(i);
                else r2.add(i);
        }

        r = TextIndex.and(r1, r2, 1);
        RoaringBitmap r3  =  RoaringBitmap.and(RoaringBitmap.addOffset(r2, 1), r1);
        if (!(RoaringBitmap.and(r,r3).getCardinality() == r.getCardinality() && RoaringBitmap.and(r,r3).getCardinality() == r3.getCardinality()))
            System.out.println();

        assertTrue(RoaringBitmap.and(r,r3).getCardinality() == r.getCardinality() && RoaringBitmap.and(r,r3).getCardinality() == r3.getCardinality());


        r = TextIndex.and(r2, r1, 1);
        r3  =  RoaringBitmap.and(RoaringBitmap.addOffset(r1, 1), r2);
        if (!(RoaringBitmap.and(r,r3).getCardinality() == r.getCardinality() && RoaringBitmap.and(r,r3).getCardinality() == r3.getCardinality()))
            System.out.println();

        assertTrue(RoaringBitmap.and(r,r3).getCardinality() == r.getCardinality() && RoaringBitmap.and(r,r3).getCardinality() == r3.getCardinality());



        r1.clear(); r2.clear();
        for (int i = 0; i < 100000000; i++){
            r1.add(i);
            r2.add(i);
        }

        r2 = RoaringBitmap.addOffset(r2, 3);
        assertTrue(TextIndex.and(r1, r2, -3).getCardinality() == 100000000 );


        r1.clear(); r2.clear();

        r1.add(100L, 1000L);
        r2.add(500L, 2000L);

        assertTrue(TextIndex.and(r1, r2, 10).getCardinality() == RoaringBitmap.and(r1, RoaringBitmap.addOffset(r2, 10)).getCardinality());

    }

    @Test
    public void testAnd() {

        Random random = new Random();

        for (int i = 0; i < 1000; i++) {
            long s = random.nextLong(0, Integer.MAX_VALUE);
            RoaringBitmap r = RoaringBitmap.bitmapOfRange(s, s + random.nextLong(0, 1000000)-1), rt = new RoaringBitmap();
            BatchedIterator batchedIterator = new BatchedIterator(r);

            while (batchedIterator.hasNext()) {
                long x = batchedIterator.next();
                rt.add(x, x + 1);
            }

            assertTrue(RoaringBitmap.andNot(r, rt).getCardinality() == 0);

        }
    }

    @Test
    public void testAfter() {

        Random random = new Random();


        long s = random.nextLong(0, Integer.MAX_VALUE);
        RoaringBitmap r = RoaringBitmap.bitmapOfRange(s, s + random.nextLong(0, 1000000)-1);
        r.add(0);

        BatchedIterator batchedIterator = new BatchedIterator(r);

        long x = 0;
        while (batchedIterator.hasNext()) {
            long dx =  1, xo = x;
            x = batchedIterator.nextAfter(x + dx);
            if (x == -1) break;
            assertTrue(xo+dx-x <= 0);
        }



        for (int i = 0; i< 100; i++)
        {
            s = random.nextLong(0, Integer.MAX_VALUE);
            r = RoaringBitmap.bitmapOfRange(s, s + random.nextLong(0, 1000000)-1);

            r.andNot(RoaringBitmap.bitmapOfRange(s + 3, r.last() - 1000));

            batchedIterator = new BatchedIterator(r);

             x = 0;
            while (batchedIterator.hasNext()) {
                long dx =  (long) random.nextInt(1000), xo = x;
                x = batchedIterator.nextAfter(x + dx);
                if (x == -1) break;
                assertTrue(xo+dx-x <= 0);
            }

        }
    }
}