// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.util.objectable;

import java.net.InetAddress;

public final class IPHash
{
    protected final InetAddress address;
    
    public IPHash(final InetAddress address) {
        this.address = address;
    }
    
    public boolean equals(final InetAddress address) {
        return this.address.equals(address);
    }
}
