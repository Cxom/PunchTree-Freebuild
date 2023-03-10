package net.punchtree.freebuild.arbor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageUtils {
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
        Pattern httpPattern = Pattern.compile("(https?://|www\\.)\\S+");
        Pattern discordPattern = Pattern.compile("discord(?:app\\.com|\\.gg)[/\\w]*");
        Pattern emailPattern = Pattern.compile("\\S+@\\S+\\.\\S+");

        Matcher matcher = httpPattern.matcher(message);
        if (matcher.find()) {
            return true;
        }
        matcher = discordPattern.matcher(message);
        if (matcher.find()) {
            return true;
        }
        matcher = emailPattern.matcher(message);
        return matcher.find();
    }
}
