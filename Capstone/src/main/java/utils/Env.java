package utils;

public class Env {
    public static String get(String key, String def) {
        String v = System.getProperty(key);
        if (v == null || v.isBlank()) v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    // Build path from a route template + id; supports "{id}" or "%s"
    public static String buildPath(String route, String id) {
        if (route == null) return "/";
        String r = route.replace("{id}", id);
        if (r.contains("%s")) r = String.format(r, id);
        if (!r.startsWith("/")) r = "/" + r;
        return r;
    }
}
