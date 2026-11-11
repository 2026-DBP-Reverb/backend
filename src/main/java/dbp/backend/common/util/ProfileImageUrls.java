package dbp.backend.common.util;

public final class ProfileImageUrls {
    private static final String IMAGE_SERVER_URI = "http://localhost:8080";
    private static final String DEFAULT_IMAGE_PATH = "/images/default-profile.png";

    private ProfileImageUrls() {
    }

    public static String resolve(String url) {
        if (url == null || url.isBlank()) {
            return IMAGE_SERVER_URI + DEFAULT_IMAGE_PATH;
        }
        return url.startsWith("/") ? IMAGE_SERVER_URI + url : url;
    }
}
