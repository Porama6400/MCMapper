package net.otlg.bitumen.pipe;

import net.otlg.bitumen.pipe.wrapper.Input;
import net.otlg.bitumen.pipe.wrapper.Output;

public interface Pipe {
    void process(Input in, Output out);
}
