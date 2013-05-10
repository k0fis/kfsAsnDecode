package kfs.kfsAsnDecode;

/**
 *
 * @author pavedrim
 */
public class kfsCCcItem {
        private final String str;
        private final Integer pos;
        
        public kfsCCcItem(final String str, final Integer pos) {
            this.str = str;
            this.pos = pos;
        }
        
        public String getCn() {
            return str;
        }
        public String getJcn() {
            return str.replace("-", "_");
        }

        public Integer getPos() {
            return pos;
        }
}
