package cognitionmodel.models.upright;

public class UrPoint {
        int position;
        int token;
        int tag = 0;

        public UrPoint(int position, int token) {
            this.position = position;
            this.token = token;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getToken() {
            return token;
        }

        public void setToken(int token) {
            this.token = token;
        }

    @Override
    public String toString() {
        return position+":"+token;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }
}
