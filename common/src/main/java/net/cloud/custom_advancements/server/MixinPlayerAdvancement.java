package net.cloud.custom_advancements.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.cloud.custom_advancements.CustomAdvancements;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.text.html.parser.Entity;
import java.io.*;
import java.util.Set;

import static net.cloud.custom_advancements.CustomAdvancements.advFile;


@Mixin(value = PlayerAdvancements.class)
public abstract class MixinPlayerAdvancement {

    @Unique
    private static JsonObject custom_advancements = null;
    @Shadow
    private ServerPlayer player;

    @Shadow
    private final Set<Advancement> progressChanged;

    @Shadow
    private final PlayerList playerList;

    protected MixinPlayerAdvancement(Set<Advancement> progressChanged, PlayerList playerList) {
        this.progressChanged = progressChanged;
        this.playerList = playerList;
    }

    @Shadow
    public abstract AdvancementProgress getOrStartProgress(Advancement p_135997_);

    @Shadow
    protected abstract void unregisterListeners(Advancement p_136009_);


    @Shadow protected abstract void markForVisibilityUpdate(Advancement p_265528_);

    protected boolean loadAdvancements() {
        //TODO read json
        {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(advFile));
                StringBuilder jsonstringbuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    jsonstringbuilder.append(line);
                }
                bufferedReader.close();
                custom_advancements = new Gson().fromJson(jsonstringbuilder.toString(), JsonObject.class);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * @author Cloud
     * @reason custom advancements !
     */
    @Inject(method = "award", at = @At(value = "HEAD"), cancellable = true)
    public void award(Advancement advancement, String string, CallbackInfoReturnable<Boolean> cir) {

        if(!loadAdvancements()) {
            CustomAdvancements.LOGGER.error("Failed to load custom advancements file at " + advFile);
            cir.setReturnValue(false);
            cir.cancel();
            return;
        } else {
            // this.playerList.broadcastSystemMessage(Component.translatable("Loaded custom advancements file at " + advFile.toString()), false);
        }


        if (custom_advancements.has(advancement.getId().toString())) {

            // Forge: don't grant advancements for fake players
            boolean bl = false;
            AdvancementProgress advancementProgress = this.getOrStartProgress(advancement);
            boolean bl2 = advancementProgress.isDone();
            if (advancementProgress.grantProgress(string)) {
                this.unregisterListeners(advancement);
                this.progressChanged.add(advancement);
                bl = true;
                if (!bl2 && advancementProgress.isDone()) {
                    advGrantRewards(advancement);
                    if (advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceChat() && this.player.level().getGameRules().getBoolean(GameRules.RULE_ANNOUNCE_ADVANCEMENTS)) {
                        advBroadcastMessages(advancement);
                    }
                }
            }
            if (!bl2 && advancementProgress.isDone()) {
                this.markForVisibilityUpdate(advancement);
            }
            cir.cancel();
            cir.setReturnValue(bl);
        }
    }

    protected void advGrantRewards(Advancement adv) {
        String advId = adv.getId().toString();
        // For debugging
        // System.err.println(advId);
        if(custom_advancements.has(advId)) {
            JsonArray cmds = ((JsonObject) custom_advancements.get(advId)).get("commands").getAsJsonArray();
            for (JsonElement element : cmds) {
                String cmd = element.getAsString();
                cmd = cmd.replace("${player}", this.player.getDisplayName().getString());
                Player entity = this.player;
                Level world = entity.level();
                if (world instanceof ServerLevel _level)
                    _level.getServer().getCommands().performPrefixedCommand(
                            new CommandSourceStack(CommandSource.NULL, new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(), cmd);

            }
        } else {
            adv.getRewards().grant(this.player);
        }
    }

    protected void advBroadcastMessages(Advancement adv) {
        String advId = adv.getId().toString();

        if (custom_advancements.has(advId)) {
            String msg = ((JsonObject) custom_advancements.get(advId)).get("message").toString();
            msg = msg.substring(1, msg.length()-1); // Trim quotes
            msg = msg.replace("${player}", this.player.getDisplayName().getString());
            msg = msg.replace("${advancement}", "${advancement} ");
            String[] parts = msg.split("\\$\\{advancement\\}");
            MutableComponent chatComponent = Component.empty();

            String part = parts[0];
            MutableComponent partComponent = Component.translatable(part);
            chatComponent.append(partComponent);
            for (int i = 1 ; i < parts.length; i++) {
                String prefixes = removeCharacters(part);

                MutableComponent advComponent = Component.translatable(prefixes + adv.getDisplay().getTitle().getString());
                MutableComponent advComponent1 = Component.translatable(advComponent.getString() + "\n" + adv.getDisplay().getDescription().getString());
                MutableComponent advComponent2 = advComponent.copy().withStyle((Style advStyle) -> advStyle.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, advComponent1)));

                chatComponent.append(advComponent2);

                part = parts[i];

                partComponent = Component.translatable(prefixes + part.substring(1));
                chatComponent.append(partComponent);
            }

            // For Debugging
            // System.err.println("The number of text components is: " + textElements.size());
            // this.playerList.broadcastSystemMessage(Component.translatable(msg), false);
            // this.playerList.broadcastSystemMessage(Component.translatable("chat.type.advancement." + adv.getDisplay().getFrame().getName(), this.player.getDisplayName(), advComponent), false);

            this.playerList.broadcastSystemMessage(chatComponent, false);
        }
    }
    private static String removeCharacters(String input) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            if (currentChar == '§') {
                result.append(currentChar);
                try {
                    result.append(input.charAt(i + 1));
                } catch (Exception ignored) {

                }
            }
        }

        return result.toString();
    }
}