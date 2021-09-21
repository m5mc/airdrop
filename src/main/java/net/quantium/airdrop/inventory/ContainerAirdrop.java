package net.quantium.airdrop.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.quantium.airdrop.entity.EntityAirdrop;

public class ContainerAirdrop extends Container {

    private final EntityAirdrop owner;
    public ContainerAirdrop(EntityAirdrop owner, EntityPlayer player) {
        this.owner = owner;

        bindInventory(owner.getInventory());
        bindPlayerInventory(player.inventory);
    }

    private void bindPlayerInventory(IInventory player) {
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(player, j + i * 9 + 9, 8 + j * 18, 38 + i * 18));
            }
        }

        for(int j = 0; j < 9; j++) {
            this.addSlotToContainer(new Slot(player, j, 8 + j * 18, 38 + 58));
        }
    }

    private void bindInventory(IInventory owner) {
        for(int j = 0; j < 9; j++) {
            this.addSlotToContainer(new LockedSlot(owner, j, 8 + j * 18, 16));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 9) {
                if (!this.mergeItemStack(itemstack1, 9, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.owner.canInteractWith(player);
    }

    private static class LockedSlot extends Slot {

        public LockedSlot(IInventory inv, int i, int x, int y) {
            super(inv, i, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }
    }
}
