package com.tom.morewires.compat.oc2;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import com.tom.morewires.network.SimpleNetworkHandler;

public class OC2NetworkHandler extends SimpleNetworkHandler<NetworkFramePocket, OC2NetworkHandler> {

    public OC2NetworkHandler(final LocalWireNetwork net, final GlobalWireNetwork global) {
        super(net, global);
    }

    @Override
    protected NetworkFramePocket connect(final IImmersiveConnectable iic) {
        if (iic instanceof NetworkFramePocket framePocket) {
            System.out.println("SUKA: connected");
            framePocket.initFramePocket();
            return framePocket;
        }
        return null;
    }

    @Override
    protected void setNetworkHandler(final NetworkFramePocket framePocket, final OC2NetworkHandler handler) {
        if (handler != null) {
            final byte[] frame = framePocket.extractFrame();
            if (frame != null) {
                System.out.println("EBAL: got frame");
                for (final NetworkFramePocket connector : allConnectors) {
                    System.out.println("EBAL: sent frame");
                    if (framePocket != connector) {
                        connector.putFrame(frame);
                    }
                }
            }
        }
    }
}