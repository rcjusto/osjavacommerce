package org.store.core.globals;

import org.apache.log4j.Logger;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Stack;

public class TableParser extends ArrayList {

    static Logger logger = Logger.getLogger(TableParser.class);

    Stack s = new Stack();

    /**
     * Process this reader.
     *
     * @param f
     * @throws IOException
     */
    public void parse(Reader f) throws IOException {

        this.clear();

        new ParserDelegator().parse(f, parser, false);

    }

    private HTMLEditorKit.ParserCallback parser = new HTMLEditorKit.ParserCallback() {

        private boolean inTD;

        private String tdBuffer;

        public void handleError(String arg0, int arg1) {

//			System.out.println("Error "+arg0);

            // TODO Auto-generated method stub

            super.handleError(arg0, arg1);

        }

        public void handleText(char[] arg0, int arg1) {

            if (inTD) {

                tdBuffer += new String(arg0);

            }

        }

        public void handleStartTag(Tag tag, MutableAttributeSet arg1, int arg2) {

            if (tag == HTML.Tag.TABLE) {

                s.add(new TableParser.HTMLTable());

            } else if (tag == HTML.Tag.TR) {

                s.add(new TableParser.HTMLRow());

            } else if (tag == HTML.Tag.TD || tag == HTML.Tag.TH) {

                inTD = true;

                tdBuffer = "";

            }

        }

        public void handleEndTag(Tag tag, int arg1) {

            if (tag == HTML.Tag.TABLE) {

                TableParser.HTMLTable T = (TableParser.HTMLTable) s.pop();

                if (s.size() == 0) {

                    TableParser.this.add(T);

                } else if (s.peek() instanceof HTMLRow) {

                    ((HTMLRow) s.peek()).add(T);

                } else {

                    logger.error("Need to be within nothing or a cell/row");

                }

            } else if (tag == HTML.Tag.TR) {

                HTMLRow r = (HTMLRow) s.pop();

                ((TableParser.HTMLTable) s.peek()).add(r);

            } else if (tag == HTML.Tag.TD || tag == HTML.Tag.TH) {

                if (inTD) {

                    ((TableParser.HTMLRow) s.peek()).add(tdBuffer);

                    inTD = false;

                }

            }

        }

    };

    public class HTMLTable extends ArrayList {
    }

    public class HTMLRow extends ArrayList {
    }

}