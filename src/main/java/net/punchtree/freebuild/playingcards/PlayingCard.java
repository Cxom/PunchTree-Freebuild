package net.punchtree.freebuild.playingcards;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record PlayingCard(Suit suit, Rank rank) {

    private static final List<Suit> CUSTOM_MODEL_DATA_SUIT_ORDER = List.of(Suit.HEARTS, Suit.DIAMONDS, Suit.CLUBS, Suit.SPADES);
    private static final List<Rank> CUSTOM_MODEL_DATA_RANK_ORDER = List.of(Rank.ACE, Rank.TWO, Rank.THREE, Rank.FOUR, Rank.FIVE, Rank.SIX, Rank.SEVEN, Rank.EIGHT, Rank.NINE, Rank.TEN, Rank.JACK, Rank.QUEEN, Rank.KING);
    public static final TextComponent FACE_DOWN_CARD_NAME = Component.text("Face Down Card").decoration(TextDecoration.ITALIC, false);
    public static final TextComponent FACE_DOWN_CARD_PILE_NAME = Component.text("Face Down Card Pile").decoration(TextDecoration.ITALIC, false);

    public static PlayingCard fromItem(ItemStack drawnCard) {
        int customModelData = drawnCard.getItemMeta().getCustomModelData();
        int suitNumber = (customModelData - 1001) / 13;
        int rankNumber = (customModelData - 1001) % 13;
        Suit suit = CUSTOM_MODEL_DATA_SUIT_ORDER.get(suitNumber);
        Rank rank = CUSTOM_MODEL_DATA_RANK_ORDER.get(rankNumber);
        return new PlayingCard(suit, rank);
    }

    public static ItemStack getItemForCardList(List<PlayingCard> cards) {
        if (cards.size() == 0) {
            return null;
        } else if (cards.size() == 1) {
            return cards.get(0).getNewItem();
        } else {
            ItemStack pile = cards.get(0).getNewPileItem();
            pile.editMeta(meta -> {
                ((BundleMeta) meta).setItems(cards.stream().map(PlayingCard::getNewItem).collect(Collectors.toList()));
            });
            return pile;
        }
    }

    public int getCustomModelDataNumber() {
        int suitNumber = CUSTOM_MODEL_DATA_SUIT_ORDER.indexOf(suit);
        int rankNumber = CUSTOM_MODEL_DATA_RANK_ORDER.indexOf(rank);
        return 1000 + suitNumber * 13 + rankNumber + 1;
    }

    ItemStack getNewItem() {
        ItemStack card = new ItemStack(Material.PAPER, 1);
        card.editMeta(meta -> {
            meta.setCustomModelData(getCustomModelDataNumber());
            meta.displayName(getName());
        });
        return card;
    }

    ItemStack getNewPileItem() {
        ItemStack cardPile = new ItemStack(Material.BUNDLE, 1);
        cardPile.editMeta(meta -> {
            meta.setCustomModelData(getCustomModelDataNumber());
            meta.displayName(getName());
            ((BundleMeta) meta).addItem(getNewItem());
        });
        return cardPile;
    }

    @NotNull TextComponent getName() {
        return Component.text(rank.getName() + " of " + suit.getName()).decoration(TextDecoration.ITALIC, false);
    }

    public static ItemStack getNewDeck() {
        ItemStack deck = new ItemStack(Material.BUNDLE);
        deck.editMeta(meta -> {
            BundleMeta bundleMeta = (BundleMeta) meta;
            meta.setCustomModelData(1001);
            meta.displayName(Component.text("Deck Of Cards").decoration(TextDecoration.ITALIC, false));
            List<ItemStack> cardsList = Arrays.stream(Suit.values())
                    .flatMap(suit -> Arrays.stream(Rank.values())
                            .map(rank -> new PlayingCard(suit, rank).getNewItem()))
                    .collect(Collectors.toList());
            Collections.shuffle(cardsList);
            bundleMeta.setItems(cardsList);
        });
        return deck;
    }

    public static ItemStack getNewFaceDownCardItem() {
        ItemStack faceDownCard = new ItemStack(Material.PAPER);
        faceDownCard.editMeta(meta -> {
            meta.setCustomModelData(1000);
            meta.displayName(PlayingCard.FACE_DOWN_CARD_NAME);
        });
        return faceDownCard;
    }

    public static ItemStack getNewFaceDownPileItem() {
        ItemStack faceDownCardPile = new ItemStack(Material.BUNDLE);
        faceDownCardPile.editMeta(meta -> {
            meta.setCustomModelData(1000);
            meta.displayName(FACE_DOWN_CARD_PILE_NAME);
        });
        return faceDownCardPile;
    }

}
