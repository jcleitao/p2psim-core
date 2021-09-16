package peernet.reports;

import java.io.PrintStream;
import java.io.File;
import java.io.FileNotFoundException;

import peernet.config.Configuration;
import peernet.core.CommonState;
import peernet.core.Control;
import peernet.util.IncrementalFreq;
import peernet.util.IncrementalStats;

public abstract class FileObserver implements Control {

    private static final String PAR_FILENAME = "filename";
    private static final String PAR_MULTIPLE_FILES = "multifile";
    private static final String PAR_TAG_TIME = "tagtime";
    private static final String PAR_SEPARATOR = "separator";

    private String filename;
    private boolean multifiles;
    private boolean tagtime;
    private PrintStream out;
    protected String separator;

    public FileObserver(String prefix) {
        filename = Configuration.getString(prefix + "." + PAR_FILENAME, null);
        if (filename == null) {
            out = System.out;
        } else {
            multifiles = Configuration.getBoolean(prefix + "." + PAR_MULTIPLE_FILES, false);
        }
        tagtime = Configuration.getBoolean(prefix + "." + PAR_TAG_TIME, false);
        separator = Configuration.getString(prefix + "." + PAR_SEPARATOR, " ");
        if (!multifiles) {
            try {
                out = new PrintStream(new File(CommonState.getExperienceName() + "-" + filename + ".txt"));
            } catch (FileNotFoundException e) {
                out = System.out;
            }
        }
    }

    public void startObservation() {
        if (this.multifiles) {
            try {
                out = new PrintStream(new File(CommonState.getExperienceName() + "-" + filename + "-" + CommonState.getTime() + ".txt"));
            } catch (FileNotFoundException e) {
                out = System.out;
            }
        }
    }

    public void output(String s) {
        if (this.tagtime) {
            this.out.print(CommonState.getTime() + this.separator);
        }
        this.out.println(s);
    }

    public void output(IncrementalStats is) {
        if (this.tagtime) {
            this.out.print(CommonState.getTime() + this.separator);
        }
        this.out.println(is.toString(separator));
    }

    public void output(IncrementalFreq stats) {
        if (this.tagtime) {
            this.out.println(CommonState.getTime() + this.separator);
        }
        this.out.println(stats.toString(separator));
    }

    public void outputNoLine(String s) {
        this.out.print(s);
    }

    public void outputEndLine() {
        this.out.println();
    }

    public void stopObservation() {
        out.flush();
        if (this.multifiles) {
            out.close();
        }
    }

}
