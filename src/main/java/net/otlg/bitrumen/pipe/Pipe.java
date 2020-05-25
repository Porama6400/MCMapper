package net.otlg.bitrumen.pipe;

import net.otlg.bitrumen.wrapper.Input;
import net.otlg.bitrumen.wrapper.Output;

public interface Pipe {
    void process(Input in, Output out);
}
