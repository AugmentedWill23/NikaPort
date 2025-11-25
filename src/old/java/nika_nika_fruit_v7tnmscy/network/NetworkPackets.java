package nika_nika_fruit_v7tnmscy.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class NetworkPackets {
    
    public record CycleMovePacket() implements CustomPayload {
        public static final CustomPayload.Id<CycleMovePacket> ID = 
            new CustomPayload.Id<>(Identifier.of("nika-nika-fruit-v7tnmscy", "cycle_move"));
        
        public static final PacketCodec<RegistryByteBuf, CycleMovePacket> CODEC = 
            PacketCodec.unit(new CycleMovePacket());
        
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record UseMovePacket() implements CustomPayload {
        public static final CustomPayload.Id<UseMovePacket> ID = 
            new CustomPayload.Id<>(Identifier.of("nika-nika-fruit-v7tnmscy", "use_move"));
        
        public static final PacketCodec<RegistryByteBuf, UseMovePacket> CODEC = 
            PacketCodec.unit(new UseMovePacket());
        
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record UltimateTransformPacket() implements CustomPayload {
        public static final CustomPayload.Id<UltimateTransformPacket> ID = 
            new CustomPayload.Id<>(Identifier.of("nika-nika-fruit-v7tnmscy", "ultimate_transform"));
        
        public static final PacketCodec<RegistryByteBuf, UltimateTransformPacket> CODEC = 
            PacketCodec.unit(new UltimateTransformPacket());
        
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    public record ShowInfoPacket() implements CustomPayload {
        public static final CustomPayload.Id<ShowInfoPacket> ID =
            new CustomPayload.Id<>(Identifier.of("nika-nika-fruit-v7tnmscy", "show_info"));
        public static final PacketCodec<RegistryByteBuf, ShowInfoPacket> CODEC =
            PacketCodec.unit(new ShowInfoPacket());
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // Haki payloads
    public record HakiCyclePacket() implements CustomPayload {
        public static final CustomPayload.Id<HakiCyclePacket> ID = new CustomPayload.Id<>(Identifier.of("nika-nika-fruit-v7tnmscy", "haki_cycle"));
        public static final PacketCodec<RegistryByteBuf, HakiCyclePacket> CODEC = PacketCodec.unit(new HakiCyclePacket());
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }
    public record HakiCycleReversePacket() implements CustomPayload {
        public static final CustomPayload.Id<HakiCycleReversePacket> ID = new CustomPayload.Id<>(Identifier.of("nika-nika-fruit-v7tnmscy", "haki_cycle_rev"));
        public static final PacketCodec<RegistryByteBuf, HakiCycleReversePacket> CODEC = PacketCodec.unit(new HakiCycleReversePacket());
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // New: Select Haki type (Up/Down)
    public record HakiSelectTypePacket() implements CustomPayload {
        public static final CustomPayload.Id<HakiSelectTypePacket> ID = new CustomPayload.Id<>(Identifier.of("nika-nika-fruit-v7tnmscy", "haki_select_type"));
        public static final PacketCodec<RegistryByteBuf, HakiSelectTypePacket> CODEC = PacketCodec.unit(new HakiSelectTypePacket());
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }
    public record HakiSelectTypeReversePacket() implements CustomPayload {
        public static final CustomPayload.Id<HakiSelectTypeReversePacket> ID = new CustomPayload.Id<>(Identifier.of("nika-nika-fruit-v7tnmscy", "haki_select_type_rev"));
        public static final PacketCodec<RegistryByteBuf, HakiSelectTypeReversePacket> CODEC = PacketCodec.unit(new HakiSelectTypeReversePacket());
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record HakiUsePacket() implements CustomPayload {
        public static final CustomPayload.Id<HakiUsePacket> ID = new CustomPayload.Id<>(Identifier.of("nika-nika-fruit-v7tnmscy", "haki_use"));
        public static final PacketCodec<RegistryByteBuf, HakiUsePacket> CODEC = PacketCodec.unit(new HakiUsePacket());
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }
    public record HakiShowMasteryPacket() implements CustomPayload {
        public static final CustomPayload.Id<HakiShowMasteryPacket> ID = new CustomPayload.Id<>(Identifier.of("nika-nika-fruit-v7tnmscy", "haki_show_mastery"));
        public static final PacketCodec<RegistryByteBuf, HakiShowMasteryPacket> CODEC = PacketCodec.unit(new HakiShowMasteryPacket());
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }
    public record HakiShowInfoPacket() implements CustomPayload {
        public static final CustomPayload.Id<HakiShowInfoPacket> ID = new CustomPayload.Id<>(Identifier.of("nika-nika-fruit-v7tnmscy", "haki_show_info"));
        public static final PacketCodec<RegistryByteBuf, HakiShowInfoPacket> CODEC = PacketCodec.unit(new HakiShowInfoPacket());
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }
    
    public record QuickUsePacket() implements CustomPayload {
        public static final CustomPayload.Id<QuickUsePacket> ID =
            new CustomPayload.Id<>(Identifier.of("nika-nika-fruit-v7tnmscy", "quick_use"));
        public static final PacketCodec<RegistryByteBuf, QuickUsePacket> CODEC =
            PacketCodec.unit(new QuickUsePacket());
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }
}