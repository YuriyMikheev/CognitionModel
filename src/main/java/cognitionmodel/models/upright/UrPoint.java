package cognitionmodel.models.upright;

public class UrPoint {
        int position;
        Object token;
        long tag = 0;

        public UrPoint(int position, Object token) {
            this.position = position;
            this.token = token;
        }

        public UrPoint(int position, Object token, long tag) {
            this.position = position;
            this.token = token;
            this.tag = tag;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public Object getToken() {
            return token;
        }

        public void setToken(int token) {
            this.token = token;
        }

    @Override
    public String toString() {
        if (token instanceof UrAgent)
            return position+":"+ ((UrAgent) token).getInfo();
        return position+":"+token;
    }

    public long getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }
}
