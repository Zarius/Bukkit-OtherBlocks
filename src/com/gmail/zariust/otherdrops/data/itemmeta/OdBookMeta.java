package com.gmail.zariust.otherdrops.data.itemmeta;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.gmail.zariust.otherdrops.subject.Target;

public class OdBookMeta extends OdItemMeta {

    private final String title;
    private final String author;
    private List<String> pages = new ArrayList<String>();

    public OdBookMeta(String author, String title, List<String> pages) {
        this.title = title;
        this.author = author;
        this.pages = pages;
    }

    @Override
    public ItemStack setOn(ItemStack stack, Target source) {
        BookMeta meta = (BookMeta) stack.getItemMeta();
        meta.setTitle(title);
        meta.setAuthor(author);
        meta.setPages(pages);
        stack.setItemMeta(meta);
        return stack;
    }

    public static OdItemMeta parse(String state) {
        String split[] = state.split(":");

        String title = "";
        String author = "";
        List<String> pages = new ArrayList<String>();

        for (String sub : split) {
            String s = sub;
            String result = "";
            String page = "";

            result = matchSection(s, "(?i)author=(.*)");
            if (!result.isEmpty()) {
                author = result;
            }
            result = matchSection(s, "(?i)title=(.*)");
            if (!result.isEmpty()) {
                title = result;
            }
            result = matchSection(s, "(?i)page=(.*)");
            if (!result.isEmpty()) {
                page = result;
            }
            if (!page.isEmpty()) {
                pages.add(page);
            }
        }

        if (!author.isEmpty() || !title.isEmpty() || !(pages.size() == 0)) {
            return new OdBookMeta(author, title, pages);
        } else {
            return null;
        }
    }

    /**
     * @param s
     */
    private static String matchSection(String s, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);
        if (m.find()) {
            String d = m.group(1);
            if (d != null) {
                return d;
            }
        }
        return "";
    }
}
