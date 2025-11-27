package cognitionmodel.models.upright.agent;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.ModelType;

import java.util.LinkedList;

public class UrPoint {
        int position;
        Object token;
        long tag = 0;
        public static Encoding encoder = Encodings.newDefaultEncodingRegistry().getEncodingForModel(ModelType.GPT_4_32K);//registry.getEncoding(EncodingType.CL100K_BASE);
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
        return position+":"+tokensToStrings((Integer) token);
    }

    public long getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public static String tokensToStrings(int token){
        LinkedList<Integer> t = new LinkedList<>();
        t.add(token);
        return encoder.decode(t);
    }
}
