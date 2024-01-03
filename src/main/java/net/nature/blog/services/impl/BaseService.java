package net.nature.blog.services.impl;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import net.nature.blog.utils.Constants;


public class BaseService {
    protected int checkPage(int page){
        if (page < Constants.Page.DEFAULT_PAGE){
            page = Constants.Page.DEFAULT_PAGE;
        }
        return page;
    }

    protected int checkSize(int size){
        if (size < Constants.Page.DEFAULT_SIZE){
            size = Constants.Page.DEFAULT_SIZE;
        }
        return size;
    }

    public static String parseMarkdownFlexmark(String markdown) {
        MutableDataSet options = new MutableDataSet();

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Node document = parser.parse(markdown);
        String html = renderer.render(document);
        return html;
    }
}
