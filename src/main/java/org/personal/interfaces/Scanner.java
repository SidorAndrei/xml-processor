package org.personal.interfaces;

import java.io.IOException;

public interface Scanner {
    void startScan() throws IOException, InterruptedException;
    boolean verifyFileName(String fileName);
}
