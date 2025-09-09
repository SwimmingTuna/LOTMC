package net.swimmingtuna.lotm.blocks.DimensionalSight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class UnorderedList {
    private HashMap<String, Integer> coll = new HashMap();
    private ArrayList<String> list;

    public UnorderedList() {
        this.list = new ArrayList();
    }

    public UnorderedList(ArrayList<String> lst) {
        for(int i = 0; i < lst.size(); ++i) {
            this.add((String)lst.get(i), false);
        }

        this.list = lst;
    }

    public void add(String key, boolean addToList) {
        if (this.coll.containsKey(key.toString())) {
            this.coll.put(key, (Integer)this.coll.get(key) + 1);
            if (addToList) {
                this.list.add(key);
            }
        } else {
            this.coll.put(key, 1);
            if (addToList) {
                this.list.add(key);
            }
        }

    }

    public String remove(String key) {
        if (this.coll.containsKey(key)) {
            this.coll.put(key, (Integer)this.coll.get(key) - 1);
            if ((Integer)this.coll.get(key) <= 0) {
                this.coll.remove(key);
            }

            this.list.remove(key);
            return key;
        } else {
            return null;
        }
    }

    public Integer get(String key) {
        return (Integer)this.coll.get(key);
    }

    public Set<String> getKeys() {
        return this.coll.keySet();
    }

    public ArrayList<String> getOrder() {
        return this.list;
    }

    public boolean equals(Object o) {
        if (!(o instanceof UnorderedList cmp)) {
            return false;
        } else {
            Set<String> cmpKeys = cmp.getKeys();
            if (!cmpKeys.equals(this.getKeys())) {
                return false;
            } else {
                Iterator var4 = cmpKeys.iterator();

                String tmp;
                do {
                    if (!var4.hasNext()) {
                        return true;
                    }

                    tmp = (String)var4.next();
                } while(cmp.get(tmp).equals(this.get(tmp)));

                return false;
            }
        }
    }

    public int size() {
        int count = 0;

        String tmp;
        for(Iterator var2 = this.getKeys().iterator(); var2.hasNext(); count += this.get(tmp)) {
            tmp = (String)var2.next();
        }

        return count;
    }

    public int hashCode() {
        int hc = 0;

        String tmp;
        for(Iterator var2 = this.list.iterator(); var2.hasNext(); hc += tmp.hashCode()) {
            tmp = (String)var2.next();
        }

        return hc;
    }
}

