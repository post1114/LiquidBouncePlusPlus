/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.SlowDownEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.item.*
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S30PacketWindowItems
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "NoSlow", spacedName = "No Slow", category = ModuleCategory.MOVEMENT, description = "Prevent you from getting slowed down by items (swords, foods, etc.) and liquids.")
class NoSlow : Module() {
    private val msTimer = MSTimer()
    private val swordModeValue = ListValue("SwordPacketMode", arrayOf("Custom", "Watchdog", "OldWatchdog", "OldHypixel", "NCP", "UpdatedNCP", "AAC", "AAC5", "None"), "None")
    private val consumeModeValue = ListValue("ConsumePacketMode", arrayOf("Custom", "Watchdog", "OldWatchdog", "OldHypixel", "NCP", "UpdatedNCP", "AAC", "AAC5", "None"), "None")
    private val bowModeValue = ListValue("BowPacketMode", arrayOf("Custom", "Watchdog", "OldWatchdog", "OldHypixel", "NCP", "UpdatedNCP", "AAC", "AAC5", "None"), "None")
    private val blockForwardMultiplier = FloatValue("BlockForwardMultiplier", 1.0F, 0.2F, 1.0F, "x")
    private val blockStrafeMultiplier = FloatValue("BlockStrafeMultiplier", 1.0F, 0.2F, 1.0F, "x")
    private val consumeForwardMultiplier = FloatValue("ConsumeForwardMultiplier", 1.0F, 0.2F, 1.0F, "x")
    private val consumeStrafeMultiplier = FloatValue("ConsumeStrafeMultiplier", 1.0F, 0.2F, 1.0F, "x")
    private val bowForwardMultiplier = FloatValue("BowForwardMultiplier", 1.0F, 0.2F, 1.0F, "x")
    private val bowStrafeMultiplier = FloatValue("BowStrafeMultiplier", 1.0F, 0.2F, 1.0F, "x")
    private val swordCustomOnGround = BoolValue("SwordCustomOnGround", false, { swordModeValue.get().equals("custom", true) })
    private val swordCustomDelayValue = IntegerValue("SwordCustomDelay", 60, 10, 200, "ms", { swordModeValue.get().equals("custom", true) })
    private val swordSendPacketValue = BoolValue("SwordSendPacket", true, { swordModeValue.get().equals("watchdog", true) })
    private val swordDebugValue = BoolValue("SwordDebug", false, { swordModeValue.get().equals("watchdog", true) })
    private val consumeCustomOnGround = BoolValue("ConsumeCustomOnGround", false, { consumeModeValue.get().equals("custom", true) })
    private val consumeCustomDelayValue = IntegerValue("ConsumeCustomDelay", 60, 10, 200, "ms", { consumeModeValue.get().equals("custom", true) })
    private val consumeSendPacketValue = BoolValue("ConsumeSendPacket", true, { consumeModeValue.get().equals("watchdog", true) })
    private val consumeDebugValue = BoolValue("ConsumeDebug", false, { consumeModeValue.get().equals("watchdog", true) })
    private val bowCustomOnGround = BoolValue("BowCustomOnGround", false, { bowModeValue.get().equals("custom", true) })
    private val bowCustomDelayValue = IntegerValue("BowCustomDelay", 60, 10, 200, "ms", { bowModeValue.get().equals("custom", true) })
    private val bowSendPacketValue = BoolValue("BowSendPacket", true, { bowModeValue.get().equals("watchdog", true) })
    private val bowDebugValue = BoolValue("BowDebug", false, { bowModeValue.get().equals("watchdog", true) })

    // Soulsand
    val soulsandValue = BoolValue("Soulsand", true)
    val liquidPushValue = BoolValue("LiquidPush", true)

    private var shouldSwap = false

    override fun onDisable() {
        msTimer.reset()
        shouldSwap = false
    }

    override val tag: String?
        get() = "S:${swordModeValue.get()}|C:${consumeModeValue.get()}|B:${bowModeValue.get()}"

    private fun sendPacket(event : MotionEvent, sendC07 : Boolean, sendC08 : Boolean, delay : Boolean, delayValue : Long, onGround : Boolean, watchDog : Boolean = false) {
        val digging = C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(-1,-1,-1), EnumFacing.DOWN)
        val blockPlace = C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem())
        val blockMent = C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer.inventory.getCurrentItem(), 0f, 0f, 0f)
        if(onGround && !mc.thePlayer.onGround) {
            return
        }
        if(sendC07 && event.eventState == EventState.PRE) {
            if(delay && msTimer.hasTimePassed(delayValue)) {
                mc.netHandler.addToSendQueue(digging)
            } else if(!delay) {
                mc.netHandler.addToSendQueue(digging)
            }
        }
        if(sendC08 && event.eventState == EventState.POST) {
            if(delay && msTimer.hasTimePassed(delayValue) && !watchDog) {
                mc.netHandler.addToSendQueue(blockPlace)
                msTimer.reset()
            } else if(!delay && !watchDog) {
                mc.netHandler.addToSendQueue(blockPlace)
            } else if(watchDog) {
                mc.netHandler.addToSendQueue(blockMent)
            }
        }
    }

    private fun getModeForItem(item: Item?): String = when (item) {
        is ItemSword -> swordModeValue.get()
        is ItemFood, is ItemPotion, is ItemBucketMilk -> consumeModeValue.get()
        is ItemBow -> bowModeValue.get()
        else -> "None"
    }

    private fun isUsingItemOrBlocking(): Boolean {
        val killAura = LiquidBounce.moduleManager[KillAura::class.java] as KillAura
        return mc.thePlayer.isUsingItem || mc.thePlayer.isBlocking() || killAura.blockingStatus || isUNCPBlocking()
    }

    private fun isUNCPBlocking(): Boolean {
        return swordModeValue.get().equals("updatedncp", true) && mc.gameSettings.keyBindUseItem.isKeyDown && (mc.thePlayer.heldItem?.item is ItemSword)
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val heldItem = mc.thePlayer.heldItem?.item ?: return
        val mode = getModeForItem(heldItem)
        val debug = when (heldItem) {
            is ItemSword -> swordDebugValue.get()
            is ItemFood, is ItemPotion, is ItemBucketMilk -> consumeDebugValue.get()
            is ItemBow -> bowDebugValue.get()
            else -> false
        }

        if (mode.equals("watchdog", true) && event.packet is S30PacketWindowItems && isUsingItemOrBlocking()) {
            event.cancelEvent()
            if (debug)
                ClientUtils.displayChatMessage("detected reset item packet")
        }

        val packet = event.packet
        if (event.isCancelled || shouldSwap) return

        if (packet is C08PacketPlayerBlockPlacement) {
            val stack = packet.stack
            if (stack?.item != null && mc.thePlayer.heldItem?.item != null && stack.item == mc.thePlayer.heldItem?.item) {
                if ((consumeModeValue.get().equals("updatedncp", true) && (stack.item is ItemFood || stack.item is ItemPotion || stack.item is ItemBucketMilk)) ||
                    (bowModeValue.get().equals("updatedncp", true) && stack.item is ItemBow)) {
                    shouldSwap = true
                }
            }
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (!MovementUtils.isMoving() && !shouldSwap)
            return

        val heldItem = mc.thePlayer.heldItem?.item ?: return
        val mode = getModeForItem(heldItem)

        if (mode.equals("none", true))
            return

        when (heldItem) {
            is ItemSword -> handleSwordMotion(event, mode)
            is ItemFood, is ItemPotion, is ItemBucketMilk -> handleConsumeMotion(event, mode)
            is ItemBow -> handleBowMotion(event, mode)
        }
    }

    private fun handleSwordMotion(event: MotionEvent, mode: String) {
        if (!isUNCPBlocking() && !mc.thePlayer.isBlocking) return

        if (mode.equals("aac5", true)) {
            if (event.eventState == EventState.POST) {
                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer.inventory.getCurrentItem(), 0f, 0f, 0f))
            }
        } else {
            when (mode.toLowerCase()) {
                "aac" -> {
                    if (mc.thePlayer.ticksExisted % 3 == 0) {
                        sendPacket(event, true, false, false, 0, false)
                    } else {
                        sendPacket(event, false, true, false, 0, false)
                    }
                }
                "custom" -> {
                    sendPacket(event, true, true, true, swordCustomDelayValue.get().toLong(), swordCustomOnGround.get())
                }
                "ncp" -> {
                    sendPacket(event, true, true, false, 0, false)
                }
                "updatedncp" -> {
                    if (event.eventState == EventState.POST) {
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, mc.thePlayer.inventory.getCurrentItem(), 0f, 0f, 0f))
                    }
                }
                "watchdog" -> {
                    if (swordSendPacketValue.get()) {
                        if (event.eventState == EventState.PRE) {
                            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(-1, -1, -1), EnumFacing.DOWN))
                        } else {
                            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0F, 0F, 0F))
                        }
                    }
                }
                "oldwatchdog" -> {
                    if (mc.thePlayer.ticksExisted % 2 == 0) {
                        sendPacket(event, true, false, false, 50, true)
                    } else {
                        sendPacket(event, false, true, false, 0, true, true)
                    }
                }
                "oldhypixel" -> {
                    if (event.eventState == EventState.PRE)
                        mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(-1, -1, -1), EnumFacing.DOWN))
                    else
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f))
                }
            }
        }
    }

    private fun handleConsumeMotion(event: MotionEvent, mode: String) {
        if (!mc.thePlayer.isUsingItem && !shouldSwap) return

        if (mode.equals("aac5", true)) {
            if (event.eventState == EventState.POST) {
                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer.inventory.getCurrentItem(), 0f, 0f, 0f))
            }
        } else {
            when (mode.toLowerCase()) {
                "aac" -> {
                    if (mc.thePlayer.ticksExisted % 3 == 0) {
                        sendPacket(event, true, false, false, 0, false)
                    } else {
                        sendPacket(event, false, true, false, 0, false)
                    }
                }
                "custom" -> {
                    sendPacket(event, true, true, true, consumeCustomDelayValue.get().toLong(), consumeCustomOnGround.get())
                }
                "ncp" -> {
                    sendPacket(event, true, true, false, 0, false)
                }
                "updatedncp" -> {
                    if (event.eventState == EventState.PRE && shouldSwap) {
                        val currentItem = mc.thePlayer.inventory.currentItem
                        mc.thePlayer.inventory.currentItem = (currentItem + 1) % 9
                        mc.thePlayer.inventory.currentItem = currentItem
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, mc.thePlayer.inventory.getCurrentItem(), 0f, 0f, 0f))
                        shouldSwap = false
                    }
                }
                "watchdog" -> {
                    if (consumeSendPacketValue.get()) {
                        if (event.eventState == EventState.PRE) {
                            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(-1, -1, -1), EnumFacing.DOWN))
                        } else {
                            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0F, 0F, 0F))
                        }
                    }
                }
                "oldwatchdog" -> {
                    if (mc.thePlayer.ticksExisted % 2 == 0) {
                        sendPacket(event, true, false, false, 50, true)
                    } else {
                        sendPacket(event, false, true, false, 0, true, true)
                    }
                }
                "oldhypixel" -> {
                    if (event.eventState == EventState.PRE)
                        mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(-1, -1, -1), EnumFacing.DOWN))
                    else
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f))
                }
            }
        }
    }

    private fun handleBowMotion(event: MotionEvent, mode: String) {
        if (!mc.thePlayer.isUsingItem && !shouldSwap) return

        if (mode.equals("aac5", true)) {
            if (event.eventState == EventState.POST) {
                mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer.inventory.getCurrentItem(), 0f, 0f, 0f))
            }
        } else {
            when (mode.toLowerCase()) {
                "aac" -> {
                    if (mc.thePlayer.ticksExisted % 3 == 0) {
                        sendPacket(event, true, false, false, 0, false)
                    } else {
                        sendPacket(event, false, true, false, 0, false)
                    }
                }
                "custom" -> {
                    sendPacket(event, true, true, true, bowCustomDelayValue.get().toLong(), bowCustomOnGround.get())
                }
                "ncp" -> {
                    sendPacket(event, true, true, false, 0, false)
                }
                "updatedncp" -> {
                    if (event.eventState == EventState.PRE && shouldSwap) {
                        val currentItem = mc.thePlayer.inventory.currentItem
                        mc.thePlayer.inventory.currentItem = (currentItem + 1) % 9
                        mc.thePlayer.inventory.currentItem = currentItem
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, mc.thePlayer.inventory.getCurrentItem(), 0f, 0f, 0f))
                        shouldSwap = false
                    }
                }
                "watchdog" -> {
                    if (bowSendPacketValue.get()) {
                        if (event.eventState == EventState.PRE) {
                            mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(-1, -1, -1), EnumFacing.DOWN))
                        } else {
                            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0F, 0F, 0F))
                        }
                    }
                }
                "oldwatchdog" -> {
                    if (mc.thePlayer.ticksExisted % 2 == 0) {
                        sendPacket(event, true, false, false, 50, true)
                    } else {
                        sendPacket(event, false, true, false, 0, true, true)
                    }
                }
                "oldhypixel" -> {
                    if (event.eventState == EventState.PRE)
                        mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(-1, -1, -1), EnumFacing.DOWN))
                    else
                        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, null, 0.0f, 0.0f, 0.0f))
                }
            }
        }
    }

    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        val heldItem = mc.thePlayer.heldItem?.item

        event.forward = getMultiplier(heldItem, true)
        event.strafe = getMultiplier(heldItem, false)
    }

    private fun getMultiplier(item: Item?, isForward: Boolean) = when (item) {
        is ItemFood, is ItemPotion, is ItemBucketMilk -> {
            if (isForward) this.consumeForwardMultiplier.get() else this.consumeStrafeMultiplier.get()
        }
        is ItemSword -> {
            if (isForward) this.blockForwardMultiplier.get() else this.blockStrafeMultiplier.get()
        }
        is ItemBow -> {
            if (isForward) this.bowForwardMultiplier.get() else this.bowStrafeMultiplier.get()
        }
        else -> 0.2F
    }
}
