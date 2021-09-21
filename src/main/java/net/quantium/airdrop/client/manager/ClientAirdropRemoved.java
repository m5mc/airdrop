package net.quantium.airdrop.client.manager;

import net.minecraftforge.fml.common.eventhandler.Event;

public class ClientAirdropRemoved extends Event {
    public final ClientAirdropList.Entry entry;

    public ClientAirdropRemoved(ClientAirdropList.Entry entry) {
        this.entry = entry;
    }
}
