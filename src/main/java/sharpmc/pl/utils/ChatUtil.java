package sharpmc.pl.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {
    private static final Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");

    public static String colored(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> colored(List<String> text) {
        return text.stream().map(ChatUtil::colored).toList();
    }

    public static String coloredHex(String message) {
        Matcher matcher = pattern.matcher(message);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group();
            StringBuilder builder = new StringBuilder();
            builder.append("x");

            for (int i = 1; i < hexCode.length(); i++) {
                char c = hexCode.charAt(i);
                builder.append("&").append(c);
            }

            matcher.appendReplacement(sb, builder.toString());
        }

        matcher.appendTail(sb);
        message = sb.toString();

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> coloredHex(List<String> text) {
        return text.stream().map(ChatUtil::coloredHex).toList();
    }

    public static String coloredHex(String message, Map<String, Object> placeholders) {
        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue().toString());
        }
        return coloredHex(message);
    }

    public static void successMessage(Player player, String message) {
        player.sendMessage(ChatUtil.coloredHex("&2&l✔ &8× &a" + message));
    }

    public static void successMessage(CommandSender player, String message) {
        player.sendMessage(ChatUtil.coloredHex("&2&l✔ &8× &a" + message));
    }

    public static void errorMessage(Player player, String message) {
        player.sendMessage(ChatUtil.coloredHex("&4&l❌ &8× &c" + message));
    }

    public static void errorMessage(CommandSender player, String message) {
        player.sendMessage(ChatUtil.coloredHex("&4&l❌ &8× &c" + message));
    }

    public static void actionBarMessage(Player player, String message) {
        player.sendActionBar(ChatUtil.coloredHex(message));
    }

    public static void successActionBar(Player player, String message) {
        player.sendActionBar(ChatUtil.coloredHex("&2&l✔ &8× &a" + message));
    }

    public static void errorActionBar(Player player, String message) {
        player.sendActionBar(ChatUtil.coloredHex("&4&l❌ &8× &c" + message));
    }

    public static void sendTitle(Player player, String title, String subtitle) {
        player.sendTitle(
                coloredHex(title),
                coloredHex(subtitle),
                10,
                70,
                20
        );
    }
}
