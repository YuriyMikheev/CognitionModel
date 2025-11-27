package cognitionmodel.models.upright.index;

import java.util.Iterator;

public class IndexPoint {
    int position;
    Object token;
    long idx;
    Iterator<Long> iterator;

    public IndexPoint(int position, Object token, Iterator<Long> iterator) {
        this.position = position;
        this.token = token;
        this.iterator = iterator;
        idx = nextIdx();
    }

    public long nextIdx(){
        if (iterator.hasNext()) return idx = iterator.next();
        else
            return idx = -1;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public long getIdx() {
        return idx;
    }

    public Object getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Iterator<Long> getIterator() {
        return iterator;
    }

    public void setIterator(Iterator<Long> iterator) {
        this.iterator = iterator;
    }
}
