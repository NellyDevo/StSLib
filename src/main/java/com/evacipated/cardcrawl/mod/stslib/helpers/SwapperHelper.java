package com.evacipated.cardcrawl.mod.stslib.helpers;

import com.evacipated.cardcrawl.mod.stslib.StSLib;
import com.evacipated.cardcrawl.mod.stslib.patches.HitboxRightClick;
import com.evacipated.cardcrawl.mod.stslib.patches.cardInterfaces.SwappableCardPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.Hitbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SwapperHelper {
    public static boolean preventUpgradeLoop = false;

    /**
     * creates a new swappable chain. If not additive, all existing swappable chains on the given cards will be deleted, else the given cards in each list will be combined into one chain.
     */
    public static void makeSwappableGroup(boolean additive, List<AbstractCard> swappableLinks) {
        if (swappableLinks.size() < 2) {
            System.out.println("Error: trying to create swappable group with fewer than 2 cards");
        } else {
            for (AbstractCard card : swappableLinks) {

                //combine any existing swappable chains, or disassemble if necessary
                List<AbstractCard> preExistingList = SwappableCardPatch.SwappableChainField.swappableCards.get(card);
                if (preExistingList != null) {
                    if (additive) {
                        List<AbstractCard> additions = new ArrayList<>();
                        preExistingList.forEach(other -> {
                            if (!swappableLinks.contains(other)) {
                                additions.add(other);
                                SwappableCardPatch.SwappableChainField.swappableCards.set(other, swappableLinks);
                            }
                        });
                        swappableLinks.addAll(additions);
                    } else {
                        for (AbstractCard other : preExistingList) {
                            SwappableCardPatch.SwappableChainField.swappableCards.set(other, null);
                        }
                    }
                }

                //set card's relevant fields
                SwappableCardPatch.SwappableChainField.swappableCards.set(card, swappableLinks);
            }
        }
    }

    public static void makeSwappableGroup(boolean additive, AbstractCard... swappableLinks) {
        makeSwappableGroup(additive, Arrays.asList(swappableLinks));
    }

    public static void makeSwappableGroup(AbstractCard... swappableLinks) {
        makeSwappableGroup(false, swappableLinks);
    }

    public static boolean isCardSwappable(AbstractCard card) {
        return SwappableCardPatch.SwappableChainField.swappableCards.get(card) != null;
    }

    public static AbstractCard getNextCard(AbstractCard card) {
        List<AbstractCard> list = SwappableCardPatch.SwappableChainField.swappableCards.get(card);
        return list.get((list.indexOf(card) + 1) % list.size());
    }

    public static void upgrade(AbstractCard source) {
        if (isCardSwappable(source)) {
            if (!preventUpgradeLoop) {
                preventUpgradeLoop = true;
                AbstractCard bufferCard = getNextCard(source);
                bufferCard.upgrade();
                while (getNextCard(bufferCard) != source) {
                    bufferCard = getNextCard(bufferCard);
                    bufferCard.upgrade();
                }
                preventUpgradeLoop = false;
            }
        }
    }

    public static AbstractCard findMasterDeckEquivalent(AbstractCard card) {
        AbstractCard masterDeckCard = StSLib.getMasterDeckEquivalent(card);
        if (masterDeckCard == null) {
            if (isCardSwappable(card)) {
                AbstractCard nextCard = getNextCard(card);
                while (nextCard != card) {
                    masterDeckCard = StSLib.getMasterDeckEquivalent(nextCard);
                    if (masterDeckCard != null) {
                        return masterDeckCard;
                    }
                    nextCard = SwapperHelper.getNextCard(nextCard);
                }
            }
        }
        return masterDeckCard;
    }

    private static boolean justPressedButtonLast = false;

    public static boolean handleInput(Hitbox hb){
        boolean isButtonPressed = HitboxRightClick.rightClicked.get(hb);
        if(isButtonPressed && !justPressedButtonLast) {
            justPressedButtonLast = true;
            return true;
        }

        if(!isButtonPressed && justPressedButtonLast)
            justPressedButtonLast = false;

        return false;
    }
}
