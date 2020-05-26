package net.otlg.bitumen.pipe;

import net.otlg.bitumen.wrapper.Input;
import net.otlg.bitumen.wrapper.Output;

public interface Pipe {
    void process(Input in, Output out);
}
