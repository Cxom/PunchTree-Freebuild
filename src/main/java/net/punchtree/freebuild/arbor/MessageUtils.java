package net.punchtree.freebuild.arbor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageUtils {
    private static final Pattern pattern = Pattern.compile("(https?://\\S+|www\\.\\S+|\\S+@\\S+|\\S+#\\d{4})");
    // the above regex pattern will match URLs, Emails, and Discord links

    public static String sanitizeMessage(String message) {
        return message.codePoints()
                .filter(cp -> Character.UnicodeBlock.of(cp).equals(Character.UnicodeBlock.BASIC_LATIN))
                .mapToObj(Character::toString)
                .collect(Collectors.joining())
                .strip();
    }

    public static String escapeEmojis(String message) {
        return message.replaceAll(":(\\w+):", "\\\\$0");
    }

    public static boolean containsHyperlink(String message) {
        return pattern.matcher(message).find();
    }

    public static String replaceCharsInUrls(String message) {
        Matcher matcher = pattern.matcher(message);
        StringBuilder replacedMessage = new StringBuilder();
        while (matcher.find()) {
            String url = matcher.group();
            String modifiedUrl = url.replaceAll("[./:@]+", " ");
            matcher.appendReplacement(replacedMessage, modifiedUrl);
        }
        matcher.appendTail(replacedMessage);

        return replacedMessage.toString();
    }
}
