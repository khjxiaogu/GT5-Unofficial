package gregtech.api.multitileentity.base;
import gregtech.api.net.GT_Packet_SendCoverData;
import gregtech.api.util.ISerializableObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;

import static gregtech.api.enums.GT_Values.NW;

public abstract class BaseNontickableMultiTileEntity extends BaseMultiTileEntity {
    boolean mConstructed = false; // Keeps track of whether this TE has been constructed and placed in the world
    public BaseNontickableMultiTileEntity() {
        super(false);
    }

    @Override
    public void issueClientUpdate() {
        if(worldObj != null && !worldObj.isRemote) sendClientData(null);
    }

    @Override
    public Packet getDescriptionPacket() {
        // We should have a world object and have been constructed by this point
        mConstructed = true;

        super.getDescriptionPacket();
        // We don't get ticked, so if we have any cover data that needs to be sent, send it now
        sendCoverDataIfNeeded();
        return null;
    }

    @Override
    public void issueCoverUpdate(byte aSide) {
        if(!mConstructed) {
            // Queue these up and send them with the description packet
            super.issueCoverUpdate(aSide);
        } else {
            // Otherwise, send the data right away
            NW.sendPacketToAllPlayersInRange(
                worldObj,
                new GT_Packet_SendCoverData(aSide, getCoverIDAtSide(aSide), getComplexCoverDataAtSide(aSide), this),
                xCoord, zCoord
            );
            // Just in case
            mCoverNeedUpdate[aSide] = false;
        }
    }

    @Override
    public void receiveCoverData(byte aCoverSide, int aCoverID, ISerializableObject aCoverData, EntityPlayerMP aPlayer) {
        super.receiveCoverData(aCoverSide, aCoverID, aCoverData, aPlayer);
        // We don't get ticked so issue the texture update right away
        issueTextureUpdate();
    }

}
