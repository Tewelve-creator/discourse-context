package org.linguapulse;

import org.linguapulse.web.WebServer;

public class LinguaPulseWebApp {

    public static void main(String[] args) throws Exception {
        boolean openBrowser = !hasFlag(args, "--no-browser");
        new WebServer().start(openBrowser);
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String arg : args) {
            if (flag.equals(arg)) {
                return true;
            }
        }
        return false;
    }
}
