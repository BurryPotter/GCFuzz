package dtjvms.fjvms;

import java.util.ArrayList;

public class JTRegParser {
    private volatile static JTRegParser instance;

    private JTRegParser() {
    }

    ;

    public static JTRegParser getInstance() {
        if (instance == null) {
            synchronized (JTRegParser.class) {
                if (instance == null) {
                    instance = new JTRegParser();
                }
            }
        }
        return instance;
    }

    public ArrayList<String> parseRun(ArrayList<String> comments) {
        ArrayList<ArrayList<String>> fragments = new ArrayList<>();
        ArrayList<String> fragment = null;
        boolean inFragment = false;
        for (String str : comments) {
            if (str.contains("@test")) {
                fragment = new ArrayList<>();
                inFragment = true;
            }
            if (inFragment) {
                fragment.add(str);
            }
            if (inFragment && str.startsWith(" */")) {
                inFragment = false;
                fragments.add(fragment);
            }
        }
        ArrayList<String> result = new ArrayList<>();
        for (ArrayList<String> f : fragments) {
            ArrayList<String> nf = this.format(f);
            nf.forEach(comment -> {
                if (comment.contains("@run")) {
                    result.add(comment);
                }
            });
        }
        return result;
    }

    private ArrayList<String> format(ArrayList<String> fragment) {
        ArrayList<String> newFragment = new ArrayList<>();
        for (String line : fragment) {
            if (line.contains("@")) {
                newFragment.add(line);
            } else {
                String concat = newFragment.get(newFragment.size() - 1) + line.replaceFirst("\\s+\\*\\s+", " ");
                newFragment.set(newFragment.size() - 1, concat);
            }
        }
        return newFragment;
    }
}
