package controllers.utils;

import play.Logger;
import play.mvc.Scope.Params;
import play.mvc.Scope.Session;

/**
 * 
 * @author f.meurisse
 */
public final class SessionUtil {

    public static final String SESSION_PARAM_ECHEANCES_MODE = "Echeances_mode";

    public static final String SESSION_PARAM_TIERS_PAGE = "Tiers_page";
    public static final String SESSION_PARAM_TIERS_FILTRE = "Tiers_filtre";

    private SessionUtil() {
    }

    public static String getSessionParam(String name) {
        return getSessionParam(name, false, null);
    }

    public static String getSessionParam(String name, boolean fromRequestBefore) {
        return getSessionParam(name, fromRequestBefore, null);
    }

    public static String getSessionParam(String name, String defaultValue) {
        return getSessionParam(name, false, defaultValue);
    }

    public static String getSessionParam(String name, boolean fromRequestBefore, String defaultValue) {
        Logger.debug(">> SessionUtil.getSessionParam >> name = %s | fromRequestBefore = %b | default = %s", name, fromRequestBefore, defaultValue);
        Session session = Session.current();
        Params params = Params.current();
        String value = null;

        if (fromRequestBefore) {
            value = params.get(name);
            Logger.debug("Value from params = %s", value);
        }

        if (value != null) {
            session.put(name, value);
            Logger.debug("Value stored in session.", value);
        }
        else {
            value = session.get(name);
            Logger.debug("Value from session = %s", value);
        }

        if (value == null) {
            value = defaultValue;
        }

        Logger.debug(">> SessionUtil.getSessionParam >> return: %s", value);
        return value;
    }

}
