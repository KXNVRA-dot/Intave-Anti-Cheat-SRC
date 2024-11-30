// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util;

import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import de.jpx3.intave.antipiracy.IIUA;
import java.io.File;
import java.io.PrintWriter;
import java.util.function.Consumer;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public final class IntaveExceptionHandler
{
    public static void printAndSaveToFile(final Exception e) {
        printAndSaveToFile(new ArrayList<String>(), e);
    }
    
    public static void printAndSaveToFile(final Exception e, final String... header) {
        printAndSaveToFile(Arrays.asList(header), e);
    }
    
    public static void printAndSaveToFile(final String header, final Exception e) {
        final List<String> f = new ArrayList<String>();
        f.add(header);
        printAndSaveToFile(f, e);
    }
    
    public static void printAndSaveToFile(final List<String> header, final Exception e) {
        print(e);
        final PrintWriter printWriter = createAndGetExceptionPrinter();
        header.forEach(printWriter::println);
        e.printStackTrace(printWriter);
        printWriter.flush();
    }
    
    private static void print(final Exception e) {
        e.printStackTrace();
    }
    
    private static PrintWriter createAndGetExceptionPrinter() {
        final File f = new File("plugins/Intave/errors");
        if (!f.exists()) {
            try {
                f.mkdirs();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        final long time = IIUA.getCurrentTimeMillis();
        final SimpleDateFormat sdf = new SimpleDateFormat("YYYY_MM_dd-HH.mm.ss");
        final String timestamp = sdf.format(time);
        final File d = new File("plugins" + File.separator + "Intave" + File.separator + "errors", "error" + timestamp + ".txt");
        if (!d.exists()) {
            try {
                d.createNewFile();
            }
            catch (IOException e2) {
                throw new IllegalStateException("An exception was thrown while logging another exception???!", e2);
            }
        }
        try {
            return new PrintWriter(new FileWriter(d, true));
        }
        catch (IOException e2) {
            throw new IllegalStateException("An exception was thrown while logging another exception???!", e2);
        }
    }
}
