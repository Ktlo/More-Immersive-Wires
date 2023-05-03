package com.tom.morewires.compat.oc2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface NetworkFramePocket {

    void initFramePocket();

    void putFrame(@Nonnull byte[] frame);

    @Nullable
    byte[] extractFrame();
}
