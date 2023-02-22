package net.punchtree.freebuild.playingcards;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

public class CardStackPersistentDataType implements PersistentDataType<byte[], List<PlayingCard>> {
    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Class<List<PlayingCard>> getComplexType() {
        return (Class<List<PlayingCard>>) Collections.<PlayingCard>emptyList().getClass();
    }

    @Override
    public byte @NotNull [] toPrimitive(@NotNull List<PlayingCard> cards, @NotNull PersistentDataAdapterContext context) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[cards.size()]);
        for (PlayingCard card : cards) {
            bb.put((byte) (card.suit().ordinal() * 13 + card.rank().ordinal()));
        }
        return bb.array();
    }

    @Override
    public @NotNull List<PlayingCard> fromPrimitive(byte @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
        ByteBuffer bb = ByteBuffer.wrap(primitive);
        int numCards = primitive.length;
        List<PlayingCard> cards = new java.util.ArrayList<>(numCards);
        for (int i = 0; i < numCards; ++i) {
            byte b = bb.get();
            cards.add(new PlayingCard(Suit.values()[b / 13], Rank.values()[b % 13]));
        }
        return cards;
    }
}
