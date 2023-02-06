package com.evacipated.cardcrawl.mod.stslib.patches.cardInterfaces;

import com.evacipated.cardcrawl.mod.stslib.actions.swappables.SwapCardAction;
import com.evacipated.cardcrawl.mod.stslib.cards.interfaces.SwappableCard;
import com.evacipated.cardcrawl.mod.stslib.helpers.SwapperHelper;
import com.evacipated.cardcrawl.modthespire.lib.SpireField;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.vfx.ThoughtBubble;

import java.util.ArrayList;
import java.util.List;

public class SwappableCardPatch {

    @SpirePatch(
            clz = AbstractCard.class,
            method = "update"
    )
    public static class AbstractCardUpdatePatch {

        public static void Postfix(AbstractCard __instance) {
            if (AbstractDungeon.player != null && __instance == AbstractDungeon.player.hoveredCard && SwapperHelper.isCardSwappable(__instance) && AbstractDungeon.actionManager.isEmpty()) {
                boolean pressed = SwapperHelper.handleInput(__instance.hb);
                if (pressed) {
                    boolean selected = (AbstractDungeon.player.isDraggingCard || AbstractDungeon.player.inSingleTargetMode);
                    int index = -1;
                    for (int i = 0; i < AbstractDungeon.player.hand.group.size(); i++) {
                        if (__instance == AbstractDungeon.player.hand.group.get(i)) {
                            index = i;
                        }
                    }
                    if (index != -1) {
                        if (__instance instanceof SwappableCard) {
                            SwappableCard swappableCard = (SwappableCard)__instance;
                            if (swappableCard.canSwap()) {
                                AbstractDungeon.actionManager.addToBottom(new SwapCardAction(__instance, SwapperHelper.getNextCard(__instance), index, selected));
                            } else {
                                AbstractDungeon.effectList.add(new ThoughtBubble(AbstractDungeon.player.dialogX, AbstractDungeon.player.dialogY, 3.0f, swappableCard.getUnableToSwapString(), true));
                            }
                        } else {
                            AbstractDungeon.actionManager.addToBottom(new SwapCardAction(__instance, SwapperHelper.getNextCard(__instance), index, selected));
                        }
                    } else {
                        System.out.println("How is clicked/hovered card not in hand?");
                    }
                }
            }
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            method = "makeStatEquivalentCopy"
    )
    public static class AbstractCardMakeStatEquivalentCopyPatch {
        private static boolean disableLoop = false;

        public static AbstractCard Postfix(AbstractCard __result, AbstractCard __instance) {
            if (SwapperHelper.isCardSwappable(__instance)) {
                if (!disableLoop) {
                    disableLoop = true;
                    //copy all other cards in __instance's swappable list and create a new swappable list for __result. __result may already have a list from normal instantiation
                    List<AbstractCard> newList = new ArrayList<>(SwappableChainField.swappableCards.get(__instance));
                    for (int i = 0; i < newList.size(); ++i) {
                        AbstractCard card = newList.get(i);
                        AbstractCard newCard = card.makeStatEquivalentCopy();
                        if (__instance == card) {
                            __result = newCard;
                        }
                        newList.set(i, newCard);
                    }
                    SwapperHelper.makeSwappableGroup(false, newList);
                    disableLoop = false;
                }
            }
            return __result;
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            method = "makeSameInstanceOf"
    )
    public static class AbstractCardMakeSameInstanceOfPatch {

        public static AbstractCard Postfix(AbstractCard __result, AbstractCard __instance) {
            if (SwapperHelper.isCardSwappable(__instance)) {
                //set the uuid of each swappable group in __result to match the cards in the swappable group of __instance. __result Swappable list should already be copied by above patch.
                List<AbstractCard> oldList = SwappableChainField.swappableCards.get(__instance);
                List<AbstractCard> newList = SwappableChainField.swappableCards.get(__result);
                if (oldList.size() != newList.size()) {
                    System.out.println("ERROR: make same instance list sizes are not the same. How did this happen?");
                }
                for (int i = 0; i < oldList.size(); ++i) {
                    newList.get(i).uuid = oldList.get(i).uuid;
                }
            }
            return __result;
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            method = "resetAttributes"
    )
    public static class AbstractCardResetAttributesPatch {
        public static void Postfix(AbstractCard __instance) {
            if (SwapperHelper.isCardSwappable(__instance)) {
                if (!SwapperHelper.preventUpgradeLoop) {
                    SwapperHelper.preventUpgradeLoop = true;
                    AbstractCard bufferCard = SwapperHelper.getNextCard(__instance);
                    bufferCard.resetAttributes();
                    while (SwapperHelper.getNextCard(bufferCard) != __instance) {
                        bufferCard = SwapperHelper.getNextCard(bufferCard);
                        bufferCard.resetAttributes();
                    }
                    SwapperHelper.preventUpgradeLoop = false;
                }
            }
        }
    }

    @SpirePatch(
            clz = AbstractCard.class,
            method = SpirePatch.CLASS
    )
    public static class SwappableChainField {
        public static SpireField<List<AbstractCard>> swappableCards = new SpireField<>(() -> null);
    }
}
