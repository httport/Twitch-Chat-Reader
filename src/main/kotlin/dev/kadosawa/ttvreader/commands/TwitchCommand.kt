package dev.kadosawa.ttvreader.commands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import dev.kadosawa.ttvreader.config.ModConfig
import dev.kadosawa.ttvreader.data.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.shedaniel.autoconfig.AutoConfig
import net.minecraft.client.MinecraftClient
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting


object TwitchCommand {
    private val chatHud = MinecraftClient.getInstance().inGameHud.chatHud

    object Configurator {
        @Throws(CommandSyntaxException::class)
        fun username(ctx: CommandContext<ServerCommandSource>): Int {
            val input = ctx.getArgument("name", String::class.java)

            val configHolder = AutoConfig.getConfigHolder(ModConfig::class.java)
            configHolder.config.username = input
            configHolder.save()

            val msg = TranslatableText("commands.twitch.config.username_updated", input).formatted(Formatting.YELLOW)
            chatHud.addMessage(msg)

            return 0
        }

        @Throws(CommandSyntaxException::class)
        fun oauth(ctx: CommandContext<ServerCommandSource>): Int {
            val input = ctx.getArgument("token", String::class.java)

            val configHolder = AutoConfig.getConfigHolder(ModConfig::class.java)
            configHolder.config.OAuthToken = "oauth:$input"
            configHolder.save()

            val msg = TranslatableText("commands.twitch.config.oauth_updated").formatted(Formatting.YELLOW)
            chatHud.addMessage(msg)

            return 0
        }
    }

    @Throws(CommandSyntaxException::class)
    fun connect(ctx: CommandContext<ServerCommandSource>): Int {
        // Before creating a new connection gotta close existing ones
        Store.reset()

        // Keep requested name in the store
        val newChannelName = ctx.getArgument("name", String::class.java)
        Store.channelName = newChannelName

        // Prepare text messages
        val textStarted = TranslatableText("commands.twitch.connect.started").formatted(Formatting.YELLOW)
        val textFailure = TranslatableText("commands.twitch.connect.fail").formatted(Formatting.RED)
        val textSuccess =
            TranslatableText("commands.twitch.connect.success", newChannelName).formatted(Formatting.YELLOW)

        // Once again, we need a coroutine scope
        CoroutineScope(Dispatchers.IO).launch {
            chatHud.addMessage(textStarted)

            when (Store.buildTwirk()) {
                true -> {
                    chatHud.addMessage(textSuccess)
                }
                else -> {
                    chatHud.addMessage(textFailure)
                }
            }
        }
        return 0
    }

    @Suppress("UNUSED_PARAMETER")
    @Throws(CommandSyntaxException::class)
    fun disconnect(ctx: CommandContext<ServerCommandSource>): Int {
        Store.reset()
        chatHud.addMessage(TranslatableText("commands.twitch.reset.success").formatted(Formatting.YELLOW))
        return 0
    }
}
