package dev.porama.bitumen.pipe;

import dev.porama.bitumen.pipe.wrapper.Input;
import dev.porama.bitumen.pipe.wrapper.Output;

public interface Pipe {
    void process(Input in, Output out);
}
