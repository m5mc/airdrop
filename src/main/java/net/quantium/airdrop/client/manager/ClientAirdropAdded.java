package net.quantium.airdrop.client.manager;

import net.minecraftforge.fml.common.eventhandler.Event;

public class ClientAirdropAdded extends Event {
    public final ClientAirdropList.Entry entry;

    public ClientAirdropAdded(ClientAirdropList.Entry entry) {
        this.entry = entry;
    }
}
