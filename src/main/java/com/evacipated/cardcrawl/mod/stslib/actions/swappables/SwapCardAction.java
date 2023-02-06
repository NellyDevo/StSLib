package com.evacipated.cardcrawl.mod.stslib.actions.swappables;

import com.evacipated.cardcrawl.mod.stslib.cards.interfaces.SwappableCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.GameCursor;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.input.InputHelper;

public class SwapCardAction extends AbstractGameAction {
    private final AbstractCard card1;
    private final AbstractCard card2;
    private final int index;
    private final boolean selected;

    public SwapCardAction(AbstractCard originalCard, AbstractCard swapCard, int spotInHand, boolean selected) {
        this.actionType = ActionType.SPECIAL;
        this.duration = Settings.ACTION_DUR_MED;
        this.card1 = originalCard;
        this.card2 = swapCard;
        this.index = spotInHand;
        this.selected = selected;
    }

    @Override
    public void update() {
        AbstractPlayer p = AbstractDungeon.player;
        p.hoveredCard = card2;
        if (card1 instanceof SwappableCard) {
            ((SwappableCard)card1).onSwapOut();
        }
        p.hand.group.remove(index);
        p.hand.group.add(index, card2);
        if (selected) {
            if (card2.target == AbstractCard.CardTarget.ENEMY || card2.target == AbstractCard.CardTarget.SELF_AND_ENEMY) {
                p.inSingleTargetMode = true;
                p.isDraggingCard = false;
                GameCursor.hidden = true;
                p.hand.refreshHandLayout();
                card2.current_x = card2.target_x;
                card2.current_y = card2.target_y;
            } else {
                p.inSingleTargetMode = false;
                p.isDraggingCard = true;
                GameCursor.hidden = false;
                card2.current_x = card1.current_x;
                card2.current_y = card1.current_y;
                card2.target_x = InputHelper.mX;
                card2.target_y = InputHelper.mY;
            }
        } else {
            card2.current_x = card1.current_x;
            card2.current_y = card1.current_y;
            card2.target_x = card1.target_x;
            card2.target_y = card1.target_y;
            if (!card2.isHoveredInHand(card2.drawScale)) {
                p.releaseCard();
            }
        }
        if (card2 instanceof SwappableCard) {
            ((SwappableCard)card2).onSwapIn();
        }
        card2.isGlowing = card1.isGlowing;
        card1.stopGlowing();
        card1.flashVfx = null;
        card2.flash();
        card1.applyPowers();
        card2.applyPowers();
        p.hand.applyPowers();
        p.hand.glowCheck();
        isDone = true;
    }
}
